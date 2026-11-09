package com.marketplace.services;

import com.marketplace.dto.RegisterRequestDTO;
import com.marketplace.dto.UserResponseDTO;
import com.marketplace.entities.User;
import com.marketplace.exceptions.EmailAlreadyRegisteredException;
import com.marketplace.repositories.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        String email = request.email();
        if (email != null) {
            email = email.strip().toLowerCase(Locale.ROOT);
        }

        RegisterRequestDTO normalizedRequest = new RegisterRequestDTO(request.name(), email, request.password());
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(normalizedRequest);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = new User(
                request.name().strip(),
                email,
                passwordEncoder.encode(request.password())
        );

        try {
            return UserResponseDTO.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            if (isEmailConflict(exception)) {
                throw new EmailAlreadyRegisteredException(exception);
            }
            throw exception;
        }
    }

    private boolean isEmailConflict(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation
                    && "uk_app_user_email".equals(violation.getConstraintName())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
