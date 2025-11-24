package com.yourcompany.gym.workload_service.service.messaging;

import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DlqListener {
    @JmsListener(destination = "ActiveMQ.DLQ")
    public void receiveDlqMessage(Message message) {
        try {
            String body = message.getBody(String.class);
            log.error("Received message in DLQ: {}", body);

        } catch (Exception e) {
            log.error("Error processing DLQ message", e);
        }
    }
}
