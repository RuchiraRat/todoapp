package com.app.todoapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.todoapp.models.Task;

/**
 * Task Repository Interface
 * Extends JpaRepository to provide standard CRUD operations for the Task entity.
 * Spring Data JPA automatically implements this interface at runtime.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

}

