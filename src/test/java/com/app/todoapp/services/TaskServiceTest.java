package com.app.todoapp.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.app.todoapp.models.Task;
import com.app.todoapp.repository.TaskRepository;

class TaskServiceTest {

    private TaskRepository repo;
    private TaskService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(TaskRepository.class);
        service = new TaskService(repo);
    }

    private Task makeTask(String title, LocalDate date, String period) {
        Task t = new Task();
        t.setId(0L);
        t.setTitle(title);
        t.setCreatedAt(date);
        t.setCompleted(false);
        t.setPeriod(period);
        // Calculate deadline based on period
        LocalDate deadline = date;
        switch (period.toLowerCase()) {
            case "day":
            case "daily":
                deadline = date.plusDays(1);
                break;
            case "week":
            case "weekly":
                deadline = date.plusDays(7);
                break;
            case "month":
            case "monthly":
                deadline = date.plusMonths(1);
                break;
        }
        t.setDeadline(deadline);
        return t;
    }

    @Test
    void filterDaily_shouldReturnOnlyDailyPeriodTasks() {
        LocalDate now = LocalDate.now();
        Task dailyTask = makeTask("daily task", now, "daily");
        Task weeklyTask = makeTask("weekly task", now, "weekly");
        Mockito.when(repo.findAll()).thenReturn(Arrays.asList(dailyTask, weeklyTask));

        List<Task> tasks = service.getFilteredTasks("daily");
        assertEquals(1, tasks.size());
        assertEquals("daily task", tasks.get(0).getTitle());

        // also test synonym "day"
        tasks = service.getFilteredTasks("day");
        assertEquals(1, tasks.size());
    }

    @Test
    void filterWeekly_shouldReturnOnlyWeeklyPeriodTasks() {
        LocalDate now = LocalDate.now();
        Task dailyTask = makeTask("daily task", now, "daily");
        Task weeklyTask = makeTask("weekly task", now, "weekly");
        Task monthlyTask = makeTask("monthly task", now, "monthly");

        Mockito.when(repo.findAll()).thenReturn(Arrays.asList(dailyTask, weeklyTask, monthlyTask));

        List<Task> tasks = service.getFilteredTasks("weekly");
        assertEquals(1, tasks.size());
        assertEquals("weekly task", tasks.get(0).getTitle());

        tasks = service.getFilteredTasks("week");
        assertEquals(1, tasks.size());
    }

    @Test
    void filterMonthly_shouldReturnOnlyMonthlyPeriodTasks() {
        LocalDate now = LocalDate.now();
        Task dailyTask = makeTask("daily task", now, "daily");
        Task weeklyTask = makeTask("weekly task", now, "weekly");
        Task monthlyTask = makeTask("monthly task", now, "monthly");

        Mockito.when(repo.findAll()).thenReturn(Arrays.asList(dailyTask, weeklyTask, monthlyTask));

        List<Task> tasks = service.getFilteredTasks("monthly");
        assertEquals(1, tasks.size());
        assertEquals("monthly task", tasks.get(0).getTitle());

        tasks = service.getFilteredTasks("month");
        assertEquals(1, tasks.size());
    }

    @Test
    void filterAll_shouldReturnAllTasks() {
        LocalDate now = LocalDate.now();
        Task dailyTask = makeTask("daily task", now, "daily");
        Task weeklyTask = makeTask("weekly task", now, "weekly");
        Task monthlyTask = makeTask("monthly task", now, "monthly");

        Mockito.when(repo.findAll()).thenReturn(Arrays.asList(dailyTask, weeklyTask, monthlyTask));

        List<Task> tasks = service.getFilteredTasks("all");
        assertEquals(3, tasks.size());
    }

    @Test
    void filterUnknown_shouldReturnAll() {
        LocalDate now = LocalDate.now();
        Task t1 = makeTask("daily", now, "daily");
        Task t2 = makeTask("weekly", now, "weekly");
        Mockito.when(repo.findAll()).thenReturn(Arrays.asList(t1, t2));
        List<Task> tasks = service.getFilteredTasks("something");
        assertEquals(2, tasks.size());
    }

    @Test
    void isTaskOverdue_shouldReturnTrueForPastDeadline() {
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        Task task = new Task();
        task.setId(0L);
        task.setTitle("overdue");
        task.setCreatedAt(twoDaysAgo);
        task.setDeadline(twoDaysAgo.plusDays(1));
        task.setCompleted(false);
        task.setPeriod("day");

        assertTrue(service.isTaskOverdue(task));
    }

    @Test
    void isTaskOverdue_shouldReturnFalseForFutureDeadline() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Task task = new Task();
        task.setId(0L);
        task.setTitle("not overdue");
        task.setCreatedAt(LocalDate.now());
        task.setDeadline(tomorrow);
        task.setCompleted(false);
        task.setPeriod("day");

        assertFalse(service.isTaskOverdue(task));
    }

    @Test
    void isTaskOverdue_shouldReturnFalseForCompletedTask() {
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        Task task = new Task();
        task.setId(0L);
        task.setTitle("completed");
        task.setCreatedAt(twoDaysAgo);
        task.setDeadline(twoDaysAgo.plusDays(1));
        task.setCompleted(true);
        task.setPeriod("day");

        assertFalse(service.isTaskOverdue(task));
    }

    @Test
    void toggleTask_shouldNotAllowCompletingOverdueTask() {
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        Task task = new Task();
        task.setId(1L);
        task.setTitle("overdue");
        task.setCreatedAt(twoDaysAgo);
        task.setDeadline(twoDaysAgo.plusDays(1));
        task.setCompleted(false);
        task.setPeriod("day");

        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class, () -> service.toggleTask(1L));
    }

    @Test
    void toggleTask_shouldAllowUncompletingOverdueTask() {
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        Task task = new Task();
        task.setId(1L);
        task.setTitle("overdue");
        task.setCreatedAt(twoDaysAgo);
        task.setDeadline(twoDaysAgo.plusDays(1));
        task.setCompleted(true);
        task.setPeriod("day");

        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(task));

        service.toggleTask(1L);
        assertFalse(task.isCompleted());
    }

    @Test
    void isTaskOverdue_shouldConsiderTimeOnDeadlineDate() {
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusMinutes(5); // 5 minutes ago

        Task task = new Task();
        task.setId(1L);
        task.setTitle("time-based task");
        task.setCreatedAt(today);
        task.setDeadline(today);
        task.setDueTime(pastTime);
        task.setCompleted(false);
        task.setPeriod("day");

        assertTrue(service.isTaskOverdue(task));
    }

    @Test
    void isTaskOverdue_shouldNotBeOverdueIfTimeIsInFuture() {
        LocalDate today = LocalDate.now();
        LocalTime futureTime = LocalTime.now().plusMinutes(30); // 30 minutes from now

        Task task = new Task();
        task.setId(2L);
        task.setTitle("future time task");
        task.setCreatedAt(today);
        task.setDeadline(today);
        task.setDueTime(futureTime);
        task.setCompleted(false);
        task.setPeriod("day");

        assertFalse(service.isTaskOverdue(task));
    }

    @Test
    void shouldShowReminder_shouldReturnTrueWithin10MinutesBefore() {
        LocalDate today = LocalDate.now();
        LocalTime dueTime = LocalTime.now().plusMinutes(5); // 5 minutes from now

        Task task = new Task();
        task.setId(3L);
        task.setTitle("reminder task");
        task.setCreatedAt(today);
        task.setDeadline(today);
        task.setDueTime(dueTime);
        task.setCompleted(false);
        task.setReminderShown(false);
        task.setPeriod("day");

        assertTrue(service.shouldShowReminder(task));
    }

    @Test
    void shouldShowReminder_shouldReturnFalseAfterReminderShown() {
        LocalDate today = LocalDate.now();
        LocalTime dueTime = LocalTime.now().plusMinutes(5); // 5 minutes from now

        Task task = new Task();
        task.setId(4L);
        task.setTitle("shown reminder task");
        task.setCreatedAt(today);
        task.setDeadline(today);
        task.setDueTime(dueTime);
        task.setCompleted(false);
        task.setReminderShown(true);
        task.setPeriod("day");

        assertFalse(service.shouldShowReminder(task));
    }

    @Test
    void shouldShowReminder_shouldReturnFalseWhenNoDueTime() {
        LocalDate today = LocalDate.now();

        Task task = new Task();
        task.setId(5L);
        task.setTitle("no time task");
        task.setCreatedAt(today);
        task.setDeadline(today);
        task.setDueTime(null);
        task.setCompleted(false);
        task.setReminderShown(false);
        task.setPeriod("day");

        assertFalse(service.shouldShowReminder(task));
    }
}