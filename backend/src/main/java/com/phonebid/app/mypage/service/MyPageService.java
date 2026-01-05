package com.phonebid.app.mypage.service;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import com.phonebid.app.mypage.domain.Account;
import com.phonebid.app.mypage.domain.Bank;
import com.phonebid.app.mypage.domain.UserDeliveryAddress;
import com.phonebid.app.mypage.dto.request.AccountCreateRequestDto;
import com.phonebid.app.mypage.dto.request.DeliveryAddressCreateRequestDto;
import com.phonebid.app.mypage.dto.request.ProfileUpdateRequestDto;
import com.phonebid.app.mypage.dto.response.AccountResponseDto;
import com.phonebid.app.mypage.dto.response.DeliveryAddressResponseDto;
import com.phonebid.app.mypage.dto.response.ProfileImageUploadResponseDto;
import com.phonebid.app.mypage.dto.response.ProfileResponseDto;
import com.phonebid.app.mypage.dto.response.PurchaseDetailResponseDto;
import com.phonebid.app.mypage.dto.response.PurchaseHistoryResponseDto;
import com.phonebid.app.mypage.repository.AccountRepository;
import com.phonebid.app.mypage.repository.UserDeliveryAddressRepository;
import com.phonebid.app.s3.service.S3Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import com.phonebid.app.trade.domain.Contract;
import com.phonebid.app.trade.domain.ContractStatus;
import com.phonebid.app.trade.domain.Delivery;
import com.phonebid.app.trade.domain.Payment;
import com.phonebid.app.trade.repository.ContractRepository;
import com.phonebid.app.trade.repository.DeliveryRepository;
import com.phonebid.app.trade.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final AccountRepository accountRepository;
    private final UserDeliveryAddressRepository userDeliveryAddressRepository;
    private final S3Service s3Service;

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "JPG", "JPEG", "PNG", "GIF", "WEBP"
    );

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 프로필 조회
     * 활성 사용자의 프로필 정보를 조회하여 DTO로 변환하여 반환합니다.
     */
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(String username) {
        User user = loadActiveUser(username);
        return ProfileResponseDto.from(user);
    }

    /**
     * 프로필 수정
     * 사용자의 이름, 닉네임, 휴대폰 번호를 선택적으로 수정합니다.
     * 닉네임의 경우 중복 검증을 수행하며, 자신의 기존 닉네임은 제외합니다.
     */
    @Transactional
    public void updateProfile(String username, ProfileUpdateRequestDto requestDto) {
        User user = loadActiveUser(username);

        String newName = requestDto.getName();
        String newNickname = requestDto.getNickname();
        String newPhone = requestDto.getPhone();

        if (newName != null && !newName.trim().isEmpty()) {
            user.updateName(newName.trim());
        }

        if (newNickname != null && !newNickname.trim().isEmpty()) {
            String trimmedNickname = newNickname.trim();
            Optional<User> checkNickname = userRepository.findByNickname(trimmedNickname);
            if (checkNickname.isPresent() && !checkNickname.get().getId().equals(user.getId())) {
                throw new CustomException(CommonErrorCode.DUPLICATE_NICKNAME);
            }
            user.updateNickname(trimmedNickname);
        }

        if (newPhone != null && !newPhone.trim().isEmpty()) {
            user.updatePhone(newPhone.trim());
        }
    }

    /**
     * 구매내역 목록 조회
     * 구매완료 또는 취소/환불 상태의 구매내역을 페이징하여 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<PurchaseHistoryResponseDto> getPurchaseHistory(String username, String status, int page, int size) {
        ContractStatus contractStatus = "CANCELLED".equals(status) 
            ? ContractStatus.CANCELLED 
            : ContractStatus.SIGNED;
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Contract> contracts = contractRepository.findByUsernameAndStatus(username, contractStatus, pageable);
        
        return contracts.map(contract -> {
            Optional<Payment> payment = paymentRepository.findByContractId(contract.getId());
            return PurchaseHistoryResponseDto.from(contract, payment.orElse(null));
        });
    }

    /**
     * 구매내역 상세 조회
     * 특정 계약의 상세 정보를 조회합니다.
     */
    @Transactional(readOnly = true)
    public PurchaseDetailResponseDto getPurchaseDetail(String username, UUID contractId) {
        Contract contract = contractRepository.findByIdAndUsername(contractId, username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.RESOURCE_NOT_FOUND));
        
        Optional<Payment> payment = paymentRepository.findByContractId(contractId);
        Optional<Delivery> delivery = deliveryRepository.findByContractId(contractId);
        
        return PurchaseDetailResponseDto.from(
            contract, 
            payment.orElse(null), 
            delivery.orElse(null)
        );
    }

    /**
     * 계좌 등록
     * 사용자의 계좌 정보를 등록합니다. 동일한 은행과 계좌번호 조합은 중복 등록할 수 없습니다.
     */
    @Transactional
    public void createAccount(String username, AccountCreateRequestDto requestDto) {
        User user = loadActiveUser(username);
        
        Bank bank = requestDto.getBank();
        String accountNumber = requestDto.getAccountNumber().trim();
        String accountHolderName = requestDto.getAccountHolderName().trim();
        
        // 동일 사용자의 동일 은행, 동일 계좌번호 중복 확인
        boolean exists = accountRepository.existsByUsernameAndAccountNumberAndBank(
            username, accountNumber, bank);
        if (exists) {
            throw new CustomException(CommonErrorCode.DUPLICATE_ACCOUNT);
        }
        
        Account account = Account.builder()
            .user(user)
            .bank(bank)
            .accountNumber(accountNumber)
            .accountHolderName(accountHolderName)
            .build();
        
        accountRepository.save(account);
    }

    /**
     * 계좌 목록 조회
     * 사용자의 등록된 계좌 목록을 페이징하여 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<AccountResponseDto> getAccounts(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accounts = accountRepository.findByUsername(username, pageable);
        
        return accounts.map(AccountResponseDto::from);
    }

    /**
     * 계좌 삭제
     * 사용자의 계좌를 소프트 삭제합니다. 본인의 계좌만 삭제할 수 있습니다.
     */
    @Transactional
    public void deleteAccount(String username, UUID accountId) {
        Account account = accountRepository.findByIdAndUsername(accountId, username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.RESOURCE_NOT_FOUND));
        
        account.softDelete(username);
    }

    /**
     * 기본 배송지 조회
     * 사용자의 기본 배송지를 조회합니다. 기본 배송지가 없는 경우 예외를 발생시킵니다.
     */
    @Transactional(readOnly = true)
    public DeliveryAddressResponseDto getDefaultDeliveryAddress(String username) {
        UserDeliveryAddress address = userDeliveryAddressRepository.findDefaultByUsername(username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.DEFAULT_DELIVERY_ADDRESS_NOT_FOUND));
        
        return DeliveryAddressResponseDto.from(address);
    }

    /**
     * 배송지 목록 조회
     * 사용자의 배송지 목록을 페이징하여 조회합니다. 기본 배송지가 먼저 오고, 그 다음 등록일 기준 내림차순으로 정렬됩니다.
     */
    @Transactional(readOnly = true)
    public Page<DeliveryAddressResponseDto> getDeliveryAddresses(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDeliveryAddress> addresses = userDeliveryAddressRepository.findByUsername(username, pageable);
        
        return addresses.map(DeliveryAddressResponseDto::from);
    }

    /**
     * 배송지 저장
     * 사용자의 배송지를 저장합니다. 기본 배송지로 저장하는 경우 기존 기본 배송지의 isDefault를 false로 변경합니다.
     */
    @Transactional
    public void createDeliveryAddress(String username, DeliveryAddressCreateRequestDto requestDto) {
        User user = loadActiveUser(username);
        
        Boolean saveAsDefault = requestDto.getSaveAsDefault() != null ? requestDto.getSaveAsDefault() : false;
        
        // 기본 배송지로 저장하는 경우 기존 기본 배송지 해제
        if (saveAsDefault) {
            userDeliveryAddressRepository.findDefaultByUsername(username).ifPresent(existingDefault -> {
                existingDefault.setDefault(false);
            });
        }
        
        UserDeliveryAddress address = UserDeliveryAddress.builder()
            .user(user)
            .addressName(requestDto.getAddressName())
            .recipientName(requestDto.getRecipientName())
            .recipientPhone(requestDto.getPhone())
            .postalCode(requestDto.getPostalCode())
            .address(requestDto.getAddress())
            .detailAddress(requestDto.getDetailAddress())
            .isDefault(saveAsDefault)
            .build();
        
        userDeliveryAddressRepository.save(address);
    }

    /**
     * 기본 배송지 설정
     * 특정 배송지를 기본 배송지로 설정합니다. 기존 기본 배송지의 isDefault를 false로 변경합니다.
     */
    @Transactional
    public void setDefaultDeliveryAddress(String username, UUID addressId) {
        UserDeliveryAddress address = userDeliveryAddressRepository.findByIdAndUsername(addressId, username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.DELIVERY_ADDRESS_NOT_FOUND));
        
        // 기존 기본 배송지 해제
        userDeliveryAddressRepository.findDefaultByUsername(username).ifPresent(existingDefault -> {
            if (!existingDefault.getId().equals(addressId)) {
                existingDefault.setDefault(false);
            }
        });
        
        // 새 기본 배송지 설정
        address.setDefault(true);
    }

    /**
     * 배송지 삭제
     * 사용자의 배송지를 소프트 삭제합니다. 본인의 배송지만 삭제할 수 있습니다.
     */
    @Transactional
    public void deleteDeliveryAddress(String username, UUID addressId) {
        UserDeliveryAddress address = userDeliveryAddressRepository.findByIdAndUsername(addressId, username)
            .orElseThrow(() -> new CustomException(CommonErrorCode.DELIVERY_ADDRESS_NOT_FOUND));
        
        address.softDelete(username);
    }

    /**
     * 프로필 이미지 업로드
     * 기존 프로필 이미지가 있으면 S3에서 삭제하고 새 이미지를 업로드합니다.
     */
    @Transactional
    public ProfileImageUploadResponseDto uploadProfileImage(String username, MultipartFile file) {
        User user = loadActiveUser(username);

        validateImageFile(file);

        String uploadedFileUrl = null;
        try {
            String previousImageUrl = user.getProfileImageUrl();

            String fileName = generateProfileImageFileName(user.getId(), file.getOriginalFilename());
            uploadedFileUrl = s3Service.uploadFile(fileName, file);

            try {
                user.updateProfileImageUrl(uploadedFileUrl);
                userRepository.save(user);

                if (previousImageUrl != null) {
                    try {
                        s3Service.deleteFileByUrl(previousImageUrl);
                        log.info("기존 프로필 이미지 삭제 완료: {}", previousImageUrl);
                    } catch (Exception e) {
                        log.warn("기존 프로필 이미지 삭제 실패(무시): {}", previousImageUrl, e);
                    }
                }

                return ProfileImageUploadResponseDto.from(uploadedFileUrl);
            } catch (Exception dbException) {
                log.error("DB 저장 실패로 업로드 파일 롤백 시작: {}", uploadedFileUrl, dbException);
                try {
                    s3Service.deleteFileByUrl(uploadedFileUrl);
                    log.info("업로드 실패 파일 롤백 완료: {}", uploadedFileUrl);
                } catch (CustomException ce) {
                    log.warn("업로드 실패 파일 롤백 실패(무시): {}", uploadedFileUrl, ce);
                }
                throw new CustomException(MemberErrorCode.FILE_UPLOAD_FAILED);
            }

        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new CustomException(MemberErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 프로필 이미지 삭제
     * S3에서 이미지를 삭제하고 User 엔티티의 profileImageUrl을 null로 설정합니다.
     */
    @Transactional
    public void deleteProfileImage(String username) {
        User user = loadActiveUser(username);

        String imageUrl = user.getProfileImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new CustomException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }

        try {
            s3Service.deleteFileByUrl(imageUrl);
            user.updateProfileImageUrl(null);
            userRepository.save(user);
            log.info("프로필 이미지 삭제 완료: {}", imageUrl);
        } catch (Exception e) {
            log.error("프로필 이미지 삭제 실패: {}", imageUrl, e);
            throw new CustomException(MemberErrorCode.FILE_DELETE_FAILED);
        }
    }

    /**
     * 이미지 파일 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(MemberErrorCode.MISSING_FILE);
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new CustomException(MemberErrorCode.FILE_SIZE_EXCEEDED);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.INVALID_FILE_NAME);
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new CustomException(MemberErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 프로필 이미지 파일명 생성
     */
    private String generateProfileImageFileName(UUID userId, String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String sanitizedFilename = sanitizeFilename(originalFilename);
        return String.format("profiles/%s/%s_%s", userId, timestamp, sanitizedFilename);
    }

    /**
     * 파일명 보안 정리
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "unnamed.jpg";
        }
        return filename.trim()
                .replaceAll("[^a-zA-Z0-9가-힣._-]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_+|_+$", "");
    }

    /**
     * 활성 사용자 조회
     * 삭제되지 않은 활성 사용자만 조회하며, 사용자가 없거나 삭제된 경우 예외를 발생시킵니다.
     */
    private User loadActiveUser(String username) {
        return userRepository.findByUsername(username)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
    }
}

