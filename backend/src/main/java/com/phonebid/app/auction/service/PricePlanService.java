package com.phonebid.app.auction.service;

import com.phonebid.app.auction.domain.Carrier;
import com.phonebid.app.auction.domain.PricePlan;
import com.phonebid.app.auction.domain.PricePlanCategory;
import com.phonebid.app.auction.dto.request.PricePlanCreateRequestDto;
import com.phonebid.app.auction.dto.response.PricePlanResponseDto;
import com.phonebid.app.auction.repository.PricePlanRepository;
import com.phonebid.app.common.errorcode.AuctionErrorCode;
import com.phonebid.app.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricePlanService {

    private final PricePlanRepository pricePlanRepository;

    @Transactional(readOnly = true)
    public List<PricePlanResponseDto> getActivePricePlans(Carrier carrier, PricePlanCategory category) {
        List<PricePlan> pricePlans;

        if (carrier != null && category != null) {
            pricePlans = pricePlanRepository.findByCarrierAndCategoryAndIsActiveTrue(carrier, category);
        } else if (carrier != null) {
            pricePlans = pricePlanRepository.findByCarrierAndIsActiveTrue(carrier);
        } else if (category != null) {
            pricePlans = pricePlanRepository.findByCategoryAndIsActiveTrue(category);
        } else {
            pricePlans = pricePlanRepository.findAllActiveOrderByCarrierAndDisplayOrder();
        }

        return pricePlans.stream()
                .map(PricePlanResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PricePlanResponseDto getPricePlanById(UUID id) {
        PricePlan pricePlan = pricePlanRepository.findById(id)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.PRICE_PLAN_NOT_FOUND));
        return PricePlanResponseDto.from(pricePlan);
    }

    @Transactional(readOnly = true)
    public List<PricePlanResponseDto> getAllPricePlansForAdmin(Carrier carrier) {
        List<PricePlan> pricePlans;

        if (carrier != null) {
            pricePlans = pricePlanRepository.findByCarrierForAdmin(carrier);
        } else {
            pricePlans = pricePlanRepository.findAllForAdmin();
        }

        return pricePlans.stream()
                .map(PricePlanResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PricePlanResponseDto createPricePlan(PricePlanCreateRequestDto requestDto) {
        if (pricePlanRepository.existsByCarrierAndPlanName(
                requestDto.getCarrier(), requestDto.getPlanName())) {
            throw new CustomException(AuctionErrorCode.PRICE_PLAN_ALREADY_EXISTS);
        }

        PricePlan pricePlan = requestDto.toEntity();
        PricePlan savedPricePlan = pricePlanRepository.save(pricePlan);

        log.info("요금제 생성 완료 - id: {}, carrier: {}, planName: {}", 
                savedPricePlan.getId(), savedPricePlan.getCarrier(), savedPricePlan.getPlanName());

        return PricePlanResponseDto.from(savedPricePlan);
    }

    @Transactional
    public void deactivatePricePlan(UUID id) {
        PricePlan pricePlan = pricePlanRepository.findById(id)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.PRICE_PLAN_NOT_FOUND));

        pricePlan.deactivate();

        log.info("요금제 비활성화 완료 - id: {}, planName: {}", id, pricePlan.getPlanName());
    }

    @Transactional
    public void activatePricePlan(UUID id) {
        PricePlan pricePlan = pricePlanRepository.findById(id)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.PRICE_PLAN_NOT_FOUND));

        pricePlan.activate();

        log.info("요금제 활성화 완료 - id: {}, planName: {}", id, pricePlan.getPlanName());
    }

    @Transactional
    public void updateDisplayOrder(UUID id, Integer displayOrder) {
        PricePlan pricePlan = pricePlanRepository.findById(id)
                .orElseThrow(() -> new CustomException(AuctionErrorCode.PRICE_PLAN_NOT_FOUND));

        pricePlan.updateDisplayOrder(displayOrder);

        log.info("요금제 순서 변경 완료 - id: {}, displayOrder: {}", id, displayOrder);
    }
}
