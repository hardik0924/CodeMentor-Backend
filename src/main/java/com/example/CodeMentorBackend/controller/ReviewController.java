package com.example.CodeMentorBackend.controller;
import com.example.CodeMentorBackend.model.ReviewRequest;
import com.example.CodeMentorBackend.model.ReviewResponse;
import com.example.CodeMentorBackend.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*") // Allow all origins for development
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/generate-review")
    public ReviewResponse generateReview(@RequestBody ReviewRequest request) {
        String review = geminiService.reviewCode(request.getCode());
        return new ReviewResponse(review);
    }
}