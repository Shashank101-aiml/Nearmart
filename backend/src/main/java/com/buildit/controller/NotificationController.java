package com.buildit.controller;

import com.buildit.dto.response.NotificationResponse;
import com.buildit.security.CustomUserDetails;
import com.buildit.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> listMine(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(notificationService.listForCustomer(principal.getUser().getId()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@AuthenticationPrincipal CustomUserDetails principal,
                                                            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(principal.getUser().getId(), id));
    }
}
