Feature: Workload Processing

  Scenario: Trainer workload increases when training added message is received
    Given Trainer "Lenya.Sidorov" has no recorded hours
    When Message with training duration 100 minutes for "Lenya.Sidorov" arrives
    Then Trainer "Lenya.Sidorov" should have 100 minutes in total workload

  Scenario: Invalid message should be moved to DLQ
    When Message with training duration 100 minutes for "dlq.test" arrives
    Then The message should be moved to the "ActiveMQ.DLQ" queue