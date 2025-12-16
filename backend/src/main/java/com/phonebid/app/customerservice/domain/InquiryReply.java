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
@Table(name = "inquiry_replies", indexes = {
    @Index(name = "idx_inquiry_replies_inquiry_id", columnList = "inquiry_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryReply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("답변 고유 ID (UUID)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false, unique = true)
    @Comment("문의")
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    @Comment("답변 작성 관리자")
    private User admin;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @Comment("답변 내용")
    private String content;

    @Builder
    public InquiryReply(Inquiry inquiry, User admin, String content) {
        this.inquiry = inquiry;
        this.admin = admin;
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete(String deletedBy) {
        this.deletedAt = java.time.LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.isDelete = true;
    }
}

