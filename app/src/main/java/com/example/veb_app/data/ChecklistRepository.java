package com.example.veb_app.data;

import android.content.Context;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChecklistRepository {
    private ChecklistDao checklistDao;
    private ChecklistTaskDao taskDao;
    private ExecutorService executor;
    
    public ChecklistRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        checklistDao = database.checklistDao();
        taskDao = database.checklistTaskDao();
        executor = Executors.newFixedThreadPool(4);
    }
    
    public void getAllChecklists(DataCallback<List<ChecklistEntity>> callback) {
        executor.execute(() -> {
            try {
                List<ChecklistEntity> checklists = checklistDao.getAllChecklists();
                callback.onSuccess(checklists);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getChecklistById(int id, DataCallback<ChecklistEntity> callback) {
        executor.execute(() -> {
            try {
                ChecklistEntity checklist = checklistDao.getChecklistById(id);
                callback.onSuccess(checklist);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getPinnedChecklist(DataCallback<ChecklistEntity> callback) {
        executor.execute(() -> {
            try {
                ChecklistEntity checklist = checklistDao.getPinnedChecklist();
                callback.onSuccess(checklist);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getMostRecentChecklist(DataCallback<ChecklistEntity> callback) {
        executor.execute(() -> {
            try {
                ChecklistEntity checklist = checklistDao.getMostRecentChecklist();
                callback.onSuccess(checklist);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void searchChecklists(String query, DataCallback<List<ChecklistEntity>> callback) {
        executor.execute(() -> {
            try {
                List<ChecklistEntity> checklists = checklistDao.searchChecklists(query);
                callback.onSuccess(checklists);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void insertChecklist(ChecklistEntity checklist, DataCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long id = checklistDao.insertChecklist(checklist);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void updateChecklist(ChecklistEntity checklist, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                checklistDao.updateChecklist(checklist);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deleteChecklist(ChecklistEntity checklist, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                taskDao.deleteTasksByChecklistId(checklist.getId());
                checklistDao.deleteChecklist(checklist);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void deleteChecklistById(int id, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                taskDao.deleteTasksByChecklistId(id);
                checklistDao.deleteChecklistById(id);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void pinChecklist(int id, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                checklistDao.unpinAllChecklists();
                checklistDao.pinChecklist(id);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void unpinChecklist(int id, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                checklistDao.unpinChecklist(id);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getChecklistsCount(DataCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = checklistDao.getChecklistsCount();
                callback.onSuccess(count);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    // Task operations
    public void getTasksByChecklistId(int checklistId, DataCallback<List<ChecklistTaskEntity>> callback) {
        executor.execute(() -> {
            try {
                List<ChecklistTaskEntity> tasks = taskDao.getTasksByChecklistId(checklistId);
                callback.onSuccess(tasks);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void insertTasks(List<ChecklistTaskEntity> tasks, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                taskDao.insertTasks(tasks);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void updateTasks(List<ChecklistTaskEntity> tasks, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                taskDao.updateTasks(tasks);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getTaskCountForChecklist(int checklistId, DataCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = taskDao.getTaskCountForChecklist(checklistId);
                callback.onSuccess(count);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void getCompletedTaskCountForChecklist(int checklistId, DataCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = taskDao.getCompletedTaskCountForChecklist(checklistId);
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
