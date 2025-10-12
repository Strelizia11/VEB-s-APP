package com.example.veb_app.ui.checklist;

import android.content.Context;
import com.example.veb_app.data.ChecklistEntity;
import com.example.veb_app.data.ChecklistTaskEntity;
import com.example.veb_app.data.ChecklistRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChecklistManagerDatabase {
    private static ChecklistManagerDatabase instance;
    private ChecklistRepository checklistRepository;
    private List<ChecklistFragment.Checklist> checklists;
    private Context context;
    
    private ChecklistManagerDatabase(Context context) {
        this.context = context.getApplicationContext();
        this.checklistRepository = new ChecklistRepository(this.context);
        this.checklists = new ArrayList<>();
        loadChecklists();
    }
    
    public static synchronized ChecklistManagerDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new ChecklistManagerDatabase(context);
        }
        return instance;
    }
    
    private void loadChecklists() {
        checklistRepository.getAllChecklists(new ChecklistRepository.DataCallback<List<ChecklistEntity>>() {
            @Override
            public void onSuccess(List<ChecklistEntity> checklistEntities) {
                checklists.clear();
                for (ChecklistEntity entity : checklistEntities) {
                    ChecklistFragment.Checklist checklist = convertEntityToChecklist(entity);
                    loadTasksForChecklist(checklist);
                    checklists.add(checklist);
                }
                sortChecklists();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("ChecklistManagerDatabase", "Error loading checklists", error);
            }
        });
    }
    
    private void loadTasksForChecklist(ChecklistFragment.Checklist checklist) {
        checklistRepository.getTasksByChecklistId((int) checklist.getId(), new ChecklistRepository.DataCallback<List<ChecklistTaskEntity>>() {
            @Override
            public void onSuccess(List<ChecklistTaskEntity> taskEntities) {
                List<ChecklistFragment.Checklist.Task> tasks = new ArrayList<>();
                for (ChecklistTaskEntity entity : taskEntities) {
                    tasks.add(convertEntityToTask(entity));
                }
                checklist.setTasks(tasks);
                sortTasks(checklist);
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("ChecklistManagerDatabase", "Error loading tasks for checklist", error);
            }
        });
    }
    
    public void addChecklist(ChecklistFragment.Checklist checklist, Callback callback) {
        ChecklistEntity entity = convertChecklistToEntity(checklist);
        checklistRepository.insertChecklist(entity, new ChecklistRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                // Create a new checklist with the database ID
                ChecklistFragment.Checklist newChecklist = new ChecklistFragment.Checklist(
                    checklist.getTitle(),
                    checklist.getTasks(),
                    checklist.isPinned(),
                    id
                );
                checklists.add(newChecklist);
                sortChecklists();
                
                // Save tasks
                if (!checklist.getTasks().isEmpty()) {
                    saveTasksForChecklist(checklist, callback);
                } else if (callback != null) {
                    callback.onSuccess();
                }
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("ChecklistManagerDatabase", "Error adding checklist", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void updateChecklist(ChecklistFragment.Checklist checklist, Callback callback) {
        ChecklistEntity entity = convertChecklistToEntity(checklist);
        checklistRepository.updateChecklist(entity, new ChecklistRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Update local list
                for (int i = 0; i < checklists.size(); i++) {
                    if (checklists.get(i).getId() == checklist.getId()) {
                        checklists.set(i, checklist);
                        break;
                    }
                }
                sortChecklists();
                
                // Save tasks
                saveTasksForChecklist(checklist, callback);
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("ChecklistManagerDatabase", "Error updating checklist", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    private void saveTasksForChecklist(ChecklistFragment.Checklist checklist, Callback callback) {
        List<ChecklistTaskEntity> taskEntities = new ArrayList<>();
        for (int i = 0; i < checklist.getTasks().size(); i++) {
            ChecklistFragment.Checklist.Task task = checklist.getTasks().get(i);
            ChecklistTaskEntity entity = convertTaskToEntity(task);
            entity.setChecklistId((int) checklist.getId());
            entity.setSortOrder(i);
            taskEntities.add(entity);
        }
        
        checklistRepository.insertTasks(taskEntities, new ChecklistRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("ChecklistManagerDatabase", "Error saving tasks", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void deleteChecklist(ChecklistFragment.Checklist checklist, Callback callback) {
        ChecklistEntity entity = convertChecklistToEntity(checklist);
        checklistRepository.deleteChecklist(entity, new ChecklistRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                checklists.removeIf(c -> c.getId() == checklist.getId());
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("ChecklistManagerDatabase", "Error deleting checklist", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void pinChecklist(ChecklistFragment.Checklist checklist, Callback callback) {
        // Unpin all other checklists first
        unpinAllChecklists(new Callback() {
            @Override
            public void onSuccess() {
                checklistRepository.pinChecklist((int) checklist.getId(), new ChecklistRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        checklist.setPinned(true);
                        sortChecklists();
                        if (callback != null) callback.onSuccess();
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        android.util.Log.e("ChecklistManagerDatabase", "Error pinning checklist", error);
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
    
    public void unpinChecklist(ChecklistFragment.Checklist checklist, Callback callback) {
        checklistRepository.unpinChecklist((int) checklist.getId(), new ChecklistRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                checklist.setPinned(false);
                sortChecklists();
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onError(Exception error) {
                android.util.Log.e("ChecklistManagerDatabase", "Error unpinning checklist", error);
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void unpinAllChecklists(Callback callback) {
        // Unpin all checklists locally
        for (ChecklistFragment.Checklist checklist : checklists) {
            checklist.setPinned(false);
        }
        sortChecklists();
        if (callback != null) callback.onSuccess();
    }
    
    public List<ChecklistFragment.Checklist> getAllChecklists() {
        return new ArrayList<>(checklists);
    }
    
    public ChecklistFragment.Checklist getFeaturedChecklist() {
        // First try to get pinned checklist
        for (ChecklistFragment.Checklist checklist : checklists) {
            if (checklist.isPinned()) {
                return checklist;
            }
        }
        
        // If no pinned checklist, return most recent
        return checklists.isEmpty() ? null : checklists.get(0);
    }
    
    public List<ChecklistFragment.Checklist> searchChecklists(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllChecklists();
        }
        
        List<ChecklistFragment.Checklist> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (ChecklistFragment.Checklist checklist : checklists) {
            if (checklist.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(checklist);
            }
        }
        
        return results;
    }
    
    public void refreshChecklists(Callback callback) {
        loadChecklists();
        if (callback != null) callback.onSuccess();
    }
    
    private void sortChecklists() {
        Collections.sort(checklists, (c1, c2) -> {
            // Pinned checklists first, then by ID (newest first)
            if (c1.isPinned() && !c2.isPinned()) return -1;
            if (!c1.isPinned() && c2.isPinned()) return 1;
            return Long.compare(c2.getId(), c1.getId());
        });
    }
    
    private void sortTasks(ChecklistFragment.Checklist checklist) {
        Collections.sort(checklist.getTasks(), (t1, t2) -> {
            // Completed tasks at the bottom
            if (t1.isCompleted() && !t2.isCompleted()) return 1;
            if (!t1.isCompleted() && t2.isCompleted()) return -1;
            return 0; // Keep original order for same completion status
        });
    }
    
    private ChecklistEntity convertChecklistToEntity(ChecklistFragment.Checklist checklist) {
        ChecklistEntity entity = new ChecklistEntity(checklist.getTitle(), checklist.isPinned());
        entity.setId((int) checklist.getId());
        entity.setCreatedDate(System.currentTimeMillis());
        entity.setModifiedDate(System.currentTimeMillis());
        return entity;
    }
    
    private ChecklistFragment.Checklist convertEntityToChecklist(ChecklistEntity entity) {
        ChecklistFragment.Checklist checklist = new ChecklistFragment.Checklist(
            entity.getTitle(),
            new ArrayList<>(), // empty tasks list initially
            entity.isPinned(),
            entity.getId()
        );
        return checklist;
    }
    
    private ChecklistTaskEntity convertTaskToEntity(ChecklistFragment.Checklist.Task task) {
        ChecklistTaskEntity entity = new ChecklistTaskEntity(
            0, // Will be set when saving
            task.getText(),
            task.isCompleted(),
            0 // Will be set when saving
        );
        entity.setId(0); // Will be set by database
        entity.setCreatedDate(System.currentTimeMillis());
        entity.setModifiedDate(System.currentTimeMillis());
        return entity;
    }
    
    private ChecklistFragment.Checklist.Task convertEntityToTask(ChecklistTaskEntity entity) {
        ChecklistFragment.Checklist.Task task = new ChecklistFragment.Checklist.Task(
            entity.getTaskText(),
            entity.isCompleted()
        );
        return task;
    }
    
    public interface Callback {
        void onSuccess();
        void onError(Exception error);
    }
}
