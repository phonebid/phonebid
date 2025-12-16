package com.phonebid.app.customerservice.domain;

import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "inquiries", indexes = {
    @Index(name = "idx_inquiries_user_id", columnList = "user_id"),
    @Index(name = "idx_inquiries_status", columnList = "status"),
    @Index(name = "idx_inquiries_category", columnList = "category")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("문의 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("문의 작성자")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @Comment("문의 카테고리")
    private InquiryCategory category;

    @Column(name = "title", nullable = false, length = 200)
    @Comment("문의 제목")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @Comment("문의 내용")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("문의 상태")
    private InquiryStatus status;

    @Builder
    public Inquiry(User user, InquiryCategory category, String title, String content) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.status = InquiryStatus.PENDING;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateCategory(InquiryCategory category) {
        this.category = category;
    }

    public void changeStatus(InquiryStatus status) {
        this.status = status;
    }

    public boolean isAnswered() {
        return this.status == InquiryStatus.ANSWERED;
    }

    public boolean canBeModified() {
        return this.status == InquiryStatus.PENDING;
    }

    public void softDelete(String deletedBy) {
        this.deletedAt = java.time.LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.isDelete = true;
    }
}

