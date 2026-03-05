package com.example.capstone_project.controller;
import com.example.capstone_project.dto.LoginRequest;
import com.example.capstone_project.entity.User;
import com.example.capstone_project.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/capstone/v1/auth")
public class AuthController {

    private AuthService authService;
    public AuthController(AuthService authService)
    {
        this.authService=authService;
    }

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> userRegister(@RequestBody User user)
    {
        return new ResponseEntity(authService.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> userLogin(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.userLogin(loginRequest));
    }

}
