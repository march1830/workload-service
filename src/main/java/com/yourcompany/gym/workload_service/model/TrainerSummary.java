package com.yourcompany.gym.workload_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "trainer_summaries")


public class TrainerSummary {
    @Id
    private String id;

    @Indexed(unique = true)
    private String trainerUsername;

    @Indexed
    private String trainerFirstName;

    @Indexed
    private String trainerLastName;

    private Boolean trainerStatus;

    private List<YearSummary> yearSummaries = new ArrayList<>();
}
