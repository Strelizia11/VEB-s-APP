package com.example.veb_app.data;

import androidx.room.*;
import java.util.List;

@Dao
public interface NoteDao {
    
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, modifiedDate DESC")
    List<NoteEntity> getAllNotes();
    
    @Query("SELECT * FROM notes WHERE id = :id")
    NoteEntity getNoteById(int id);
    
    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY modifiedDate DESC LIMIT 1")
    NoteEntity getPinnedNote();
    
    @Query("SELECT * FROM notes WHERE isPinned = 0 ORDER BY modifiedDate DESC LIMIT 1")
    NoteEntity getMostRecentNote();
    
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY isPinned DESC, modifiedDate DESC")
    List<NoteEntity> searchNotes(String query);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(NoteEntity note);
    
    @Update
    void updateNote(NoteEntity note);
    
    @Delete
    void deleteNote(NoteEntity note);
    
    @Query("DELETE FROM notes WHERE id = :id")
    void deleteNoteById(int id);
    
    @Query("UPDATE notes SET isPinned = 0 WHERE isPinned = 1")
    void unpinAllNotes();
    
    @Query("UPDATE notes SET isPinned = 1 WHERE id = :id")
    void pinNote(int id);
    
    @Query("UPDATE notes SET isPinned = 0 WHERE id = :id")
    void unpinNote(int id);
    
    @Query("SELECT COUNT(*) FROM notes")
    int getNotesCount();
}
