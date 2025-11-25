package com.yourcompany.gym.workload_service.service;

import com.yourcompany.gym.workload_service.dto.TrainerWorkloadRequest;
import com.yourcompany.gym.workload_service.model.MonthSummary;
import com.yourcompany.gym.workload_service.model.TrainerSummary;
import com.yourcompany.gym.workload_service.model.YearSummary;
import com.yourcompany.gym.workload_service.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {


    private final TrainerWorkloadRepository workloadRepository;

    public void processWorkload (TrainerWorkloadRequest request) {
        String username = request.getTrainerUsername();
        TrainerSummary trainerSummary = workloadRepository.findByTrainerUsername(username)
                .orElseGet(() -> {
            log.info("Creating new summary for trainer: {}", username);
            TrainerSummary newSummary = new TrainerSummary();
                    newSummary.setTrainerUsername(username);
                    newSummary.setTrainerFirstName(request.getTrainerFirstName());
                    newSummary.setTrainerLastName(request.getTrainerLastName());
                    newSummary.setTrainerStatus(request.getIsActive());
                    newSummary.setYearSummaries(new ArrayList<>());
                    return newSummary;
                });
        LocalDate trainingDate = request.getTrainingDate();
        int year = trainingDate.getYear();
        Month month = trainingDate.getMonth();

        List<YearSummary> yearList = trainerSummary.getYearSummaries();
        YearSummary yearSummary = null;
        for (YearSummary summaryInList : yearList) {
            if (summaryInList.getYear() == year) {
                yearSummary = summaryInList;
                break;
            }
        }
        if (yearSummary == null) {
            log.info("YearSummary not found for {}. Creating new one.", year);
            yearSummary = new YearSummary();
            yearSummary.setYear(year);
            yearSummary.setMonthSummaries(new ArrayList<>());
            yearList.add(yearSummary);
        }
        List<MonthSummary> monthList = yearSummary.getMonthSummaries();
        MonthSummary monthSummary = null;
        for (MonthSummary summaryInList : monthList) {
            if (summaryInList.getMonth() == month) {
                monthSummary = summaryInList;
                break;
            }
        }
        if (monthSummary == null) {
            log.info("MonthSummary not found for {}{}. Creating new one", month, year);
            monthSummary = new MonthSummary();
            monthSummary.setMonth(month);
            monthSummary.setHours(0L);
            monthList.add(monthSummary);
        }
        Long currentHours = monthSummary.getHours();
        String actionType = request.getActionType();
        Long trainingDuration = request.getTrainingDuration();
        log.info("Updating hours for {}. Month: {}. Action: {}. Duration: {}. Current hours (before): {}", username, month, actionType, trainingDuration, currentHours);

        if (actionType.equals("ADD")) {
            Long newHours = currentHours + trainingDuration;
            monthSummary.setHours(newHours);
            }
                else if (actionType.equals("DELETE")) {
                    Long newHours = currentHours - trainingDuration;
                    monthSummary.setHours(newHours);
                }
        log.info("New total hours for {}{}{}:", year, month, monthSummary.getHours());

        workloadRepository.save(trainerSummary);
    }
    public TrainerSummary getTrainerSummary(String username) {
        return workloadRepository.findByTrainerUsername(username)
                .orElse(null);
    }
}