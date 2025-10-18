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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.veb_app.R;
import com.example.veb_app.databinding.FragmentChecklistBinding;
import com.example.veb_app.data.DatabaseManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChecklistFragment extends Fragment {

    private FragmentChecklistBinding binding;
    private static boolean isFirstLoad = true;
    private boolean isCurrentlyLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Don't use setRetainInstance for navigation - it's for configuration changes only
        android.util.Log.d("ChecklistFragment", "onCreate: Fragment created");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ChecklistViewModel checklistViewModel =
                new ViewModelProvider(requireActivity()).get(ChecklistViewModel.class);

        binding = FragmentChecklistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Initialize ChecklistManager and TaskRepository if not already done
        if (getContext() != null) {
            ChecklistManager.getInstance().initialize(getContext());
            TaskRepository.getInstance().initialize(getContext());
            android.util.Log.d("ChecklistFragment", "ChecklistManager initialized with " + ChecklistManager.getInstance().getChecklistsCount() + " checklists");
            
            // Ensure TaskRepository is properly initialized
            if (TaskRepository.getInstance().getAllTaskStates() != null) {
                android.util.Log.d("ChecklistFragment", "TaskRepository initialized with " + TaskRepository.getInstance().getAllTaskStates().size() + " saved states");
            }
        }

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

        // Setup search functionality
        TextInputEditText etSearch = root.findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchChecklists(s.toString());
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // Load existing checklists from ChecklistManager
        loadExistingChecklists();
        
        // Log fragment state for debugging
        android.util.Log.d("ChecklistFragment", "onCreateView: Fragment view created, retain instance: " + getRetainInstance());
        
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("ChecklistFragment", "onResume: Fragment resumed");
        
        if (binding == null) {
            android.util.Log.d("ChecklistFragment", "onResume: Binding is null, skipping");
            return;
        }
        
        // Get ViewModel scoped to activity to maintain state across navigation
        ChecklistViewModel viewModel = new ViewModelProvider(requireActivity()).get(ChecklistViewModel.class);
        
        // Only reload if we don't have any checklists displayed or if data is not loaded
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        if (checklistsGrid == null || checklistsGrid.getChildCount() == 0 || !viewModel.isDataLoaded() || !viewModel.isFragmentLoaded()) {
            android.util.Log.d("ChecklistFragment", "onResume: Loading checklists (grid=" + (checklistsGrid != null) + ", count=" + (checklistsGrid != null ? checklistsGrid.getChildCount() : 0) + ", dataLoaded=" + viewModel.isDataLoaded() + ", fragmentLoaded=" + viewModel.isFragmentLoaded() + ")");
            loadExistingChecklists();
        } else {
            android.util.Log.d("ChecklistFragment", "onResume: Views exist, preserving state");
            // Even if views exist, ensure we're displaying the latest data from ChecklistManager
            refreshChecklistViews();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Save all task states to SharedPreferences when user navigates away
        android.util.Log.d("ChecklistFragment", "onPause: Saving all task states to SharedPreferences");
        if (binding != null) {
            saveAllTaskStatesToSharedPreferences();
        } else {
            android.util.Log.d("ChecklistFragment", "onPause: Binding is null, skipping save");
        }
    }
    
    /**
     * Save all task states to SharedPreferences using TaskRepository
     */
    private void saveAllTaskStatesToSharedPreferences() {
        if (binding == null) return;
        
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        if (checklistsGrid == null) return;
        
        // Iterate through all checklist containers and save their current task states
        for (int i = 0; i < checklistsGrid.getChildCount(); i++) {
            View checklistContainer = checklistsGrid.getChildAt(i);
            if (checklistContainer.getTag() != null) {
                Checklist checklist = (Checklist) checklistContainer.getTag();
                
                // Update the checklist data with current view states
                updateChecklistFromViews(checklist, checklistContainer);
                
                // Save task states to SharedPreferences
                TaskRepository.getInstance().saveAllTaskStates(checklist);
                
                // Also save to ChecklistManager for consistency
                ChecklistManager.getInstance().updateChecklist(checklist);
            }
        }
        
        // Force save all data to ensure persistence
        ChecklistManager.getInstance().forceSaveAllData();
    }
    
    /**
     * Save all checklist data to ensure persistence when navigating away
     */
    private void saveAllChecklistData() {
        if (binding == null) return;
        
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        if (checklistsGrid == null) return;
        
        // Iterate through all checklist containers and save their current state
        for (int i = 0; i < checklistsGrid.getChildCount(); i++) {
            View checklistContainer = checklistsGrid.getChildAt(i);
            if (checklistContainer.getTag() != null) {
                Checklist checklist = (Checklist) checklistContainer.getTag();
                
                // Update the checklist data with current view states
                updateChecklistFromViews(checklist, checklistContainer);
                
                // Save the updated checklist
                ChecklistManager.getInstance().updateChecklist(checklist);
            }
        }
        
        // Force save all data to ensure persistence
        ChecklistManager.getInstance().forceSaveAllData();
    }
    
    /**
     * Update checklist data from current view states
     */
    private void updateChecklistFromViews(Checklist checklist, View checklistContainer) {
        LinearLayout tasksContainer = checklistContainer.findViewById(R.id.tasks_container);
        if (tasksContainer == null) return;
        
        // Update each task's state from its view
        for (int i = 0; i < tasksContainer.getChildCount(); i++) {
            View taskView = tasksContainer.getChildAt(i);
            if (taskView.getTag() != null) {
                Checklist.Task task = (Checklist.Task) taskView.getTag();
                CheckBox cbTask = taskView.findViewById(R.id.cb_task);
                
                if (cbTask != null) {
                    // Update task state from checkbox
                    boolean currentChecked = cbTask.isChecked();
                    if (task.isChecked() != currentChecked) {
                        android.util.Log.d("ChecklistFragment", "Updating task state from view: '" + task.getText() + "' to: " + currentChecked);
                        task.setChecked(currentChecked);
                    }
                }
            }
        }
    }
    
    /**
     * Refresh checklist views to ensure they display the latest data from ChecklistManager
     */
    private void refreshChecklistViews() {
        if (binding == null) return;
        
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        if (checklistsGrid == null) return;
        
        // Get the latest data from ChecklistManager
        List<Checklist> allChecklists = ChecklistManager.getInstance().getAllChecklists();
        
        // Update each existing checklist container with the latest data
        for (int i = 0; i < checklistsGrid.getChildCount(); i++) {
            View checklistContainer = checklistsGrid.getChildAt(i);
            if (checklistContainer.getTag() != null) {
                Checklist existingChecklist = (Checklist) checklistContainer.getTag();
                
                // Find the corresponding checklist in the latest data
                Checklist latestChecklist = null;
                for (Checklist checklist : allChecklists) {
                    if (checklist.getId() == existingChecklist.getId()) {
                        latestChecklist = checklist;
                        break;
                    }
                }
                
                if (latestChecklist != null) {
                    // Load task states from SharedPreferences
                    TaskRepository.getInstance().loadAllTaskStates(latestChecklist);
                    
                    // Update the container's tag with the latest data
                    checklistContainer.setTag(latestChecklist);
                    
                    // Refresh the task views to match the latest data
                    refreshTaskViews(checklistContainer, latestChecklist);
                }
            }
        }
    }
    
    /**
     * Refresh task views to match the latest data
     */
    private void refreshTaskViews(View checklistContainer, Checklist checklist) {
        LinearLayout tasksContainer = checklistContainer.findViewById(R.id.tasks_container);
        if (tasksContainer == null) return;
        
        // Update each task view to match the latest data
        for (int i = 0; i < tasksContainer.getChildCount(); i++) {
            View taskView = tasksContainer.getChildAt(i);
            if (taskView.getTag() != null) {
                Checklist.Task task = (Checklist.Task) taskView.getTag();
                CheckBox cbTask = taskView.findViewById(R.id.cb_task);
                
                if (cbTask != null) {
                    // Update checkbox state to match the latest data
                    boolean latestChecked = task.isChecked();
                    if (cbTask.isChecked() != latestChecked) {
                        android.util.Log.d("ChecklistFragment", "Refreshing task view: '" + task.getText() + "' to: " + latestChecked);
                        cbTask.setOnCheckedChangeListener(null); // Clear listener temporarily
                        cbTask.setChecked(latestChecked);
                        // Reattach listener will be handled by the existing task creation methods
                    }
                }
            }
        }
    }
    
    /**
     * Force refresh the checklist display - use this when you know data has changed
     * This will clear all views and recreate them, so use sparingly
     */
    public void forceRefreshChecklists() {
        if (binding != null) {
            android.util.Log.d("ChecklistFragment", "Force refreshing checklists");
            LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
            if (checklistsGrid != null) {
                checklistsGrid.removeAllViews(); // Force clear all views
            }
            loadExistingChecklists();
        }
    }
    
    /**
     * Check for and fix corrupted task states
     * This method detects if all tasks are checked (which is likely a bug) and resets them
     */
    private void fixCorruptedTaskStates(Checklist checklist) {
        if (checklist == null || checklist.getTasks().isEmpty()) return;
        
        // Count how many tasks are checked
        int checkedCount = 0;
        for (Checklist.Task task : checklist.getTasks()) {
            if (task.isChecked()) {
                checkedCount++;
            }
        }
        
        // If all tasks are checked, this is likely a bug - reset them
        if (checkedCount == checklist.getTasks().size() && checklist.getTasks().size() > 1) {
            android.util.Log.w("ChecklistFragment", "Detected corrupted task states in checklist: " + checklist.getTitle() + " - resetting all tasks to unchecked");
            TaskRepository.getInstance().resetChecklistTaskStates(checklist);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        android.util.Log.d("ChecklistFragment", "onDestroyView: Fragment view destroyed");
        // Set binding to null when view is destroyed to prevent crashes
        // The fragment instance is retained, but the view is destroyed
        binding = null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        android.util.Log.d("ChecklistFragment", "onDestroy: Fragment destroyed");
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
        LinearLayout tasksContainer = dialogView.findViewById(R.id.tasks_container);
        LinearLayout btnAddNewTask = dialogView.findViewById(R.id.btn_add_new_task);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // List to track tasks in dialog
        List<Checklist.Task> dialogTasks = new ArrayList<>();

        // Pre-fill fields if editing existing checklist
        if (existingChecklist != null) {
            etTitle.setText(existingChecklist.getTitle());
            dialogTasks.addAll(existingChecklist.getTasks());
        } else {
            // For new checklist, add one blank task
            dialogTasks.add(new Checklist.Task("", false));
        }

        // Populate tasks container
        refreshTasksInDialog(tasksContainer, dialogTasks, btnAddNewTask);

        // Add new task button
        btnAddNewTask.setOnClickListener(v -> {
            dialogTasks.add(new Checklist.Task("", false));
            refreshTasksInDialog(tasksContainer, dialogTasks, btnAddNewTask);
            
            // Focus on the new blank task instead of title
            if (!dialogTasks.isEmpty()) {
                View lastTaskView = tasksContainer.getChildAt(tasksContainer.getChildCount() - 1);
                if (lastTaskView != null) {
                    TextInputEditText etTaskText = lastTaskView.findViewById(R.id.et_task_text);
                    if (etTaskText != null) {
                        etTaskText.requestFocus();
                        // Show keyboard
                        if (getActivity() != null) {
                            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.showSoftInput(etTaskText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                            }
                        }
                    }
                }
            }
        });

        // Save button
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a checklist title", Toast.LENGTH_SHORT).show();
                return;
            }

            // Filter out empty tasks
            List<Checklist.Task> validTasks = new ArrayList<>();
            for (Checklist.Task task : dialogTasks) {
                if (!task.getText().trim().isEmpty()) {
                    validTasks.add(task);
                }
            }

            if (validTasks.isEmpty()) {
                Toast.makeText(getContext(), "Please enter at least one task", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (existingChecklist != null) {
                // Update existing checklist
                existingChecklist.setTitle(title);
                existingChecklist.setTasks(validTasks);
                ChecklistManager.getInstance().updateChecklist(existingChecklist);
                updateChecklistDisplay(existingChecklist);
                Toast.makeText(getContext(), "Checklist updated: " + title, Toast.LENGTH_SHORT).show();
            } else {
                // Create new checklist
                Checklist newChecklist = new Checklist(title, validTasks);
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

    private void refreshTasksInDialog(LinearLayout tasksContainer, List<Checklist.Task> dialogTasks, LinearLayout btnAddNewTask) {
        tasksContainer.removeAllViews();
        
        for (int i = 0; i < dialogTasks.size(); i++) {
            Checklist.Task task = dialogTasks.get(i);
            View taskView = createDialogTaskView(task, dialogTasks, tasksContainer, btnAddNewTask, i);
            tasksContainer.addView(taskView);
        }
    }

    private View createDialogTaskView(Checklist.Task task, List<Checklist.Task> dialogTasks, LinearLayout tasksContainer, LinearLayout btnAddNewTask, int index) {
        View taskView = LayoutInflater.from(getContext()).inflate(R.layout.item_checklist_task_editable, null);
        
        CheckBox cbTask = taskView.findViewById(R.id.cb_task);
        TextInputEditText etTaskText = taskView.findViewById(R.id.et_task_text);
        TextView btnDeleteTask = taskView.findViewById(R.id.btn_delete_task);
        
        // Store the task reference in the view tag for debugging and verification
        taskView.setTag(task);
        
        // Clear any existing listener first
        cbTask.setOnCheckedChangeListener(null);
        
        // Setup checkbox state from data model
        cbTask.setChecked(task.isChecked());
        cbTask.setEnabled(true);
        cbTask.setAlpha(1.0f);
        
        // Setup text input
        etTaskText.setText(task.getText());
        
        // Setup delete button
        btnDeleteTask.setOnClickListener(v -> {
            // Remove the task from the list
            dialogTasks.remove(task);
            // Refresh the dialog to update indices
            refreshTasksInDialog(tasksContainer, dialogTasks, btnAddNewTask);
        });
        
        // Function to update delete button visibility
        android.util.Log.d("ChecklistFragment", "Initial task text: '" + task.getText() + "'");
        updateDeleteButtonVisibility(btnDeleteTask, task.getText());
        
        // Set up text change listener
        etTaskText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                task.setText(s.toString());
                // Update delete button visibility based on text content
                updateDeleteButtonVisibility(btnDeleteTask, s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        // Set up checkbox listener AFTER setting the initial state
        cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Verify this is the correct task before updating
            Checklist.Task currentTask = (Checklist.Task) taskView.getTag();
            if (currentTask == task && isChecked != task.isChecked()) {
                task.setChecked(isChecked);
                
                // Save task state to SharedPreferences immediately
                String taskId = "dialog_" + System.currentTimeMillis() + "_" + dialogTasks.indexOf(task);
                TaskRepository.getInstance().saveTaskState(taskId, isChecked);
            }
        });
        
        // Handle enter key to add new task
        etTaskText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE || 
                (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                
                // Add new task after current one
                dialogTasks.add(index + 1, new Checklist.Task("", false));
                refreshTasksInDialog(tasksContainer, dialogTasks, btnAddNewTask);
                
                // Focus on the new task
                View newTaskView = tasksContainer.getChildAt(index + 1);
                if (newTaskView != null) {
                    TextInputEditText newEtTaskText = newTaskView.findViewById(R.id.et_task_text);
                    newEtTaskText.requestFocus();
                }
                return true;
            }
            return false;
        });
        
        return taskView;
    }

    private void updateDeleteButtonVisibility(TextView btnDeleteTask, String taskText) {
        if (taskText != null && !taskText.trim().isEmpty()) {
            // Show minus button when user has typed something
            btnDeleteTask.setVisibility(View.VISIBLE);
        } else {
            // Hide minus button when task is empty
            btnDeleteTask.setVisibility(View.GONE);
        }
    }

    private void addChecklistToDisplay(Checklist checklist) {
        if (binding == null) return;
        
        // Hide the default text
        binding.textChecklist.setVisibility(View.GONE);

        // Create checklist container
        View checklistContainer = createChecklistContainer(checklist);
        
        // Add to the checklists grid at the beginning (most recent first)
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        checklistsGrid.addView(checklistContainer, 0); // Insert at position 0 (top)
    }

    private void updateChecklistDisplay(Checklist checklist) {
        if (binding == null) return;
        
        // Find and update the existing checklist container
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        
        // Find the checklist container by looking for the one with matching checklist data
        for (int i = 0; i < checklistsGrid.getChildCount(); i++) {
            View child = checklistsGrid.getChildAt(i);
            if (child.getTag() != null) {
                Checklist existingChecklist = (Checklist) child.getTag();
                if (existingChecklist.getId() == checklist.getId()) {
                    // Instead of recreating the entire container, just update the progress display
                    // This prevents checkbox state issues
                    updateProgressDisplay(child, checklist);
                    android.util.Log.d("ChecklistFragment", "Updated progress display for checklist: " + checklist.getTitle());
                    break;
                }
            }
        }
    }
    
    /**
     * Update only the progress display without recreating the entire checklist container
     */
    private void updateProgressDisplay(View checklistContainer, Checklist checklist) {
        LinearLayout progressContainer = checklistContainer.findViewById(R.id.progress_container);
        TextView tvProgressText = checklistContainer.findViewById(R.id.tv_progress_text);
        com.google.android.material.progressindicator.LinearProgressIndicator progressBar = checklistContainer.findViewById(R.id.progress_bar);
        
        if (progressContainer != null && tvProgressText != null && progressBar != null) {
            if (!checklist.getTasks().isEmpty()) {
                int completedCount = 0;
                for (Checklist.Task task : checklist.getTasks()) {
                    if (task.isChecked()) completedCount++;
                }
                
                int totalTasks = checklist.getTasks().size();
                int progress = totalTasks > 0 ? (completedCount * 100) / totalTasks : 0;
                
                tvProgressText.setText(completedCount + " of " + totalTasks + " completed");
                progressBar.setProgress(progress);
                progressContainer.setVisibility(View.VISIBLE);
            } else {
                progressContainer.setVisibility(View.GONE);
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
        LinearLayout btnAddTask = cardView.findViewById(R.id.btn_add_task);
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

        // Setup add task button
        btnAddTask.setOnClickListener(v -> {
            addNewTaskToChecklist(checklist, tasksContainer, btnAddTask);
        });

        // Load task states from SharedPreferences before creating views
        TaskRepository.getInstance().loadAllTaskStates(checklist);
        
        // Add tasks to container
        List<Checklist.Task> sortedTasks = new ArrayList<>(checklist.getTasks());
        // Sort tasks for display only (don't modify original data)
        Collections.sort(sortedTasks, (task1, task2) -> {
            // Incomplete tasks first
            if (!task1.isCompleted() && task2.isCompleted()) return -1;
            if (task1.isCompleted() && !task2.isCompleted()) return 1;
            
            // Within same completion status, maintain original order
            return 0; // Keep original insertion order for tasks
        });

        // Clear existing tasks
        tasksContainer.removeAllViews();

        for (Checklist.Task task : sortedTasks) {
            View taskView = createTaskView(task, checklist);
            tasksContainer.addView(taskView);
        }

        // Show add task button if there are tasks or if checklist is empty
        if (!checklist.getTasks().isEmpty()) {
            btnAddTask.setVisibility(View.VISIBLE);
        } else {
            btnAddTask.setVisibility(View.VISIBLE);
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
        
        // Store the task reference in the view tag for debugging and verification
        taskView.setTag(task);
        
        // Debug: Log task state when creating view
        android.util.Log.d("ChecklistFragment", "Creating task view for: '" + task.getText() + "' checked: " + task.isChecked());
        
        // Handle empty tasks (blank indicators)
        if (task.getText().isEmpty()) {
            // Show blank task indicator
            tvTaskText.setText(" ");
            tvTaskText.setTextColor(getResources().getColor(R.color.md_theme_light_onSurfaceVariant));
            tvTaskText.setAlpha(0.3f);
            cbTask.setEnabled(false);
            cbTask.setAlpha(0.3f);
            cbTask.setOnCheckedChangeListener(null); // Clear listener for empty tasks
        } else {
            // Set task text with Google Keep styling
            if (task.isChecked()) {
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
            
            // CRITICAL: Clear any existing listener first to prevent triggering during setup
            cbTask.setOnCheckedChangeListener(null);
            
            // Set checkbox state from data model (this should NOT trigger listener)
            cbTask.setChecked(task.isChecked());
            cbTask.setEnabled(true);
            cbTask.setAlpha(1.0f);
            
            // Set checkbox listener AFTER setting the state
            cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Verify this is the correct task before updating
                Checklist.Task currentTask = (Checklist.Task) taskView.getTag();
                if (currentTask == task && isChecked != task.isChecked()) {
                    android.util.Log.d("ChecklistFragment", "Checkbox changed for task: '" + task.getText() + "' to: " + isChecked);
                    task.setChecked(isChecked);
                    
                    // Save task state to SharedPreferences immediately
                    String taskId = checklist.getId() + "_" + checklist.getTasks().indexOf(task);
                    TaskRepository.getInstance().saveTaskState(taskId, isChecked);
                    
                    ChecklistManager.getInstance().updateChecklist(checklist);
                    
                    // Refresh the display to reorder tasks and update progress
                    updateChecklistDisplay(checklist);
                }
            });
        }
        
        return taskView;
    }

    private void addNewTaskToChecklist(Checklist checklist, LinearLayout tasksContainer, LinearLayout btnAddTask) {
        // Create a new empty task
        Checklist.Task newTask = new Checklist.Task("", false);
        checklist.getTasks().add(newTask);
        
        // Create editable task view
        View editableTaskView = createEditableTaskView(newTask, checklist, tasksContainer, btnAddTask);
        
        // Add to tasks container
        tasksContainer.addView(editableTaskView);
        
        // Hide add button temporarily
        btnAddTask.setVisibility(View.GONE);
        
        // Focus on the text input
        TextInputEditText etTaskText = editableTaskView.findViewById(R.id.et_task_text);
        etTaskText.requestFocus();
        
        // Show keyboard
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etTaskText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private View createEditableTaskView(Checklist.Task task, Checklist checklist, LinearLayout tasksContainer, LinearLayout btnAddTask) {
        View taskView = LayoutInflater.from(getContext()).inflate(R.layout.item_checklist_task_editable, null);
        
        CheckBox cbTask = taskView.findViewById(R.id.cb_task);
        TextInputEditText etTaskText = taskView.findViewById(R.id.et_task_text);
        
        // Store the task reference in the view tag for debugging and verification
        taskView.setTag(task);
        
        // Clear any existing listener first
        cbTask.setOnCheckedChangeListener(null);
        
        // Setup checkbox state from data model
        cbTask.setChecked(task.isChecked());
        cbTask.setEnabled(true);
        cbTask.setAlpha(1.0f);
        
        // Setup text input
        etTaskText.setText(task.getText());
        
        // Set up text change listener
        etTaskText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                task.setText(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        // Set up checkbox listener AFTER setting the initial state
        cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Verify this is the correct task before updating
            Checklist.Task currentTask = (Checklist.Task) taskView.getTag();
            if (currentTask == task && isChecked != task.isChecked()) {
                task.setChecked(isChecked);
                
                // Save task state to SharedPreferences immediately
                String taskId = checklist.getId() + "_" + checklist.getTasks().indexOf(task);
                TaskRepository.getInstance().saveTaskState(taskId, isChecked);
                
                ChecklistManager.getInstance().updateChecklist(checklist);
                updateChecklistDisplay(checklist);
            }
        });
        
        // Handle enter key and focus loss
        etTaskText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE || 
                (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                
                // If task has text, add a new empty task
                if (!task.getText().trim().isEmpty()) {
                    addNewTaskToChecklist(checklist, tasksContainer, btnAddTask);
                }
                return true;
            }
            return false;
        });
        
        // Handle focus loss
        etTaskText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // If task is empty, remove it
                if (task.getText().trim().isEmpty()) {
                    checklist.getTasks().remove(task);
                    tasksContainer.removeView(taskView);
                    ChecklistManager.getInstance().updateChecklist(checklist);
                    updateChecklistDisplay(checklist);
                } else {
                    // Show add button again
                    btnAddTask.setVisibility(View.VISIBLE);
                }
            }
        });
        
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
        
        // Prevent multiple simultaneous loads
        if (isCurrentlyLoading) {
            android.util.Log.d("ChecklistFragment", "Already loading, skipping duplicate load");
            return;
        }
        
        isCurrentlyLoading = true;
        android.util.Log.d("ChecklistFragment", "Starting to load existing checklists");
        
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        if (checklistsGrid == null) {
            isCurrentlyLoading = false;
            return;
        }
        
        // Only clear and recreate views if the grid is empty or if we need to refresh
        boolean needsRefresh = checklistsGrid.getChildCount() == 0;
        
        if (needsRefresh) {
            // Clear existing checklists from the grid only if needed
            checklistsGrid.removeAllViews();
            
            // Get all checklists from ChecklistManager (shared singleton instance)
            List<Checklist> allChecklists = ChecklistManager.getInstance().getAllChecklists();
            
            android.util.Log.d("ChecklistFragment", "Loading existing checklists. Count: " + allChecklists.size());
            
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
            
            // Debug: Log all task states before creating views
            for (Checklist checklist : sortedChecklists) {
                android.util.Log.d("ChecklistFragment", "Checklist: " + checklist.getTitle());
                for (Checklist.Task task : checklist.getTasks()) {
                    android.util.Log.d("ChecklistFragment", "Task: '" + task.getText() + "' checked: " + task.isChecked());
                }
            }
            
        // Add checklists to grid in sorted order
        for (Checklist checklist : sortedChecklists) {
            // Check for and fix any corrupted task states
            fixCorruptedTaskStates(checklist);
            
            // Load task states from SharedPreferences before creating views
            TaskRepository.getInstance().loadAllTaskStates(checklist);
            View checklistContainer = createChecklistContainer(checklist);
            checklistsGrid.addView(checklistContainer);
        }
        
        // Update ViewModel with loaded data
        ChecklistViewModel viewModel = new ViewModelProvider(requireActivity()).get(ChecklistViewModel.class);
        viewModel.setChecklists(sortedChecklists);
        viewModel.setFragmentLoaded(true);
        } else {
            android.util.Log.d("ChecklistFragment", "Views already exist, skipping recreation to preserve state");
        }
        
        isCurrentlyLoading = false;
        android.util.Log.d("ChecklistFragment", "Finished loading existing checklists");
    }

    private void searchChecklists(String query) {
        if (binding == null) return;
        
        LinearLayout checklistsGrid = binding.getRoot().findViewById(R.id.checklists_grid);
        if (checklistsGrid == null) return;
        
        // Clear existing checklists from the grid
        checklistsGrid.removeAllViews();
        
        // Get all checklists from ChecklistManager
        List<Checklist> allChecklists = ChecklistManager.getInstance().getAllChecklists();
        
        if (query.trim().isEmpty()) {
            // No search query, show all checklists
            loadExistingChecklists();
            return;
        }
        
        // Filter checklists based on search query
        List<Checklist> filteredChecklists = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (Checklist checklist : allChecklists) {
            // Search in title
            if (checklist.getTitle().toLowerCase().contains(lowerQuery)) {
                filteredChecklists.add(checklist);
            } else {
                // Search in task text
                for (Checklist.Task task : checklist.getTasks()) {
                    if (task.getText().toLowerCase().contains(lowerQuery)) {
                        filteredChecklists.add(checklist);
                        break; // Add checklist only once even if multiple tasks match
                    }
                }
            }
        }
        
        if (filteredChecklists.isEmpty()) {
            // Show empty state
            binding.textChecklist.setVisibility(View.VISIBLE);
            return;
        }
        
        // Hide default text
        binding.textChecklist.setVisibility(View.GONE);
        
        // Sort checklists: pinned first, then by ID (most recent first)
        filteredChecklists.sort((checklist1, checklist2) -> {
            // Pinned checklists first
            if (checklist1.isPinned() && !checklist2.isPinned()) return -1;
            if (!checklist1.isPinned() && checklist2.isPinned()) return 1;
            
            // Then by ID (most recent first)
            return Long.compare(checklist2.getId(), checklist1.getId());
        });
        
        // Add filtered checklists to grid
        for (Checklist checklist : filteredChecklists) {
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

        // Default constructor for JSON deserialization
        public Checklist() {
            this.title = "";
            this.tasks = new ArrayList<>();
            this.isPinned = false;
            this.id = System.currentTimeMillis();
        }

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
            private boolean taskCompleted; // Rename field to avoid conflicts

            // Default constructor for JSON deserialization
            public Task() {
                this.text = "";
                this.taskCompleted = false;
            }

            public Task(String text, boolean completed) {
                this.text = text;
                this.taskCompleted = completed;
            }

            public String getText() { return text; }
            public boolean isCompleted() { return taskCompleted; }
            public boolean isChecked() { return taskCompleted; } // Alias for clarity
            
            public void setText(String text) { this.text = text; }
            public void setCompleted(boolean completed) { this.taskCompleted = completed; }
            public void setChecked(boolean checked) { this.taskCompleted = checked; } // Alias for clarity
            
            // Gson getters/setters
            public boolean getTaskCompleted() { return taskCompleted; }
            public void setTaskCompleted(boolean taskCompleted) { this.taskCompleted = taskCompleted; }
        }
    }
}
