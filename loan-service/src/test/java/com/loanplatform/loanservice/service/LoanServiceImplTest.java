package com.loanplatform.loanservice.service;

import com.loanplatform.common.dto.LoanApplicationDto;
import com.loanplatform.common.exception.BadRequestException;
import com.loanplatform.common.exception.ResourceNotFoundException;
import com.loanplatform.loanservice.dto.LoanApplicationRequest;
import com.loanplatform.loanservice.dto.LoanDecisionRequest;
import com.loanplatform.loanservice.entity.LoanApplication;
import com.loanplatform.loanservice.entity.LoanStatus;
import com.loanplatform.loanservice.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanApplicationRepository loanRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private LoanServiceImpl loanService;

    private LoanApplication loanApplication;
    private LoanApplicationRequest applicationRequest;

    @BeforeEach
    void setUp() {
        applicationRequest = LoanApplicationRequest.builder()
                .userId(1L)
                .amount(new BigDecimal("50000"))
                .termMonths(36)
                .purpose("Home renovation")
                .build();

        loanApplication = LoanApplication.builder()
                .id(1L)
                .userId(1L)
                .amount(new BigDecimal("50000"))
                .termMonths(36)
                .purpose("Home renovation")
                .status(LoanStatus.PENDING)
                .interestRate(new BigDecimal("5.86"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void submitApplication_createsLoanWithPendingStatus() {
        when(loanRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

        LoanApplicationDto result = loanService.submitApplication(applicationRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(kafkaProducerService).sendLoanEvent(any());
    }

    @Test
    void getApplicationById_withExistingId_returnsDto() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loanApplication));

        LoanApplicationDto result = loanService.getApplicationById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPurpose()).isEqualTo("Home renovation");
    }

    @Test
    void getApplicationById_withNonExistentId_throwsResourceNotFound() {
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.getApplicationById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getApplicationsByUserId_returnsListOfDtos() {
        when(loanRepository.findByUserId(1L)).thenReturn(List.of(loanApplication));

        var results = loanService.getApplicationsByUserId(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void processDecision_approvesLoan() {
        LoanDecisionRequest decision = LoanDecisionRequest.builder()
                .decision(LoanStatus.APPROVED)
                .interestRate(new BigDecimal("7.50"))
                .build();

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loanApplication));
        when(loanRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

        LoanApplicationDto result = loanService.processDecision(1L, decision);

        assertThat(result).isNotNull();
        verify(loanRepository).save(any(LoanApplication.class));
        verify(kafkaProducerService).sendLoanEvent(any());
    }

    @Test
    void processDecision_rejectsLoan() {
        LoanDecisionRequest decision = LoanDecisionRequest.builder()
                .decision(LoanStatus.REJECTED)
                .build();

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loanApplication));
        when(loanRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

        LoanApplicationDto result = loanService.processDecision(1L, decision);

        assertThat(result).isNotNull();
        verify(kafkaProducerService).sendLoanEvent(any());
    }

    @Test
    void processDecision_alreadyProcessed_throwsBadRequest() {
        loanApplication.setStatus(LoanStatus.APPROVED);
        LoanDecisionRequest decision = LoanDecisionRequest.builder()
                .decision(LoanStatus.REJECTED)
                .build();

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loanApplication));

        assertThatThrownBy(() -> loanService.processDecision(1L, decision))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been processed");
    }
}
