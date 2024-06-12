package com.example.service;

import com.example.entity.Project;

import java.util.List;

public interface ProjectService {

    void saveProject(Project project);

    List<Project> findAll();
}
