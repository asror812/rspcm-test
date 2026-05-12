package org.example.rspcm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-delete")
public class TestDeleteController {

    @GetMapping
    public ResponseEntity<String> testDelete() {
        return ResponseEntity.ok("success");
    }

    @GetMapping("/test")
    public ResponseEntity<String> testDelete2() {
        return ResponseEntity.ok("success");
    }
}
