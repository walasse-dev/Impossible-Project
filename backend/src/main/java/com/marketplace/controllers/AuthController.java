package com.marketplace.controllers;

import com.marketplace.dto.CsrfResponseDTO;
import com.marketplace.dto.RegisterRequestDTO;
import com.marketplace.dto.UserResponseDTO;
import com.marketplace.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    private final UserService userService;

    @GetMapping("/csrf")
    public CsrfResponseDTO csrf(CsrfToken csrfToken) {
        return new CsrfResponseDTO(
                csrfToken.getHeaderName(),
                csrfToken.getToken()
        );
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        return userService.register(request);
    }
}
