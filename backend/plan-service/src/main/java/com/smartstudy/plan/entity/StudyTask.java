package com.smartstudy.plan.entity;

public class StudyTask {

    private Long id;
    private String title;
    private Integer stage;
    private String status;

    public StudyTask() {
    }

    public StudyTask(Long id, String title, Integer stage, String status) {
        this.id = id;
        this.title = title;
        this.stage = stage;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
