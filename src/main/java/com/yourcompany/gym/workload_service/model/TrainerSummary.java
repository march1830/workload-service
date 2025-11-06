package com.yourcompany.gym.workload_service.model;

import lombok.Data;

import java.util.List;

@Data
public class TrainerSummary {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastname;
    private Boolean trainerStatus;
    private List<YearSummary> yearSummaries;
}
