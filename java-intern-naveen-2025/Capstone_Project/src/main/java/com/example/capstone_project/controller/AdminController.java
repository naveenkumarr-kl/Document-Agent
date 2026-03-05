package com.example.capstone_project.controller;


import com.example.capstone_project.entity.User;
import com.example.capstone_project.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/capstone/v1/admin")
public class AdminController {

    private final AdminService adminService;
    public AdminController(AdminService adminService)
    {
        this.adminService = adminService;
    }

    @GetMapping("/viewAll")
    public List<User> viewAll()
    {
        return adminService.viewAll();
    }

    @DeleteMapping("/deleteByEmail")
    public ResponseEntity<String> deleteByEmail(@RequestParam String email)
    {
        return ResponseEntity.ok(adminService.deleteByEmail(email));
    }

    @GetMapping("/sortByRole")
    public ResponseEntity<List<User>> sortByRole(@RequestParam String role)
    {
        return ResponseEntity.ok(adminService.sortByRole(role));
    }
}
