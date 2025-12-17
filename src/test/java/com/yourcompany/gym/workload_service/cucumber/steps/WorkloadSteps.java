package com.yourcompany.gym.workload_service.cucumber.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.jms.Message;
import com.yourcompany.gym.workload_service.dto.TrainerWorkloadRequest;
import com.yourcompany.gym.workload_service.repository.TrainerWorkloadRepository;
import com.yourcompany.gym.workload_service.model.TrainerSummary;
import com.yourcompany.gym.workload_service.model.YearSummary;
import com.yourcompany.gym.workload_service.model.MonthSummary;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;
import java.util.Optional;

public class WorkloadSteps {

    @Autowired private JmsTemplate jmsTemplate;

    @Autowired private TrainerWorkloadRepository repository;

    @Given("Trainer {string} has no recorded hours")
    public void clearTrainerData(String username) {
        Optional<TrainerSummary> summary = repository.findByTrainerUsername(username);
        summary.ifPresent(s -> repository.delete(s));

    }

    @When("Message with training duration {int} minutes for {string} arrives")
    public void sendMessage(int duration, String username) throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName("Test");
        request.setTrainerLastName("Trainer");
        request.setIsActive(true);
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDuration((long) duration);
        request.setActionType("ADD");

        jmsTemplate.convertAndSend("workload.topic", request);

        Thread.sleep(2000);
    }

    @Then("Trainer {string} should have {int} minutes in total workload")
    public void verifyWorkload(String username, int expectedDuration) {
         Optional<TrainerSummary> summaryOpt = repository.findByTrainerUsername(username);

         Assertions.assertTrue(summaryOpt.isPresent(), "The trainer's record was not found in MongoDB");
        TrainerSummary summary = summaryOpt.get();

        int actualDuration = 0;

        if (summary.getYearSummaries() != null) {
            for (YearSummary year : summary.getYearSummaries()) {
                if (year.getMonthSummaries() != null) {
                    for (MonthSummary month : year.getMonthSummaries()) {
                        actualDuration += month.getHours();
                    }
                }
            }
        }

        Assertions.assertEquals(expectedDuration, actualDuration, "The training time does not match");
    }
    @Then("The message should be moved to the {string} queue")
    public void verifyMessageInDlq(String queueName) {
        jmsTemplate.setReceiveTimeout(15000);
        Message message = jmsTemplate.receive(queueName);
        Assertions.assertNotNull(message, "The message should have been sent to DLQ, but the queue is empty! Check if an exception is thrown in the Listener.");
    }
}