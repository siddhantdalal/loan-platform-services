package com.loanplatform.loanservice.service;

import com.loanplatform.common.dto.LoanApplicationDto;
import com.loanplatform.loanservice.dto.LoanApplicationRequest;
import com.loanplatform.loanservice.dto.LoanDecisionRequest;

import java.util.List;

public interface LoanService {

    LoanApplicationDto submitApplication(LoanApplicationRequest request);

    LoanApplicationDto getApplicationById(Long id);

    List<LoanApplicationDto> getApplicationsByUserId(Long userId);

    LoanApplicationDto processDecision(Long id, LoanDecisionRequest decision);

    List<LoanApplicationDto> getAllApplications();
}
