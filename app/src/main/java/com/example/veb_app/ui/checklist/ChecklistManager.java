package com.example.veb_app.ui.checklist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChecklistManager {
    private static ChecklistManager instance;
    private final List<ChecklistFragment.Checklist> checklistList;

    private ChecklistManager() {
        checklistList = new ArrayList<>();
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
}
