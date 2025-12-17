package com.yourcompany.gym.workload_service.cucumber.steps;

import com.yourcompany.gym.workload_service.model.MonthSummary;
import com.yourcompany.gym.workload_service.model.TrainerSummary;
import com.yourcompany.gym.workload_service.model.YearSummary;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.awaitility.Awaitility.await;

public class IntegrationSteps {

    @Autowired
    private TestRestTemplate internalRestTemplate;

    private final RestTemplate externalRestTemplate = new RestTemplate();
    private final String CRM_BASE_URL = "http://localhost:8080/api";

    private static String validTraineeToken;
    private static String registeredTraineeUsername;
    private static final Map<String, String> realTrainerUsernames = new HashMap<>();

    @When("User call Gym CRM to add a training with duration {int} minutes for trainer {string}")
    public void callGymCrm(int duration, String featureFileTrainerName) {

        if (validTraineeToken == null) {
            prepareTraineeUser();
        }

        String realTrainerUsername;
        if (realTrainerUsernames.containsKey(featureFileTrainerName)) {
            realTrainerUsername = realTrainerUsernames.get(featureFileTrainerName);
        } else {

            String[] parts = featureFileTrainerName.split("\\.");
            String firstName = parts.length > 0 ? parts[0] : "Test";
            String lastName = parts.length > 1 ? parts[1] : "Trainer";

            if(featureFileTrainerName.equals("dlq.test")) {
                firstName = "Dlq";
                lastName = "Test";
            }

            realTrainerUsername = registerTrainer(firstName, lastName);
            realTrainerUsernames.put(featureFileTrainerName, realTrainerUsername);
            System.out.println("Mapping created: " + featureFileTrainerName + " -> " + realTrainerUsername);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("traineeUsername", registeredTraineeUsername);
        body.put("trainerUsername", realTrainerUsername);
        body.put("trainingName", "E2E Integration Test");
        body.put("trainingDate", "2025-12-25");
        body.put("trainingDuration", duration);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(validTraineeToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            externalRestTemplate.postForEntity(CRM_BASE_URL + "/trainings", request, String.class);
        } catch (HttpClientErrorException e) {
            System.err.println("CRM Request Failed: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("CRM Connection Error: " + e.getMessage());
        }
    }

    @Then("User call Workload API for {string} and expect {int} minutes duration")
    public void verifyWorkloadViaApi(String featureFileTrainerName, int expectedDuration) {
        String realTrainerUsername = realTrainerUsernames.get(featureFileTrainerName);

        Assertions.assertNotNull(realTrainerUsername, "Trainer was not registered in previous steps!");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validTraineeToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    ResponseEntity<TrainerSummary> response = internalRestTemplate.exchange(
                            "/api/v1/workload/" + realTrainerUsername,
                            HttpMethod.GET,
                            entity,
                            TrainerSummary.class
                    );

                    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
                    TrainerSummary summary = response.getBody();
                    Assertions.assertNotNull(summary);

                    int totalDuration = 0;
                    if (summary.getYearSummaries() != null) {
                        for (YearSummary year : summary.getYearSummaries()) {
                            if (year.getMonthSummaries() != null) {
                                for (MonthSummary month : year.getMonthSummaries()) {
                                    totalDuration += month.getHours();
                                }
                            }
                        }
                    }
                    Assertions.assertEquals(expectedDuration, totalDuration);
                });
    }

    private void prepareTraineeUser() {
        Map<String, String> regBody = new HashMap<>();
        regBody.put("firstName", "EndToEnd");
        regBody.put("lastName", "Trainee");
        regBody.put("dateOfBirth", "1990-01-01");
        regBody.put("address", "Test Address");

        try {
            ResponseEntity<Map> regResponse = externalRestTemplate.postForEntity(
                    CRM_BASE_URL + "/register/trainee", regBody, Map.class);

            registeredTraineeUsername = (String) regResponse.getBody().get("username");
            String password = (String) regResponse.getBody().get("password");

            Map<String, String> loginBody = new HashMap<>();
            loginBody.put("username", registeredTraineeUsername);
            loginBody.put("password", password);

            ResponseEntity<Map> loginResponse = externalRestTemplate.postForEntity(
                    CRM_BASE_URL + "/auth/login", loginBody, Map.class);

            validTraineeToken = (String) loginResponse.getBody().get("token");

        } catch (Exception e) {
            System.out.println("Could not register trainee (maybe exists). Error: " + e.getMessage());
        }
    }

    private String registerTrainer(String firstName, String lastName) {
        Map<String, Object> regBody = new HashMap<>();
        regBody.put("firstName", firstName);
        regBody.put("lastName", lastName);
        regBody.put("specializationId", 2);

        try {
            ResponseEntity<Map> response = externalRestTemplate.postForEntity(
                    CRM_BASE_URL + "/register/trainer", regBody, Map.class);
            return (String) response.getBody().get("username");
        } catch (Exception e) {
            System.out.println("Trainer registration error: " + e.getMessage());
            return firstName.toLowerCase() + "." + lastName.toLowerCase();
        }
    }
}