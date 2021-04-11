package com.gdamiens.website.service;

import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.security.JwtTokenProvider;
import com.gdamiens.website.security.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final JwtTokenProvider jwtTokenProvider;

    public UserService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String signIn(String username, String password) {
        try {
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            List<Role> roleList = new ArrayList<>();
            roleList.add(Role.ROLE_ADMIN);

            return jwtTokenProvider.createToken(username, roleList);
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public String refresh(String username) {
        List<Role> roleList = new ArrayList<>();
        roleList.add(Role.ROLE_ADMIN);
        return jwtTokenProvider.createToken(username, roleList);
    }

}