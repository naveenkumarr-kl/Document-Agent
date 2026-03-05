package com.example.capstone_project.service;


import com.example.capstone_project.entity.Documents;
import com.example.capstone_project.entity.User;
import com.example.capstone_project.repository.DocumentsRepository;
import com.example.capstone_project.util.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DocumentService {


    private final DocumentsRepository documentsRepository;
    private final JwtUtil jwtUtil;
    private final User user;

    public DocumentService(DocumentsRepository documentsRepository, JwtUtil jwtUtil, User user) {
        this.documentsRepository = documentsRepository;
        this.jwtUtil = jwtUtil;
        this.user = user;
    }

    public String upload(MultipartFile file) throws IOException {
        if (file.isEmpty())
            return "FILE IS EMPTY !!";
        if (documentsRepository.findByName(file.getOriginalFilename()) != null)
            return "FILE NAME ALREADY EXIST !!";

        Documents d = new Documents();
        d.setName(file.getOriginalFilename());  // Setting File Name
        d.setContent(new String(file.getBytes())); // Setting File Content
        d.setUploaded_at(LocalDateTime.now());
        d.setStatus(Documents.Status.UPLOADED);
        d.setUser(user);// Status : Uploaded

        documentsRepository.save(d);

        return "FILE UPLOADED SUCCESSFULLY " + file.getOriginalFilename();
    }


    public Page<Documents> viewAll(int page) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), 10, Sort.by("uploaded_at").descending());
        return documentsRepository.findAll(pageRequest);
    }
/*
    public List<Documents> viewAll() {
        return documentsRepository.findAll();
    }
*/
    public Documents viewById(String id) {
        Optional<Documents> optionalDocuments = documentsRepository.findById(id);
        if (optionalDocuments.isPresent()) {
            return optionalDocuments.get();
        }
        return optionalDocuments.get();

    }

    public String updateStatus(String id, Documents.Status status) {

        Optional<Documents> optionalDocuments = documentsRepository.findById(id);

        if (optionalDocuments.isPresent()) {
            optionalDocuments.get().setStatus(status);
        }

        return "STATUS UPDATED SUCCESSFULLY !!";
    }

    public String removeById(String id) {
        if (documentsRepository.findById(id) != null) {
            // Use existsById to check presence instead of comparing Optional to null
            if (documentsRepository.existsById(id)) {
                documentsRepository.deleteById(id);
                return "FILE REMOVED SUCCESSFULLY !!";
            }
        }
            return "FILE DOESN'T EXIST";

    }
}
