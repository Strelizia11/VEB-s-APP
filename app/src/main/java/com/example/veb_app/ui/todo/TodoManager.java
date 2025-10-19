package com.example.veb_app.ui.todo;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple to-do manager with clean data persistence
 */
public class TodoManager {
    private static TodoManager instance;
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private static final String PREFS_NAME = "todo_items";
    private static final String KEY_ITEMS = "items";

    private TodoManager() {
        this.gson = new Gson();
    }

    public static TodoManager getInstance() {
        if (instance == null) {
            instance = new TodoManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get all to-do items sorted by priority and creation date
     */
    public List<TodoItem> getAllItems() {
        String json = prefs.getString(KEY_ITEMS, "[]");
        Type listType = new TypeToken<List<TodoItem>>(){}.getType();
        List<TodoItem> items = gson.fromJson(json, listType);
        
        if (items == null) {
            items = new ArrayList<>();
        }
        
        // Sort: pinned first, then by creation date (newest first)
        Collections.sort(items, (item1, item2) -> {
            // Pinned items first
            if (item1.isPinned() && !item2.isPinned()) return -1;
            if (!item1.isPinned() && item2.isPinned()) return 1;
            
            // Then by creation date (newest first)
            return Long.compare(item2.getCreatedAt(), item1.getCreatedAt());
        });
        
        return items;
    }

    /**
     * Add a new to-do item
     */
    public void addItem(TodoItem item) {
        List<TodoItem> items = getAllItems();
        items.add(item);
        saveItems(items);
    }

    /**
     * Update an existing to-do item
     */
    public void updateItem(TodoItem item) {
        List<TodoItem> items = getAllItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                break;
            }
        }
        saveItems(items);
    }

    /**
     * Pin a to-do item (unpins all others)
     */
    public void pinItem(TodoItem item) {
        List<TodoItem> items = getAllItems();
        
        // Unpin all other items and update them
        for (TodoItem todo : items) {
            if (!todo.getId().equals(item.getId()) && todo.isPinned()) {
                todo.setPinned(false);
                // Update the item in the list
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).getId().equals(todo.getId())) {
                        items.set(i, todo);
                        break;
                    }
                }
            }
        }
        
        // Pin the selected item
        item.setPinned(true);
        
        // Update the item in the list
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                break;
            }
        }
        
        // Save all items
        saveItems(items);
    }

    /**
     * Unpin a to-do item
     */
    public void unpinItem(TodoItem item) {
        item.setPinned(false);
        updateItem(item);
    }

    /**
     * Delete a to-do item
     */
    public void deleteItem(TodoItem item) {
        List<TodoItem> items = getAllItems();
        items.removeIf(i -> i.getId().equals(item.getId()));
        saveItems(items);
    }

    /**
     * Toggle completion status of an item
     */
    public void toggleItem(TodoItem item) {
        item.setCompleted(!item.isCompleted());
        updateItem(item);
    }

    /**
     * Get completed items count
     */
    public int getCompletedCount() {
        List<TodoItem> items = getAllItems();
        int count = 0;
        for (TodoItem item : items) {
            if (item.isCompleted()) count++;
        }
        return count;
    }

    /**
     * Get total items count
     */
    public int getTotalCount() {
        return getAllItems().size();
    }

    /**
     * Clear all completed items
     */
    public void clearCompleted() {
        List<TodoItem> items = getAllItems();
        items.removeIf(TodoItem::isCompleted);
        saveItems(items);
    }

    /**
     * Save all items to SharedPreferences
     */
    private void saveItems(List<TodoItem> items) {
        String json = gson.toJson(items);
        prefs.edit().putString(KEY_ITEMS, json).apply();
    }
}
