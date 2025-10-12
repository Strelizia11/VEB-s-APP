package com.example.veb_app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "notes")
public class NoteEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String body;
    private boolean isPinned;
    private long createdDate;
    private long modifiedDate;
    
    public NoteEntity(String title, String body, boolean isPinned) {
        this.title = title;
        this.body = body;
        this.isPinned = isPinned;
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
    
    public String getBody() { return body; }
    public void setBody(String body) { 
        this.body = body;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { 
        this.isPinned = pinned;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
    
    public long getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(long modifiedDate) { this.modifiedDate = modifiedDate; }
    
    public Date getCreatedDateAsDate() { return new Date(createdDate); }
    public Date getModifiedDateAsDate() { return new Date(modifiedDate); }
}
