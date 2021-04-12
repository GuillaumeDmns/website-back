package com.gdamiens.website.service;

import com.gdamiens.website.security.JwtTokenProvider;
import com.gdamiens.website.security.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Value("${security.jwt.token.auth-user}")
    private String authUser;

    @Value("${security.jwt.token.auth-password}")
    private String authPassword;

    private final JwtTokenProvider jwtTokenProvider;

    public UserService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String signIn(String username, String password) {
        if (!authUser.equals(username) || !authPassword.equals(password)) {
            return null;
        }

//        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        List<Role> roleList = new ArrayList<>();
        roleList.add(Role.ROLE_ADMIN);

        return jwtTokenProvider.createToken(username, roleList);
    }

    public String refresh(String username) {
        List<Role> roleList = new ArrayList<>();
        roleList.add(Role.ROLE_ADMIN);
        return jwtTokenProvider.createToken(username, roleList);
    }

}