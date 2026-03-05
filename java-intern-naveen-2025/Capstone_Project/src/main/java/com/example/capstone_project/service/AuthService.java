package com.example.capstone_project.service;

import com.example.capstone_project.repository.UserRepository;
import com.example.capstone_project.util.JwtUtil;
import com.example.capstone_project.dto.LoginRequest;
import com.example.capstone_project.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.Base64;
@Service
@Slf4j
public class AuthService {

    Logger logger= LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    public AuthService (UserRepository userRepository, JwtUtil jwtUtil)
    {
        this.userRepository = userRepository;
        this.jwtUtil=jwtUtil;
    }

    public HttpStatus registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return HttpStatus.ALREADY_REPORTED;
        }

        String encodePassword = Base64.getEncoder().encodeToString(user.getPassword().getBytes());
        user.setPassword(encodePassword);

        userRepository.save(user);

        return HttpStatus.CREATED;
    }

    public String userLogin(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null) {
            logger.error("...........USER NOT FOUND !!!......");
            return "USER NOT FOUND !!!";
        }

        String enteredPassword = Base64.getEncoder().encodeToString(loginRequest.getPassword().getBytes());

        if(user.getPassword().equals(enteredPassword)) {

            return jwtUtil.generateToken(user);


        }

        logger.warn("..........INVALID CREDENTIALS !!...........");
        return "INVALID CREDENTIALS !!";

    }
}