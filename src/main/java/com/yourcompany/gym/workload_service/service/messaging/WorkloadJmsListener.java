package com.yourcompany.gym.workload_service.service.messaging;

import com.yourcompany.gym.workload_service.dto.TrainerWorkloadRequest;
import com.yourcompany.gym.workload_service.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadJmsListener {

    private final TrainerWorkloadService trainerWorkloadService;

    @JmsListener(destination = "workload.topic", containerFactory = "myFactory")

    public void receiveWorkload(@Payload TrainerWorkloadRequest request,
                                @Headers Map<String, Object> headers) {

        String transactionId = (String) headers.get("transactionId");

        if (transactionId != null) {
            MDC.put("transactionId", transactionId);
        }

        log.info("Receive message ActiveMQ for trainer: {}", request.getTrainerUsername());

        // Твой код для теста DLQ
        // if ("dlq.test".equals(request.getTrainerUsername())) {
        //     throw new RuntimeException("Test DLQ: test error for dlq_test");
        // }

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
