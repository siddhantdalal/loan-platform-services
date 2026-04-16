package com.loanplatform.userservice.service;

import com.loanplatform.common.dto.UserDto;
import com.loanplatform.userservice.dto.AuthResponse;
import com.loanplatform.userservice.dto.LoginRequest;
import com.loanplatform.userservice.dto.RegisterRequest;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserDto getUserById(Long id);

    UserDto getUserByEmail(String email);

    UserDto updateUser(Long id, RegisterRequest request);
}
