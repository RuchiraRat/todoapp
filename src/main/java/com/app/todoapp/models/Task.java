package com.app.todoapp.models;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Task Entity
 * Represents a task in the To-Do list application.
 * This class is mapped to a database table using JPA annotations.
 */
@Entity
@Data
public class Task {
    /**
     * Unique identifier for the task.
     * Generated automatically by the database (Auto Increment).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title or description of the task.
     */
    private String title;

    /**
     * Status of the task (true if completed, false otherwise).
     */
    private boolean completed;

    /**
     * Date when the task was created.
     */
    private LocalDate createdAt;

    /**
     * Time period for the task (day, week, month).
     */
    private String period;

    /**
     * Deadline for completing the task based on period.
     */
    private LocalDate deadline;

    /**
     * Specific time to complete the task (e.g., 12:15 PM).
     */
    private LocalTime dueTime;

    /**
     * Whether a reminder has been shown for this task (for 10-min before
     * notification).
     */
    private boolean reminderShown;
}
