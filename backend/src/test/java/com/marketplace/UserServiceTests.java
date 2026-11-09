package com.marketplace;

import com.marketplace.dto.RegisterRequestDTO;
import com.marketplace.entities.Role;
import com.marketplace.entities.User;
import com.marketplace.exceptions.EmailAlreadyRegisteredException;
import com.marketplace.repositories.UserRepository;
import com.marketplace.services.UserService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class UserServiceTests {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Validator validator;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void registersCustomerWithNormalizedEmailAndHashedPassword() {
        var response = userService.register(new RegisterRequestDTO(" Ana ", " ANA@EXAMPLE.COM ", "password1"));
        var stored = userRepository.findByEmailIgnoreCase("ANA@EXAMPLE.COM").orElseThrow();

        assertNotNull(response.id());
        assertEquals(stored.getId(), response.id());
        assertEquals("Ana", response.name());
        assertEquals("ana@example.com", response.email());
        assertEquals(Role.CUSTOMER, response.role());
        assertNotEquals("password1", stored.getPasswordHash());
        assertTrue(passwordEncoder.matches("password1", stored.getPasswordHash()));
    }

    @Test
    void rejectsAnExistingEmailRegardlessOfCase() {
        userService.register(new RegisterRequestDTO("Ana", "ana@example.com", "password1"));

        assertThrows(EmailAlreadyRegisteredException.class,
                () -> userService.register(new RegisterRequestDTO("Other", " ANA@EXAMPLE.COM ", "password2")));
        assertEquals(1, userRepository.count());
    }

    @Test
    void validatesTheRequestBeforePersisting() {
        assertThrows(IllegalArgumentException.class, () -> userService.register(null));
        List<RegisterRequestDTO> invalidRequests = List.of(
                new RegisterRequestDTO(" ", "ana@example.com", "password1"),
                new RegisterRequestDTO("Ana", null, "password1"),
                new RegisterRequestDTO("Ana", "invalid", "password1"),
                new RegisterRequestDTO("Ana", "ana@example.com", null),
                new RegisterRequestDTO("Ana", "ana@example.com", "short"),
                new RegisterRequestDTO("Ana", "ana@example.com", "é".repeat(37))
        );
        for (var request : invalidRequests) {
            assertThrows(ConstraintViolationException.class, () -> userService.register(request));
        }
        assertEquals(0, userRepository.count());
    }

    @Test
    void acceptsExactly72Utf8Bytes() {
        String password = "é".repeat(36);
        userService.register(new RegisterRequestDTO("Ana", "ana@example.com", password));
        var stored = userRepository.findByEmailIgnoreCase("ana@example.com").orElseThrow();

        assertTrue(passwordEncoder.matches(password, stored.getPasswordHash()));
    }

    @Test
    void preventsDuplicateEmailsAtTheDatabaseLevel() {
        String hash = passwordEncoder.encode("password1");
        userRepository.saveAndFlush(new User("Ana", "ANA@example.com", hash));

        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(new User("Other", "ana@example.com", hash)));
        assertEquals(1, userRepository.count());
    }

    @Test
    void concurrentRegistrationsCreateOnlyOneUser() throws Exception {
        List<Callable<Boolean>> registrations = List.of(
                () -> registerIfAvailable("race@example.com"),
                () -> registerIfAvailable(" RACE@EXAMPLE.COM ")
        );
        try (var executor = Executors.newFixedThreadPool(2)) {
            int successfulRegistrations = 0;
            for (var result : executor.invokeAll(registrations)) {
                if (result.get()) {
                    successfulRegistrations++;
                }
            }
            assertEquals(1, successfulRegistrations);
        }
        assertEquals(1, userRepository.count());
    }

    @Test
    void translatesAnEmailConflictThatOccursDuringFlush() {
        var repository = mock(UserRepository.class);
        var databaseCause = new org.hibernate.exception.ConstraintViolationException(
                "Duplicate email", new SQLException("Duplicate email", "23505"), "uk_app_user_email");
        var failure = new DataIntegrityViolationException("Insert failed", databaseCause);
        when(repository.saveAndFlush(any(User.class))).thenThrow(failure);
        var service = new UserService(repository, passwordEncoder, validator);

        var exception = assertThrows(EmailAlreadyRegisteredException.class,
                () -> service.register(new RegisterRequestDTO("Ana", "ana@example.com", "password1")));
        assertSame(failure, exception.getCause());
    }

    @Test
    void preservesOtherPersistenceFailures() {
        var repository = mock(UserRepository.class);
        var failure = new DataIntegrityViolationException("Another constraint failed");
        when(repository.saveAndFlush(any(User.class))).thenThrow(failure);
        var service = new UserService(repository, passwordEncoder, validator);

        var exception = assertThrows(DataIntegrityViolationException.class,
                () -> service.register(new RegisterRequestDTO("Ana", "ana@example.com", "password1")));
        assertSame(failure, exception);
    }

    private boolean registerIfAvailable(String email) {
        try {
            userService.register(new RegisterRequestDTO("Ana", email, "password1"));
            return true;
        } catch (EmailAlreadyRegisteredException exception) {
            return false;
        }
    }
}
