package com.example.veb_app.data;

import androidx.room.*;
import java.util.List;

@Dao
public interface ChecklistTaskDao {
    
    @Query("SELECT * FROM checklist_tasks WHERE checklistId = :checklistId ORDER BY sortOrder ASC, createdDate ASC")
    List<ChecklistTaskEntity> getTasksByChecklistId(int checklistId);
    
    @Query("SELECT * FROM checklist_tasks WHERE id = :id")
    ChecklistTaskEntity getTaskById(int id);
    
    @Query("SELECT COUNT(*) FROM checklist_tasks WHERE checklistId = :checklistId")
    int getTaskCountForChecklist(int checklistId);
    
    @Query("SELECT COUNT(*) FROM checklist_tasks WHERE checklistId = :checklistId AND isCompleted = 1")
    int getCompletedTaskCountForChecklist(int checklistId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTask(ChecklistTaskEntity task);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTasks(List<ChecklistTaskEntity> tasks);
    
    @Update
    void updateTask(ChecklistTaskEntity task);
    
    @Update
    void updateTasks(List<ChecklistTaskEntity> tasks);
    
    @Delete
    void deleteTask(ChecklistTaskEntity task);
    
    @Query("DELETE FROM checklist_tasks WHERE id = :id")
    void deleteTaskById(int id);
    
    @Query("DELETE FROM checklist_tasks WHERE checklistId = :checklistId")
    void deleteTasksByChecklistId(int checklistId);
    
    @Query("UPDATE checklist_tasks SET sortOrder = :newOrder WHERE id = :taskId")
    void updateTaskOrder(int taskId, int newOrder);
    
    @Query("UPDATE checklist_tasks SET isCompleted = :completed WHERE id = :taskId")
    void updateTaskCompletion(int taskId, boolean completed);
}
