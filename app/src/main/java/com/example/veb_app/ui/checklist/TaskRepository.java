package com.example.veb_app.ui.checklist;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing task states with SharedPreferences persistence
 * Ensures task states persist across app sessions and page navigation
 */
public class TaskRepository {
    private static TaskRepository instance;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "task_states";
    private static final String TASK_STATE_PREFIX = "task_state_";
    
    private TaskRepository() {
        // Private constructor for singleton
    }
    
    public static synchronized TaskRepository getInstance() {
        if (instance == null) {
            instance = new TaskRepository();
        }
        return instance;
    }
    
    public void initialize(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        android.util.Log.d("TaskRepository", "Initialized with context");
    }
    
    /**
     * Save a task's checked state
     * @param taskId Unique identifier for the task (checklistId + taskIndex)
     * @param isChecked Whether the task is checked
     */
    public void saveTaskState(String taskId, boolean isChecked) {
        if (prefs != null) {
            String key = TASK_STATE_PREFIX + taskId;
            prefs.edit().putBoolean(key, isChecked).apply();
            android.util.Log.d("TaskRepository", "Saved task state: " + taskId + " = " + isChecked);
        }
    }
    
    /**
     * Load a task's checked state
     * @param taskId Unique identifier for the task
     * @param defaultValue Default value if no saved state exists
     * @return The saved checked state or default value
     */
    public boolean loadTaskState(String taskId, boolean defaultValue) {
        if (prefs != null) {
            String key = TASK_STATE_PREFIX + taskId;
            boolean state = prefs.getBoolean(key, defaultValue);
            android.util.Log.d("TaskRepository", "Loaded task state: " + taskId + " = " + state);
            return state;
        }
        return defaultValue;
    }
    
    /**
     * Save all task states from a checklist
     * @param checklist The checklist containing tasks to save
     */
    public void saveAllTaskStates(ChecklistFragment.Checklist checklist) {
        if (prefs == null || checklist == null) return;
        
        SharedPreferences.Editor editor = prefs.edit();
        String checklistId = String.valueOf(checklist.getId());
        
        for (int i = 0; i < checklist.getTasks().size(); i++) {
            ChecklistFragment.Checklist.Task task = checklist.getTasks().get(i);
            String taskId = checklistId + "_" + i;
            String key = TASK_STATE_PREFIX + taskId;
            editor.putBoolean(key, task.isChecked());
            android.util.Log.d("TaskRepository", "Saving task: " + taskId + " = " + task.isChecked());
        }
        
        editor.apply();
        android.util.Log.d("TaskRepository", "Saved all task states for checklist: " + checklist.getTitle());
    }
    
    /**
     * Load all task states for a checklist
     * @param checklist The checklist to load states for
     */
    public void loadAllTaskStates(ChecklistFragment.Checklist checklist) {
        if (prefs == null || checklist == null) {
            android.util.Log.w("TaskRepository", "Cannot load task states: prefs=" + (prefs != null) + ", checklist=" + (checklist != null));
            return;
        }
        
        String checklistId = String.valueOf(checklist.getId());
        android.util.Log.d("TaskRepository", "Loading task states for checklist: " + checklist.getTitle() + " (ID: " + checklistId + ")");
        
        for (int i = 0; i < checklist.getTasks().size(); i++) {
            ChecklistFragment.Checklist.Task task = checklist.getTasks().get(i);
            String taskId = checklistId + "_" + i;
            boolean originalState = task.isChecked();
            
            // Check if we have a saved state for this task
            String key = TASK_STATE_PREFIX + taskId;
            boolean hasSavedState = prefs.contains(key);
            
            if (hasSavedState) {
                // Use false as default value to ensure correct behavior
                boolean savedState = loadTaskState(taskId, false);
                task.setChecked(savedState);
                android.util.Log.d("TaskRepository", "Loaded saved task: " + taskId + " from " + originalState + " to " + savedState);
            } else {
                // No saved state, ensure task is unchecked by default
                if (task.isChecked()) {
                    task.setChecked(false);
                    android.util.Log.d("TaskRepository", "Reset unsaved task: " + taskId + " from " + originalState + " to false");
                } else {
                    android.util.Log.d("TaskRepository", "Task already unchecked: " + taskId);
                }
            }
        }
        
        android.util.Log.d("TaskRepository", "Finished loading task states for checklist: " + checklist.getTitle());
    }
    
    /**
     * Clear all saved task states (useful for testing or reset)
     */
    public void clearAllTaskStates() {
        if (prefs != null) {
            SharedPreferences.Editor editor = prefs.edit();
            Map<String, ?> allPrefs = prefs.getAll();
            
            for (String key : allPrefs.keySet()) {
                if (key.startsWith(TASK_STATE_PREFIX)) {
                    editor.remove(key);
                }
            }
            
            editor.apply();
            android.util.Log.d("TaskRepository", "Cleared all task states");
        }
    }
    
    /**
     * Reset all task states to unchecked for a specific checklist
     * This can be used to fix corrupted states
     */
    public void resetChecklistTaskStates(ChecklistFragment.Checklist checklist) {
        if (prefs == null || checklist == null) return;
        
        String checklistId = String.valueOf(checklist.getId());
        SharedPreferences.Editor editor = prefs.edit();
        
        // Reset all tasks in this checklist to unchecked
        for (int i = 0; i < checklist.getTasks().size(); i++) {
            String taskId = checklistId + "_" + i;
            String key = TASK_STATE_PREFIX + taskId;
            editor.putBoolean(key, false);
            // Also update the task object
            checklist.getTasks().get(i).setChecked(false);
        }
        
        editor.apply();
        android.util.Log.d("TaskRepository", "Reset all task states to unchecked for checklist: " + checklist.getTitle());
    }
    
    /**
     * Get all saved task states (for debugging)
     * @return Map of task IDs to their states
     */
    public Map<String, ?> getAllTaskStates() {
        if (prefs != null) {
            return prefs.getAll();
        }
        return null;
    }
}
