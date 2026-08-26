package com.gdamiens.website.service;

import com.gdamiens.website.model.User;
import com.gdamiens.website.repository.UserRepository;
import com.gdamiens.website.security.JwtTokenProvider;
import com.gdamiens.website.security.Role;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public String signIn(String username, String password) {
        return Optional.ofNullable(userRepository.getByLogin(username))
            .filter(user -> passwordEncoder.matches(password, user.getPassword()))
            .map(user -> jwtTokenProvider.createToken(username, List.of(Role.ROLE_ADMIN)))
            .orElse(null);
    }

    public String refresh(String username) {
        Optional<User> user = userRepository.getByLogin(username);
        
        if (user.isPresent()) {
            return jwtTokenProvider.createToken(
                username,
                List.of(Role.ROLE_ADMIN));
        }
        
        return null;
    }
}
