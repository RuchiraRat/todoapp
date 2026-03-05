package com.app.todoapp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.todoapp.models.Task;
import com.app.todoapp.services.TaskService;

@Controller
// @RequestMapping("/tasks") // Optional: depending on if we want a prefix or
// root
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String getTasks(@RequestParam(defaultValue = "all") String filter, Model model) {
        List<Task> tasks = taskService.getFilteredTasks(filter);

        // Add overdue status and reminder status for each task
        java.util.Map<Long, Boolean> overdueMap = new java.util.HashMap<>();
        java.util.Map<Long, Boolean> reminderMap = new java.util.HashMap<>();
        for (Task task : tasks) {
            overdueMap.put(task.getId(), taskService.isTaskOverdue(task));
            reminderMap.put(task.getId(), taskService.shouldShowReminder(task));

            // Mark reminder as shown if it should be displayed
            if (taskService.shouldShowReminder(task)) {
                taskService.markReminderAsShown(task.getId());
            }
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("overdueMap", overdueMap);
        model.addAttribute("reminderMap", reminderMap);
        return "tasks";
    }

    @PostMapping("/")
    public String createTask(
            @RequestParam String title,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(required = false) String dueTime) {
        taskService.createTask(title, period, dueTime);
        return "redirect:/";
    }

    @GetMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id, @RequestParam(defaultValue = "all") String filter) {
        taskService.deleteTask(id);
        return "redirect:/?filter=" + filter;
    }

    @GetMapping("/{id}/toggle")
    public String toggleTask(@PathVariable Long id, @RequestParam(defaultValue = "all") String filter) {
        taskService.toggleTask(id);
        return "redirect:/?filter=" + filter;
    }

    // Daily grouped view - shows only DAY period tasks
    @GetMapping("/daily")
    public String getTasksDaily(@RequestParam(defaultValue = "all") String filter, Model model) {
        var groupedTasks = taskService.getTasksGroupedByDate("day"); // Filter by DAY period
        java.util.Map<Long, Boolean> overdueMap = new java.util.HashMap<>();
        java.util.Map<Long, Boolean> reminderMap = new java.util.HashMap<>();

        // Populate maps for all tasks
        for (java.util.List<Task> tasks : groupedTasks.values()) {
            for (Task task : tasks) {
                overdueMap.put(task.getId(), taskService.isTaskOverdue(task));
                reminderMap.put(task.getId(), taskService.shouldShowReminder(task));
                if (taskService.shouldShowReminder(task)) {
                    taskService.markReminderAsShown(task.getId());
                }
            }
        }

        model.addAttribute("groupedTasks", groupedTasks);
        model.addAttribute("viewType", "daily");
        model.addAttribute("stats", taskService.getTaskStats("day", "daily")); // Stats for DAY tasks
        model.addAttribute("overdueMap", overdueMap);
        model.addAttribute("reminderMap", reminderMap);
        return "tasks-grouped";
    }

    // Weekly grouped view - shows only WEEK period tasks
    @GetMapping("/weekly")
    public String getTasksWeekly(@RequestParam(defaultValue = "all") String filter, Model model) {
        var groupedTasks = taskService.getTasksGroupedByWeek("week"); // Filter by WEEK period
        java.util.Map<Long, Boolean> overdueMap = new java.util.HashMap<>();
        java.util.Map<Long, Boolean> reminderMap = new java.util.HashMap<>();

        // Populate maps for all tasks
        for (java.util.List<Task> tasks : groupedTasks.values()) {
            for (Task task : tasks) {
                overdueMap.put(task.getId(), taskService.isTaskOverdue(task));
                reminderMap.put(task.getId(), taskService.shouldShowReminder(task));
                if (taskService.shouldShowReminder(task)) {
                    taskService.markReminderAsShown(task.getId());
                }
            }
        }

        model.addAttribute("groupedTasks", groupedTasks);
        model.addAttribute("viewType", "weekly");
        model.addAttribute("stats", taskService.getTaskStats("week", "weekly")); // Stats for WEEK tasks
        model.addAttribute("overdueMap", overdueMap);
        model.addAttribute("reminderMap", reminderMap);
        return "tasks-grouped";
    }

    // Monthly grouped view - shows only MONTH period tasks
    @GetMapping("/monthly")
    public String getTasksMonthly(@RequestParam(defaultValue = "all") String filter, Model model) {
        var groupedTasks = taskService.getTasksGroupedByMonth("month"); // Filter by MONTH period
        java.util.Map<Long, Boolean> overdueMap = new java.util.HashMap<>();
        java.util.Map<Long, Boolean> reminderMap = new java.util.HashMap<>();

        // Populate maps for all tasks
        for (java.util.List<Task> tasks : groupedTasks.values()) {
            for (Task task : tasks) {
                overdueMap.put(task.getId(), taskService.isTaskOverdue(task));
                reminderMap.put(task.getId(), taskService.shouldShowReminder(task));
                if (taskService.shouldShowReminder(task)) {
                    taskService.markReminderAsShown(task.getId());
                }
            }
        }

        model.addAttribute("groupedTasks", groupedTasks);
        model.addAttribute("viewType", "monthly");
        model.addAttribute("stats", taskService.getTaskStats("month", "monthly")); // Stats for MONTH tasks
        model.addAttribute("overdueMap", overdueMap);
        model.addAttribute("reminderMap", reminderMap);
        return "tasks-grouped";
    }
}
