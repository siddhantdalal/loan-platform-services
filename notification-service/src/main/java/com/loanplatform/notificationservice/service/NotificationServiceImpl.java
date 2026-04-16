package com.loanplatform.notificationservice.service;

import com.loanplatform.common.event.LoanEvent;
import com.loanplatform.common.event.UserEvent;
import com.loanplatform.notificationservice.entity.*;
import com.loanplatform.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    public void processUserEvent(UserEvent event) {
        log.info("Processing user event: {} for user: {}", event.getEventType(), event.getUser().getEmail());

        if (UserEvent.REGISTERED.equals(event.getEventType())) {
            String subject = "Welcome to Loan Platform!";
            String content = String.format(
                    "Dear %s %s,\n\nWelcome to our Loan Platform! Your account has been successfully created.\n\n" +
                    "You can now apply for loans, track your applications, and manage your profile.\n\n" +
                    "Best regards,\nLoan Platform Team",
                    event.getUser().getFirstName(),
                    event.getUser().getLastName()
            );

            sendAndSaveNotification(
                    event.getUser().getId(),
                    event.getUser().getEmail(),
                    NotificationType.USER_REGISTERED,
                    subject,
                    content
            );
        }
    }

    @Override
    public void processLoanEvent(LoanEvent event) {
        log.info("Processing loan event: {} for loan ID: {}", event.getEventType(), event.getLoan().getId());

        switch (event.getEventType()) {
            case LoanEvent.SUBMITTED -> {
                String subject = "Loan Application Received";
                String content = String.format(
                        "Your loan application #%d has been received.\n\n" +
                        "Details:\n- Amount: $%s\n- Term: %d months\n- Purpose: %s\n\n" +
                        "We will review your application and notify you of the decision.\n\n" +
                        "Best regards,\nLoan Platform Team",
                        event.getLoan().getId(),
                        event.getLoan().getAmount(),
                        event.getLoan().getTermMonths(),
                        event.getLoan().getPurpose()
                );
                sendAndSaveNotification(
                        event.getLoan().getUserId(),
                        null,
                        NotificationType.LOAN_SUBMITTED,
                        subject,
                        content
                );
            }
            case LoanEvent.APPROVED -> {
                String subject = "Loan Application Approved!";
                String content = String.format(
                        "Congratulations! Your loan application #%d has been approved.\n\n" +
                        "Approved Details:\n- Amount: $%s\n- Term: %d months\n- Interest Rate: %s%%\n\n" +
                        "Our team will contact you with the next steps.\n\n" +
                        "Best regards,\nLoan Platform Team",
                        event.getLoan().getId(),
                        event.getLoan().getAmount(),
                        event.getLoan().getTermMonths(),
                        event.getLoan().getInterestRate()
                );
                sendAndSaveNotification(
                        event.getLoan().getUserId(),
                        null,
                        NotificationType.LOAN_APPROVED,
                        subject,
                        content
                );
            }
            case LoanEvent.REJECTED -> {
                String subject = "Loan Application Update";
                String content = String.format(
                        "We regret to inform you that your loan application #%d has not been approved at this time.\n\n" +
                        "Application Details:\n- Amount: $%s\n- Term: %d months\n- Purpose: %s\n\n" +
                        "You may reapply after reviewing our eligibility criteria.\n\n" +
                        "Best regards,\nLoan Platform Team",
                        event.getLoan().getId(),
                        event.getLoan().getAmount(),
                        event.getLoan().getTermMonths(),
                        event.getLoan().getPurpose()
                );
                sendAndSaveNotification(
                        event.getLoan().getUserId(),
                        null,
                        NotificationType.LOAN_REJECTED,
                        subject,
                        content
                );
            }
            default -> log.warn("Unknown loan event type: {}", event.getEventType());
        }
    }

    @Override
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    private void sendAndSaveNotification(Long userId, String email, NotificationType type,
                                          String subject, String content) {
        boolean sent = false;
        if (email != null) {
            sent = emailService.sendEmail(email, subject, content);
        } else {
            // Log notification when email is not available
            log.info("Notification for user {}: [{}] {}", userId, subject, content);
            sent = true;
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .channel(NotificationChannel.EMAIL)
                .subject(subject)
                .content(content)
                .status(sent ? NotificationStatus.SENT : NotificationStatus.FAILED)
                .recipientEmail(email)
                .build();

        notificationRepository.save(notification);
        log.info("Notification saved: type={}, userId={}, status={}", type, userId, notification.getStatus());
    }
}
