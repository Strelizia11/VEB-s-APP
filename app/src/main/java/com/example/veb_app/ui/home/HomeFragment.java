package com.example.veb_app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.veb_app.R;
import com.example.veb_app.databinding.FragmentHomeBinding;
import com.example.veb_app.data.DatabaseManager;
import com.example.veb_app.ui.notes.NotesManager;
import com.example.veb_app.ui.notes.NotesFragment;
import com.example.veb_app.ui.todo.TodoManager;
import com.example.veb_app.ui.todo.TodoItem;
import com.example.veb_app.ui.budget.BudgetManager;
import com.example.veb_app.ui.calendar.EventManager;
import com.example.veb_app.ui.calendar.HolidayManager;
import com.example.veb_app.ui.calendar.Event;
import com.example.veb_app.ui.calendar.Holiday;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static HomeFragment instance;
    private Timer refreshTimer;

    public static void refreshHomePageIfVisible() {
        android.util.Log.d("HomeFragment", "refreshHomePageIfVisible called");
        android.util.Log.d("HomeFragment", "instance: " + instance);
        android.util.Log.d("HomeFragment", "isAdded: " + (instance != null ? instance.isAdded() : "null"));
        android.util.Log.d("HomeFragment", "activity: " + (instance != null ? instance.getActivity() : "null"));
        
        if (instance != null && instance.isAdded() && instance.getActivity() != null) {
            android.util.Log.d("HomeFragment", "Refreshing home page...");
            instance.refreshHomePage();
        } else {
            android.util.Log.d("HomeFragment", "Home page not refreshed - conditions not met");
        }
    }
    
    public void forceRefresh() {
        android.util.Log.d("HomeFragment", "forceRefresh called");
        refreshHomePage();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Set instance for static access
        instance = this;

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        
            // Setup featured note and checklist display
            setupTodayCalendar(root);
            setupFeaturedNote(root);
            setupFeaturedChecklist(root);
            setupBudgetOverview(root);
        
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always refresh all data when returning to home
        android.util.Log.d("HomeFragment", "onResume called - refreshing home page");
        refreshHomePage();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // Also refresh when starting to ensure data is up to date
        android.util.Log.d("HomeFragment", "onStart called - refreshing home page");
        refreshHomePage();
        
        // Start periodic refresh every 2 seconds
        startPeriodicRefresh();
    }
    
    private void startPeriodicRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
        
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            android.util.Log.d("HomeFragment", "Periodic refresh triggered");
                            refreshHomePage();
                        }
                    });
                }
            }
        }, 2000, 2000); // Start after 2 seconds, repeat every 2 seconds
    }
    
    private void stopPeriodicRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        android.util.Log.d("HomeFragment", "onHiddenChanged called - hidden: " + hidden);
        if (!hidden) {
            // Fragment is becoming visible, refresh data
            refreshHomePage();
        }
    }
    
    private void refreshHomePage() {
        android.util.Log.d("HomeFragment", "refreshHomePage called");
        android.util.Log.d("HomeFragment", "binding: " + binding);
        
        if (binding != null) {
            android.util.Log.d("HomeFragment", "Setting up all components...");
            setupTodayCalendar(binding.getRoot());
            setupFeaturedNote(binding.getRoot());
            setupFeaturedChecklist(binding.getRoot());
            setupBudgetOverview(binding.getRoot());
            android.util.Log.d("HomeFragment", "All components set up");
        } else {
            android.util.Log.d("HomeFragment", "Binding is null, cannot refresh");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPeriodicRefresh();
        binding = null;
        instance = null;
    }

    private void setupFeaturedNote(View root) {
        MaterialCardView cardFeaturedNote = root.findViewById(R.id.card_featured_note);
        TextView tvFeaturedNoteTitle = root.findViewById(R.id.tv_featured_note_title);
        TextView tvFeaturedNoteContent = root.findViewById(R.id.tv_featured_note_content);
        MaterialButton btnViewAllNotes = root.findViewById(R.id.btn_view_all_notes);
        TextView tvNoNotesMessage = root.findViewById(R.id.tv_no_notes_message);

        // Get featured note (pinned or most recent)
        NotesManager notesManager = NotesManager.getInstance();
        if (getContext() != null) {
            notesManager.initialize(getContext());
        }
        NotesFragment.Note featuredNote = notesManager.getFeaturedNote();

        if (featuredNote != null) {
            // Display featured note
            tvFeaturedNoteTitle.setText(featuredNote.getTitle());
            
            // Truncate content for preview
            String content = featuredNote.getBody();
            if (content.length() > 150) {
                content = content.substring(0, 147) + "...";
            }
            tvFeaturedNoteContent.setText(content);
        } else {
            // No notes available - show empty state
            tvFeaturedNoteTitle.setText("Notes");
            tvFeaturedNoteContent.setText("There is no existing notes yet");
        }
        
        // Always show the note card
        cardFeaturedNote.setVisibility(View.VISIBLE);
        tvNoNotesMessage.setVisibility(View.GONE);

        // Setup View All Notes button
        btnViewAllNotes.setOnClickListener(v -> {
            // Trigger the NavigationView's built-in navigation mechanism
            if (getActivity() != null) {
                com.google.android.material.navigation.NavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null) {
                    android.view.MenuItem notesMenuItem = navView.getMenu().findItem(R.id.nav_notes);
                    if (notesMenuItem != null) {
                        // Trigger the menu item's click event which will use NavigationUI
                        notesMenuItem.setChecked(true);
                        
                        // Get the NavController and navigate using NavigationUI
                        androidx.navigation.fragment.NavHostFragment navHostFragment = 
                            (androidx.navigation.fragment.NavHostFragment) getActivity().getSupportFragmentManager()
                                .findFragmentById(R.id.nav_host_fragment_content_main);
                        if (navHostFragment != null) {
                            NavController navController = navHostFragment.getNavController();
                            // Use NavigationUI to handle the navigation properly
                            androidx.navigation.ui.NavigationUI.onNavDestinationSelected(notesMenuItem, navController);
                        }
                    }
                }
            }
        });
    }

    private void setupFeaturedChecklist(View root) {
        MaterialCardView cardFeaturedChecklist = root.findViewById(R.id.card_featured_checklist);
        TextView tvFeaturedChecklistTitle = root.findViewById(R.id.tv_featured_checklist_title);
        TextView tvFeaturedChecklistProgress = root.findViewById(R.id.tv_featured_checklist_progress);
        CircularProgressIndicator progressCircle = root.findViewById(R.id.progress_circle_checklist);
        MaterialButton btnViewAllChecklists = root.findViewById(R.id.btn_view_all_checklists);
        TextView tvNoChecklistsMessage = root.findViewById(R.id.tv_no_checklists_message);

        // Get featured todo (pinned or most recent)
        TodoManager todoManager = TodoManager.getInstance();
        todoManager.initialize(requireContext());
        TodoItem featuredTodo = getFeaturedTodo(todoManager);

        if (featuredTodo != null) {
            // Display featured todo
            tvFeaturedChecklistTitle.setText(featuredTodo.getTitle());
            
            // Calculate completion stats
            int completedTasks = featuredTodo.getCompletedTasksCount();
            int totalTasks = featuredTodo.getTotalTasksCount();
            
            // If no sub-tasks, use the main todo completion status
            if (totalTasks == 0) {
                totalTasks = 1;
                completedTasks = featuredTodo.isCompleted() ? 1 : 0;
            }
            
            int progress = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;
            
            // Set progress text and circle
            tvFeaturedChecklistProgress.setText(completedTasks + " of " + totalTasks + " completed");
            progressCircle.setProgress(progress);
            
        } else {
            // No todos available - show empty state
            tvFeaturedChecklistTitle.setText("To-Do");
            tvFeaturedChecklistProgress.setText("There are no to-dos yet");
            progressCircle.setProgress(0);
        }
        
        // Always show the checklist card
        cardFeaturedChecklist.setVisibility(View.VISIBLE);
        tvNoChecklistsMessage.setVisibility(View.GONE);

        // Setup View All Checklists button
        btnViewAllChecklists.setOnClickListener(v -> {
            // Trigger the NavigationView's built-in navigation mechanism
            if (getActivity() != null) {
                com.google.android.material.navigation.NavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null) {
                    android.view.MenuItem checklistMenuItem = navView.getMenu().findItem(R.id.nav_checklist);
                    if (checklistMenuItem != null) {
                        // Trigger the menu item's click event which will use NavigationUI
                        checklistMenuItem.setChecked(true);
                        
                        // Get the NavController and navigate using NavigationUI
                        androidx.navigation.fragment.NavHostFragment navHostFragment = 
                            (androidx.navigation.fragment.NavHostFragment) getActivity().getSupportFragmentManager()
                                .findFragmentById(R.id.nav_host_fragment_content_main);
                        if (navHostFragment != null) {
                            NavController navController = navHostFragment.getNavController();
                            // Use NavigationUI to handle the navigation properly
                            androidx.navigation.ui.NavigationUI.onNavDestinationSelected(checklistMenuItem, navController);
                        }
                    }
                }
            }
        });
    }
    
    private TodoItem getFeaturedTodo(TodoManager todoManager) {
        List<TodoItem> todos = todoManager.getAllItems();
        
        if (todos.isEmpty()) {
            return null;
        }
        
        // First, look for pinned todos
        for (TodoItem todo : todos) {
            if (todo.isPinned()) {
                return todo;
            }
        }
        
        // If no pinned todos, return the most recent (first in the list)
        return todos.get(0);
    }

    private void setupBudgetOverview(View root) {
        MaterialCardView cardBudgetOverview = root.findViewById(R.id.card_budget_overview);
        TextView tvBudgetTitleHome = root.findViewById(R.id.tv_budget_title_home);
        TextView tvBudgetIncomeHome = root.findViewById(R.id.tv_budget_income_home);
        TextView tvBudgetExpensesHome = root.findViewById(R.id.tv_budget_expenses_home);
        TextView tvBudgetRemainingHome = root.findViewById(R.id.tv_budget_remaining_home);
        com.google.android.material.progressindicator.LinearProgressIndicator progressBudget = root.findViewById(R.id.progress_budget_home);
        MaterialButton btnViewBudget = root.findViewById(R.id.btn_view_budget);

        // Initialize BudgetManager
        BudgetManager budgetManager = BudgetManager.getInstance();
        if (getContext() != null) {
            budgetManager.initialize(getContext());
        }

        // Get budget data (total across all time, not monthly)
        double totalIncome = budgetManager.getTotalIncome();
        double totalSpent = budgetManager.getTotalExpenses();
        double remaining = budgetManager.getTotalRemaining();

        // Format currency
        java.text.DecimalFormat currencyFormat = new java.text.DecimalFormat("₱#,##0.00");

        // Update title to show "Budget" instead of month
        if (tvBudgetTitleHome != null) {
            tvBudgetTitleHome.setText("Budget");
        }

        // Update total income (all time)
        if (tvBudgetIncomeHome != null) {
            tvBudgetIncomeHome.setText("Income " + currencyFormat.format(totalIncome));
        }

        // Update total expenses (all time)
        if (tvBudgetExpensesHome != null) {
            tvBudgetExpensesHome.setText("Expenses " + currencyFormat.format(totalSpent));
        }

        // Update remaining budget (total across all time)
        if (tvBudgetRemainingHome != null) {
            tvBudgetRemainingHome.setText(currencyFormat.format(remaining));
        }

        if (progressBudget != null) {
            int progress = totalIncome > 0 ? (int) ((totalSpent / totalIncome) * 100) : 0;
            progressBudget.setProgress(progress);
        }

        // Setup View Budget button
        if (btnViewBudget != null) {
            btnViewBudget.setOnClickListener(v -> {
                // Trigger the NavigationView's built-in navigation mechanism
                if (getActivity() != null) {
                    com.google.android.material.navigation.NavigationView navView = getActivity().findViewById(R.id.nav_view);
                    if (navView != null) {
                        android.view.MenuItem budgetMenuItem = navView.getMenu().findItem(R.id.nav_budget);
                        if (budgetMenuItem != null) {
                            // Trigger the menu item's click event which will use NavigationUI
                            budgetMenuItem.setChecked(true);
                            
                            // Get the NavController and navigate using NavigationUI
                            androidx.navigation.fragment.NavHostFragment navHostFragment = 
                                (androidx.navigation.fragment.NavHostFragment) getActivity().getSupportFragmentManager()
                                    .findFragmentById(R.id.nav_host_fragment_content_main);
                            if (navHostFragment != null) {
                                NavController navController = navHostFragment.getNavController();
                                // Use NavigationUI to handle the navigation properly
                                androidx.navigation.ui.NavigationUI.onNavDestinationSelected(budgetMenuItem, navController);
                            }
                        }
                    }
                }
            });
        }
    }

    private void setupTodayCalendar(View root) {
        TextView tvTodayMonth = root.findViewById(R.id.tv_today_month);
        TextView tvTodayDay = root.findViewById(R.id.tv_today_day);
        TextView tvTodayDayName = root.findViewById(R.id.tv_today_day_name);
        TextView tvTodayEvent = root.findViewById(R.id.tv_today_event);
        TextView dotIncome = root.findViewById(R.id.dot_income);
        TextView dotExpense = root.findViewById(R.id.dot_expense);
        TextView dotEvent = root.findViewById(R.id.dot_event);

        // Get today's date
        Calendar today = Calendar.getInstance();
        Date todayDate = today.getTime();

        // Set date information
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", java.util.Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", java.util.Locale.getDefault());
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", java.util.Locale.getDefault());

        tvTodayMonth.setText(monthFormat.format(todayDate).toUpperCase());
        tvTodayDay.setText(dayFormat.format(todayDate));
        tvTodayDayName.setText(dayNameFormat.format(todayDate).toUpperCase());

        // Get events and holidays for today
        EventManager eventManager = EventManager.getInstance();
        if (getContext() != null) {
            eventManager.initialize(getContext());
        }
        HolidayManager holidayManager = HolidayManager.getInstance();
        BudgetManager budgetManager = BudgetManager.getInstance();
        if (getContext() != null) {
            budgetManager.initialize(getContext());
        }

        List<Event> events = eventManager.getEventsByDate(todayDate);
        List<Holiday> holidays = holidayManager.getHolidaysForDate(todayDate);
        // Get all transactions and filter for today
        List<BudgetManager.Transaction> allTransactions = budgetManager.getAllTransactions();
        List<BudgetManager.Transaction> todayTransactions = new java.util.ArrayList<>();
        
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(todayDate);
        
        for (BudgetManager.Transaction transaction : allTransactions) {
            Calendar transactionCal = Calendar.getInstance();
            transactionCal.setTime(transaction.getDate());
            
            if (todayCal.get(Calendar.YEAR) == transactionCal.get(Calendar.YEAR) &&
                todayCal.get(Calendar.MONTH) == transactionCal.get(Calendar.MONTH) &&
                todayCal.get(Calendar.DAY_OF_MONTH) == transactionCal.get(Calendar.DAY_OF_MONTH)) {
                todayTransactions.add(transaction);
            }
        }

        // Determine what to display as the event
        StringBuilder eventText = new StringBuilder();
        
        // Show holidays first
        if (!holidays.isEmpty()) {
            for (Holiday holiday : holidays) {
                if (eventText.length() > 0) eventText.append(", ");
                eventText.append("Holiday: ").append(holiday.getName());
            }
        }
        
        // Then show custom events
        if (!events.isEmpty()) {
            for (Event event : events) {
                if (eventText.length() > 0) eventText.append("\n");
                String timeText = event.getTime() != null && !event.getTime().isEmpty() ? 
                                 " (" + event.getTime() + ")" : "";
                eventText.append(event.getTitle()).append(timeText);
            }
        }

        if (eventText.length() == 0) {
            tvTodayEvent.setText("No Scheduled Event");
        } else {
            tvTodayEvent.setText(eventText.toString());
        }

        // Use the same logic as the calendar to determine dots
        boolean hasIncome = false;
        boolean hasExpenses = false;
        boolean hasEvents = !events.isEmpty();

        for (BudgetManager.Transaction transaction : todayTransactions) {
            if (transaction.getType() == BudgetManager.Transaction.TransactionType.INCOME) {
                hasIncome = true;
            } else {
                hasExpenses = true;
            }
        }

        // Show dots based on the same logic as the calendar
        dotIncome.setVisibility(hasIncome ? View.VISIBLE : View.GONE);
        dotExpense.setVisibility(hasExpenses ? View.VISIBLE : View.GONE);
        dotEvent.setVisibility(hasEvents ? View.VISIBLE : View.GONE);
    }
}
