package com.yourcompany.gym.workload_service.service;

import com.yourcompany.gym.workload_service.dto.TrainerWorkloadRequest;
import com.yourcompany.gym.workload_service.model.TrainerSummary;
import com.yourcompany.gym.workload_service.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enable Mockito extension
class TrainerWorkloadServiceTest {

    @Mock // Create a mock repository
    private TrainerWorkloadRepository workloadRepository;

    @InjectMocks // Inject the mock repository into the real service instance
    private TrainerWorkloadService workloadService;

    private TrainerWorkloadRequest request;

    @BeforeEach
    void setUp() {
        // Prepare data before each test
        request = new TrainerWorkloadRequest();
        request.setTrainerUsername("test.trainer");
        request.setTrainerFirstName("Test");
        request.setTrainerLastName("Trainer");
        request.setIsActive(true);
        request.setTrainingDate(LocalDate.of(2025, 1, 15));
        request.setTrainingDuration(100L);
        request.setActionType("ADD");
    }

    @Test
    void processWorkload_ShouldCreateNewRecord_WhenTrainerNotFound() {
        // 1. Configure mock behavior
        // Specify: "If asked for trainer 'test.trainer', return empty (not found)"
        when(workloadRepository.findByTrainerUsername("test.trainer"))
                .thenReturn(Optional.empty());

        // 2. Call the method under test
        workloadService.processWorkload(request);

        // 3. Verification (Assert)
        // Verify that the save() method was called exactly once
        verify(workloadRepository, times(1)).save(any(TrainerSummary.class));
    }

    @Test
    void processWorkload_ShouldUpdateExistingRecord_WhenTrainerFound() {
        // 1. Prepare an existing record simulating the "database"
        TrainerSummary existingSummary = new TrainerSummary();
        existingSummary.setTrainerUsername("test.trainer");
        // (Year lists are initialized inside the class, so they are not null)

        // Specify: "If asked for the trainer, return this existing object"
        when(workloadRepository.findByTrainerUsername("test.trainer"))
                .thenReturn(Optional.of(existingSummary));

        // 2. Call the method under test
        workloadService.processWorkload(request);

        // 3. Verification
        verify(workloadRepository, times(1)).save(existingSummary);

        // Note: We could also verify that hours were actually calculated/added,
        // but for this task, verifying the save call is sufficient.
    }
}