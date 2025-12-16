package com.phonebid.app.customerservice.service;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.customerservice.domain.Inquiry;
import com.phonebid.app.customerservice.domain.InquiryCategory;
import com.phonebid.app.customerservice.domain.InquiryReply;
import com.phonebid.app.customerservice.domain.InquiryStatus;
import com.phonebid.app.customerservice.dto.request.InquiryCreateRequestDto;
import com.phonebid.app.customerservice.dto.request.InquiryReplyRequestDto;
import com.phonebid.app.customerservice.dto.request.InquiryUpdateRequestDto;
import com.phonebid.app.customerservice.dto.response.InquiryDetailResponseDto;
import com.phonebid.app.customerservice.dto.response.InquiryResponseDto;
import com.phonebid.app.customerservice.errorcode.CustomerServiceErrorCode;
import com.phonebid.app.customerservice.repository.InquiryReplyRepository;
import com.phonebid.app.customerservice.repository.InquiryRepository;
import com.phonebid.app.member.domain.User;
import com.phonebid.app.member.repository.UserRepository;
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
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createInquiry(String username, InquiryCreateRequestDto requestDto) {
        User user = loadActiveUser(username);

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .category(requestDto.getCategory())
                .title(requestDto.getTitle().trim())
                .content(requestDto.getContent().trim())
                .build();

        inquiryRepository.save(inquiry);
    }

    @Transactional(readOnly = true)
    public Page<InquiryResponseDto> getMyInquiries(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Inquiry> inquiries = inquiryRepository.findByUsername(username, pageable);
        return inquiries.map(InquiryResponseDto::from);
    }

    @Transactional(readOnly = true)
    public InquiryDetailResponseDto getInquiryDetail(String username, UUID inquiryId) {
        Inquiry inquiry = inquiryRepository.findByIdAndUsername(inquiryId, username)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.INQUIRY_NOT_FOUND));

        Optional<InquiryReply> reply = inquiryReplyRepository.findByInquiryId(inquiryId);
        return InquiryDetailResponseDto.from(inquiry, reply.orElse(null));
    }

    @Transactional
    public void updateInquiry(String username, UUID inquiryId, InquiryUpdateRequestDto requestDto) {
        Inquiry inquiry = inquiryRepository.findByIdAndUsername(inquiryId, username)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.INQUIRY_NOT_FOUND));

        if (!inquiry.canBeModified()) {
            throw new CustomException(CustomerServiceErrorCode.INQUIRY_CANNOT_MODIFY);
        }

        if (requestDto.getCategory() != null) {
            inquiry.updateCategory(requestDto.getCategory());
        }
        if (requestDto.getTitle() != null && !requestDto.getTitle().trim().isEmpty()) {
            inquiry.updateTitle(requestDto.getTitle().trim());
        }
        if (requestDto.getContent() != null && !requestDto.getContent().trim().isEmpty()) {
            inquiry.updateContent(requestDto.getContent().trim());
        }
    }

    @Transactional
    public void deleteInquiry(String username, UUID inquiryId) {
        Inquiry inquiry = inquiryRepository.findByIdAndUsername(inquiryId, username)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.INQUIRY_NOT_FOUND));

        inquiry.softDelete(username);
    }

    @Transactional(readOnly = true)
    public Page<InquiryResponseDto> getAllInquiries(InquiryStatus status, InquiryCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Inquiry> inquiries = inquiryRepository.findAllWithFilters(status, category, pageable);
        return inquiries.map(InquiryResponseDto::from);
    }

    @Transactional
    public void createReply(String adminUsername, UUID inquiryId, InquiryReplyRequestDto requestDto) {
        User admin = loadActiveAdmin(adminUsername);
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.INQUIRY_NOT_FOUND));

        Optional<InquiryReply> existingReply = inquiryReplyRepository.findByInquiryId(inquiryId);
        if (existingReply.isPresent()) {
            throw new CustomException(CustomerServiceErrorCode.INQUIRY_REPLY_ALREADY_EXISTS);
        }

        InquiryReply reply = InquiryReply.builder()
                .inquiry(inquiry)
                .admin(admin)
                .content(requestDto.getContent().trim())
                .build();

        inquiryReplyRepository.save(reply);
        inquiry.changeStatus(InquiryStatus.ANSWERED);
    }

    @Transactional
    public void updateReply(String adminUsername, UUID inquiryId, UUID replyId, InquiryReplyRequestDto requestDto) {
        InquiryReply reply = inquiryReplyRepository.findById(replyId)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.INQUIRY_REPLY_NOT_FOUND));

        if (!reply.getInquiry().getId().equals(inquiryId)) {
            throw new CustomException(CustomerServiceErrorCode.INQUIRY_REPLY_NOT_FOUND);
        }

        reply.updateContent(requestDto.getContent().trim());
    }

    @Transactional
    public void deleteReply(String adminUsername, UUID inquiryId, UUID replyId) {
        InquiryReply reply = inquiryReplyRepository.findById(replyId)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.INQUIRY_REPLY_NOT_FOUND));

        if (!reply.getInquiry().getId().equals(inquiryId)) {
            throw new CustomException(CustomerServiceErrorCode.INQUIRY_REPLY_NOT_FOUND);
        }

        reply.softDelete(adminUsername);
        reply.getInquiry().changeStatus(InquiryStatus.PENDING);
    }

    private User loadActiveUser(String username) {
        return userRepository.findByUsername(username)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));
    }

    private User loadActiveAdmin(String username) {
        User user = loadActiveUser(username);
        if (!user.isAdmin()) {
            throw new CustomException(CustomerServiceErrorCode.ONLY_ADMIN_ALLOWED);
        }
        return user;
    }
}

