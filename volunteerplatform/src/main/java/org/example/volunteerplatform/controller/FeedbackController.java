package org.example.volunteerplatform.controller;

import jakarta.validation.Valid;
import org.example.volunteerplatform.dto.CreateFeedbackRequest;
import org.example.volunteerplatform.dto.FeedbackDto;
import org.example.volunteerplatform.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedbacks")
@CrossOrigin(origins = "http://localhost:3000")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/event/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeedbackDto> createFeedback(@PathVariable Long eventId, @Valid @RequestBody CreateFeedbackRequest request) {
        FeedbackDto feedback = feedbackService.createFeedback(eventId, request);
        return ResponseEntity.ok(feedback);
    }
}
