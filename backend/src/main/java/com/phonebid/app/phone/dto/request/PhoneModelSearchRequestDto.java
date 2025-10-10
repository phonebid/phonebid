package com.phonebid.app.phone.dto.request;

import com.phonebid.app.phone.domain.Brand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 휴대폰 모델 검색 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PhoneModelSearchRequestDto {

    private Brand brand;
    private String model;
    private String modelNumber;
    private Integer minPrice;
    private Integer maxPrice;
    private LocalDate releasedAfter;
    private LocalDate releasedBefore;
    private Boolean isReleased;
    private String searchKeyword; // 모델명 또는 모델번호로 검색

    // 페이징 및 정렬
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    public PhoneModelSearchRequestDto(Brand brand, String model, String modelNumber, 
                                    Integer minPrice, Integer maxPrice, LocalDate releasedAfter, 
                                    LocalDate releasedBefore, Boolean isReleased, String searchKeyword,
                                    int page, int size, String sortBy, String sortDirection) {
        this.brand = brand;
        this.model = model;
        this.modelNumber = modelNumber;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.releasedAfter = releasedAfter;
        this.releasedBefore = releasedBefore;
        this.isReleased = isReleased;
        this.searchKeyword = searchKeyword;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    /**
     * 검색 조건이 있는지 확인
     */
    public boolean hasSearchConditions() {
        return brand != null || 
               (model != null && !model.trim().isEmpty()) ||
               (modelNumber != null && !modelNumber.trim().isEmpty()) ||
               minPrice != null || maxPrice != null ||
               releasedAfter != null || releasedBefore != null ||
               isReleased != null ||
               (searchKeyword != null && !searchKeyword.trim().isEmpty());
    }

    /**
     * 가격 범위 검색 조건이 있는지 확인
     */
    public boolean hasPriceRange() {
        return minPrice != null || maxPrice != null;
    }

    /**
     * 출시일 범위 검색 조건이 있는지 확인
     */
    public boolean hasReleaseDateRange() {
        return releasedAfter != null || releasedBefore != null;
    }
}
