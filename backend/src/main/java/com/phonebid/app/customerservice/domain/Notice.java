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
@Table(name = "notices", indexes = {
    @Index(name = "idx_notices_is_important", columnList = "is_important"),
    @Index(name = "idx_notices_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("공지사항 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    @Comment("작성 관리자")
    private User admin;

    @Column(name = "title", nullable = false, length = 200)
    @Comment("공지사항 제목")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @Comment("공지사항 내용")
    private String content;

    @Column(name = "is_important", nullable = false)
    @Comment("중요 공지 여부")
    private Boolean isImportant;

    @Column(name = "view_count", nullable = false)
    @Comment("조회수")
    private Long viewCount;

    @Builder
    public Notice(User admin, String title, String content, Boolean isImportant) {
        this.admin = admin;
        this.title = title;
        this.content = content;
        this.isImportant = isImportant != null ? isImportant : false;
        this.viewCount = 0L;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateIsImportant(Boolean isImportant) {
        this.isImportant = isImportant;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void softDelete(String deletedBy) {
        this.deletedAt = java.time.LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.isDelete = true;
    }
}

