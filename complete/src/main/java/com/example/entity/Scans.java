package com.example.entity;

import jakarta.persistence.*;

import java.util.Date;

/**
 * @author ishrat.jahan
 * @since 06/13,2024
 */
@Entity
@Table(name="scans")
public class Scans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fileName")
    private String fileName;

    @Column(name = "filePath")
    private String filePath;

    @Column(name = "created")
    private Date uploadDate;

    @OneToOne
    private User uploadedBy;

    public Scans() {
    }

    public Scans(String name, String key, User user) {
        this.fileName = name;
        this.filePath = key;
        this.uploadDate = new Date();
        this.uploadedBy = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}
