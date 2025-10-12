package com.example.veb_app.data;

import android.content.Context;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private NoteDao noteDao;
    private ExecutorService executor;
    
    public NoteRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        noteDao = database.noteDao();
        executor = Executors.newFixedThreadPool(4);
    }
    
    public void getAllNotes(DataCallback<List<NoteEntity>> callback) {
        executor.execute(() -> {
            try {
                List<NoteEntity> notes = noteDao.getAllNotes();
                callback.onSuccess(notes);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getNoteById(int id, DataCallback<NoteEntity> callback) {
        executor.execute(() -> {
            try {
                NoteEntity note = noteDao.getNoteById(id);
                callback.onSuccess(note);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getPinnedNote(DataCallback<NoteEntity> callback) {
        executor.execute(() -> {
            try {
                NoteEntity note = noteDao.getPinnedNote();
                callback.onSuccess(note);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getMostRecentNote(DataCallback<NoteEntity> callback) {
        executor.execute(() -> {
            try {
                NoteEntity note = noteDao.getMostRecentNote();
                callback.onSuccess(note);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void searchNotes(String query, DataCallback<List<NoteEntity>> callback) {
        executor.execute(() -> {
            try {
                List<NoteEntity> notes = noteDao.searchNotes(query);
                callback.onSuccess(notes);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void insertNote(NoteEntity note, DataCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long id = noteDao.insertNote(note);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void updateNote(NoteEntity note, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                noteDao.updateNote(note);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deleteNote(NoteEntity note, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                noteDao.deleteNote(note);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deleteNoteById(int id, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                noteDao.deleteNoteById(id);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void pinNote(int id, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                noteDao.unpinAllNotes();
                noteDao.pinNote(id);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void unpinNote(int id, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                noteDao.unpinNote(id);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getNotesCount(DataCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = noteDao.getNotesCount();
                callback.onSuccess(count);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(Exception error);
    }
}
