package com.example.capstone_project.service;

import com.example.capstone_project.entity.User;
import com.example.capstone_project.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    public List<User> viewAll()
    {
        return userRepository.findAll();
    }

    public String deleteByEmail(String email)
    {
        User user = userRepository.findByEmail(email);
        if(user != null ) {
            userRepository.delete(user);
        }
        return "USER DELETED SUCCESSFULLY !! ";
    }

    public List<User> sortByRole(String role)
    {
        List<User> finalResult = userRepository.findByRole(User.Role.valueOf(role));
        return finalResult;
    }

}
