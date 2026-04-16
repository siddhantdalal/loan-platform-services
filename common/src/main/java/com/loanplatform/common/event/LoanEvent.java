package com.loanplatform.common.event;

import com.loanplatform.common.dto.LoanApplicationDto;
import com.loanplatform.common.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEvent {

    private String eventType;
    private LoanApplicationDto loan;
    private UserDto user;
    private LocalDateTime timestamp;

    public static final String SUBMITTED = "SUBMITTED";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
}
