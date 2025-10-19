package com.example.veb_app.ui.todo;

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
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.veb_app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying to-do items in RecyclerView
 */
public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<TodoItem> todoItems;
    private OnTodoClickListener listener;

    public interface OnTodoClickListener {
        void onTodoClick(TodoItem todo);
        void onTodoLongClick(TodoItem todo, View view);
        void onTaskCheckboxClick(TodoItem todo, TodoItem.TodoTask task, boolean isChecked);
    }

    public TodoAdapter(List<TodoItem> todoItems, OnTodoClickListener listener) {
        this.todoItems = todoItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem todo = todoItems.get(position);
        holder.bind(todo);
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    public void updateTodos(List<TodoItem> newTodos) {
        this.todoItems = newTodos;
        notifyDataSetChanged();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDeadline;
        private LinearLayout tasksContainer;
        private TextView tvProgress;
        private ImageView ivPin;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_todo_title);
            tvDeadline = itemView.findViewById(R.id.tv_deadline);
            tasksContainer = itemView.findViewById(R.id.tasks_container);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            ivPin = itemView.findViewById(R.id.iv_pin);
        }

        public void bind(TodoItem todo) {
            // Set title
            tvTitle.setText(todo.getTitle());
            
            // Set deadline
            if (todo.getDeadline() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String deadlineText = "Due: " + sdf.format(new Date(todo.getDeadline()));
                tvDeadline.setText(deadlineText);
                tvDeadline.setVisibility(View.VISIBLE);
            } else {
                tvDeadline.setVisibility(View.GONE);
            }
            
            // Set pin state and styling
            if (todo.isPinned()) {
                ivPin.setVisibility(View.VISIBLE);
                ivPin.setImageResource(R.drawable.ic_paper_pin_24dp);
                // Apply pinned styling to MaterialCardView
                if (itemView instanceof com.google.android.material.card.MaterialCardView) {
                    com.google.android.material.card.MaterialCardView cardView = (com.google.android.material.card.MaterialCardView) itemView;
                    cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.pinned_todo_background, null));
                    cardView.setCardElevation(6f); // Higher elevation for pinned items
                }
            } else {
                ivPin.setVisibility(View.GONE);
                // Reset to default styling
                if (itemView instanceof com.google.android.material.card.MaterialCardView) {
                    com.google.android.material.card.MaterialCardView cardView = (com.google.android.material.card.MaterialCardView) itemView;
                    cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.md_theme_light_surface, null));
                    cardView.setCardElevation(4f); // Default elevation
                }
            }
            
            // Update progress
            updateProgress(todo);
            
            // Update tasks (read-only display)
            updateTasks(todo);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTodoClick(todo);
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTodoLongClick(todo, itemView);
                }
                return true;
            });
        }
        
        
        private void updateProgress(TodoItem todo) {
            int totalTasks = todo.getTotalTasksCount();
            int completedTasks = todo.getCompletedTasksCount();
            
            if (totalTasks > 0) {
                tvProgress.setText(completedTasks + "/" + totalTasks + " tasks");
                tvProgress.setVisibility(View.VISIBLE);
            } else {
                tvProgress.setVisibility(View.GONE);
            }
        }
        
        private void updateTasks(TodoItem todo) {
            tasksContainer.removeAllViews();
            
            if (todo.getTasks().isEmpty()) {
                tasksContainer.setVisibility(View.GONE);
            } else {
                tasksContainer.setVisibility(View.VISIBLE);
                
                // Sort tasks: unchecked first, then checked
                List<TodoItem.TodoTask> sortedTasks = new java.util.ArrayList<>(todo.getTasks());
                sortedTasks.sort((task1, task2) -> {
                    if (task1.isCompleted() == task2.isCompleted()) {
                        return 0; // Keep original order if both have same completion status
                    }
                    return task1.isCompleted() ? 1 : -1; // Unchecked tasks first
                });
                
                for (TodoItem.TodoTask task : sortedTasks) {
                    View taskView = createTaskView(task, todo);
                    tasksContainer.addView(taskView);
                }
            }
        }
        
        private View createTaskView(TodoItem.TodoTask task, TodoItem todo) {
            View taskView = LayoutInflater.from(itemView.getContext())
                .inflate(R.layout.item_todo_task, tasksContainer, false);
            
            CheckBox cbTask = taskView.findViewById(R.id.cb_task);
            TextView tvTaskText = taskView.findViewById(R.id.tv_task_text);
            
            // Make tasks interactive - allow checking/unchecking
            cbTask.setChecked(task.isCompleted());
            cbTask.setEnabled(true); // Enable checkbox interaction
            
            // Add "Done" label for completed tasks
            String displayText = task.isCompleted() ? "" + task.getText() : task.getText();
            tvTaskText.setText(displayText);
            
            // Update task text appearance
            if (task.isCompleted()) {
                SpannableString spannable = new SpannableString(displayText);
                spannable.setSpan(new StrikethroughSpan(), 0, displayText.length(), 
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvTaskText.setText(spannable);
                tvTaskText.setAlpha(0.6f);
            } else {
                tvTaskText.setText(displayText);
                tvTaskText.setAlpha(1.0f);
            }
            
            // Add checkbox listener for direct interaction
            cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                if (listener != null) {
                    listener.onTaskCheckboxClick(todo, task, isChecked);
                }
            });
            
            return taskView;
        }
    }
}
