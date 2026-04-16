package com.loanplatform.notificationservice.service;

import com.loanplatform.common.event.LoanEvent;
import com.loanplatform.common.event.UserEvent;
import com.loanplatform.notificationservice.entity.Notification;

import java.util.List;

public interface NotificationService {

    void processUserEvent(UserEvent event);

    void processLoanEvent(LoanEvent event);

    List<Notification> getNotificationsByUserId(Long userId);
}
