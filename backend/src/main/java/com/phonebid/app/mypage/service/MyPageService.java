package com.phonebid.app.mypage.service;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import com.phonebid.app.mypage.dto.request.ProfileUpdateRequestDto;
import com.phonebid.app.mypage.dto.response.ProfileResponseDto;
import com.phonebid.app.mypage.dto.response.PurchaseDetailResponseDto;
import com.phonebid.app.mypage.dto.response.PurchaseHistoryResponseDto;
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
     * 활성 사용자 조회
     * 삭제되지 않은 활성 사용자만 조회하며, 사용자가 없거나 삭제된 경우 예외를 발생시킵니다.
     */
    private User loadActiveUser(String username) {
        return userRepository.findByUsername(username)
            .filter(user -> !user.isDeleted())
            .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
    }
}

