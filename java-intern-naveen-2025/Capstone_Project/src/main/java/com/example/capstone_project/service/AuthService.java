package com.example.capstone_project.service;

import com.example.capstone_project.repository.UserRepository;
import com.example.capstone_project.util.JwtUtil;
import com.example.capstone_project.dto.LoginRequest;
import com.example.capstone_project.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

    Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.jwtUtil         = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public HttpStatus registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return HttpStatus.ALREADY_REPORTED;
        }

        // Store a BCrypt hash – never store plain-text or reversible encodings
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return HttpStatus.CREATED;
    }

    public String userLogin(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null) {
            logger.error("...........USER NOT FOUND !!!......");
            return "USER NOT FOUND !!!";
        }

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtUtil.generateToken(user);
        }

        logger.warn("..........INVALID CREDENTIALS !!...........");
        return "INVALID CREDENTIALS !!";
    }
}