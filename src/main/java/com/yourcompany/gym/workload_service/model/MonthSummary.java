package com.yourcompany.gym.workload_service.model;

import lombok.Data;

import java.time.Month;

@Data
public class MonthSummary {
    private Month month;
    private Long hours;
}
