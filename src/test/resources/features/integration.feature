Feature: System Integration (E2E)
  Check that Gym CRM and Workload Service communicate correctly via ActiveMQ.

  # positive scenario

  Scenario: End-to-End Training Processing
    When User call Gym CRM to add a training with duration 60 minutes for trainer "Integration.Trainer"
    Then User call Workload API for "Integration.Trainer" and expect 60 minutes duration

  # negative scenario (DLQ)

  Scenario: End-to-End Error Handling (DLQ)
    When User call Gym CRM to add a training with duration 100 minutes for trainer "dlq.test"
    Then The message should be moved to the "ActiveMQ.DLQ" queue