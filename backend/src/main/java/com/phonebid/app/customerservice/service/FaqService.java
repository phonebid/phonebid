package com.phonebid.app.customerservice.service;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.customerservice.domain.Faq;
import com.phonebid.app.customerservice.domain.FaqCategory;
import com.phonebid.app.customerservice.dto.request.FaqCreateRequestDto;
import com.phonebid.app.customerservice.dto.request.FaqUpdateRequestDto;
import com.phonebid.app.customerservice.dto.response.FaqDetailResponseDto;
import com.phonebid.app.customerservice.dto.response.FaqResponseDto;
import com.phonebid.app.customerservice.errorcode.CustomerServiceErrorCode;
import com.phonebid.app.customerservice.repository.FaqRepository;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<FaqResponseDto> getAllFaqs(FaqCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Faq> faqs = faqRepository.findAllWithCategoryFilter(category, pageable);
        return faqs.map(FaqResponseDto::from);
    }

    @Transactional
    public FaqDetailResponseDto getFaqDetail(UUID faqId) {
        Faq faq = faqRepository.findByIdForView(faqId)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.FAQ_NOT_FOUND));

        faq.incrementViewCount();
        return FaqDetailResponseDto.from(faq);
    }

    @Transactional
    public void createFaq(String adminUsername, FaqCreateRequestDto requestDto) {
        loadActiveAdmin(adminUsername);

        Faq faq = Faq.builder()
                .category(requestDto.getCategory())
                .question(requestDto.getQuestion().trim())
                .answer(requestDto.getAnswer().trim())
                .build();

        faqRepository.save(faq);
    }

    @Transactional
    public void updateFaq(String adminUsername, UUID faqId, FaqUpdateRequestDto requestDto) {
        loadActiveAdmin(adminUsername);

        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.FAQ_NOT_FOUND));

        if (requestDto.getCategory() != null) {
            faq.updateCategory(requestDto.getCategory());
        }
        if (requestDto.getQuestion() != null && !requestDto.getQuestion().trim().isEmpty()) {
            faq.updateQuestion(requestDto.getQuestion().trim());
        }
        if (requestDto.getAnswer() != null && !requestDto.getAnswer().trim().isEmpty()) {
            faq.updateAnswer(requestDto.getAnswer().trim());
        }
    }

    @Transactional
    public void deleteFaq(String adminUsername, UUID faqId) {
        loadActiveAdmin(adminUsername);

        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.FAQ_NOT_FOUND));

        faq.softDelete(adminUsername);
    }

    private User loadActiveUser(String username) {
        return userRepository.findByUsername(username)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
    }

    /**
     * 활성 관리자 조회 및 권한 검증
     * 관리자가 아닌 경우 예외를 발생시킵니다.
     */
    private User loadActiveAdmin(String username) {
        User user = loadActiveUser(username);
        if (!user.isAdmin()) {
            throw new CustomException(CustomerServiceErrorCode.ONLY_ADMIN_ALLOWED);
        }
        return user;
    }
}

