package com.example.veb_app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import java.util.Date;

@Entity(
    tableName = "checklist_tasks",
    foreignKeys = @ForeignKey(
        entity = ChecklistEntity.class,
        parentColumns = "id",
        childColumns = "checklistId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("checklistId")}
)
public class ChecklistTaskEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int checklistId;
    private String taskText;
    private boolean isCompleted;
    private int sortOrder;
    private long createdDate;
    private long modifiedDate;
    
    public ChecklistTaskEntity(int checklistId, String taskText, boolean isCompleted, int sortOrder) {
        this.checklistId = checklistId;
        this.taskText = taskText;
        this.isCompleted = isCompleted;
        this.sortOrder = sortOrder;
        this.createdDate = System.currentTimeMillis();
        this.modifiedDate = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getChecklistId() { return checklistId; }
    public void setChecklistId(int checklistId) { this.checklistId = checklistId; }
    
    public String getTaskText() { return taskText; }
    public void setTaskText(String taskText) { 
        this.taskText = taskText;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { 
        this.isCompleted = completed;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { 
        this.sortOrder = sortOrder;
        this.modifiedDate = System.currentTimeMillis();
    }
    
    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
    
    public long getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(long modifiedDate) { this.modifiedDate = modifiedDate; }
    
    public Date getCreatedDateAsDate() { return new Date(createdDate); }
    public Date getModifiedDateAsDate() { return new Date(modifiedDate); }
}
