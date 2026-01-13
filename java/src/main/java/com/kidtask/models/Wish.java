package com.kidtask.models;
import java.io.Serializable;

public class Wish implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description;
    private int requiredLevel;
    private boolean isApproved = false;

    public Wish(String description, int requiredLevel) {
        this.description = description;
        this.requiredLevel = requiredLevel;
    }
    public int getRequiredLevel() { return requiredLevel; }
    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean a) { this.isApproved = a; }
    @Override
    public String toString() { 
        String status = isApproved ? "🎁 Onaylandı" : "❓ Talep Edildi";
        return status + " | " + description + " (Gerekli Lvl: " + requiredLevel + ")"; 
    }
}