package com.example.veb_app.ui.notes;

import android.content.Context;
import com.example.veb_app.data.NoteEntity;
import com.example.veb_app.data.NoteRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NotesManagerDatabase {
    private static NotesManagerDatabase instance;
    private NoteRepository noteRepository;
    private List<NotesFragment.Note> notes;
    private Context context;
    
    private NotesManagerDatabase(Context context) {
        this.context = context.getApplicationContext();
        this.noteRepository = new NoteRepository(this.context);
        this.notes = new ArrayList<>();
        loadNotes();
    }
    
    public static synchronized NotesManagerDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new NotesManagerDatabase(context);
        }
        return instance;
    }
    
    private void loadNotes() {
        noteRepository.getAllNotes(new NoteRepository.DataCallback<List<NoteEntity>>() {
            @Override
            public void onSuccess(List<NoteEntity> noteEntities) {
                notes.clear();
                for (NoteEntity entity : noteEntities) {
                    notes.add(convertEntityToNote(entity));
                }
                sortNotes();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("NotesManagerDatabase", "Error loading notes", error);
            }
        });
    }
    
    public void addNote(NotesFragment.Note note, Callback callback) {
        NoteEntity entity = convertNoteToEntity(note);
        noteRepository.insertNote(entity, new NoteRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                // Create a new note with the database ID
                NotesFragment.Note newNote = new NotesFragment.Note(
                    note.getTitle(), 
                    note.getBody(), 
                    note.getFormattedBody(),
                    note.isPinned(), 
                    id
                );
                notes.add(newNote);
                sortNotes();
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("NotesManagerDatabase", "Error adding note", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void updateNote(NotesFragment.Note note, Callback callback) {
        NoteEntity entity = convertNoteToEntity(note);
        noteRepository.updateNote(entity, new NoteRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Update local list
                for (int i = 0; i < notes.size(); i++) {
                    if (notes.get(i).getId() == note.getId()) {
                        notes.set(i, note);
                        break;
                    }
                }
                sortNotes();
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("NotesManagerDatabase", "Error updating note", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void deleteNote(NotesFragment.Note note, Callback callback) {
        NoteEntity entity = convertNoteToEntity(note);
        noteRepository.deleteNote(entity, new NoteRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                notes.removeIf(n -> n.getId() == note.getId());
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("NotesManagerDatabase", "Error deleting note", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void pinNote(NotesFragment.Note note, Callback callback) {
        // Unpin all other notes first
        unpinAllNotes(new Callback() {
            @Override
            public void onSuccess() {
                noteRepository.pinNote((int) note.getId(), new NoteRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        note.setPinned(true);
                        sortNotes();
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        android.util.Log.e("NotesManagerDatabase", "Error pinning note", error);
                        if (callback != null) callback.onError(error);
                    }
                });
            }
            
            @Override
            public void onError(Exception error) {
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void unpinNote(NotesFragment.Note note, Callback callback) {
        noteRepository.unpinNote((int) note.getId(), new NoteRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                note.setPinned(false);
                sortNotes();
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("NotesManagerDatabase", "Error unpinning note", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void unpinAllNotes(Callback callback) {
        // Unpin all notes locally
        for (NotesFragment.Note note : notes) {
            note.setPinned(false);
        }
        sortNotes();
        if (callback != null) callback.onSuccess();
    }
    
    public List<NotesFragment.Note> getAllNotes() {
        return new ArrayList<>(notes);
    }
    
    public NotesFragment.Note getFeaturedNote() {
        // First try to get pinned note
        for (NotesFragment.Note note : notes) {
            if (note.isPinned()) {
                return note;
            }
        }
        
        // If no pinned note, return most recent
        return notes.isEmpty() ? null : notes.get(0);
    }
    
    public List<NotesFragment.Note> searchNotes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllNotes();
        }
        
        List<NotesFragment.Note> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (NotesFragment.Note note : notes) {
            if (note.getTitle().toLowerCase().contains(lowerQuery) ||
                note.getBody().toLowerCase().contains(lowerQuery)) {
                results.add(note);
            }
        }
        
        return results;
    }
    
    public void refreshNotes(Callback callback) {
        loadNotes();
        if (callback != null) callback.onSuccess();
    }
    
    private void sortNotes() {
        Collections.sort(notes, (n1, n2) -> {
            // Pinned notes first, then by ID (newest first)
            if (n1.isPinned() && !n2.isPinned()) return -1;
            if (!n1.isPinned() && n2.isPinned()) return 1;
            return Long.compare(n2.getId(), n1.getId());
        });
    }
    
    private NoteEntity convertNoteToEntity(NotesFragment.Note note) {
        NoteEntity entity = new NoteEntity(note.getTitle(), note.getBody(), note.isPinned());
        entity.setId((int) note.getId());
        entity.setCreatedDate(System.currentTimeMillis());
        entity.setModifiedDate(System.currentTimeMillis());
        return entity;
    }
    
    private NotesFragment.Note convertEntityToNote(NoteEntity entity) {
        NotesFragment.Note note = new NotesFragment.Note(
            entity.getTitle(), 
            entity.getBody(), 
            null, // formattedBody - will be set later if needed
            entity.isPinned(), 
            entity.getId()
        );
        return note;
    }
    
    public interface Callback {
        void onSuccess();
        void onError(Exception error);
    }
}
