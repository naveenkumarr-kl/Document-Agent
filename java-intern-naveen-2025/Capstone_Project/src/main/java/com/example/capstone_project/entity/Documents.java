package com.example.capstone_project.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
public class Documents {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String id;
    @NotBlank
    private String name;
    @Lob
    /*
    VARCHAR is designed for storing the short to medium length text (Max Size : 65 KB/row )
    @Lob is used to store Large Data Files like : Images, Audio & Video files.

    Lob can map the data in MySQl as CLOB (Character Large Obj)and BLOB(Binary Large Obj)
    It's max size String + @Lob in ~4 GB
     */
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;
    @NotNull
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")

    private LocalDateTime uploaded_at;
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum Status
    {
        UPLOADED,
        APPROVED,
        REJECTED
    }
}
