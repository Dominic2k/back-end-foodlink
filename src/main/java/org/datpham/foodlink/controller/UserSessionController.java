package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.request.SessionRequest;
import org.datpham.foodlink.entity.UserSession;
import org.datpham.foodlink.repository.UserSessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.datpham.foodlink.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class UserSessionController {

    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<BaseResponse<String>> recordSession(@Valid @RequestBody SessionRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        userRepository.findByEmail(email).ifPresent(user -> {
            UserSession session = new UserSession();
            session.setUserId(user.getId());
            session.setStartedAt(LocalDateTime.now().minusSeconds(request.getDurationSeconds()));
            session.setEndedAt(LocalDateTime.now());
            session.setDurationSeconds(request.getDurationSeconds());
            userSessionRepository.save(session);
            log.info("Session recorded for user {}: {} seconds", email, request.getDurationSeconds());
        });

        return ResponseEntity.ok(new BaseResponse<>("Session recorded", "OK", 200));
    }
}
