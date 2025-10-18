package dev.matheuslf.desafio.inscritos.service.impl;

import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskResponse;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskStatusUpdateRequest;
import dev.matheuslf.desafio.inscritos.domain.entities.Task;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskPriority;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskStatus;
import dev.matheuslf.desafio.inscritos.mapper.TaskMapper;
import dev.matheuslf.desafio.inscritos.repository.ProjectRepository;
import dev.matheuslf.desafio.inscritos.repository.TaskRepository;
import dev.matheuslf.desafio.inscritos.service.TaskService;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional
    public TaskResponse create(TaskRequest request) {
        var project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new NoSuchElementException("Projeto não encontrado com ID: " + request.projectId()));

        validateDueDate(request.dueDate());
        var task = taskMapper.toEntity(request);
        task.setProject(project);

        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findById(UUID id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task não encontrada com o ID: " + id));
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findAllWithFilters(TaskStatus status, TaskPriority priority, UUID projectId) {
        Specification<Task> spec = buildTaskSpecification(status, priority, projectId);

        var tasks = taskRepository.findAll(spec);
        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse update(UUID id, TaskRequest request) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task não encontrada para o o ID de atualização: " + id));

        validateDueDate(request.dueDate());

        if (!task.getProject().getId().equals(request.projectId())) {
            var newProject = projectRepository.findById(request.projectId())
                    .orElseThrow(() -> new NoSuchElementException("Novo Projeto não encontrado com ID: " + request.projectId()));
            task.setProject(newProject);
        }

        taskMapper.updateFromDto(request, task);
        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(UUID id, TaskStatusUpdateRequest request) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tarefa não encontrada para o ID de atualização de status: " + id));

        task.setStatus(request.status());
        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new NoSuchElementException(("Não é possível deletar. Task não encontrada com o ID: " + id));
        }
        taskRepository.deleteById(id);
    }

    public static Specification<Task> buildTaskSpecification(
            TaskStatus status,
            TaskPriority priority,
            UUID projectId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (projectId != null) {
                predicates.add((Predicate) cb.equal(
                        root.join("project", JoinType.INNER).get("id"),
                        projectId
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateDueDate(LocalDate dueDate) {
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data limite (dueDate) não pode ser anterior à data atual.");
        }
    }
}
