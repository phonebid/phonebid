package com.phonebid.app.member.service;

import com.phonebid.app.common.domain.Address;
import com.phonebid.app.common.errorcode.MemberErrorCode;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.domain.Role;
import com.phonebid.app.member.domain.ApprovalStatus;
import com.phonebid.app.member.dto.request.SellerRegisterRequestDto;
import com.phonebid.app.member.dto.request.SellerProfileUpdateRequestDto;
import com.phonebid.app.member.dto.response.SellerProfileResponseDto;
import com.phonebid.app.member.repository.SellerRepository;
import com.phonebid.app.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 판매자 등록
     */
    @Transactional
    public void registerSeller(String username, SellerRegisterRequestDto requestDto) {
        // 사용자 존재 여부 확인
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.USER_NOT_FOUND));

        // 이미 판매자로 등록된 사용자인지 확인
        if (sellerRepository.existsByUsername(username)) {
            throw new CustomException(MemberErrorCode.SELLER_ALREADY_EXISTS);
        }

        // 사업자등록번호 중복 확인
        if (sellerRepository.existsByBusinessNumber(requestDto.getBusinessNumber())) {
            throw new CustomException(MemberErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
        }

        // 사용자 역할을 판매자로 변경하고 저장
        user.updateRole(Role.SELLER);
        userRepository.save(user);

        // 판매자 엔티티 생성 (주소는 null로 설정)
        Seller seller = Seller.builder()
                .user(user)
                .businessNumber(requestDto.getBusinessNumber())
                .storeName(requestDto.getStoreName())
                .storeAddress(null) // 주소는 별도 API로 관리
                .build();

        // 판매자 저장
        sellerRepository.save(seller);
        
        log.info("판매자 등록 완료: sellerId={}, username={}, businessNumber={}", 
                seller.getSellerId(), username, requestDto.getBusinessNumber());
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