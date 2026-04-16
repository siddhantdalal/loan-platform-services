package com.loanplatform.common.constants;

public final class KafkaConstants {

    private KafkaConstants() {
    }

    public static final String USER_EVENTS_TOPIC = "user-events";
    public static final String LOAN_EVENTS_TOPIC = "loan-events";

    public static final String NOTIFICATION_GROUP = "notification-group";
    public static final String LOAN_GROUP = "loan-group";
}
