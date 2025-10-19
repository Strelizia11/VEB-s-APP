package com.example.veb_app.ui.checklist;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChecklistManager {
    private static ChecklistManager instance;
    private final List<ChecklistFragment.Checklist> checklistList;
    private SharedPreferences prefs;
    private Gson gson;

    private ChecklistManager() {
        checklistList = new ArrayList<>();
        // Create Gson with custom configuration to handle boolean fields properly
        gson = new Gson();
    }
    
    public void initialize(Context context) {
        prefs = context.getSharedPreferences("checklist_prefs", Context.MODE_PRIVATE);
        loadChecklists();
    }

    public static synchronized ChecklistManager getInstance() {
        if (instance == null) {
            instance = new ChecklistManager();
        }
        return instance;
    }

    public void addChecklist(ChecklistFragment.Checklist checklist) {
        checklistList.add(checklist);
        sortChecklists();
        saveChecklists();
    }

    public void updateChecklist(ChecklistFragment.Checklist updatedChecklist) {
        // If pinning a checklist, unpin all others first
        if (updatedChecklist.isPinned()) {
            unpinAllOtherChecklists(updatedChecklist.getId());
        }
        
        for (int i = 0; i < checklistList.size(); i++) {
            if (checklistList.get(i).getId() == updatedChecklist.getId()) {
                checklistList.set(i, updatedChecklist);
                break;
            }
        }
        sortChecklists();
        saveChecklists();
    }
    
    private void unpinAllOtherChecklists(long currentChecklistId) {
        for (ChecklistFragment.Checklist checklist : checklistList) {
            if (checklist.getId() != currentChecklistId && checklist.isPinned()) {
                checklist.setPinned(false);
            }
        }
    }

    public void deleteChecklist(ChecklistFragment.Checklist checklist) {
        checklistList.remove(checklist);
        sortChecklists();
        saveChecklists();
    }

    public List<ChecklistFragment.Checklist> getAllChecklists() {
        return new ArrayList<>(checklistList); // Return a copy to prevent external modification
    }

    public ChecklistFragment.Checklist getChecklistById(long id) {
        for (ChecklistFragment.Checklist checklist : checklistList) {
            if (checklist.getId() == id) {
                return checklist;
            }
        }
        return null;
    }

    public int getChecklistsCount() {
        return checklistList.size();
    }
    
    /**
     * Force save all checklist data immediately
     * Use this when you need to ensure data is persisted (e.g., on app pause)
     */
    public void forceSaveAllData() {
        android.util.Log.d("ChecklistManager", "Force saving all checklist data");
        saveChecklists();
    }

    // Method to get the featured checklist (pinned or most recent)
    public ChecklistFragment.Checklist getFeaturedChecklist() {
        // First, check for a pinned checklist
        for (ChecklistFragment.Checklist checklist : checklistList) {
            if (checklist.isPinned()) {
                return checklist;
            }
        }

        // If no pinned checklist, return the most recent checklist
        if (!checklistList.isEmpty()) {
            List<ChecklistFragment.Checklist> sortedByRecent = new ArrayList<>(checklistList);
            sortedByRecent.sort((c1, c2) -> Long.compare(c2.getId(), c1.getId()));
            return sortedByRecent.get(0);
        }
        return null; // No checklists available
    }

    private void sortChecklists() {
        // Sort checklists: pinned first, then by ID (most recent first)
        Collections.sort(checklistList, (checklist1, checklist2) -> {
            // Pinned checklists first
            if (checklist1.isPinned() && !checklist2.isPinned()) return -1;
            if (!checklist1.isPinned() && checklist2.isPinned()) return 1;

            // Then by ID (most recent first)
            return Long.compare(checklist2.getId(), checklist1.getId());
        });
    }

    // Method to sort tasks within a checklist: incomplete first, then completed (most recent first within each group)
    public void sortTasksInChecklist(ChecklistFragment.Checklist checklist) {
        if (checklist != null && checklist.getTasks() != null) {
            Collections.sort(checklist.getTasks(), (task1, task2) -> {
                // Incomplete tasks first
                if (!task1.isChecked() && task2.isChecked()) return -1;
                if (task1.isChecked() && !task2.isChecked()) return 1;
                
                // Within same completion status, maintain original order (most recent first)
                return 0; // Keep original insertion order for tasks
            });
        }
    }
    
    private void saveChecklists() {
        if (prefs != null) {
            // Use Gson for reliable JSON serialization
            String checklistsJson = gson.toJson(checklistList);
            android.util.Log.d("ChecklistManager", "Saving checklists JSON: " + checklistsJson);
            
            // Log task states being saved
            for (ChecklistFragment.Checklist checklist : checklistList) {
                android.util.Log.d("ChecklistManager", "Saving checklist: " + checklist.getTitle());
                for (ChecklistFragment.Checklist.Task task : checklist.getTasks()) {
                    android.util.Log.d("ChecklistManager", "Task: '" + task.getText() + "' checked: " + task.isChecked());
                }
            }
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("checklists", checklistsJson);
            editor.apply();
        }
    }
    
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    private void loadChecklists() {
        if (prefs != null) {
            String checklistsJson = prefs.getString("checklists", "");
            android.util.Log.d("ChecklistManager", "Loading checklists JSON: " + checklistsJson);
            if (!checklistsJson.isEmpty()) {
                try {
                    // Use Gson directly for reliable parsing
                    Type listType = new TypeToken<List<ChecklistFragment.Checklist>>(){}.getType();
                    List<ChecklistFragment.Checklist> loadedChecklists = gson.fromJson(checklistsJson, listType);
                    
                    if (loadedChecklists != null) {
                        checklistList.clear();
                        checklistList.addAll(loadedChecklists);
                        
                        // Log task states as they are loaded from JSON
                        for (ChecklistFragment.Checklist checklist : loadedChecklists) {
                            android.util.Log.d("ChecklistManager", "Loaded checklist: " + checklist.getTitle());
                            
                            for (ChecklistFragment.Checklist.Task task : checklist.getTasks()) {
                                android.util.Log.d("ChecklistManager", "Task: '" + task.getText() + "' checked: " + task.isChecked());
                            }
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("ChecklistManager", "Error loading checklists: " + e.getMessage());
                    // If there's an error, clear the corrupted data
                    checklistList.clear();
                }
            }
        }
    }
    
    private List<ChecklistFragment.Checklist> parseChecklistsManually(String json) {
        try {
            // Simple manual JSON parsing for checklists
            List<ChecklistFragment.Checklist> checklists = new ArrayList<>();
            
            // Remove outer brackets and split by checklist objects
            String content = json.trim();
            if (!content.startsWith("[") || !content.endsWith("]")) {
                return null; // Not valid JSON array
            }
            
            content = content.substring(1, content.length() - 1).trim();
            if (content.isEmpty()) {
                return checklists; // Empty array
            }
            
            // Split by checklist objects (simple approach)
            String[] checklistStrings = splitJsonObjects(content);
            
            for (String checklistStr : checklistStrings) {
                ChecklistFragment.Checklist checklist = parseChecklistManually(checklistStr.trim());
                if (checklist != null) {
                    checklists.add(checklist);
                }
            }
            
            return checklists;
        } catch (Exception e) {
            android.util.Log.e("ChecklistManager", "Manual parsing failed: " + e.getMessage());
            return null;
        }
    }
    
    private String[] splitJsonObjects(String content) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0;
        int start = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (braceCount == 0) start = i;
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    objects.add(content.substring(start, i + 1));
                }
            }
        }
        
        return objects.toArray(new String[0]);
    }
    
    private ChecklistFragment.Checklist parseChecklistManually(String json) {
        try {
            String title = extractStringValue(json, "title");
            boolean isPinned = extractBooleanValue(json, "isPinned");
            long id = extractLongValue(json, "id");
            
            // Extract tasks array
            String tasksJson = extractArrayValue(json, "tasks");
            List<ChecklistFragment.Checklist.Task> tasks = parseTasksManually(tasksJson);
            
            return new ChecklistFragment.Checklist(title, tasks, isPinned, id);
        } catch (Exception e) {
            android.util.Log.e("ChecklistManager", "Error parsing checklist: " + e.getMessage());
            return null;
        }
    }
    
    private List<ChecklistFragment.Checklist.Task> parseTasksManually(String tasksJson) {
        List<ChecklistFragment.Checklist.Task> tasks = new ArrayList<>();
        
        if (tasksJson == null || tasksJson.trim().isEmpty()) {
            return tasks;
        }
        
        String[] taskStrings = splitJsonObjects(tasksJson);
        
        for (String taskStr : taskStrings) {
            try {
                String text = extractStringValue(taskStr, "text");
                String taskId = extractStringValue(taskStr, "taskId");
                boolean completed = extractBooleanValue(taskStr, "taskCompleted");
                
                // If no taskId in JSON, generate one for backward compatibility
                if (taskId.isEmpty()) {
                    taskId = java.util.UUID.randomUUID().toString();
                    android.util.Log.d("ChecklistManager", "Generated new taskId for legacy task: " + taskId);
                }
                
                android.util.Log.d("ChecklistManager", "Parsed task: '" + text + "' (ID: " + taskId + ") completed: " + completed);
                tasks.add(new ChecklistFragment.Checklist.Task(text, completed, taskId));
            } catch (Exception e) {
                android.util.Log.e("ChecklistManager", "Error parsing task: " + e.getMessage());
            }
        }
        
        return tasks;
    }
    
    private String extractStringValue(String json, String key) {
        String pattern = "\"" + key + "\":\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return "";
    }
    
    private boolean extractBooleanValue(String json, String key) {
        // Try multiple patterns to handle different JSON formats
        String[] patterns = {
            "\"" + key + "\":(true|false)",           // Standard format
            "\"" + key + "\":\\s*(true|false)",       // With whitespace
            key + ":(true|false)",                    // Without quotes
            key + ":\\s*(true|false)"                 // Without quotes, with whitespace
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                boolean result = "true".equals(m.group(1));
                android.util.Log.d("ChecklistManager", "Extracted boolean for " + key + ": " + result + " from: " + m.group(1) + " using pattern: " + pattern);
                return result;
            }
        }
        
        android.util.Log.w("ChecklistManager", "Could not extract boolean for " + key + " from JSON: " + json);
        return false; // Default to false - this ensures tasks are not completed by default
    }
    
    private long extractLongValue(String json, String key) {
        String pattern = "\"" + key + "\":(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        return System.currentTimeMillis();
    }
    
    private String extractArrayValue(String json, String key) {
        String pattern = "\"" + key + "\":\\[([^\\]]*)\\]";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }
}
