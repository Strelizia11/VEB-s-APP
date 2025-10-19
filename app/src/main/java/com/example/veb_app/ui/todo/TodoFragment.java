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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
                showAddEditTodoDialog(todo);
            }

            @Override
            public void onTodoLongClick(TodoItem todo, View view) {
                showContextMenu(todo, view);
            }

            @Override
            public void onTaskCheckboxClick(TodoItem todo, TodoItem.TodoTask task, boolean isChecked) {
                task.setCompleted(isChecked);
                todoManager.updateItem(todo);
                loadTodos(); // Reload to update progress
                Toast.makeText(getContext(), isChecked ? "Task completed!" : "Task marked active", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(todoAdapter);

        // Setup FAB
        FloatingActionButton fabAddTodo = binding.fabAddTodo;
        fabAddTodo.setOnClickListener(v -> showAddEditTodoDialog(null));

        loadTodos();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodos();
    }

    private void loadTodos() {
        List<TodoItem> todos = todoManager.getAllItems();
        todoAdapter.updateTodos(todos);
        updateEmptyState(todos.isEmpty());
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
        LinearLayout tasksContainer = dialogView.findViewById(R.id.tasks_container);
        CheckBox cbPinTodo = dialogView.findViewById(R.id.cb_pin_todo);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_todo);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_todo);
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);

        // Pre-fill for editing
        if (existingTodo != null) {
            dialogTitle.setText("Edit To-Do");
            etTodoTitle.setText(existingTodo.getTitle());
            cbPinTodo.setChecked(existingTodo.isPinned());
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

            if (existingTodo != null) {
                existingTodo.setTitle(title);
                existingTodo.setPinned(cbPinTodo.isChecked());
                existingTodo.getTasks().clear();
                existingTodo.getTasks().addAll(tasks);
                todoManager.updateItem(existingTodo);
                Toast.makeText(getContext(), "To-Do updated!", Toast.LENGTH_SHORT).show();
            } else {
                TodoItem newTodo = new TodoItem(title);
                newTodo.setPinned(cbPinTodo.isChecked());
                newTodo.getTasks().addAll(tasks);
                todoManager.addItem(newTodo);
                Toast.makeText(getContext(), "To-Do added!", Toast.LENGTH_SHORT).show();
            }
            loadTodos();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }


    private void addEmptyTaskToDialog(LinearLayout tasksContainer) {
        addTaskToDialog(tasksContainer, "");
    }

    private void addTaskToDialog(LinearLayout tasksContainer, String taskText) {
        View taskView = LayoutInflater.from(getContext())
            .inflate(R.layout.item_add_task, tasksContainer, false);
        
        TextInputEditText etTaskText = taskView.findViewById(R.id.et_task_text);
        ImageView ivDeleteTask = taskView.findViewById(R.id.iv_delete_task);
        
        etTaskText.setText(taskText);
        if (taskText.isEmpty()) {
            etTaskText.requestFocus();
        }
        
        // Delete task listener
        ivDeleteTask.setOnClickListener(v -> {
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

    private void showEditTodoDialog(TodoItem todo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_todo, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText etTodoTitle = dialogView.findViewById(R.id.et_todo_title);
        CheckBox cbPinTodo = dialogView.findViewById(R.id.cb_pin_todo);
        LinearLayout tasksContainer = dialogView.findViewById(R.id.tasks_container);
        TextInputEditText etAddTask = dialogView.findViewById(R.id.et_add_task);
        MaterialButton btnAddTask = dialogView.findViewById(R.id.btn_add_task);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_todo);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_todo);

        // Pre-fill for editing
        etTodoTitle.setText(todo.getTitle());
        cbPinTodo.setChecked(todo.isPinned());

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
            todo.setPinned(cbPinTodo.isChecked());
            todoManager.updateItem(todo);
            loadTodos();
            Toast.makeText(getContext(), "To-Do updated!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void loadTasksInDialog(LinearLayout container, TodoItem todo) {
        container.removeAllViews();
        
        for (TodoItem.TodoTask task : todo.getTasks()) {
            View taskView = createEditTaskView(task, todo, container);
            container.addView(taskView);
        }
    }

    private View createEditTaskView(TodoItem.TodoTask task, TodoItem todo, LinearLayout container) {
        View taskView = LayoutInflater.from(getContext())
            .inflate(R.layout.item_edit_task, container, false);
        
        CheckBox cbTask = taskView.findViewById(R.id.cb_task);
        TextInputEditText etTaskText = taskView.findViewById(R.id.et_task_text);
        ImageView ivDeleteTask = taskView.findViewById(R.id.iv_delete_task);
        
        cbTask.setChecked(task.isCompleted());
        etTaskText.setText(task.getText());
        
        // Task checkbox listener
        cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
        });
        
        // Task text listener
        etTaskText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                task.setText(s.toString());
            }
        });
        
        // Delete task listener
        ivDeleteTask.setOnClickListener(v -> {
            todo.getTasks().remove(task);
            container.removeView(taskView);
        });
        
        return taskView;
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

    private void showContextMenu(TodoItem todo, View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.todo_context_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_todo) {
                showAddEditTodoDialog(todo);
                return true;
            } else if (itemId == R.id.action_delete_todo) {
                deleteTodo(todo);
                return true;
            } else if (itemId == R.id.action_pin_todo) {
                todo.setPinned(!todo.isPinned());
                todoManager.updateItem(todo);
                loadTodos();
                Toast.makeText(getContext(), todo.isPinned() ? "To-Do pinned!" : "To-Do unpinned!", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void deleteTodo(TodoItem todo) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete To-Do")
                .setMessage("Are you sure you want to delete this to-do item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    todoManager.deleteItem(todo);
                    loadTodos();
                    Toast.makeText(getContext(), "To-Do deleted!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}