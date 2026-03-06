package org.datpham.foodlink.controller;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.service.GenaiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-genai")
@RequiredArgsConstructor
public class TestGenaiController {
    private final GenaiService genaiService;

    @GetMapping
    public ResponseEntity<BaseResponse<String>> testGenai(
            @RequestParam(defaultValue = "How does AI work?") String prompt
    ) {
        return ResponseEntity.ok(
                new BaseResponse<>(genaiService.testGenai(prompt), "Success", 200)
        );
    }

}
