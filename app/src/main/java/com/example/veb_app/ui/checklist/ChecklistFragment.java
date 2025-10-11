package com.example.veb_app.ui.checklist;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.veb_app.R;
import com.example.veb_app.databinding.FragmentChecklistBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ChecklistFragment extends Fragment {

    private FragmentChecklistBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ChecklistViewModel checklistViewModel =
                new ViewModelProvider(this).get(ChecklistViewModel.class);

        binding = FragmentChecklistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textChecklist;
        checklistViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Setup FAB click handler
        FloatingActionButton fabAddChecklist = binding.fabAddChecklist;
        if (fabAddChecklist != null) {
            fabAddChecklist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCreateChecklistDialog();
                }
            });
        }

        // Load existing checklists from ChecklistManager
        loadExistingChecklists();
        
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload checklists when returning to this fragment
        loadExistingChecklists();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showCreateChecklistDialog() {
        showCreateChecklistDialog(null);
    }

    private void showCreateChecklistDialog(Checklist existingChecklist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_checklist, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Get dialog views
        TextInputEditText etTitle = dialogView.findViewById(R.id.et_checklist_title);
        TextInputEditText etTasks = dialogView.findViewById(R.id.et_checklist_tasks);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Pre-fill fields if editing existing checklist
        if (existingChecklist != null) {
            etTitle.setText(existingChecklist.getTitle());
            
            // Convert task list to text format, preserving completion states
            StringBuilder tasksText = new StringBuilder();
            for (Checklist.Task task : existingChecklist.getTasks()) {
                tasksText.append(task.getText()).append("\n");
            }
            etTasks.setText(tasksText.toString());
        } else {
            // For new checklist, add a blank line to show empty task indicator
            etTasks.setText("\n");
        }

        // Save button
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String tasksText = etTasks.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a checklist title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tasksText.isEmpty()) {
                Toast.makeText(getContext(), "Please enter at least one task", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse tasks from text
            List<Checklist.Task> tasks;
            if (existingChecklist != null) {
                // Preserve completion states when editing
                tasks = parseTasksFromTextWithCompletion(tasksText, existingChecklist);
            } else {
                tasks = parseTasksFromText(tasksText);
            }
            
            if (existingChecklist != null) {
                // Update existing checklist
                existingChecklist.setTitle(title);
                existingChecklist.setTasks(tasks);
                ChecklistManager.getInstance().updateChecklist(existingChecklist);
                updateChecklistDisplay(existingChecklist);
                Toast.makeText(getContext(), "Checklist updated: " + title, Toast.LENGTH_SHORT).show();
            } else {
                // Create new checklist
                Checklist newChecklist = new Checklist(title, tasks);
                ChecklistManager.getInstance().addChecklist(newChecklist);
                addChecklistToDisplay(newChecklist);
                Toast.makeText(getContext(), "Checklist saved: " + title, Toast.LENGTH_SHORT).show();
            }
            
            dialog.dismiss();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private List<Checklist.Task> parseTasksFromText(String tasksText) {
        List<Checklist.Task> tasks = new ArrayList<>();
        String[] lines = tasksText.split("\n");
        
        for (String line : lines) {
            String taskText = line.trim();
            // Add task even if empty (for blank task indicators)
            tasks.add(new Checklist.Task(taskText, false));
        }
        
        return tasks;
    }
    
    private List<Checklist.Task> parseTasksFromTextWithCompletion(String tasksText, Checklist existingChecklist) {
        List<Checklist.Task> tasks = new ArrayList<>();
        String[] lines = tasksText.split("\n");
        List<Checklist.Task> existingTasks = existingChecklist.getTasks();
        
        for (int i = 0; i < lines.length; i++) {
            String taskText = lines[i].trim();
            
            // Check if this task existed before and preserve its completion state
            boolean wasCompleted = false;
            if (i < existingTasks.size()) {
                wasCompleted = existingTasks.get(i).isCompleted();
            }
            
            // Only preserve completion state if the task text hasn't changed
            boolean preserveCompletion = false;
            if (i < existingTasks.size() && taskText.equals(existingTasks.get(i).getText())) {
                preserveCompletion = true;
            }
            
            tasks.add(new Checklist.Task(taskText, preserveCompletion && wasCompleted));
        }
        
        return tasks;
    }

    private void addChecklistToDisplay(Checklist checklist) {
        // Hide the default text
        binding.textChecklist.setVisibility(View.GONE);

        // Create checklist container
        View checklistContainer = createChecklistContainer(checklist);
        
        // Add to the checklists grid at the beginning (most recent first)
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        checklistsGrid.addView(checklistContainer, 0); // Insert at position 0 (top)
    }

    private void updateChecklistDisplay(Checklist checklist) {
        // Find and update the existing checklist container
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        
        // Find the checklist container by looking for the one with matching checklist data
        for (int i = 0; i < checklistsGrid.getChildCount(); i++) {
            View child = checklistsGrid.getChildAt(i);
            if (child.getTag() != null) {
                Checklist existingChecklist = (Checklist) child.getTag();
                if (existingChecklist.getId() == checklist.getId()) {
                    // Remove old container
                    checklistsGrid.removeViewAt(i);
                    
                    // Create new container with updated content
                    View newChecklistContainer = createChecklistContainer(checklist);
                    checklistsGrid.addView(newChecklistContainer, i);
                    break;
                }
            }
        }
    }

    private View createChecklistContainer(Checklist checklist) {
        // Inflate Google Keep-style card layout
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_checklist_card, null);
        
        // Set tag to identify this checklist container
        cardView.setTag(checklist);

        // Get views from the card
        com.google.android.material.card.MaterialCardView cardChecklist = cardView.findViewById(R.id.card_checklist);
        TextView tvTitle = cardView.findViewById(R.id.tv_checklist_title);
        LinearLayout tasksContainer = cardView.findViewById(R.id.tasks_container);
        LinearLayout progressContainer = cardView.findViewById(R.id.progress_container);
        TextView tvProgressText = cardView.findViewById(R.id.tv_progress_text);
        com.google.android.material.progressindicator.LinearProgressIndicator progressBar = cardView.findViewById(R.id.progress_bar);

        // Set title
        tvTitle.setText(checklist.getTitle());
        
        // Set different background color for pinned checklists
        if (checklist.isPinned()) {
            cardChecklist.setCardBackgroundColor(getResources().getColor(R.color.pinned_checklist_background));
        } else {
            cardChecklist.setCardBackgroundColor(getResources().getColor(R.color.md_theme_light_surface));
        }

        // Add tasks to container
        List<Checklist.Task> sortedTasks = new ArrayList<>(checklist.getTasks());
        // Sort tasks: incomplete first, then completed
        sortedTasks.sort((task1, task2) -> {
            if (task1.isCompleted() == task2.isCompleted()) return 0;
            return task1.isCompleted() ? 1 : -1;
        });

        // Clear existing tasks
        tasksContainer.removeAllViews();

        for (Checklist.Task task : sortedTasks) {
            View taskView = createTaskView(task, checklist);
            tasksContainer.addView(taskView);
        }

        // Show progress if there are tasks
        if (!checklist.getTasks().isEmpty()) {
            int completedCount = 0;
            for (Checklist.Task task : checklist.getTasks()) {
                if (task.isCompleted()) completedCount++;
            }
            
            int totalTasks = checklist.getTasks().size();
            int progress = totalTasks > 0 ? (completedCount * 100) / totalTasks : 0;
            
            tvProgressText.setText(completedCount + " of " + totalTasks + " completed");
            progressBar.setProgress(progress);
            progressContainer.setVisibility(View.VISIBLE);
        } else {
            progressContainer.setVisibility(View.GONE);
        }

        // Add long press listener for context menu
        cardView.setOnLongClickListener(v -> {
            showChecklistContextMenu(checklist, cardView);
            return true;
        });

        return cardView;
    }

    private View createTaskView(Checklist.Task task, Checklist checklist) {
        View taskView = LayoutInflater.from(getContext()).inflate(R.layout.item_checklist_task, null);
        
        CheckBox cbTask = taskView.findViewById(R.id.cb_task);
        TextView tvTaskText = taskView.findViewById(R.id.tv_task_text);
        
        // Handle empty tasks (blank indicators)
        if (task.getText().isEmpty()) {
            // Show blank task indicator
            tvTaskText.setText(" ");
            tvTaskText.setTextColor(getResources().getColor(R.color.md_theme_light_onSurfaceVariant));
            tvTaskText.setAlpha(0.3f);
            cbTask.setEnabled(false);
            cbTask.setAlpha(0.3f);
        } else {
            // Set task text with Google Keep styling
            if (task.isCompleted()) {
                // Apply strikethrough and fade color for completed tasks (Google Keep style)
                SpannableString spannable = new SpannableString(task.getText());
                spannable.setSpan(new StrikethroughSpan(), 0, task.getText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvTaskText.setText(spannable);
                tvTaskText.setTextColor(getResources().getColor(R.color.md_theme_light_onSurfaceVariant));
                tvTaskText.setAlpha(0.5f);
            } else {
                tvTaskText.setText(task.getText());
                tvTaskText.setTextColor(getResources().getColor(R.color.md_theme_light_onSurface));
                tvTaskText.setAlpha(1.0f);
            }
            
            // Set checkbox state and enable it
            cbTask.setChecked(task.isCompleted());
            cbTask.setEnabled(true);
            cbTask.setAlpha(1.0f);
            
            // Set checkbox listener
            cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                ChecklistManager.getInstance().updateChecklist(checklist);
                
                // Refresh the display to reorder tasks and update progress
                updateChecklistDisplay(checklist);
            });
        }
        
        return taskView;
    }

    private void showChecklistContextMenu(Checklist checklist, View checklistContainer) {
        PopupMenu popupMenu = new PopupMenu(getContext(), checklistContainer);
        popupMenu.getMenuInflater().inflate(R.menu.checklist_context_menu, popupMenu.getMenu());
        
        // Update pin/unpin text based on current state
        if (checklist.isPinned()) {
            popupMenu.getMenu().findItem(R.id.action_pin).setTitle("Unpin");
        } else {
            popupMenu.getMenu().findItem(R.id.action_pin).setTitle("Pin");
        }
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                editChecklist(checklist, checklistContainer);
                return true;
            } else if (itemId == R.id.action_delete) {
                deleteChecklist(checklist, checklistContainer);
                return true;
            } else if (itemId == R.id.action_pin) {
                togglePinChecklist(checklist, checklistContainer);
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    private void editChecklist(Checklist checklist, View checklistContainer) {
        showCreateChecklistDialog(checklist);
    }

    private void deleteChecklist(Checklist checklist, View checklistContainer) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Checklist")
                .setMessage("Are you sure you want to delete this checklist?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Remove the checklist container from the layout
                    LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
                    checklistsGrid.removeView(checklistContainer);
                    
                    // Delete from manager
                    ChecklistManager.getInstance().deleteChecklist(checklist);
                    
                    // Show the default text if no checklists remain
                    if (checklistsGrid.getChildCount() == 0) {
                        binding.textChecklist.setVisibility(View.VISIBLE);
                    }
                    
                    Toast.makeText(getContext(), "Checklist deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void togglePinChecklist(Checklist checklist, View checklistContainer) {
        boolean wasPinned = checklist.isPinned();
        boolean isNowPinned = !wasPinned;

        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);

        if (isNowPinned) {
            // If pinning a new checklist, first unpin any existing pinned checklist
            unpinExistingChecklist(checklistsGrid);
            
            // Now pin the new checklist
            checklist.setPinned(true);
            ChecklistManager.getInstance().updateChecklist(checklist);
            
            // Remove the old container and create a new one with updated pin status
            checklistsGrid.removeView(checklistContainer);
            View newChecklistContainer = createChecklistContainer(checklist);
            
            // Add pinned checklist at the top
            checklistsGrid.addView(newChecklistContainer, 0);
            Toast.makeText(getContext(), "Checklist pinned", Toast.LENGTH_SHORT).show();
        } else {
            // Unpinning the checklist
            checklist.setPinned(false);
            ChecklistManager.getInstance().updateChecklist(checklist);
            
            // Remove the old container and create a new one with updated pin status
            checklistsGrid.removeView(checklistContainer);
            View newChecklistContainer = createChecklistContainer(checklist);
            
            // Add unpinned checklist at the end
            checklistsGrid.addView(newChecklistContainer);
            Toast.makeText(getContext(), "Checklist unpinned", Toast.LENGTH_SHORT).show();
        }
    }

    private void unpinExistingChecklist(LinearLayout checklistsContainer) {
        // Find and unpin any existing pinned checklist
        for (int i = 0; i < checklistsContainer.getChildCount(); i++) {
            View child = checklistsContainer.getChildAt(i);
            if (child.getTag() instanceof Checklist) {
                Checklist existingChecklist = (Checklist) child.getTag();
                if (existingChecklist.isPinned()) {
                    // Unpin the existing checklist
                    existingChecklist.setPinned(false);
                    ChecklistManager.getInstance().updateChecklist(existingChecklist);
                    
                    // Remove the old container and create a new one
                    checklistsContainer.removeView(child);
                    View newChecklistContainer = createChecklistContainer(existingChecklist);
                    
                    // Add the unpinned checklist at the end
                    checklistsContainer.addView(newChecklistContainer);
                    
                    break; // Only one checklist can be pinned at a time
                }
            }
        }
    }

    private void loadExistingChecklists() {
        if (binding == null) return;
        
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        if (checklistsGrid == null) return;
        
        // Clear existing checklists from the grid
        checklistsGrid.removeAllViews();
        
        // Get all checklists from ChecklistManager
        List<Checklist> allChecklists = ChecklistManager.getInstance().getAllChecklists();
        
        if (allChecklists.isEmpty()) {
            // Show empty state
            binding.textChecklist.setVisibility(View.VISIBLE);
            return;
        }
        
        // Hide default text
        binding.textChecklist.setVisibility(View.GONE);
        
        // Sort checklists: pinned first, then by ID (most recent first)
        List<Checklist> sortedChecklists = new ArrayList<>(allChecklists);
        sortedChecklists.sort((checklist1, checklist2) -> {
            // Pinned checklists first
            if (checklist1.isPinned() && !checklist2.isPinned()) return -1;
            if (!checklist1.isPinned() && checklist2.isPinned()) return 1;
            
            // Then by ID (most recent first)
            return Long.compare(checklist2.getId(), checklist1.getId());
        });
        
        // Add checklists to grid in sorted order
        for (Checklist checklist : sortedChecklists) {
            View checklistContainer = createChecklistContainer(checklist);
            checklistsGrid.addView(checklistContainer);
        }
    }

    // Checklist class
    public static class Checklist {
        private String title;
        private List<Task> tasks;
        private boolean isPinned;
        private long id;

        public Checklist(String title, List<Task> tasks) {
            this.title = title;
            this.tasks = tasks;
            this.isPinned = false;
            this.id = System.currentTimeMillis(); // Simple ID generation
        }

        public Checklist(String title, List<Task> tasks, boolean isPinned, long id) {
            this.title = title;
            this.tasks = tasks;
            this.isPinned = isPinned;
            this.id = id;
        }

        public String getTitle() { return title; }
        public List<Task> getTasks() { return tasks; }
        public boolean isPinned() { return isPinned; }
        public long getId() { return id; }
        
        public void setPinned(boolean pinned) { this.isPinned = pinned; }
        public void setTitle(String title) { this.title = title; }
        public void setTasks(List<Task> tasks) { this.tasks = tasks; }

        // Task class
        public static class Task {
            private String text;
            private boolean completed;

            public Task(String text, boolean completed) {
                this.text = text;
                this.completed = completed;
            }

            public String getText() { return text; }
            public boolean isCompleted() { return completed; }
            
            public void setText(String text) { this.text = text; }
            public void setCompleted(boolean completed) { this.completed = completed; }
        }
    }
}
