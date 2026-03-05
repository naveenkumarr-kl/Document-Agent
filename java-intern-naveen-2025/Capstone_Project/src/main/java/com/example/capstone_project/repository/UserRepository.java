package com.example.capstone_project.repository;


import com.example.capstone_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    User findByEmail(String email);
    List<User> findByRole(User.Role role);
    User findByUid(String uid);

}
