package com.phonebid.app.customerservice.service;

import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.CommonErrorCode;
import com.phonebid.app.customerservice.domain.Notice;
import com.phonebid.app.customerservice.dto.request.NoticeCreateRequestDto;
import com.phonebid.app.customerservice.dto.request.NoticeUpdateRequestDto;
import com.phonebid.app.customerservice.dto.response.NoticeDetailResponseDto;
import com.phonebid.app.customerservice.dto.response.NoticeResponseDto;
import com.phonebid.app.customerservice.errorcode.CustomerServiceErrorCode;
import com.phonebid.app.customerservice.repository.NoticeRepository;
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
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<NoticeResponseDto> getAllNotices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notice> notices = noticeRepository.findAllOrdered(pageable);
        return notices.map(NoticeResponseDto::from);
    }

    @Transactional
    public NoticeDetailResponseDto getNoticeDetail(UUID noticeId) {
        Notice notice = noticeRepository.findByIdForView(noticeId)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.NOTICE_NOT_FOUND));

        notice.incrementViewCount();
        return NoticeDetailResponseDto.from(notice);
    }

    @Transactional
    public void createNotice(String adminUsername, NoticeCreateRequestDto requestDto) {
        User admin = loadActiveAdmin(adminUsername);

        Notice notice = Notice.builder()
                .admin(admin)
                .title(requestDto.getTitle().trim())
                .content(requestDto.getContent().trim())
                .isImportant(requestDto.getIsImportant())
                .build();

        noticeRepository.save(notice);
    }

    @Transactional
    public void updateNotice(String adminUsername, UUID noticeId, NoticeUpdateRequestDto requestDto) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.NOTICE_NOT_FOUND));

        if (requestDto.getTitle() != null && !requestDto.getTitle().trim().isEmpty()) {
            notice.updateTitle(requestDto.getTitle().trim());
        }
        if (requestDto.getContent() != null && !requestDto.getContent().trim().isEmpty()) {
            notice.updateContent(requestDto.getContent().trim());
        }
        if (requestDto.getIsImportant() != null) {
            notice.updateIsImportant(requestDto.getIsImportant());
        }
    }

    @Transactional
    public void deleteNotice(String adminUsername, UUID noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new CustomException(CustomerServiceErrorCode.NOTICE_NOT_FOUND));

        notice.softDelete(adminUsername);
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

