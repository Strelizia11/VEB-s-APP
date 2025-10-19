package com.example.veb_app.ui.todo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.veb_app.R;
import com.example.veb_app.databinding.FragmentTodoBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Keep-style to-do fragment with title, body, and tasks
 */
public class TodoFragment extends Fragment {

    private FragmentTodoBinding binding;
    private TodoManager todoManager;
    private TodoAdapter todoAdapter;
    private List<TodoItem> todoItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        todoManager = TodoManager.getInstance();
        todoManager.initialize(requireContext());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTodoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup RecyclerView
        RecyclerView recyclerView = binding.recyclerViewTodos;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        todoAdapter = new TodoAdapter(new ArrayList<>(), new TodoAdapter.OnTodoClickListener() {
            @Override
            public void onTodoClick(TodoItem todo) {
                // Edit feature removed - no action on click
            }

            @Override
            public void onTodoLongClick(TodoItem todo, View view) {
                showContextMenu(todo, view);
            }

            @Override
            public void onTaskCheckboxClick(TodoItem todo, TodoItem.TodoTask task, boolean isChecked) {
                task.setCompleted(isChecked);
                todoManager.updateItem(todo);
                
                // Notify the adapter to refresh the specific to-do item
                todoAdapter.notifyItemChanged(todoItems.indexOf(todo));
                
                Toast.makeText(getContext(), isChecked ? "Task completed!" : "Task marked active", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(todoAdapter);

        // Setup FAB
        FloatingActionButton fabAddTodo = binding.fabAddTodo;
        fabAddTodo.setOnClickListener(v -> showAddEditTodoDialog(null));

        // Setup search functionality
        setupSearch();

        loadTodos();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodos();
    }

    private void setupSearch() {
        TextInputEditText etSearch = binding.etSearchTodos;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase().trim();
                if (query.isEmpty()) {
                    todoAdapter.updateTodos(todoItems);
                } else {
                    List<TodoItem> filteredTodos = new ArrayList<>();
                    for (TodoItem todo : todoItems) {
                        if (todo.getTitle().toLowerCase().contains(query)) {
                            filteredTodos.add(todo);
                        }
                    }
                    todoAdapter.updateTodos(filteredTodos);
                }
                updateEmptyState(todoAdapter.getItemCount() == 0);
            }
        });
    }

    private void loadTodos() {
        todoItems = todoManager.getAllItems();
        todoAdapter.updateTodos(todoItems);
        updateEmptyState(todoItems.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewTodos.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.recyclerViewTodos.setVisibility(View.VISIBLE);
        }
    }

