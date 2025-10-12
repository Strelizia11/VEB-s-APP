package com.example.veb_app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "events")
public class EventEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String description;
    private String time;
    private String category;
    private String color;
    private boolean isAllDay;
    private long eventDate;
    private long createdDate;
    private long modifiedDate;
    
    public EventEntity(String title, String description, String time, String category, String color, boolean isAllDay, long eventDate) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.category = category;
        this.color = color;
        this.isAllDay = isAllDay;
        this.eventDate = eventDate;
        this.createdDate = System.currentTimeMillis();
        this.modifiedDate = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public String getTime() { return time; }
    public void setTime(String time) { 
        this.time = time;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public String getColor() { return color; }
    public void setColor(String color) { 
        this.color = color;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public boolean isAllDay() { return isAllDay; }
    public void setAllDay(boolean allDay) { 
        this.isAllDay = allDay;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public long getEventDate() { return eventDate; }
    public void setEventDate(long eventDate) { 
        this.eventDate = eventDate;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
    
    public long getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(long modifiedDate) { this.modifiedDate = modifiedDate; }
    
    public Date getEventDateAsDate() { return new Date(eventDate); }
    public Date getCreatedDateAsDate() { return new Date(createdDate); }
    public Date getModifiedDateAsDate() { return new Date(modifiedDate); }
}
