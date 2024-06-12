package com.example.service;

import com.example.entity.Project;
import com.example.repository.ProjectRepository;
import org.springframework.stereotype.Service;

/**
 * @author ishrat.jahan
 * @since 06/13,2024
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void saveProject(Project project) {
        projectRepository.save(project);
    }
}
