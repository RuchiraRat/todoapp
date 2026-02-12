package com.app.todoapp.models;

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
}
