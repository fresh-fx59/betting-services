package com.betting.betting_services.controller;

import com.betting.betting_services.model.ApiResponse;
import com.betting.betting_services.model.BetDto;
import com.betting.betting_services.service.BetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bets")
@RequiredArgsConstructor
@Slf4j
public class BetController {

    private final BetService betService;

    @PostMapping
    public ResponseEntity<ApiResponse<BetDto>> placeBet(@Valid @RequestBody BetDto betDto) {
        log.info("Received bet request: {}", betDto);

        BetDto response = betService.placeBet(betDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Bet placed successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Betting service is up and running");
    }
}
