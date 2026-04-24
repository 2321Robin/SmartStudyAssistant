package com.smartstudy.user.service;

import com.smartstudy.user.dto.AuthRequest;
import com.smartstudy.user.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerMapsUniqueConstraintFailureToDuplicateUsernameError() {
        given(userAccountRepository.existsByUsername("alice")).willReturn(false);
        given(userAccountRepository.save(any())).willThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> authService.register(new AuthRequest("alice", "password123")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Username already exists");
    }
}
