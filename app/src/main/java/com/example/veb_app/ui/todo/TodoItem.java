package com.example.veb_app.ui.todo;

import java.util.ArrayList;
import java.util.List;

/**
 * To-do model with title and tasks
 */
public class TodoItem {
    private String id;
    private String title;
    private List<TodoTask> tasks;
    private boolean isCompleted;
    private long createdAt;
    private boolean isPinned;

    public TodoItem() {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = "";
        this.tasks = new ArrayList<>();
        this.isCompleted = false;
        this.createdAt = System.currentTimeMillis();
        this.isPinned = false;
    }

    public TodoItem(String title) {
        this();
        this.title = title;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public List<TodoTask> getTasks() { return tasks; }
    public void setTasks(List<TodoTask> tasks) { this.tasks = tasks; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { this.isPinned = pinned; }
    
    /**
     * Get completed tasks count
     */
    public int getCompletedTasksCount() {
        int count = 0;
        for (TodoTask task : tasks) {
            if (task.isCompleted()) count++;
        }
        return count;
    }
    
    /**
     * Get total tasks count
     */
    public int getTotalTasksCount() {
        return tasks.size();
    }
    
    /**
     * Get progress percentage
     */
    public int getProgressPercentage() {
        if (tasks.isEmpty()) return 0;
        return (getCompletedTasksCount() * 100) / tasks.size();
    }

    /**
     * TodoTask inner class
     */
    public static class TodoTask {
        private String id;
        private String text;
        private boolean isCompleted;

        public TodoTask() {
            this.id = java.util.UUID.randomUUID().toString();
            this.text = "";
            this.isCompleted = false;
        }

        public TodoTask(String text) {
            this();
            this.text = text;
        }

        public TodoTask(String text, boolean isCompleted) {
            this(text);
            this.isCompleted = isCompleted;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { this.isCompleted = completed; }
    }
}
