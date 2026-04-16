package com.loanplatform.common.event;

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
public class UserEvent {

    private String eventType;
    private UserDto user;
    private LocalDateTime timestamp;

    public static final String REGISTERED = "REGISTERED";
    public static final String UPDATED = "UPDATED";
}
