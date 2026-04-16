package com.loanplatform.loanservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Term in months is required")
    @Min(value = 6, message = "Minimum term is 6 months")
    @Max(value = 360, message = "Maximum term is 360 months")
    private Integer termMonths;

    @NotBlank(message = "Purpose is required")
    private String purpose;
}
