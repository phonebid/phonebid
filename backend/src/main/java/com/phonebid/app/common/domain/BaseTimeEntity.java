package com.phonebid.app.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 최소한의 시간 정보만 포함하는 베이스 엔티티
 * 
 * immutable한 엔티티(예: RefreshToken)에 사용됩니다.
 * 생성 시각(createdAt)과 삭제 시각(deletedAt)만 관리하며,
 * 수정 시각, 생성자, 수정자, 삭제자 등의 정보는 포함하지 않습니다.
 * 
 * 일반 엔티티(예: User, Product)는 BaseEntity를 사용하세요.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE {h-table} SET deleted_at = CURRENT_TIMESTAMP WHERE {h-where}")
public abstract class BaseTimeEntity implements Serializable {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @Comment("생성 시각")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    @Comment("삭제 시각 (soft delete)")
    protected LocalDateTime deletedAt;
}

