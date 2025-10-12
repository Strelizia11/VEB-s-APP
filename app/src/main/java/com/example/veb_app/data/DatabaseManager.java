package com.example.veb_app.data;

import android.content.Context;
import com.example.veb_app.ui.notes.NotesManagerDatabase;
import com.example.veb_app.ui.budget.BudgetManagerDatabase;
import com.example.veb_app.ui.checklist.ChecklistManagerDatabase;
import com.example.veb_app.ui.calendar.EventManagerDatabase;

/**
 * Central manager for accessing all database-backed managers
 * This provides a clean interface to use the new Room database system
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Context context;
    
    private NotesManagerDatabase notesManager;
    private BudgetManagerDatabase budgetManager;
    private ChecklistManagerDatabase checklistManager;
    private EventManagerDatabase eventManager;
    
    private DatabaseManager(Context context) {
        this.context = context.getApplicationContext();
        initializeManagers();
    }
    
    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }
    
    private void initializeManagers() {
        notesManager = NotesManagerDatabase.getInstance(context);
        budgetManager = BudgetManagerDatabase.getInstance(context);
        checklistManager = ChecklistManagerDatabase.getInstance(context);
        eventManager = EventManagerDatabase.getInstance(context);
    }
    
    public NotesManagerDatabase getNotesManager() {
        return notesManager;
    }
    
    public BudgetManagerDatabase getBudgetManager() {
        return budgetManager;
    }
    
    public ChecklistManagerDatabase getChecklistManager() {
        return checklistManager;
    }
    
    public EventManagerDatabase getEventManager() {
        return eventManager;
    }
    
    /**
     * Use this method in fragments to get the database manager
     * Example: DatabaseManager.getInstance(getContext()).getNotesManager()
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseManager not initialized. Call getInstance(Context) first.");
        }
        return instance;
    }
}
