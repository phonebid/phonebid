package com.phonebid.app.phone.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.phonebid.app.phone.repository.PhoneOptionRepository;
import com.phonebid.app.phone.repository.PhoneModelRepository;
import com.phonebid.app.common.exception.CustomException;
import com.phonebid.app.common.errorcode.PhoneErrorCode;
import com.phonebid.app.phone.dto.response.PhoneOptionResponseDto;
import com.phonebid.app.phone.dto.request.PhoneOptionCreateRequestDto;
import com.phonebid.app.phone.dto.request.PhoneOptionUpdateRequestDto;
import com.phonebid.app.phone.dto.request.PhoneOptionDeleteRequestDto;
import com.phonebid.app.phone.domain.PhoneOption;
import com.phonebid.app.phone.domain.PhoneModel;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhoneOptionService {
    
    private final PhoneOptionRepository phoneOptionRepository;
    private final PhoneModelRepository phoneModelRepository;

    public List<PhoneOptionResponseDto> getPhoneOptions() {
        List<PhoneOption> options = phoneOptionRepository.findAll();
        if (options.isEmpty()) {
            throw new CustomException(PhoneErrorCode.PHONE_OPTION_NOT_FOUND);
        }

        return options.stream().map(PhoneOptionResponseDto::from).collect(Collectors.toList());
    }
    
    public PhoneOptionResponseDto createPhoneOption(PhoneOptionCreateRequestDto requestDto) {
        // 모델 존재 확인
        PhoneModel model = phoneModelRepository.findById(requestDto.getModelId())
            .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_MODEL_NOT_FOUND));
        
        // 중복 확인
        if (phoneOptionRepository.findByModelIdAndOptionTypeAndOptionValue(
                requestDto.getModelId(), requestDto.getOptionType(), requestDto.getOptionValue()).isPresent()) {
            throw new CustomException(PhoneErrorCode.PHONE_OPTION_ALREADY_EXISTS);
        }
        
        return PhoneOptionResponseDto.from(phoneOptionRepository.save(requestDto.toEntity(model)));
    }

    public List<PhoneOptionResponseDto> createPhoneOptions(List<PhoneOptionCreateRequestDto> requestDtos) {
        if (requestDtos == null || requestDtos.isEmpty()) {
            throw new CustomException(PhoneErrorCode.PHONE_OPTION_NOT_FOUND);
        }
        
        return requestDtos.stream()
            .map(this::createPhoneOption)
            .collect(Collectors.toList());
    }
    
    public PhoneOptionResponseDto updatePhoneOption(PhoneOptionUpdateRequestDto requestDto) {
        PhoneOption existingOption = phoneOptionRepository.findById(requestDto.getId())
            .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_OPTION_NOT_FOUND));
        
        // 옵션 타입과 옵션 값 조합의 중복 확인 (둘 다 업데이트되는 경우)
        validateOptionCombination(requestDto, existingOption);
        
        // 한 번의 메서드 호출로 모든 필드 업데이트
        updateFields(requestDto, existingOption);
        return PhoneOptionResponseDto.from(phoneOptionRepository.save(existingOption));
    }

    private void validateOptionCombination(PhoneOptionUpdateRequestDto requestDto, PhoneOption existingOption) {
        if (phoneOptionRepository.findByModelIdAndOptionTypeAndOptionValue(existingOption.getModel().getId(), requestDto.getOptionType(), requestDto.getOptionValue()).isPresent()) {
            throw new CustomException(PhoneErrorCode.PHONE_OPTION_ALREADY_EXISTS);
        }
    }


    private void updateFields(PhoneOptionUpdateRequestDto requestDto, PhoneOption existingOption) {
        // 한 번의 메서드 호출로 모든 필드 업데이트
        existingOption.updateOptionValue(requestDto.getOptionValue());
        existingOption.updateDisplayLabel(requestDto.getDisplayLabel());
    }

    
    public void deletePhoneOption(PhoneOptionDeleteRequestDto requestDto) {
        if (phoneOptionRepository.findById(requestDto.getId()).isEmpty()) {
            throw new CustomException(PhoneErrorCode.PHONE_OPTION_NOT_FOUND);
        }
        phoneOptionRepository.deleteById(requestDto.getId());
    }
    
    public PhoneOptionResponseDto getPhoneOption(UUID id) {
        PhoneOption option = phoneOptionRepository.findById(id)
            .orElseThrow(() -> new CustomException(PhoneErrorCode.PHONE_OPTION_NOT_FOUND));
        return PhoneOptionResponseDto.from(option);
    }
}