    private void showAddEditTodoDialog(TodoItem existingTodo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_todo, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText etTodoTitle = dialogView.findViewById(R.id.et_todo_title);
        TextInputEditText etDeadline = dialogView.findViewById(R.id.et_deadline);
        LinearLayout tasksContainer = dialogView.findViewById(R.id.tasks_container);
        TextView tvTasksPlaceholder = dialogView.findViewById(R.id.tv_tasks_placeholder);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_todo);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_todo);
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);

        // Setup deadline picker
        setupDeadlinePicker(etDeadline, existingTodo);

        // Pre-fill for editing
        if (existingTodo != null) {
            dialogTitle.setText("Edit To-Do");
            etTodoTitle.setText(existingTodo.getTitle());
            loadTasksInAddDialog(tasksContainer, existingTodo);
        } else {
            dialogTitle.setText("Add New To-Do");
            // Add one empty task by default for new todos
            addEmptyTaskToDialog(tasksContainer);
        }

        btnSave.setOnClickListener(v -> {
            String title = etTodoTitle.getText().toString().trim();
            
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get all tasks from the container
            List<TodoItem.TodoTask> tasks = getTasksFromContainer(tasksContainer);
            if (tasks.isEmpty()) {
                Toast.makeText(getContext(), "Please add at least one task", Toast.LENGTH_SHORT).show();
                return;
            }

            // Handle deadline
            long deadline = 0;
            String deadlineText = etDeadline.getText().toString().trim();
            if (!deadlineText.isEmpty()) {
                try {
                    // Parse the formatted date and time
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
                    Date date = sdf.parse(deadlineText);
                    deadline = date.getTime();
                } catch (Exception e) {
                    // Invalid date format, ignore
                }
            }

            if (existingTodo != null) {
                existingTodo.setTitle(title);
                existingTodo.setDeadline(deadline);
                existingTodo.getTasks().clear();
                existingTodo.getTasks().addAll(tasks);
                todoManager.updateItem(existingTodo);
                
                // Schedule notifications for updated deadline
                if (deadline > 0) {
                    NotificationScheduler.scheduleDeadlineNotifications(getContext(), existingTodo);
                }
                
                Toast.makeText(getContext(), "To-Do updated!", Toast.LENGTH_SHORT).show();
            } else {
                TodoItem newTodo = new TodoItem(title);
                newTodo.setDeadline(deadline);
                newTodo.getTasks().addAll(tasks);
                todoManager.addItem(newTodo);
                
                // Schedule notifications for new deadline
                if (deadline > 0) {
                    NotificationScheduler.scheduleDeadlineNotifications(getContext(), newTodo);
                }
                
                Toast.makeText(getContext(), "To-Do added!", Toast.LENGTH_SHORT).show();
            }
            loadTodos();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void showContextMenu(TodoItem todo, View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.todo_context_menu, popupMenu.getMenu());
        
        // Update menu items based on pin state
        if (todo.isPinned()) {
            popupMenu.getMenu().findItem(R.id.action_pin_todo).setVisible(false);
            popupMenu.getMenu().findItem(R.id.action_unpin_todo).setVisible(true);
        } else {
            popupMenu.getMenu().findItem(R.id.action_pin_todo).setVisible(true);
            popupMenu.getMenu().findItem(R.id.action_unpin_todo).setVisible(false);
        }
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_delete_todo) {
                deleteTodo(todo);
                return true;
            } else if (itemId == R.id.action_pin_todo) {
                pinTodo(todo);
                return true;
            } else if (itemId == R.id.action_unpin_todo) {
                unpinTodo(todo);
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    private void deleteTodo(TodoItem todo) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete To-Do")
            .setMessage("Are you sure you want to delete \"" + todo.getTitle() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                todoManager.deleteItem(todo);
                loadTodos();
                Toast.makeText(getContext(), "To-Do deleted!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void pinTodo(TodoItem todo) {
        todoManager.pinItem(todo);
        loadTodos();
        Toast.makeText(getContext(), "To-Do pinned!", Toast.LENGTH_SHORT).show();
    }

    private void unpinTodo(TodoItem todo) {
        todoManager.unpinItem(todo);
        loadTodos();
        Toast.makeText(getContext(), "To-Do unpinned!", Toast.LENGTH_SHORT).show();
    }

    private void setupDeadlinePicker(TextInputEditText etDeadline, TodoItem existingTodo) {
        etDeadline.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            
            // If editing existing todo with deadline, use that date
            if (existingTodo != null && existingTodo.getDeadline() > 0) {
                calendar.setTimeInMillis(existingTodo.getDeadline());
            }
            
            // Show date picker
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // After date is selected, show time picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getContext(),
                        (timeView, hourOfDay, minute) -> {
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            
                            // Format and display the selected date and time
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
                            etDeadline.setText(sdf.format(calendar.getTime()));
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false // 12-hour format
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void addEmptyTaskToDialog(LinearLayout tasksContainer) {
        // Count actual task inputs (excluding placeholder)
        int taskCount = 0;
        for (int i = 0; i < tasksContainer.getChildCount(); i++) {
            View child = tasksContainer.getChildAt(i);
            if (child.findViewById(R.id.et_task_text) != null) {
                taskCount++;
            }
        }
        
        // Always add a new task input
        addTaskToDialog(tasksContainer, "");
    }

    private void addTaskToDialog(LinearLayout tasksContainer, String taskText) {
        View taskView = LayoutInflater.from(getContext())
            .inflate(R.layout.item_add_task, tasksContainer, false);
        
        TextInputEditText etTaskText = taskView.findViewById(R.id.et_task_text);
        TextView ivDeleteTask = taskView.findViewById(R.id.iv_delete_task);
        
        etTaskText.setText(taskText);
        if (taskText.isEmpty()) {
            etTaskText.requestFocus();
        }
        
        // Hide placeholder when first task is added
        if (tasksContainer.getChildCount() == 1) { // Only placeholder is present
            View placeholder = tasksContainer.findViewById(R.id.tv_tasks_placeholder);
            if (placeholder != null) {
                placeholder.setVisibility(View.GONE);
            }
        }
        
        // Delete task listener
        ivDeleteTask.setOnClickListener(v -> {
            // Count actual task inputs (excluding placeholder)
            int taskCount = 0;
            for (int i = 0; i < tasksContainer.getChildCount(); i++) {
                View child = tasksContainer.getChildAt(i);
                if (child.findViewById(R.id.et_task_text) != null) {
                    taskCount++;
                }
            }
            
            // Don't allow deleting if it's the only task input
            if (taskCount <= 1) {
                return; // Don't delete the last task input
            }
            
            tasksContainer.removeView(taskView);
        });
        
        // Enter key to add new task
        etTaskText.setOnEditorActionListener((v, actionId, event) -> {
            String currentText = etTaskText.getText().toString().trim();
            if (!currentText.isEmpty()) {
                addEmptyTaskToDialog(tasksContainer);
            }
            return true;
        });
        
        // Text watcher for newline detection
        etTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Check for newline and add new task
                if (s.toString().contains("\n")) {
                    String text = s.toString().replace("\n", "").trim();
                    etTaskText.setText(text);
                    etTaskText.setSelection(text.length());
                    
                    if (!text.isEmpty()) {
                        addEmptyTaskToDialog(tasksContainer);
                    }
                }
            }
        });
        
        tasksContainer.addView(taskView);
    }

    private void loadTasksInAddDialog(LinearLayout tasksContainer, TodoItem todo) {
        tasksContainer.removeAllViews();
        
        if (todo.getTasks().isEmpty()) {
            addEmptyTaskToDialog(tasksContainer);
        } else {
            for (TodoItem.TodoTask task : todo.getTasks()) {
                addTaskToDialog(tasksContainer, task.getText());
            }
        }
    }

    private List<TodoItem.TodoTask> getTasksFromContainer(LinearLayout tasksContainer) {
        List<TodoItem.TodoTask> tasks = new ArrayList<>();
        
        for (int i = 0; i < tasksContainer.getChildCount(); i++) {
            View taskView = tasksContainer.getChildAt(i);
            TextInputEditText etTaskText = taskView.findViewById(R.id.et_task_text);
            String taskText = etTaskText.getText().toString().trim();
            
            if (!taskText.isEmpty()) {
                TodoItem.TodoTask task = new TodoItem.TodoTask(taskText);
                tasks.add(task);
            }
        }
        
        return tasks;
    }

    // Edit feature removed - method disabled
    private void showEditTodoDialog_DISABLED(TodoItem todo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_todo, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText etTodoTitle = dialogView.findViewById(R.id.et_todo_title);
        LinearLayout tasksContainer = dialogView.findViewById(R.id.tasks_container);
        TextInputEditText etAddTask = dialogView.findViewById(R.id.et_add_task);
        MaterialButton btnAddTask = dialogView.findViewById(R.id.btn_add_task);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_todo);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_todo);

        // Pre-fill for editing
        etTodoTitle.setText(todo.getTitle());

        // Load existing tasks
        loadTasksInDialog(tasksContainer, todo);

        // Add task functionality
        btnAddTask.setOnClickListener(v -> {
            String taskText = etAddTask.getText().toString().trim();
            if (!taskText.isEmpty()) {
                TodoItem.TodoTask newTask = new TodoItem.TodoTask(taskText);
                todo.getTasks().add(newTask);
                loadTasksInDialog(tasksContainer, todo);
                etAddTask.setText("");
            }
        });

        // Save changes
        btnSave.setOnClickListener(v -> {
            String title = etTodoTitle.getText().toString().trim();
            
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            todo.setTitle(title);
            todoManager.updateItem(todo);
            loadTodos();
            Toast.makeText(getContext(), "To-Do updated!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void loadTasksInDialog(LinearLayout container, TodoItem todo) {
        container.removeAllViews();
        
        // Sort tasks: unchecked first, then checked
        List<TodoItem.TodoTask> sortedTasks = new ArrayList<>(todo.getTasks());
        sortedTasks.sort((task1, task2) -> {
            if (task1.isCompleted() == task2.isCompleted()) {
                return 0; // Keep original order if both have same completion status
            }
            return task1.isCompleted() ? 1 : -1; // Unchecked tasks first
        });
        
        for (TodoItem.TodoTask task : sortedTasks) {
            View taskView = createEditTaskView(task, todo, container);
            container.addView(taskView);
        }
    }

    private View createEditTaskView(TodoItem.TodoTask task, TodoItem todo, LinearLayout container) {
        View taskView = LayoutInflater.from(getContext())
            .inflate(R.layout.item_edit_task, container, false);
        
        CheckBox cbTask = taskView.findViewById(R.id.cb_task);
        TextInputEditText etTaskText = taskView.findViewById(R.id.et_task_text);
        TextView ivDeleteTask = taskView.findViewById(R.id.iv_delete_task);
        
        cbTask.setChecked(task.isCompleted());
        
        // Add "Done" label for completed tasks
        if (task.isCompleted()) {
            etTaskText.setText("" + task.getText());
        } else {
            etTaskText.setText(task.getText());
        }
        
        // Update editability based on completion status
        updateTaskEditability(etTaskText, ivDeleteTask, task.isCompleted());
        
        // Task checkbox listener
        cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            
            // Update text with "Done" label
            if (isChecked) {
                etTaskText.setText("" + task.getText());
            } else {
                etTaskText.setText(task.getText());
            }
            
            // Update editability when checkbox state changes
            updateTaskEditability(etTaskText, ivDeleteTask, isChecked);
            
            // Don't refresh the entire dialog - this was causing the checkbox reset issue
        });
        
        // Task text listener (only if not completed)
        if (!task.isCompleted()) {
            etTaskText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(android.text.Editable s) {
                    // Remove "Done: " prefix if it exists and update the task text
                    String text = s.toString();
                    if (text.startsWith("Done: ")) {
                        task.setText(text.substring(6)); // Remove "Done: " prefix
                    } else {
                        task.setText(text);
                    }
                }
            });
        }
        
        // Delete task listener (only if not completed)
        if (!task.isCompleted()) {
            ivDeleteTask.setOnClickListener(v -> {
                todo.getTasks().remove(task);
                container.removeView(taskView);
            });
        }
        
        return taskView;
    }

    private void updateTaskEditability(TextInputEditText etTaskText, TextView ivDeleteTask, boolean isCompleted) {
        if (isCompleted) {
            // Make completed tasks non-editable
            etTaskText.setEnabled(false);
            etTaskText.setFocusable(false);
            etTaskText.setFocusableInTouchMode(false);
            etTaskText.setAlpha(0.6f); // Make it visually appear disabled
            ivDeleteTask.setEnabled(false);
            ivDeleteTask.setAlpha(0.3f); // Make delete button appear disabled
        } else {
            // Make incomplete tasks editable
            etTaskText.setEnabled(true);
            etTaskText.setFocusable(true);
            etTaskText.setFocusableInTouchMode(true);
            etTaskText.setAlpha(1.0f);
            ivDeleteTask.setEnabled(true);
            ivDeleteTask.setAlpha(1.0f);
        }
    }

    private void showAddTaskDialog(TodoItem todo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText etTaskText = dialogView.findViewById(R.id.et_task_text);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_task);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_task);

        btnSave.setOnClickListener(v -> {
            String taskText = etTaskText.getText().toString().trim();
            if (taskText.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a task description", Toast.LENGTH_SHORT).show();
                return;
            }

            TodoItem.TodoTask newTask = new TodoItem.TodoTask(taskText);
            todo.getTasks().add(newTask);
            todoManager.updateItem(todo);
            loadTodos();
            Toast.makeText(getContext(), "Task added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    // Edit and context menu features removed

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}