package com.example.veb_app.ui.notes;

import java.util.ArrayList;
import java.util.List;

public class NotesManager {
    private static NotesManager instance;
    private List<NotesFragment.Note> notes;
    private NotesFragment.Note pinnedNote;

    private NotesManager() {
        notes = new ArrayList<>();
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
    }

    public void removeNote(NotesFragment.Note note) {
        notes.removeIf(n -> n.getId() == note.getId());
        if (pinnedNote != null && pinnedNote.getId() == note.getId()) {
            pinnedNote = null;
        }
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
}
