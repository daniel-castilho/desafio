package dev.matheuslf.desafio.inscritos.service;

import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskResponse;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskStatusUpdateRequest;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskPriority;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse create(TaskRequest request);
    TaskResponse findById(UUID id);
    List<TaskResponse> findAllWithFilters(TaskStatus status, TaskPriority priority, UUID projectId);
    TaskResponse update(UUID id, TaskRequest request);
    TaskResponse updateStatus(UUID id, TaskStatusUpdateRequest request);
    void delete(UUID id);
}
