package com.loanplatform.userservice.mapper;

import com.loanplatform.common.dto.UserDto;
import com.loanplatform.userservice.entity.User;
import com.loanplatform.userservice.dto.RegisterRequest;
import com.loanplatform.userservice.entity.Role;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .build();
    }

    public static User toEntity(RegisterRequest request) {
        return User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.USER)
                .build();
    }
}
