package com.phonebid.app.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @Comment("생성 시각")
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by")
    @Comment("생성자")
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    @Comment("수정 시각")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    @Comment("수정자")
    private String updatedBy;

    @Column(name = "deleted_at")
    @Comment("삭제 시각 (소프트 삭제)")
    protected LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    @Comment("삭제자")
    protected String deletedBy;

    @Column(name = "is_delete")
    @Comment("삭제 여부")
    protected Boolean isDelete;

    @PrePersist
    protected void onCreate() {
        if (this.isDelete == null) {
            this.isDelete = false;
        }
    }
    
}
