package dev.matheuslf.desafio.inscritos.mapper;

import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectResponse;
import dev.matheuslf.desafio.inscritos.domain.entities.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(target = "id", ignore = true)
    Project toEntity(ProjectRequest dto);

    ProjectResponse toResponse(Project entity);

    @Mapping(target = "id", ignore = true)
    void updateFromDto(ProjectRequest dto, @MappingTarget Project project);
}
