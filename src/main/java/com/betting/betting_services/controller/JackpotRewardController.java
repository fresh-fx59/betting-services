package com.betting.betting_services.controller;

import com.betting.betting_services.model.ApiResponse;
import com.betting.betting_services.model.JackpotRewardDto;
import com.betting.betting_services.service.JackpotRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jackpot-rewards")
@RequiredArgsConstructor
@Slf4j
public class JackpotRewardController {

    private final JackpotRewardService jackpotRewardService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JackpotRewardDto>> getJackpotRewardById(@PathVariable Long id) {
        log.info("Received request to get jackpot reward with ID: {}", id);

        return jackpotRewardService.findById(id)
                .map(reward -> ResponseEntity.ok(ApiResponse.success(reward, "Jackpot reward found")))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
