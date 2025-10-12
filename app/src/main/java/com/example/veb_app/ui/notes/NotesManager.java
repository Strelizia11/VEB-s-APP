package com.example.veb_app.ui.notes;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotesManager {
    private static NotesManager instance;
    private List<NotesFragment.Note> notes;
    private NotesFragment.Note pinnedNote;
    private SharedPreferences prefs;
    private Gson gson;

    private NotesManager() {
        notes = new ArrayList<>();
        gson = new Gson();
    }
    
    public void initialize(Context context) {
        prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE);
        loadNotes();
    }

    public static NotesManager getInstance() {
        if (instance == null) {
            instance = new NotesManager();
        }
        return instance;
    }

    public void addNote(NotesFragment.Note note) {
        notes.add(note);
        if (note.isPinned()) {
            pinnedNote = note;
        }
        saveNotes();
    }

    public void updateNote(NotesFragment.Note note) {
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getId() == note.getId()) {
                notes.set(i, note);
                if (note.isPinned()) {
                    pinnedNote = note;
                } else if (pinnedNote != null && pinnedNote.getId() == note.getId()) {
                    pinnedNote = null;
                }
                break;
            }
        }
        saveNotes();
    }

    public void removeNote(NotesFragment.Note note) {
        notes.removeIf(n -> n.getId() == note.getId());
        if (pinnedNote != null && pinnedNote.getId() == note.getId()) {
            pinnedNote = null;
        }
        saveNotes();
    }

    public List<NotesFragment.Note> getAllNotes() {
        return new ArrayList<>(notes);
    }

    public NotesFragment.Note getPinnedNote() {
        return pinnedNote;
    }

    public NotesFragment.Note getMostRecentNote() {
        if (notes.isEmpty()) {
            return null;
        }
        
        // Find the note with the highest ID (most recent)
        NotesFragment.Note mostRecent = notes.get(0);
        for (NotesFragment.Note note : notes) {
            if (note.getId() > mostRecent.getId()) {
                mostRecent = note;
            }
        }
        return mostRecent;
    }

    public NotesFragment.Note getFeaturedNote() {
        // First try to get pinned note
        NotesFragment.Note featured = getPinnedNote();
        if (featured != null) {
            return featured;
        }
        
        // If no pinned note, get most recent note
        return getMostRecentNote();
    }

    public int getNotesCount() {
        return notes.size();
    }
    
    private void saveNotes() {
        if (prefs != null) {
            String notesJson = gson.toJson(notes);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("notes", notesJson);
            editor.apply();
        }
    }
    
    private void loadNotes() {
        if (prefs != null) {
            String notesJson = prefs.getString("notes", "");
            if (!notesJson.isEmpty()) {
                Type listType = new TypeToken<List<NotesFragment.Note>>(){}.getType();
                List<NotesFragment.Note> loadedNotes = gson.fromJson(notesJson, listType);
                if (loadedNotes != null) {
                    notes = loadedNotes;
                    // Update pinned note reference
                    pinnedNote = null;
                    for (NotesFragment.Note note : notes) {
                        if (note.isPinned()) {
                            pinnedNote = note;
                            break;
                        }
                    }
                }
            }
        }
    }
}
