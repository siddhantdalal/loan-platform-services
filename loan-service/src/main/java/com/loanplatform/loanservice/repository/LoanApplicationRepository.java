package com.loanplatform.loanservice.repository;

import com.loanplatform.loanservice.entity.LoanApplication;
import com.loanplatform.loanservice.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByUserId(Long userId);

    List<LoanApplication> findByStatus(LoanStatus status);
}
