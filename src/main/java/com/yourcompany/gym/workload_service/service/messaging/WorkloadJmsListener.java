package com.yourcompany.gym.workload_service.service.messaging;

import com.yourcompany.gym.workload_service.dto.TrainerWorkloadRequest;
import com.yourcompany.gym.workload_service.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadJmsListener {

    private final TrainerWorkloadService trainerWorkloadService;

    @JmsListener(destination = "workload.topic", containerFactory = "myFactory")
    public void receiveWorkload(TrainerWorkloadRequest request) {
        MDC.put("transactionId", request.getTransactionId());

        log.info("Receive message ActiveMQ for trainer: {}", request.getTrainerUsername());

        if (request.getTrainerUsername() != null && request.getTrainerUsername().startsWith("dlq.test")) {
            log.info("Test DLQ trigger activated for: {}", request.getTrainerUsername());
            throw new RuntimeException("Test DLQ: test error for " + request.getTrainerUsername());
        }

        try {
            trainerWorkloadService.processWorkload(request);
            log.info("Workload update successfully");
        } catch (Exception e) {
            log.error("Error processing the message", e);
            throw new RuntimeException(e);
        } finally {
            MDC.clear();

        }
    }

}
