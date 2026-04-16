package com.loanplatform.userservice.service;

import com.loanplatform.common.dto.UserDto;
import com.loanplatform.common.event.UserEvent;
import com.loanplatform.common.exception.BadRequestException;
import com.loanplatform.common.exception.ResourceNotFoundException;
import com.loanplatform.common.security.JwtUtil;
import com.loanplatform.userservice.dto.AuthResponse;
import com.loanplatform.userservice.dto.LoginRequest;
import com.loanplatform.userservice.dto.RegisterRequest;
import com.loanplatform.userservice.entity.User;
import com.loanplatform.userservice.mapper.UserMapper;
import com.loanplatform.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", savedUser.getEmail());

        UserDto userDto = UserMapper.toDto(savedUser);
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        // Publish user registered event to Kafka
        UserEvent event = UserEvent.builder()
                .eventType(UserEvent.REGISTERED)
                .user(userDto)
                .timestamp(LocalDateTime.now())
                .build();
        kafkaProducerService.sendUserEvent(event);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userDto)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        log.info("User logged in: {}", user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        UserDto userDto = UserMapper.toDto(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userDto)
                .build();
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return UserMapper.toDto(user);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserDto updateUser(Long id, RegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getEmail());

        UserDto userDto = UserMapper.toDto(updatedUser);

        // Publish user updated event to Kafka
        UserEvent event = UserEvent.builder()
                .eventType(UserEvent.UPDATED)
                .user(userDto)
                .timestamp(LocalDateTime.now())
                .build();
        kafkaProducerService.sendUserEvent(event);

        return userDto;
    }
}
