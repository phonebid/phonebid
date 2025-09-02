package com.phonebid.app.phone.domain;

/**
 * 휴대폰 브랜드 Enum
 * 지원하는 주요 브랜드들을 정의
 */
public enum Brand {
    APPLE("Apple", "애플"),
    SAMSUNG("Samsung", "삼성");

    private final String englishName;
    private final String koreanName;

    Brand(String englishName, String koreanName) {
        this.englishName = englishName;
        this.koreanName = koreanName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public String getDisplayName() {
        return englishName;
    }

    public String getDisplayNameKorean() {
        return koreanName;
    }

    /**
     * 브랜드명으로 Enum 찾기 (대소문자 구분 없음)
     */
    public static Brand fromName(String name) {
        if (name == null) {
            return null;
        }
        
        for (Brand brand : values()) {
            if (brand.englishName.equalsIgnoreCase(name) || 
                brand.koreanName.equals(name) ||
                brand.name().equalsIgnoreCase(name)) {
                return brand;
            }
        }
        
        throw new IllegalArgumentException("Unknown brand: " + name);
    }

    /**
     * 브랜드가 주요 브랜드인지 확인
     */
    public boolean isMajorBrand() {
        return this == APPLE || this == SAMSUNG ;
    }

    /**
     * 브랜드가 안드로이드 기반인지 확인
     */
    public boolean isAndroidBrand() {
        return this != APPLE;
    }

    @Override
    public String toString() {
        return englishName;
    }
}

