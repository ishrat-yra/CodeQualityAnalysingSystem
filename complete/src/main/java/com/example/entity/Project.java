package com.example.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ishrat.jahan
 * @since 06/13,2024
 */
@Entity
@Table(name="project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "projectKey")
    private String key;

    @Column(name = "created")
    private Date created;

    @OneToMany
    List<Scans> scans;

    public Project() {
    }

    public Project(String name, String key) {
        this.name = name;
        this.key = key;
        this.created = new Date();
        this.scans = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<Scans> getScans() {
        return scans;
    }

    public void setScans(List<Scans> scans) {
        this.scans = scans;
    }
}
