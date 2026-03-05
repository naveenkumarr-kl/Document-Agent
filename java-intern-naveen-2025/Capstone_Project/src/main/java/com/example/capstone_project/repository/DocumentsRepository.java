package com.example.capstone_project.repository;


import com.example.capstone_project.entity.Documents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentsRepository extends JpaRepository<Documents,String> {

    Documents findByName(String fileName);
    void deleteById(String id);
}
