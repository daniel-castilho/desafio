package dev.matheuslf.desafio.inscritos.controller;

import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectResponse;
import dev.matheuslf.desafio.inscritos.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/projects")
@Tag(name = "Projects", description = "Endpoints para gerenciamento de projetos")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Cria um novo projeto", description = "Cria um novo projeto com base nos dados fornecidos.")
    @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso.")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (erros de validação).", content = @Content)
    @PostMapping
    public ResponseEntity<EntityModel<ProjectResponse>> createProject(@RequestBody @Valid ProjectRequest request) {
        ProjectResponse response = projectService.create(request);
        EntityModel<ProjectResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(ProjectController.class).getProjectById(response.id())).withSelfRel());
        resource.add(linkTo(methodOn(ProjectController.class).getAllProjects()).withRel("all-projects"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @Operation(summary = "Busca um projeto pelo ID", description = "Retorna os detalhes de um projeto específico.")
    @ApiResponse(responseCode = "200", description = "Projeto encontrado com sucesso.")
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado.", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ProjectResponse>> getProjectById(@PathVariable UUID id) {
        ProjectResponse response = projectService.findById(id);
        EntityModel<ProjectResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(ProjectController.class).getProjectById(id)).withSelfRel());
        resource.add(linkTo(methodOn(ProjectController.class).getAllProjects()).withRel("all-projects"));
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Lista todos os projetos", description = "Retorna uma lista com todos os projetos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Busca concluída com sucesso.")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ProjectResponse>>> getAllProjects() {
        List<ProjectResponse> projects = projectService.findAll();
        List<EntityModel<ProjectResponse>> projectResources = projects.stream()
                .map(project -> {
                    EntityModel<ProjectResponse> resource = EntityModel.of(project);
                    resource.add(linkTo(methodOn(ProjectController.class).getProjectById(project.id())).withSelfRel());
                    return resource;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(projectResources,
                linkTo(methodOn(ProjectController.class).getAllProjects()).withSelfRel()));
    }

    @Operation(summary = "Atualiza um projeto existente", description = "Atualiza os dados de um projeto existente pelo seu ID.")
    @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso.")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (erros de validação).", content = @Content)
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado.", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ProjectResponse>> updateProject(@PathVariable UUID id, @RequestBody @Valid ProjectRequest request) {
        ProjectResponse response = projectService.update(id, request);
        EntityModel<ProjectResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(ProjectController.class).getProjectById(response.id())).withSelfRel());
        resource.add(linkTo(methodOn(ProjectController.class).getAllProjects()).withRel("all-projects"));
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Exclui um projeto", description = "Exclui um projeto permanentemente pelo seu ID. A exclusão pode falhar se o projeto tiver tarefas associadas.")
    @ApiResponse(responseCode = "204", description = "Projeto excluído com sucesso.")
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado.", content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
