package com.app.todoapp.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.todoapp.models.Task;
import com.app.todoapp.repository.TaskRepository;

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
     * 
     * @param taskRepository The repository to handle database operations.
     */
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Retrieves all tasks from the database.
     * 
     * @return A list of all tasks.
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Creates a new task with the given title, period, and due time.
     * The task is initially marked as not completed.
     * Deadline is calculated based on the period.
     * 
     * @param title   The title of the new task.
     * @param period  The time period (day, week, month).
     * @param dueTime The specific time to complete the task (e.g., "12:15").
     */
    public void createTask(String title, String period, String dueTime) {
        Task task = new Task();
        task.setTitle(title);
        task.setCompleted(false);
        task.setCreatedAt(LocalDate.now());
        task.setPeriod(period != null && !period.isEmpty() ? period.toLowerCase() : "day");
        task.setDeadline(calculateDeadline(LocalDate.now(), task.getPeriod()));

        // Parse and set due time
        if (dueTime != null && !dueTime.isEmpty()) {
            try {
                task.setDueTime(LocalTime.parse(dueTime));
            } catch (Exception e) {
                task.setDueTime(null);
            }
        }

        task.setReminderShown(false);
        taskRepository.save(task);
    }

    /**
     * Creates a new task with the given title and period.
     * 
     * @param title  The title of the new task.
     * @param period The time period (day, week, month).
     */
    public void createTask(String title, String period) {
        createTask(title, period, null);
    }

    // Group tasks by creation date (daily)
    public java.util.Map<java.time.LocalDate, java.util.List<Task>> getTasksGroupedByDate(String filter) {
        return getFilteredTasks(filter).stream()
                .filter(task -> task.getCreatedAt() != null)
                .collect(java.util.stream.Collectors.groupingBy(Task::getCreatedAt,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()));
    }

    // Group tasks by week
    public java.util.Map<String, java.util.List<Task>> getTasksGroupedByWeek(String filter) {
        java.util.Map<String, java.util.List<Task>> weeklyTasks = new java.util.LinkedHashMap<>();
        for (Task task : getFilteredTasks(filter)) {
            java.time.LocalDate taskDate = task.getCreatedAt();
            if (taskDate == null) {
                taskDate = java.time.LocalDate.now();
            }
            java.time.LocalDate weekStart = taskDate.with(java.time.DayOfWeek.MONDAY);
            java.time.LocalDate weekEnd = weekStart.plusDays(6);
            String weekKey = weekStart + " to " + weekEnd;
            weeklyTasks.computeIfAbsent(weekKey, k -> new java.util.ArrayList<>()).add(task);
        }
        return weeklyTasks;
    }

    // Group tasks by month
    public java.util.Map<String, java.util.List<Task>> getTasksGroupedByMonth(String filter) {
        return getFilteredTasks(filter).stream()
                .filter(task -> task.getCreatedAt() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        task -> task.getCreatedAt().getMonth() + " " + task.getCreatedAt().getYear(),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()));
    }

    // Get summary stats for a group
    public java.util.Map<String, Object> getTaskStats(String filter, String groupBy) {
        java.util.List<Task> tasks = getFilteredTasks(filter);
        long completed = tasks.stream().filter(Task::isCompleted).count();
        long total = tasks.size();
        return java.util.Map.of(
                "total", total,
                "completed", completed,
                "pending", total - completed,
                "completionRate", total > 0 ? (completed * 100 / total) : 0);
    }

    /**
     * Calculates the deadline based on creation date and period.
     * 
     * @param createdAt The date the task was created.
     * @param period    The period (day, week, month).
     * @return The deadline date.
     */
    private LocalDate calculateDeadline(LocalDate createdAt, String period) {
        if (period == null) {
            period = "day";
        }
        return switch (period.toLowerCase()) {
            case "day", "daily" -> createdAt.plusDays(1);
            case "week", "weekly" -> createdAt.plusDays(7);
            case "month", "monthly" -> createdAt.plusMonths(1);
            default -> createdAt.plusDays(1);
        };
    }

    /**
     * Checks if a task is overdue (past deadline and not completed).
     * Considers both date and time if due time is set.
     * 
     * @param task The task to check.
     * @return True if the task is overdue, false otherwise.
     */
    public boolean isTaskOverdue(Task task) {
        if (task == null || task.isCompleted() || task.getDeadline() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // If deadline is in the future (date-wise), not overdue
        if (today.isBefore(task.getDeadline())) {
            return false;
        }

        // If deadline is in the past (date-wise), definitely overdue
        if (today.isAfter(task.getDeadline())) {
            return true;
        }

        // If deadline is today, check the time
        if (today.isEqual(task.getDeadline())) {
            if (task.getDueTime() != null) {
                return now.isAfter(task.getDueTime());
            }
        }

        return false;
    }

    /**
     * Checks if a reminder should be shown (10 minutes before due time on the
     * deadline date).
     * 
     * @param task The task to check.
     * @return True if a reminder should be shown, false otherwise.
     */
    public boolean shouldShowReminder(Task task) {
        if (task == null || task.isCompleted() || task.getDeadline() == null || task.getDueTime() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Only show reminder on the deadline date
        if (!today.isEqual(task.getDeadline())) {
            return false;
        }

        // Calculate reminder time (10 minutes before due time)
        LocalTime reminderTime = task.getDueTime().minusMinutes(10);

        // Show reminder if current time is between reminder time and due time, and
        // reminder hasn't been shown
        return now.isAfter(reminderTime) && now.isBefore(task.getDueTime()) && !task.isReminderShown();
    }

    /**
     * Marks a reminder as shown for a task.
     * 
     * @param id The ID of the task.
     */
    public void markReminderAsShown(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task id"));
        task.setReminderShown(true);
        taskRepository.save(task);
    }

    /**
     * Filters tasks based on their period type (daily, weekly, monthly).
     * 
     * @param category The category to filter by (day, week, month, or all).
     * @return A list of filtered tasks grouped by period type.
     */
    public List<Task> getFilteredTasks(String category) {
        List<Task> allTasks = getAllTasks();

        // normalize synonyms: daily->day, weekly->week, monthly->month
        String cat = category == null ? "all" : category.toLowerCase();
        switch (cat) {
            case "day":
            case "daily":
                return allTasks.stream()
                        .filter(task -> task.getPeriod() != null &&
                                (task.getPeriod().equalsIgnoreCase("day")
                                        || task.getPeriod().equalsIgnoreCase("daily")))
                        .collect(Collectors.toList());
            case "week":
            case "weekly":
                return allTasks.stream()
                        .filter(task -> task.getPeriod() != null &&
                                (task.getPeriod().equalsIgnoreCase("week")
                                        || task.getPeriod().equalsIgnoreCase("weekly")))
                        .collect(Collectors.toList());
            case "month":
            case "monthly":
                return allTasks.stream()
                        .filter(task -> task.getPeriod() != null &&
                                (task.getPeriod().equalsIgnoreCase("month")
                                        || task.getPeriod().equalsIgnoreCase("monthly")))
                        .collect(Collectors.toList());
            default:
                return allTasks;
        }
    }

    /**
     * Deletes a task by its ID.
     * 
     * @param id The ID of the task to delete.
     */
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    /**
     * Toggles the completion status of a task.
     * Cannot mark a task as complete if it's overdue.
     * 
     * @param id The ID of the task to toggle.
     * @throws IllegalArgumentException if the task is not found or is overdue.
     */
    public void toggleTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task id"));

        // Prevent marking as complete if overdue but allow uncompleting
        if (!task.isCompleted() && isTaskOverdue(task)) {
            throw new IllegalArgumentException("Cannot complete overdue task!");
        }

        task.setCompleted(!task.isCompleted());
        taskRepository.save(task);
    }

    /**
     * Retrieves a single task by its ID.
     * 
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
     * 
     * @param id    The ID of the task to update.
     * @param title The new title for the task.
     */
    public void updateTask(Long id, String title) {
        Task task = getTask(id);
        task.setTitle(title);
        taskRepository.save(task);
    }
}
