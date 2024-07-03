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

    @ManyToOne
    private User uploadedBy;

    @Column(name = "reportPayload")
    private String reportPayload;

    public Scans() {
    }

    public Scans(String fileName, String filePath, User user) {
        this.fileName = fileName;
        this.filePath = filePath;
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

    public String getReportPayload() {
        return reportPayload;
    }

    public void setReportPayload(String reportPayload) {
        this.reportPayload = reportPayload;
    }

}
