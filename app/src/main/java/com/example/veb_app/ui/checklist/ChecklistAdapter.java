package com.example.veb_app.ui.checklist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.veb_app.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for displaying checklists in a RecyclerView.
 * This adapter handles the display of checklist cards with tasks and progress indicators.
 */
public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder> {
    
    private List<ChecklistFragment.Checklist> checklists;
    private OnChecklistClickListener clickListener;
    private OnTaskClickListener taskClickListener;
    
    public interface OnChecklistClickListener {
        void onChecklistClick(ChecklistFragment.Checklist checklist);
        void onChecklistLongClick(ChecklistFragment.Checklist checklist, View view);
    }
    
    public interface OnTaskClickListener {
        void onTaskClick(ChecklistFragment.Checklist.Task task, ChecklistFragment.Checklist checklist);
        void onTaskCheckboxClick(ChecklistFragment.Checklist.Task task, ChecklistFragment.Checklist checklist, boolean isChecked);
    }
    
    public ChecklistAdapter() {
        this.checklists = new ArrayList<>();
    }
    
    public void setOnChecklistClickListener(OnChecklistClickListener listener) {
        this.clickListener = listener;
    }
    
    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.taskClickListener = listener;
    }
    
    @NonNull
    @Override
    public ChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checklist_card, parent, false);
        return new ChecklistViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChecklistViewHolder holder, int position) {
        ChecklistFragment.Checklist checklist = checklists.get(position);
        android.util.Log.d("ChecklistAdapter", "Binding checklist at position " + position + ": " + checklist.getTitle());
        holder.bind(checklist);
    }
    
    @Override
    public void onViewRecycled(@NonNull ChecklistViewHolder holder) {
        super.onViewRecycled(holder);
        // Only clear listeners, don't remove views to preserve state
        holder.clearTaskListeners();
        android.util.Log.d("ChecklistAdapter", "View recycled, cleared task listeners only");
    }
    
    @Override
    public int getItemCount() {
        return checklists.size();
    }
    
    public void updateChecklists(List<ChecklistFragment.Checklist> newChecklists) {
        // Use DiffUtil for better performance, but for now use clear and add
        int oldSize = this.checklists.size();
        this.checklists.clear();
        notifyItemRangeRemoved(0, oldSize);
        
        this.checklists.addAll(newChecklists);
        notifyItemRangeInserted(0, newChecklists.size());
    }
    
    public void addChecklist(ChecklistFragment.Checklist checklist) {
        checklists.add(0, checklist); // Add at the beginning
        notifyItemInserted(0);
    }
    
    public void updateChecklist(ChecklistFragment.Checklist checklist) {
        for (int i = 0; i < checklists.size(); i++) {
            if (checklists.get(i).getId() == checklist.getId()) {
                checklists.set(i, checklist);
                notifyItemChanged(i);
                break;
            }
        }
    }
    
    public void removeChecklist(ChecklistFragment.Checklist checklist) {
        for (int i = 0; i < checklists.size(); i++) {
            if (checklists.get(i).getId() == checklist.getId()) {
                checklists.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }
    
    public List<ChecklistFragment.Checklist> getChecklists() {
        return new ArrayList<>(checklists);
    }
    
    class ChecklistViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardChecklist;
        private TextView tvTitle;
        private LinearLayout tasksContainer;
        private LinearLayout btnAddTask;
        private LinearLayout progressContainer;
        private TextView tvProgressText;
        private LinearProgressIndicator progressBar;
        
        public ChecklistViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardChecklist = itemView.findViewById(R.id.card_checklist);
            tvTitle = itemView.findViewById(R.id.tv_checklist_title);
            tasksContainer = itemView.findViewById(R.id.tasks_container);
            btnAddTask = itemView.findViewById(R.id.btn_add_task);
            progressContainer = itemView.findViewById(R.id.progress_container);
            tvProgressText = itemView.findViewById(R.id.tv_progress_text);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
        
        public void bind(ChecklistFragment.Checklist checklist) {
            // Store the checklist reference for debugging
            itemView.setTag(checklist);
            
            android.util.Log.d("ChecklistAdapter", "Binding checklist: " + checklist.getTitle());
            
            // Debug: Log task states from data model (don't modify them)
            for (ChecklistFragment.Checklist.Task task : checklist.getTasks()) {
                android.util.Log.d("ChecklistAdapter", "Task from data model: '" + task.getText() + "' checked: " + task.isChecked());
            }
            
            // Set title
            tvTitle.setText(checklist.getTitle());
            
            // Set different background color for pinned checklists
            if (checklist.isPinned()) {
                cardChecklist.setCardBackgroundColor(itemView.getContext()
                        .getResources().getColor(R.color.pinned_checklist_background));
            } else {
                cardChecklist.setCardBackgroundColor(itemView.getContext()
                        .getResources().getColor(R.color.md_theme_light_surface));
            }
            
            // Setup add task button
            btnAddTask.setOnClickListener(v -> {
                if (taskClickListener != null) {
                    // This would need to be handled by the fragment
                    // For now, we'll just show the button
                }
            });
            
            // CRITICAL: Clear existing tasks and their listeners first
            clearTaskViews();
            
            // Add tasks to container
            List<ChecklistFragment.Checklist.Task> sortedTasks = new ArrayList<>(checklist.getTasks());
            // Sort tasks for display: incomplete first, then completed
            Collections.sort(sortedTasks, (task1, task2) -> {
                if (!task1.isChecked() && task2.isChecked()) return -1;
                if (task1.isChecked() && !task2.isChecked()) return 1;
                return 0; // Keep original order within same completion status
            });
            
            for (ChecklistFragment.Checklist.Task task : sortedTasks) {
                View taskView = createTaskView(task, checklist);
                tasksContainer.addView(taskView);
            }
            
            // Show add task button
            btnAddTask.setVisibility(View.VISIBLE);
            
            // Show progress if there are tasks
            if (!checklist.getTasks().isEmpty()) {
                int completedCount = 0;
                for (ChecklistFragment.Checklist.Task task : checklist.getTasks()) {
                    if (task.isChecked()) completedCount++;
                }
                
                int totalTasks = checklist.getTasks().size();
                int progress = totalTasks > 0 ? (completedCount * 100) / totalTasks : 0;
                
                tvProgressText.setText(itemView.getContext().getString(
                        R.string.checklist_progress_format, completedCount, totalTasks));
                progressBar.setProgress(progress);
                progressContainer.setVisibility(View.VISIBLE);
            } else {
                progressContainer.setVisibility(View.GONE);
            }
            
            // Set click listeners
            cardChecklist.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onChecklistClick(checklist);
                }
            });
            
            cardChecklist.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onChecklistLongClick(checklist, v);
                }
                return true;
            });
        }
        
        private View createTaskView(ChecklistFragment.Checklist.Task task, ChecklistFragment.Checklist checklist) {
            View taskView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.item_checklist_task, tasksContainer, false);
            
            CheckBox cbTask = taskView.findViewById(R.id.cb_task);
            TextView tvTaskText = taskView.findViewById(R.id.tv_task_text);
            
            // Store the task reference in the view tag for debugging and verification
            taskView.setTag(task);
            
            // Handle empty tasks (blank indicators)
            if (task.getText().isEmpty()) {
                tvTaskText.setText(" ");
                tvTaskText.setTextColor(itemView.getContext().getResources()
                        .getColor(R.color.md_theme_light_onSurfaceVariant));
                tvTaskText.setAlpha(0.3f);
                cbTask.setEnabled(false);
                cbTask.setAlpha(0.3f);
                cbTask.setOnCheckedChangeListener(null); // Clear listener for empty tasks
            } else {
                // Set task text with proper styling
                if (task.isChecked()) {
                    // Apply strikethrough for completed tasks
                    tvTaskText.setText(task.getText());
                    tvTaskText.setTextColor(itemView.getContext().getResources()
                            .getColor(R.color.md_theme_light_onSurfaceVariant));
                    tvTaskText.setAlpha(0.5f);
                    // Note: StrikethroughSpan would need to be applied here
                } else {
                    tvTaskText.setText(task.getText());
                    tvTaskText.setTextColor(itemView.getContext().getResources()
                            .getColor(R.color.md_theme_light_onSurface));
                    tvTaskText.setAlpha(1.0f);
                }
                
                // CRITICAL: Remove any existing listener first to prevent triggering during setup
                cbTask.setOnCheckedChangeListener(null);
                
                // Set checkbox state from data model (this should NOT trigger listener)
                boolean taskChecked = task.isChecked();
                cbTask.setChecked(taskChecked);
                cbTask.setEnabled(true);
                cbTask.setAlpha(1.0f);
                
                android.util.Log.d("ChecklistAdapter", "Setting checkbox for task: '" + task.getText() + "' to: " + taskChecked);
                
                // Set checkbox listener AFTER setting the state
                cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    // Verify this is the correct task before updating
                    ChecklistFragment.Checklist.Task currentTask = (ChecklistFragment.Checklist.Task) taskView.getTag();
                    if (currentTask == task && isChecked != task.isChecked()) {
                        android.util.Log.d("ChecklistAdapter", "Checkbox changed for task: '" + task.getText() + "' to: " + isChecked);
                        // Update the task model
                        task.setChecked(isChecked);
                        
                        // Save task state to SharedPreferences immediately
                        // Use the same ID generation logic as TaskRepository
                        int taskIndex = checklist.getTasks().indexOf(task);
                        String taskId = checklist.getId() + "_" + taskIndex;
                        android.util.Log.d("ChecklistAdapter", "Saving task state for task at index " + taskIndex + " with ID: " + taskId);
                        TaskRepository.getInstance().saveTaskState(taskId, isChecked);
                        
                        // Notify listener if available
                        if (taskClickListener != null) {
                            taskClickListener.onTaskCheckboxClick(task, checklist, isChecked);
                        }
                        // Update progress display
                        updateProgressDisplay(checklist);
                    }
                });
                
                // Set task click listener
                taskView.setOnClickListener(v -> {
                    if (taskClickListener != null) {
                        taskClickListener.onTaskClick(task, checklist);
                    }
                });
            }
            
            return taskView;
        }
        
        /**
         * Clear all task views and their listeners to prevent memory leaks and cross-task interference
         */
        public void clearTaskViews() {
            // Clear all existing task views and their listeners
            for (int i = 0; i < tasksContainer.getChildCount(); i++) {
                View taskView = tasksContainer.getChildAt(i);
                if (taskView != null) {
                    CheckBox cbTask = taskView.findViewById(R.id.cb_task);
                    if (cbTask != null) {
                        // Clear checkbox listener to prevent memory leaks
                        cbTask.setOnCheckedChangeListener(null);
                    }
                    // Clear task view click listener
                    taskView.setOnClickListener(null);
                    // Clear task view tag
                    taskView.setTag(null);
                }
            }
            // Remove all views from container
            tasksContainer.removeAllViews();
        }
        
        /**
         * Clear only task listeners without removing views to preserve state
         */
        public void clearTaskListeners() {
            // Clear only listeners, don't remove views
            for (int i = 0; i < tasksContainer.getChildCount(); i++) {
                View taskView = tasksContainer.getChildAt(i);
                if (taskView != null) {
                    CheckBox cbTask = taskView.findViewById(R.id.cb_task);
                    if (cbTask != null) {
                        // Clear checkbox listener to prevent memory leaks
                        cbTask.setOnCheckedChangeListener(null);
                    }
                    // Clear task view click listener
                    taskView.setOnClickListener(null);
                }
            }
        }
        
        /**
         * Update the progress display for the checklist
         */
        private void updateProgressDisplay(ChecklistFragment.Checklist checklist) {
            if (!checklist.getTasks().isEmpty()) {
                int completedCount = 0;
                for (ChecklistFragment.Checklist.Task task : checklist.getTasks()) {
                    if (task.isChecked()) completedCount++;
                }
                
                int totalTasks = checklist.getTasks().size();
                int progress = totalTasks > 0 ? (completedCount * 100) / totalTasks : 0;
                
                tvProgressText.setText(itemView.getContext().getString(
                        R.string.checklist_progress_format, completedCount, totalTasks));
                progressBar.setProgress(progress);
                progressContainer.setVisibility(View.VISIBLE);
            } else {
                progressContainer.setVisibility(View.GONE);
            }
        }
    }
}
