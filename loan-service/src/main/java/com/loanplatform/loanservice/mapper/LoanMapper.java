package com.loanplatform.loanservice.mapper;

import com.loanplatform.common.dto.LoanApplicationDto;
import com.loanplatform.loanservice.dto.LoanApplicationRequest;
import com.loanplatform.loanservice.entity.LoanApplication;

public final class LoanMapper {

    private LoanMapper() {
    }

    public static LoanApplicationDto toDto(LoanApplication entity) {
        return LoanApplicationDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .amount(entity.getAmount())
                .termMonths(entity.getTermMonths())
                .purpose(entity.getPurpose())
                .status(entity.getStatus().name())
                .interestRate(entity.getInterestRate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static LoanApplication toEntity(LoanApplicationRequest request) {
        return LoanApplication.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .termMonths(request.getTermMonths())
                .purpose(request.getPurpose())
                .build();
    }
}
