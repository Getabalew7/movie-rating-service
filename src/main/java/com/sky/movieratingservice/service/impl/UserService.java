package com.sky.movieratingservice.service.impl;

import com.sky.movieratingservice.api.dto.response.UserResponseDto;
import com.sky.movieratingservice.domain.entity.User;
import com.sky.movieratingservice.domain.exception.ResourceNotFoundException;
import com.sky.movieratingservice.domain.repository.UserRepository;
import com.sky.movieratingservice.mapper.UserMapper;
import com.sky.movieratingservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        log.debug("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return userMapper.toUserResponse(user);
    }
}
