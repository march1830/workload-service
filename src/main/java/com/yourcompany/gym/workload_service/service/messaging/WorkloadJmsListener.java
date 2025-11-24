package com.yourcompany.gym.workload_service.service.messaging;

import com.yourcompany.gym.workload_service.dto.TrainerWorkloadRequest;
import com.yourcompany.gym.workload_service.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadJmsListener {

    private final TrainerWorkloadService trainerWorkloadService;

    @JmsListener(destination = "workload.topic", containerFactory = "myFactory")
    public void receiveWorkload(TrainerWorkloadRequest request) {
        log.info("Receive message ActiveMQ for trainer: ", request.getTrainerUsername());

        if ("dlq.test".equals(request.getTrainerUsername())) {
            throw new RuntimeException("Test DLQ: test error for dlq_test");
        }

        try {
            trainerWorkloadService.processWorkload(request);
            log.info("Workload update successfully");
        } catch (Exception e) {
            log.error("Error processing the message", e);
            throw e;

        }
    }
}
