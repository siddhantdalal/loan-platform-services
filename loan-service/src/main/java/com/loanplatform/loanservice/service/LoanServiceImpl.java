package com.loanplatform.loanservice.service;

import com.loanplatform.common.dto.LoanApplicationDto;
import com.loanplatform.common.event.LoanEvent;
import com.loanplatform.common.exception.BadRequestException;
import com.loanplatform.common.exception.ResourceNotFoundException;
import com.loanplatform.loanservice.dto.LoanApplicationRequest;
import com.loanplatform.loanservice.dto.LoanDecisionRequest;
import com.loanplatform.loanservice.entity.LoanApplication;
import com.loanplatform.loanservice.entity.LoanStatus;
import com.loanplatform.loanservice.mapper.LoanMapper;
import com.loanplatform.loanservice.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanApplicationRepository loanRepository;
    private final KafkaProducerService kafkaProducerService;

    private static final BigDecimal BASE_RATE = new BigDecimal("5.00");
    private static final BigDecimal AMOUNT_FACTOR = new BigDecimal("0.00001");
    private static final BigDecimal TERM_FACTOR = new BigDecimal("0.01");

    @Override
    @Transactional
    public LoanApplicationDto submitApplication(LoanApplicationRequest request) {
        LoanApplication application = LoanMapper.toEntity(request);

        // Calculate a preliminary interest rate based on amount and term
        BigDecimal interestRate = calculateInterestRate(request.getAmount(), request.getTermMonths());
        application.setInterestRate(interestRate);

        LoanApplication saved = loanRepository.save(application);
        log.info("Loan application submitted: ID={}, userId={}, amount={}", saved.getId(), saved.getUserId(), saved.getAmount());

        LoanApplicationDto dto = LoanMapper.toDto(saved);

        // Publish loan submitted event
        LoanEvent event = LoanEvent.builder()
                .eventType(LoanEvent.SUBMITTED)
                .loan(dto)
                .timestamp(LocalDateTime.now())
                .build();
        kafkaProducerService.sendLoanEvent(event);

        return dto;
    }

    @Override
    @Cacheable(value = "loans", key = "#id")
    public LoanApplicationDto getApplicationById(Long id) {
        LoanApplication application = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Application", "id", id));
        return LoanMapper.toDto(application);
    }

    @Override
    public List<LoanApplicationDto> getApplicationsByUserId(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .map(LoanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "loans", key = "#id")
    public LoanApplicationDto processDecision(Long id, LoanDecisionRequest decision) {
        LoanApplication application = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Application", "id", id));

        if (application.getStatus() != LoanStatus.PENDING && application.getStatus() != LoanStatus.UNDER_REVIEW) {
            throw new BadRequestException("Loan application has already been processed with status: " + application.getStatus());
        }

        if (decision.getDecision() != LoanStatus.APPROVED && decision.getDecision() != LoanStatus.REJECTED) {
            throw new BadRequestException("Decision must be APPROVED or REJECTED");
        }

        application.setStatus(decision.getDecision());

        if (decision.getDecision() == LoanStatus.APPROVED && decision.getInterestRate() != null) {
            application.setInterestRate(decision.getInterestRate());
        }

        LoanApplication updated = loanRepository.save(application);
        log.info("Loan decision processed: ID={}, status={}", updated.getId(), updated.getStatus());

        LoanApplicationDto dto = LoanMapper.toDto(updated);

        // Publish loan decision event
        String eventType = decision.getDecision() == LoanStatus.APPROVED ? LoanEvent.APPROVED : LoanEvent.REJECTED;
        LoanEvent event = LoanEvent.builder()
                .eventType(eventType)
                .loan(dto)
                .timestamp(LocalDateTime.now())
                .build();
        kafkaProducerService.sendLoanEvent(event);

        return dto;
    }

    @Override
    public List<LoanApplicationDto> getAllApplications() {
        return loanRepository.findAll().stream()
                .map(LoanMapper::toDto)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateInterestRate(BigDecimal amount, Integer termMonths) {
        // Simple interest rate calculation: base rate + risk adjustment
        BigDecimal amountRisk = amount.multiply(AMOUNT_FACTOR);
        BigDecimal termRisk = BigDecimal.valueOf(termMonths).multiply(TERM_FACTOR);
        return BASE_RATE.add(amountRisk).add(termRisk).setScale(2, RoundingMode.HALF_UP);
    }
}
