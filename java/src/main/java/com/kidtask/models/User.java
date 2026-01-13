package com.kidtask.models;
import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String PARENT = "Ebeveyn", CHILD = "Çocuk", TEACHER = "Öğretmen";
    private String name, role;
    private int points = 0, level = 1;

    public User(String name, String role) { this.name = name; this.role = role; }
    
    public void addPoints(int pts) { 
        this.points += pts; 
        this.level = Math.max(1, (this.points / 100) + 1); // Her 100 puan bir seviye
    }
    public String getName() { return name; }
    public String getRole() { return role; }
    public int getPoints() { return points; }
    public int getLevel() { return level; }
    public boolean isChild() { return role.equals(CHILD); }
}