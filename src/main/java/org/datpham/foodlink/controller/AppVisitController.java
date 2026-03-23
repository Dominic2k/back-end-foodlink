package org.datpham.foodlink.controller;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.entity.AppVisit;
import org.datpham.foodlink.repository.AppVisitRepository;
import org.datpham.foodlink.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class AppVisitController {

    private final AppVisitRepository appVisitRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<BaseResponse<String>> recordVisit() {
        AppVisit visit = new AppVisit();

        // Try to extract userId from authenticated user
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String email = auth.getName();
                userRepository.findByEmail(email)
                        .ifPresent(user -> visit.setUserId(user.getId()));
            }
        } catch (Exception ignored) {
        }

        appVisitRepository.save(visit);
        return ResponseEntity.ok(new BaseResponse<>("Visit recorded", "Success", 200));
    }
}
