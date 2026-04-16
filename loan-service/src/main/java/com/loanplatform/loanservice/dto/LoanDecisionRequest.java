package com.loanplatform.loanservice.dto;

import com.loanplatform.loanservice.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDecisionRequest {

    @NotNull(message = "Decision is required")
    private LoanStatus decision;

    private BigDecimal interestRate;
}
