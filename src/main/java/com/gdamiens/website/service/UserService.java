package com.gdamiens.website.service;

import com.gdamiens.website.model.User;
import com.gdamiens.website.repository.UserRepository;
import com.gdamiens.website.security.JwtTokenProvider;
import com.gdamiens.website.security.Role;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;

    public UserService(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public String signIn(String username, String password) {
        Optional<User> optionalUser = userRepository.getByLogin(username);
        if (optionalUser.isPresent() && optionalUser.get().getPassword().trim().equals(password)) {
            List<Role> roleList = new ArrayList<>();
            roleList.add(Role.ROLE_ADMIN);

            return jwtTokenProvider.createToken(username, roleList);
        }
        return null;
    }

    public String refresh(String username) {
        List<Role> roleList = new ArrayList<>();
        roleList.add(Role.ROLE_ADMIN);

        return jwtTokenProvider.createToken(username, roleList);
    }



}
