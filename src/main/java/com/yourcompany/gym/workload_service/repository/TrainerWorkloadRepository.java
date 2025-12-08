package com.yourcompany.gym.workload_service.repository;

import com.yourcompany.gym.workload_service.model.TrainerSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerWorkloadRepository extends MongoRepository<TrainerSummary, String> {
    Optional<TrainerSummary> findByTrainerUsername(String username);
    List<TrainerSummary> findByTrainerFirstNameAndTrainerLastName(String firstName, String lastName);
}
