package com.phonebid.app.phone.domain;

import com.phonebid.app.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "phone_options",
       uniqueConstraints = @UniqueConstraint(name = "uq_phone_options_model_type_value", 
                                           columnNames = {"model_id", "option_type", "option_value"}),
       indexes = {
           @Index(name = "idx_phone_options_model_id", columnList = "model_id"),
           @Index(name = "idx_phone_options_model_type", columnList = "model_id, option_type")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @Comment("옵션 고유 ID (UUID)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private PhoneModel model;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false)
    @Comment("예: 'COLOR', 'STORAGE'")
    private OptionType optionType;

    @Column(name = "option_value", nullable = false)
    @Comment("예: 'Black', '128'")
    private String optionValue;

    @Column(name = "display_label")
    @Comment("표시명(예: '블랙', '128GB')")
    private String displayLabel;

    @Builder
    public PhoneOption(PhoneModel model, OptionType optionType, String optionValue, String displayLabel) {
        this.model = model;
        this.optionType = optionType;
        this.optionValue = optionValue;
        this.displayLabel = displayLabel != null ? displayLabel : optionValue;
    }

    // 비즈니스 메서드
    public void updateOptionValue(String optionValue) {
        this.optionValue = optionValue;
        if (this.displayLabel == null || this.displayLabel.equals(this.optionValue)) {
            this.displayLabel = optionValue;
        }
    }

    public void updateDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public boolean isColorOption() {
        return optionType == OptionType.COLOR;
    }

    public boolean isStorageOption() {
        return optionType == OptionType.STORAGE;
    }

    public String getFormattedValue() {
        if (isStorageOption() && !optionValue.toLowerCase().contains("gb") && !optionValue.toLowerCase().contains("tb")) {
            return optionValue + "GB";
        }
        return displayLabel != null ? displayLabel : optionValue;
    }

    public String getOptionSummary() {
        return String.format("%s: %s", optionType.getDisplayName(), getFormattedValue());
    }

    // Enum 정의
    public enum OptionType {
        COLOR("색상"),
        STORAGE("저장용량");

        private final String displayName;

        OptionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

