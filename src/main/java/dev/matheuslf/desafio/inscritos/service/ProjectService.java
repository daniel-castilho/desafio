package dev.matheuslf.desafio.inscritos.service;

import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse create(ProjectRequest request);
    ProjectResponse findById(UUID id);
    List<ProjectResponse> findAll();
    ProjectResponse update(UUID id, ProjectRequest request);
    void delete(UUID id);
}