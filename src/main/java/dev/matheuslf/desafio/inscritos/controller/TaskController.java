package dev.matheuslf.desafio.inscritos.controller;

import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskResponse;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskStatusUpdateRequest;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskPriority;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskStatus;
import dev.matheuslf.desafio.inscritos.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "Endpoints para gerenciamento de tarefas")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Cria uma nova tarefa", description = "Cria uma tarefa e a associa a um projeto existente.")
    @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso.")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (erros de validação ou regra de negócio).", content = @Content)
    @ApiResponse(responseCode = "404", description = "Projeto pai não encontrado.", content = @Content)
    @PostMapping
    public ResponseEntity<EntityModel<TaskResponse>> createTask(@RequestBody @Valid TaskRequest request) {
        TaskResponse response = taskService.create(request);
        EntityModel<TaskResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(TaskController.class).getTaskById(response.id())).withSelfRel());
        resource.add(linkTo(methodOn(TaskController.class).getAllTaskWithFilters(null, null, null)).withRel("all-tasks"));
        resource.add(linkTo(methodOn(ProjectController.class).getProjectById(response.projectId())).withRel("project"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @Operation(summary = "Busca uma tarefa pelo ID", description = "Retorna os detalhes de uma tarefa específica.")
    @ApiResponse(responseCode = "200", description = "Tarefa encontrada com sucesso.")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada.", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<TaskResponse>> getTaskById(@PathVariable UUID id) {
        TaskResponse response = taskService.findById(id);
        EntityModel<TaskResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(TaskController.class).getTaskById(id)).withSelfRel());
        resource.add(linkTo(methodOn(TaskController.class).getAllTaskWithFilters(null, null, null)).withRel("all-tasks"));
        resource.add(linkTo(methodOn(ProjectController.class).getProjectById(response.projectId())).withRel("project"));
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Lista todas as tarefas com filtros", description = "Retorna uma lista de tarefas, com filtros opcionais por status, prioridade ou projeto.")
    @ApiResponse(responseCode = "200", description = "Busca concluída com sucesso.")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<TaskResponse>>> getAllTaskWithFilters(
            @Parameter(description = "Filtrar por status da tarefa") @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filtrar por prioridade da tarefa") @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "Filtrar por ID do projeto") @RequestParam(required = false) UUID projectId) {
        List<TaskResponse> tasks = taskService.findAllWithFilters(status, priority, projectId);
        List<EntityModel<TaskResponse>> taskResources = tasks.stream()
                .map(task -> {
                    EntityModel<TaskResponse> resource = EntityModel.of(task);
                    resource.add(linkTo(methodOn(TaskController.class).getTaskById(task.id())).withSelfRel());
                    resource.add(linkTo(methodOn(ProjectController.class).getProjectById(task.projectId())).withRel("project"));
                    return resource;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(taskResources,
                linkTo(methodOn(TaskController.class).getAllTaskWithFilters(status, priority, projectId)).withSelfRel()));
    }

    @Operation(summary = "Atualiza uma tarefa existente", description = "Atualiza todos os dados de uma tarefa existente pelo seu ID.")
    @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso.")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (erros de validação).", content = @Content)
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada.", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<TaskResponse>> updateTask(@PathVariable UUID id, @RequestBody @Valid TaskRequest request) {
        TaskResponse response = taskService.update(id, request);
        EntityModel<TaskResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(TaskController.class).getTaskById(response.id())).withSelfRel());
        resource.add(linkTo(methodOn(TaskController.class).getAllTaskWithFilters(null, null, null)).withRel("all-tasks"));
        resource.add(linkTo(methodOn(ProjectController.class).getProjectById(response.projectId())).withRel("project"));
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Atualiza o status de uma tarefa", description = "Atualiza apenas o status de uma tarefa específica.")
    @ApiResponse(responseCode = "200", description = "Status da tarefa atualizado com sucesso.")
    @ApiResponse(responseCode = "400", description = "Status inválido.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada.", content = @Content)
    @PatchMapping("/{id}/status")
    public ResponseEntity<EntityModel<TaskResponse>> updateTaskStatus(@PathVariable UUID id, @RequestBody @Valid TaskStatusUpdateRequest request) {
        TaskResponse response = taskService.updateStatus(id, request);
        EntityModel<TaskResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(TaskController.class).getTaskById(response.id())).withSelfRel());
        resource.add(linkTo(methodOn(TaskController.class).getAllTaskWithFilters(null, null, null)).withRel("all-tasks"));
        resource.add(linkTo(methodOn(ProjectController.class).getProjectById(response.projectId())).withRel("project"));
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Exclui uma tarefa", description = "Exclui uma tarefa permanentemente pelo seu ID.")
    @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso.")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada.", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
