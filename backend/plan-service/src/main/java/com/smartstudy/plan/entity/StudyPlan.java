package com.smartstudy.plan.entity;

import java.util.ArrayList;
import java.util.List;

public class StudyPlan {

    private Long id;
    private String title;
    private String goal;
    private Integer durationDays;
    private Integer dailyHours;
    private String status;
    private List<StudyTask> tasks = new ArrayList<>();

    public StudyPlan() {
    }

    public StudyPlan(Long id, String title, String goal, Integer durationDays, Integer dailyHours, String status, List<StudyTask> tasks) {
        this.id = id;
        this.title = title;
        this.goal = goal;
        this.durationDays = durationDays;
        this.dailyHours = dailyHours;
        this.status = status;
        this.tasks = new ArrayList<>(tasks);
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

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getDailyHours() {
        return dailyHours;
    }

    public void setDailyHours(Integer dailyHours) {
        this.dailyHours = dailyHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<StudyTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<StudyTask> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }
}
