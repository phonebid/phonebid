package com.phonebid.app.customerservice.domain;

import com.phonebid.app.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "faqs", indexes = {
    @Index(name = "idx_faqs_category", columnList = "category")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Faq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("FAQ 고유 ID (UUID)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @Comment("FAQ 카테고리")
    private FaqCategory category;

    @Column(name = "question", nullable = false, length = 200)
    @Comment("질문")
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    @Comment("답변")
    private String answer;

    @Column(name = "view_count", nullable = false)
    @Comment("조회수")
    private Long viewCount;

    @Builder
    public Faq(FaqCategory category, String question, String answer) {
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.viewCount = 0L;
    }

    public void updateCategory(FaqCategory category) {
        this.category = category;
    }

    public void updateQuestion(String question) {
        this.question = question;
    }

    public void updateAnswer(String answer) {
        this.answer = answer;
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

