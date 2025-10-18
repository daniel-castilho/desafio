package dev.matheuslf.desafio.inscritos.mapper;

import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskResponse;
import dev.matheuslf.desafio.inscritos.domain.entities.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    Task toEntity(TaskRequest dto);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "dueDate", source = "dueDate")
    TaskResponse toResponse(Task entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateFromDto(TaskRequest dto, @MappingTarget Task task);
}
