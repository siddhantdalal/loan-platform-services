package com.loanplatform.loanservice.controller;

import com.loanplatform.common.dto.ApiResponse;
import com.loanplatform.common.dto.LoanApplicationDto;
import com.loanplatform.loanservice.dto.LoanApplicationRequest;
import com.loanplatform.loanservice.dto.LoanDecisionRequest;
import com.loanplatform.loanservice.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanApplicationDto>> submitApplication(
            @Valid @RequestBody LoanApplicationRequest request) {
        LoanApplicationDto loan = loanService.submitApplication(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(loan, "Loan application submitted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanApplicationDto>>> getAllApplications() {
        List<LoanApplicationDto> loans = loanService.getAllApplications();
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanApplicationDto>> getApplicationById(@PathVariable Long id) {
        LoanApplicationDto loan = loanService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.success(loan));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<LoanApplicationDto>>> getApplicationsByUserId(@PathVariable Long userId) {
        List<LoanApplicationDto> loans = loanService.getApplicationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @PutMapping("/{id}/decision")
    public ResponseEntity<ApiResponse<LoanApplicationDto>> processDecision(
            @PathVariable Long id,
            @Valid @RequestBody LoanDecisionRequest decision) {
        LoanApplicationDto loan = loanService.processDecision(id, decision);
        return ResponseEntity.ok(ApiResponse.success(loan, "Loan decision processed successfully"));
    }
}
