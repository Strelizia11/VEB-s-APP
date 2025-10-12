package com.example.veb_app.data;

import androidx.room.*;
import java.util.List;

@Dao
public interface ChecklistDao {
    
    @Query("SELECT * FROM checklists ORDER BY isPinned DESC, modifiedDate DESC")
    List<ChecklistEntity> getAllChecklists();
    
    @Query("SELECT * FROM checklists WHERE id = :id")
    ChecklistEntity getChecklistById(int id);
    
    @Query("SELECT * FROM checklists WHERE isPinned = 1 ORDER BY modifiedDate DESC LIMIT 1")
    ChecklistEntity getPinnedChecklist();
    
    @Query("SELECT * FROM checklists WHERE isPinned = 0 ORDER BY modifiedDate DESC LIMIT 1")
    ChecklistEntity getMostRecentChecklist();
    
    @Query("SELECT * FROM checklists WHERE title LIKE '%' || :query || '%' ORDER BY isPinned DESC, modifiedDate DESC")
    List<ChecklistEntity> searchChecklists(String query);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertChecklist(ChecklistEntity checklist);
    
    @Update
    void updateChecklist(ChecklistEntity checklist);
    
    @Delete
    void deleteChecklist(ChecklistEntity checklist);
    
    @Query("DELETE FROM checklists WHERE id = :id")
    void deleteChecklistById(int id);
    
    @Query("UPDATE checklists SET isPinned = 0 WHERE isPinned = 1")
    void unpinAllChecklists();
    
    @Query("UPDATE checklists SET isPinned = 1 WHERE id = :id")
    void pinChecklist(int id);
    
    @Query("UPDATE checklists SET isPinned = 0 WHERE id = :id")
    void unpinChecklist(int id);
    
    @Query("SELECT COUNT(*) FROM checklists")
    int getChecklistsCount();
}
