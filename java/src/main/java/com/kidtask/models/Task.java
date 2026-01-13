package com.kidtask.models;
import java.io.Serializable;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title, completedBy;
    private int points;
    private boolean isCompleted = false, isApproved = false;

    public Task(String title, int points) {
        this.title = title;
        this.points = points;
    }
    public String getTitle() { return title; }
    public int getPoints() { return points; }
    public String getCompletedBy() { return completedBy; }
    public boolean isCompleted() { return isCompleted; }
    public boolean isApproved() { return isApproved; }
    public void setCompleted(boolean c, String cb) { this.isCompleted = c; this.completedBy = cb; }
    public void setApproved(boolean a) { this.isApproved = a; }
    @Override
    public String toString() { 
        String status = isApproved ? "✅ Onaylı" : (isCompleted ? "⏳ Bekliyor" : "🚀 Aktif");
        return status + " | " + title + " (" + points + " Puan)"; 
    }
}