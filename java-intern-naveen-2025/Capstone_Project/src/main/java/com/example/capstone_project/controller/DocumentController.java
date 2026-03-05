package com.example.capstone_project.controller;


import com.example.capstone_project.entity.Documents;
import com.example.capstone_project.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/capstone/v1/user/document")
public class DocumentController {

    private final DocumentService documentService;
    public DocumentController(DocumentService documentService)
    {
        this.documentService=documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {

        return ResponseEntity.ok(documentService.upload(file));
    }

    @GetMapping("/viewAll")
    public Page<Documents> viewAll(@RequestParam int PageSize)
    {
        return documentService.viewAll(PageSize);
    }

    @GetMapping("/viewById")
    public ResponseEntity<Documents> viewById(@RequestParam String id)
    {
        return ResponseEntity.ok(documentService.viewById(id));
    }
    @PatchMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(@RequestParam String id,
                                               @RequestParam Documents.Status status)
    {
        return ResponseEntity.ok(documentService.updateStatus(id,status));
    }

    @DeleteMapping("/removeById")
    public ResponseEntity<String> removeDocument(@RequestParam String id)
    {
        return ResponseEntity.ok(documentService.removeById(id));
    }
}
