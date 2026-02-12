package com.app.todoapp.services;

import com.app.todoapp.models.Task;
import com.app.todoapp.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Task Service
 * Contains the business logic for managing tasks.
 * Acts as an intermediary between the Controller and the Repository.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Constructor Injection for TaskRepository.
     * @param taskRepository The repository to handle database operations.
     */
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Retrieves all tasks from the database.
     * @return A list of all tasks.
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Creates a new task with the given title.
     * The task is initially marked as not completed.
     * @param title The title of the new task.
     */
    public void createTask(String title) {
        Task task = new Task();
        task.setTitle(title);
        task.setCompleted(false);
        taskRepository.save(task);
    }

    /**
     * Deletes a task by its ID.
     * @param id The ID of the task to delete.
     */
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    /**
     * Toggles the completion status of a task.
     * If completed, it becomes incomplete, and vice versa.
     * @param id The ID of the task to toggle.
     * @throws IllegalArgumentException if the task is not found.
     */
    public void toggleTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task id"));
        task.setCompleted(!task.isCompleted());
        taskRepository.save(task);
    }

    /**
     * Retrieves a single task by its ID.
     * @param id The ID of the task to retrieve.
     * @return The found Task object.
     * @throws IllegalArgumentException if the task is not found.
     */
    public Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task id"));
    }

    /**
     * Updates the title of an existing task.
     * @param id The ID of the task to update.
     * @param title The new title for the task.
     */
    public void updateTask(Long id, String title) {
        Task task = getTask(id);
        task.setTitle(title);
        taskRepository.save(task);
    }
}
