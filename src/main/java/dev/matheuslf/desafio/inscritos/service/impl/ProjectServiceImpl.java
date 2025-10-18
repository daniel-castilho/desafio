package dev.matheuslf.desafio.inscritos.service.impl;

import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectResponse;
import dev.matheuslf.desafio.inscritos.domain.entities.Project;
import dev.matheuslf.desafio.inscritos.mapper.ProjectMapper;
import dev.matheuslf.desafio.inscritos.repository.ProjectRepository;
import dev.matheuslf.desafio.inscritos.service.ProjectService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        validateProjectNameUniqueness(request.name());
        validateStartDateBiggerThanEndDate(request.startDate(), request.endDate());

        var project = projectMapper.toEntity(request);
        project = projectRepository.save(project);
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse findById(UUID id) {
        var project = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Projeto não encontrado com ID: " + id));
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        var project = projectRepository.findAll()
                .stream()
                .map(projectMapper::toResponse)
                .toList();
        return project;
    }

    @Override
    @Transactional
    public ProjectResponse update(UUID id, ProjectRequest request) {
        var project = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Projeto não encontrado para o ID de atualização: " + id));

        // Se o nome foi alterado, valida a unicidade do novo nome
        if (!project.getName().equalsIgnoreCase(request.name())) {
            validateProjectNameUniqueness(request.name());
        }

        validateStartDateBiggerThanEndDate(request.startDate(), request.endDate());

        projectMapper.updateFromDto(request, project);
        project = projectRepository.save(project);
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new NoSuchElementException("Não é possível deletar. Projeto não encontrado com ID: " + id);
        }
        projectRepository.deleteById(id);
    }

    private void validateStartDateBiggerThanEndDate(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null &&
                endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("A data de término não pode ser anterior à data de início do projeto.");
        }
    }

    private void validateProjectNameUniqueness(String name) {
        if (projectRepository.existsByName(name)) {
            throw new IllegalArgumentException("Já existe um projeto com o nome: " + name);
        }
    }
}
