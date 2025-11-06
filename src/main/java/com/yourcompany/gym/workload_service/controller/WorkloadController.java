package com.yourcompany.gym.workload_service.controller;

import com.yourcompany.gym.workload_service.dto.TrainerWorkloadRequest;
import com.yourcompany.gym.workload_service.model.TrainerSummary;
import com.yourcompany.gym.workload_service.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workload")
public class WorkloadController {
    private final TrainerWorkloadService trainerWorkloadService;

    @PostMapping
    public ResponseEntity<Void> handleWorkload(@RequestBody TrainerWorkloadRequest request) {
        log.info("Handling workload request for trainer: {}", request.getTrainerUsername());
        trainerWorkloadService.processWorkload(request);
        log.info("Workload request processed successfully (200 OK)");
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{username}")
    public ResponseEntity<TrainerSummary> getWorkload(@PathVariable String username) {
        TrainerSummary summary = trainerWorkloadService.getTrainerSummary(username);
        return ResponseEntity.ok(summary);
    }

}
