package com.smartstudy.user.service;

import com.smartstudy.common.security.JwtUtils;
import com.smartstudy.user.dto.AuthRequest;
import com.smartstudy.user.dto.AuthResponse;
import com.smartstudy.user.entity.UserAccount;
import com.smartstudy.user.repository.UserAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        if (userAccountRepository.existsByUsername(request.username())) {
            throw new IllegalStateException("Username already exists");
        }

        UserAccount savedUser;
        try {
            savedUser = userAccountRepository.save(
                    new UserAccount(request.username(), passwordEncoder.encode(request.password()))
            );
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("Username already exists", exception);
        }

        return new AuthResponse(JwtUtils.generateToken(savedUser.getUsername()), savedUser.getUsername());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        UserAccount user = userAccountRepository.findByUsername(request.username())
                .filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        return new AuthResponse(JwtUtils.generateToken(user.getUsername()), user.getUsername());
    }
}
