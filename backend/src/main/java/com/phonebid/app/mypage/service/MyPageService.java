package com.phonebid.app.mypage.service;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import com.phonebid.app.mypage.domain.Account;
import com.phonebid.app.mypage.domain.Bank;
import com.phonebid.app.mypage.dto.request.AccountCreateRequestDto;
import com.phonebid.app.mypage.dto.request.ProfileUpdateRequestDto;
import com.phonebid.app.mypage.dto.response.AccountResponseDto;
import com.phonebid.app.mypage.dto.response.ProfileResponseDto;
import com.phonebid.app.mypage.dto.response.PurchaseDetailResponseDto;
import com.phonebid.app.mypage.dto.response.PurchaseHistoryResponseDto;
import com.phonebid.app.mypage.repository.AccountRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final AccountRepository accountRepository;

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
     * 활성 사용자 조회
     * 삭제되지 않은 활성 사용자만 조회하며, 사용자가 없거나 삭제된 경우 예외를 발생시킵니다.
     */
    private User loadActiveUser(String username) {
        return userRepository.findByUsername(username)
            .filter(user -> !user.isDeleted())
            .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
    }
}

