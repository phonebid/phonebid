package com.phonebid.app.member.service;

import com.phonebid.app.common.domain.Address;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.SellerDocument;
import com.phonebid.app.member.domain.DocumentType;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.dto.request.SellerRegisterRequestDto;
import com.phonebid.app.member.dto.request.SellerProfileUpdateRequestDto;
import com.phonebid.app.member.dto.request.SignupRequestDto;
import com.phonebid.app.member.dto.response.SellerProfileResponseDto;
import com.phonebid.app.member.repository.SellerDocumentRepository;
import com.phonebid.app.member.repository.SellerRepository;
import com.phonebid.app.member.repository.UserRepository;
import com.phonebid.app.mypage.domain.Account;
import com.phonebid.app.mypage.domain.Bank;
import com.phonebid.app.mypage.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판매자 서비스
 * 판매자 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final SellerDocumentRepository sellerDocumentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 판매자 등록 (회원가입 포함)
     */
    @Transactional
    public void registerSeller(SellerRegisterRequestDto requestDto) {
        SignupRequestDto userInfo = requestDto.getUserInfo();
        
        // 1. User 생성
        // 아이디 중복 확인
        if (userRepository.findByUsername(userInfo.getUsername()).isPresent()) {
            throw new CustomException(CommonErrorCode.DUPLICATE_USERNAME);
        }

        // 이메일 중복 확인
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new CustomException(CommonErrorCode.DUPLICATE_EMAIL);
        }

        // 닉네임 중복 확인
        if (userRepository.findByNickname(userInfo.getNickname()).isPresent()) {
            throw new CustomException(CommonErrorCode.DUPLICATE_NICKNAME);
        }

        // 사업자등록번호 중복 확인
        if (sellerRepository.existsByBusinessNumber(requestDto.getBusinessNumber())) {
            throw new CustomException(MemberErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
        }

        // User 생성
        String encodedPassword = passwordEncoder.encode(userInfo.getPassword());
        User user = User.builder()
                .username(userInfo.getUsername())
                .password(encodedPassword)
                .email(requestDto.getEmail())
                .name(userInfo.getName())
                .nickname(userInfo.getNickname())
                .phone(requestDto.getRepresentativePhone().replace("-", "")) // 하이픈 제거
                .role(Role.SELLER)
                .build();
        user = userRepository.save(user);

        // 2. Seller 생성
        Address businessAddress = requestDto.getBusinessAddress().toEntity();
        Address storeAddress = requestDto.getStoreAddress().toEntity();
        
        Seller seller = Seller.builder()
                .user(user)
                .businessNumber(requestDto.getBusinessNumber())
                .storeName(requestDto.getStoreName())
                .storeAddress(storeAddress)
                .isAgent(requestDto.getIsAgent())
                .representativeName(requestDto.getRepresentativeName())
                .businessAddress(businessAddress)
                .consentNumber(requestDto.getConsentNumber())
                .customerServicePhone(requestDto.getCustomerServicePhone())
                .build();
        seller = sellerRepository.save(seller);

        // 3. Account 생성 (정산 계좌)
        Bank bank = requestDto.getSettlementAccount().getBank();
        String accountNumber = requestDto.getSettlementAccount().getAccountNumber().trim();
        String accountHolderName = requestDto.getSettlementAccount().getAccountHolderName().trim();
        
        Account account = Account.builder()
                .user(user)
                .bank(bank)
                .accountNumber(accountNumber)
                .accountHolderName(accountHolderName)
                .build();
        accountRepository.save(account);

        // 4. SellerDocument 생성 (사업자등록증)
        SellerDocument businessLicense = SellerDocument.builder()
                .seller(seller)
                .type(DocumentType.BUSINESS_LICENSE)
                .fileUrl(requestDto.getBusinessLicenseFileUrl())
                .build();
        sellerDocumentRepository.save(businessLicense);

        // 5. SellerDocument 생성 (사전승낙서 - 대리점이 아닌 경우만)
        if (!requestDto.getIsAgent() && requestDto.getConsentFormFileUrl() != null 
                && !requestDto.getConsentFormFileUrl().trim().isEmpty()) {
            SellerDocument consentForm = SellerDocument.builder()
                    .seller(seller)
                    .type(DocumentType.CONSENT_FORM)
                    .fileUrl(requestDto.getConsentFormFileUrl())
                    .build();
            sellerDocumentRepository.save(consentForm);
        }
    }

    /**
     * 판매자 프로필 조회
     */
    public SellerProfileResponseDto getSellerProfile(String username) {
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.SELLER_NOT_FOUND));

        return SellerProfileResponseDto.from(seller);
    }

    /**
     * 판매자 프로필 수정
     */
    @Transactional
    public void updateSellerProfile(String username, SellerProfileUpdateRequestDto requestDto) {
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.SELLER_NOT_FOUND));

        // 승인된 판매자만 수정 가능
        if (!seller.canSell()) {
            throw new CustomException(MemberErrorCode.SELLER_NOT_APPROVED);
        }

        // 매장명 업데이트 (제공된 경우에만)
        if (requestDto.getStoreName() != null && !requestDto.getStoreName().trim().isEmpty()) {
            seller.updateStoreName(requestDto.getStoreName());
        }

        // 주소 업데이트 (제공된 경우에만)
        if (requestDto.getStoreAddress() != null) {
            seller.updateStoreAddress(requestDto.getStoreAddress());
        }

        // 사용자 정보 업데이트 (연락처, 이메일)
        User user = seller.getUser();
        
        // 전화번호 업데이트 (제공된 경우에만)
        if (requestDto.getPhoneNumber() != null && !requestDto.getPhoneNumber().trim().isEmpty()) {
            // 전화번호에서 하이픈 제거 (User 엔티티는 숫자만 허용)
            String phoneNumberWithoutHyphen = requestDto.getPhoneNumber().replace("-", "");
            user.updatePhone(phoneNumberWithoutHyphen);
        }
        
        // 이메일 업데이트 (제공된 경우에만)
        if (requestDto.getEmail() != null && !requestDto.getEmail().trim().isEmpty()) {
            String newEmail = requestDto.getEmail().trim();
            userRepository.findByEmail(newEmail).ifPresent(other -> {
                if (!other.getId().equals(user.getId())) {
                    throw new CustomException(MemberErrorCode.DUPLICATE_EMAIL);
                }
            });
            user.updateEmail(newEmail);
        }

        sellerRepository.save(seller);
        userRepository.save(user);
    }
} 