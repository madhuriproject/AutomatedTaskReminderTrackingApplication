package com.mywaysai.automatedtaskremindertrackingapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mywaysai.automatedtaskremindertrackingapplication.entity.Task;
import com.mywaysai.automatedtaskremindertrackingapplication.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository repo;
    private final SchedulerService schedulerService;

    public TaskService(TaskRepository repo, SchedulerService schedulerService) {
        this.repo = repo;
        this.schedulerService = schedulerService;
    }

    public Task addTask(Task t) {
        Task saved = repo.save(t);
        // schedule reminder
        schedulerService.scheduleReminderForTask(saved);
        return saved;
    }

    public List<Task> listAll() {
        return repo.findAll();
    }

    public Optional<Task> findById(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public Task updateTask(Task updated) {
        Task t = repo.findById(updated.getId()).orElseThrow();
        t.setTitle(updated.getTitle());
        t.setDescription(updated.getDescription());
        t.setDueDate(updated.getDueDate());
        t.setReminderEmail(updated.getReminderEmail());
        t.setReminderMinutesBefore(updated.getReminderMinutesBefore());
        t.setVisits(updated.getVisits());
        Task saved = repo.save(t);
        schedulerService.scheduleReminderForTask(saved);
        return saved;
    }

    @Transactional
    public Task markCompleted(Long id) {
        Task t = repo.findById(id).orElseThrow();
        t.setCompleted(true);
        t.setCompletedAt(java.time.LocalDateTime.now());
        repo.save(t);
        // cancel future reminders if any
        schedulerService.cancelReminderForTask(id);
        return t;
    }

    public void delete(Long id) {
        repo.deleteById(id);
        schedulerService.cancelReminderForTask(id);
    }

    public List<Task> pendingTasks() {
        return repo.findByCompletedFalse();
    }
}
