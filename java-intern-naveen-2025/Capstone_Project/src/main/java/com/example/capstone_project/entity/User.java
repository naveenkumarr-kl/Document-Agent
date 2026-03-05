package com.example.capstone_project.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String uid;
    @NotBlank
    private String name;
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;
    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role
    {
        ADMIN,
        CREATOR,
        APPROVER
    };

}
