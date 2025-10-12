package com.example.veb_app.ui.calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.veb_app.R;
import com.example.veb_app.databinding.FragmentCalendarNewBinding;
import com.example.veb_app.ui.budget.BudgetManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private FragmentCalendarNewBinding binding;
    private Calendar currentCalendar;
    private SimpleDateFormat monthYearFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private DecimalFormat currencyFormat;
    private EventManager eventManager;
    private BudgetManager budgetManager;
    private HolidayManager holidayManager;
    private Date selectedDate;
    private String selectedEventColor = "#9CAF88";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CalendarViewModel calendarViewModel =
                new ViewModelProvider(this).get(CalendarViewModel.class);

        binding = FragmentCalendarNewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initializeManagers();
        initializeFormatters();
        setupUI(root);
        loadCalendarData();

        return root;
    }

    private void initializeManagers() {
        eventManager = EventManager.getInstance();
        if (getContext() != null) {
            eventManager.initialize(getContext());
        }

        budgetManager = BudgetManager.getInstance();
        if (getContext() != null) {
            budgetManager.initialize(getContext());
        }
        
        holidayManager = HolidayManager.getInstance();
    }

    private void initializeFormatters() {
        currentCalendar = Calendar.getInstance();
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        currencyFormat = new DecimalFormat("₱#,##0.00");
    }

    private void setupUI(View root) {
        // Setup month navigation
        MaterialButton btnPrevMonth = root.findViewById(R.id.btn_prev_month);
        MaterialButton btnNextMonth = root.findViewById(R.id.btn_next_month);
        TextView tvMonthYear = root.findViewById(R.id.tv_month_year);

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, -1);
                updateCalendar();
            });
        }

        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, 1);
                updateCalendar();
            });
        }

        // FAB removed - events are added via long press
    }

    private void loadCalendarData() {
        updateCalendar();
        updateSelectedDateDetails();
    }

    private void updateCalendar() {
        TextView tvMonthYear = binding.getRoot().findViewById(R.id.tv_month_year);
        GridLayout calendarGrid = binding.getRoot().findViewById(R.id.calendar_grid);

        if (tvMonthYear != null) {
            tvMonthYear.setText(monthYearFormat.format(currentCalendar.getTime()));
        }

        if (calendarGrid != null) {
            populateCalendarGrid(calendarGrid);
        }
    }

    private void populateCalendarGrid(GridLayout grid) {
        // Clear existing day views (keep day headers)
        for (int i = grid.getChildCount() - 1; i >= 7; i--) {
            grid.removeViewAt(i);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentCalendar.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add empty cells for days before the first day of the month
        for (int i = 1; i < firstDayOfWeek; i++) {
            TextView emptyDay = createEmptyDayView();
            grid.addView(emptyDay);
        }

        // Add days of the month
        for (int day = 1; day <= daysInMonth; day++) {
            TextView dayView = createDayView(day);
            grid.addView(dayView);
        }
    }

    private TextView createEmptyDayView() {
        TextView emptyDay = new TextView(getContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(4, 4, 4, 4);
        emptyDay.setLayoutParams(params);
        emptyDay.setHeight(80);
        return emptyDay;
    }

    private TextView createDayView(int day) {
        TextView dayView = new TextView(getContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 120;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(0, 0, 0, 0);
        dayView.setLayoutParams(params);

        dayView.setText(String.valueOf(day));
        dayView.setTextAppearance(android.R.style.TextAppearance_Medium);
        dayView.setGravity(android.view.Gravity.CENTER);
        dayView.setBackgroundResource(R.drawable.day_background);
        dayView.setFocusable(true);
        dayView.setClickable(true);
        dayView.setLongClickable(true);
        dayView.setHapticFeedbackEnabled(true);
        dayView.setPadding(4, 4, 4, 4);
        
        // Set current date
        Calendar cal = Calendar.getInstance();
        cal.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), day);
        Date date = cal.getTime();
        
        // Store both the day number and the date as tags for easy comparison
        dayView.setTag(day);
        dayView.setTag(R.id.day_date_tag, date); // Store the actual date object

        // Check for holidays and set text color accordingly
        List<Holiday> holidaysForDate = holidayManager.getHolidaysForDate(date);
        int textColor = getResources().getColor(R.color.md_theme_light_onSurface); // Default color
        
        if (!holidaysForDate.isEmpty()) {
            Holiday.HolidayType holidayType = holidaysForDate.get(0).getType();
            if (holidayType == Holiday.HolidayType.REGULAR) {
                textColor = getResources().getColor(R.color.error); // Red for regular holidays
            } else if (holidayType == Holiday.HolidayType.SPECIAL) {
                textColor = android.graphics.Color.parseColor("#2196F3"); // Blue for special holidays
            }
        }

        // Highlight today
        Calendar today = Calendar.getInstance();
        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
            cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
            dayView.setBackgroundResource(R.drawable.today_background);
            dayView.setTextColor(getResources().getColor(R.color.md_theme_light_onPrimary));
        } else {
            dayView.setBackgroundResource(R.drawable.day_background);
            dayView.setTextColor(textColor);
        }

        // Add indicators for income, expenses, and events
        List<BudgetManager.Transaction> transactionsForDay = getTransactionsForDate(date);
        List<Event> eventsForDay = eventManager.getEventsByDate(date);
        
        boolean hasIncome = false;
        boolean hasExpenses = false;
        boolean hasEvents = !eventsForDay.isEmpty();
        
        for (BudgetManager.Transaction transaction : transactionsForDay) {
            if (transaction.getType() == BudgetManager.Transaction.TransactionType.INCOME) {
                hasIncome = true;
            } else {
                hasExpenses = true;
            }
        }
        
        // Add text indicators below the day number
        if (hasIncome || hasExpenses || hasEvents) {
            String dayText = String.valueOf(day);
            String indicators = "";
            int startPos = dayText.length() + 1; // Position after day number and newline
            
            if (hasIncome) indicators += "●"; // Green for income
            if (hasExpenses) indicators += "●"; // Red for expenses
            if (hasEvents) indicators += "●"; // Blue for events
            
            dayText += "\n" + indicators;
            
            // Create SpannableString to color the dots
            android.text.SpannableString spannableText = new android.text.SpannableString(dayText);
            int currentPos = startPos;
            
            if (hasIncome) {
                spannableText.setSpan(new android.text.style.ForegroundColorSpan(getResources().getColor(R.color.md_theme_light_primary)), 
                    currentPos, currentPos + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                currentPos++;
            }
            if (hasExpenses) {
                spannableText.setSpan(new android.text.style.ForegroundColorSpan(getResources().getColor(R.color.error)), 
                    currentPos, currentPos + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                currentPos++;
            }
            if (hasEvents) {
                spannableText.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#2196F3")), 
                    currentPos, currentPos + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                currentPos++;
            }
            
            dayView.setText(spannableText);
        }

        // Set click listener
        dayView.setOnClickListener(v -> {
            // Get the stored date from the view tag
            Date viewDate = (Date) dayView.getTag(R.id.day_date_tag);
            if (viewDate == null) {
                viewDate = date; // Fallback to the original date
            }
            
            // Debug logging
            android.util.Log.d("CalendarFragment", "Day clicked - View date: " + viewDate + ", Selected date: " + selectedDate);
            
            // Compare dates by day, month, and year only (ignore time)
            boolean isSameDate = false;
            if (selectedDate != null && viewDate != null) {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(selectedDate);
                Calendar viewCal = Calendar.getInstance();
                viewCal.setTime(viewDate);
                
                isSameDate = selectedCal.get(Calendar.YEAR) == viewCal.get(Calendar.YEAR) &&
                            selectedCal.get(Calendar.MONTH) == viewCal.get(Calendar.MONTH) &&
                            selectedCal.get(Calendar.DAY_OF_MONTH) == viewCal.get(Calendar.DAY_OF_MONTH);
                
                android.util.Log.d("CalendarFragment", "Date comparison - Same date: " + isSameDate + 
                    ", Selected: " + selectedCal.get(Calendar.YEAR) + "/" + selectedCal.get(Calendar.MONTH) + "/" + selectedCal.get(Calendar.DAY_OF_MONTH) +
                    ", View: " + viewCal.get(Calendar.YEAR) + "/" + viewCal.get(Calendar.MONTH) + "/" + viewCal.get(Calendar.DAY_OF_MONTH));
            }
            
            if (isSameDate) {
                // If clicking the same date, deselect it
                android.util.Log.d("CalendarFragment", "Deselecting date");
                selectedDate = null;
                updateSelectedDateDetails();
                highlightSelectedDay(null);
            } else {
                // Select new date
                android.util.Log.d("CalendarFragment", "Selecting new date");
                selectedDate = viewDate;
                updateSelectedDateDetails();
                highlightSelectedDay(dayView);
            }
        });
        
        // Set long click listener for event management
        dayView.setOnLongClickListener(v -> {
            try {
                android.util.Log.d("CalendarFragment", "Long press detected on day: " + day);
                Toast.makeText(getContext(), "Long press detected on day " + day, Toast.LENGTH_SHORT).show();
                
                // Get the stored date from the view tag
                Date viewDate = (Date) dayView.getTag(R.id.day_date_tag);
                if (viewDate == null) {
                    viewDate = date; // Fallback to the original date
                }
                
                android.util.Log.d("CalendarFragment", "Date for long press: " + viewDate);
                
                selectedDate = viewDate;
                showEventManagementDialog(viewDate);
                return true;
            } catch (Exception e) {
                android.util.Log.e("CalendarFragment", "Error in long press", e);
                Toast.makeText(getContext(), "Error opening event dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        return dayView;
    }

    private void highlightSelectedDay(TextView selectedDayView) {
        android.util.Log.d("CalendarFragment", "highlightSelectedDay called with: " + (selectedDayView != null ? "selected view" : "null"));
        
        // Reset all day views to default state
        GridLayout calendarGrid = binding.getRoot().findViewById(R.id.calendar_grid);
        if (calendarGrid != null) {
            for (int i = 7; i < calendarGrid.getChildCount(); i++) {
                View child = calendarGrid.getChildAt(i);
                if (child instanceof TextView) {
                    TextView dayView = (TextView) child;
                    
                    // Get the day number from the tag instead of parsing text
                    Integer dayNumber = (Integer) dayView.getTag();
                    if (dayNumber != null) {
                        // Reset background for numbered days
                        Calendar today = Calendar.getInstance();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(currentCalendar.getTime());
                        cal.set(Calendar.DAY_OF_MONTH, dayNumber);
                        
                        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                            cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                            // This is today
                            dayView.setBackgroundResource(R.drawable.today_background);
                            dayView.setTextColor(getResources().getColor(R.color.md_theme_light_onPrimary));
                        } else {
                            // Regular day
                            dayView.setBackgroundResource(R.drawable.day_background);
                            dayView.setTextColor(getResources().getColor(R.color.md_theme_light_onSurface));
                        }
                    }
                }
            }
        }

        // Highlight selected day if provided
        if (selectedDayView != null) {
            android.util.Log.d("CalendarFragment", "Highlighting selected day");
            selectedDayView.setBackgroundResource(R.drawable.selected_day_background);
            selectedDayView.setTextColor(getResources().getColor(R.color.md_theme_light_onPrimary));
        } else {
            android.util.Log.d("CalendarFragment", "No day to highlight (deselecting)");
        }
    }

    private List<BudgetManager.Transaction> getTransactionsForDate(Date date) {
        List<BudgetManager.Transaction> allTransactions = budgetManager.getAllTransactions();
        List<BudgetManager.Transaction> transactionsForDate = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        
        for (BudgetManager.Transaction transaction : allTransactions) {
            cal.setTime(transaction.getDate());
            if (cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH) &&
                cal.get(Calendar.DAY_OF_MONTH) == dateCal.get(Calendar.DAY_OF_MONTH)) {
                transactionsForDate.add(transaction);
            }
        }
        
        return transactionsForDate;
    }


    private void updateSelectedDateDetails() {
        MaterialCardView cardSelectedDate = binding.getRoot().findViewById(R.id.card_selected_date);
        TextView tvSelectedDate = binding.getRoot().findViewById(R.id.tv_selected_date);
        TextView tvScheduledEvent = binding.getRoot().findViewById(R.id.tv_scheduled_event);
        TextView tvDailyIncome = binding.getRoot().findViewById(R.id.tv_daily_income);
        TextView tvDailyExpenses = binding.getRoot().findViewById(R.id.tv_daily_expenses);
        TextView tvDailyRemaining = binding.getRoot().findViewById(R.id.tv_daily_remaining);

        if (selectedDate == null) {
            if (cardSelectedDate != null) {
                cardSelectedDate.setVisibility(View.GONE);
            }
            return;
        }

        if (cardSelectedDate != null) {
            cardSelectedDate.setVisibility(View.VISIBLE);
        }

        if (tvSelectedDate != null) {
            tvSelectedDate.setText(dateFormat.format(selectedDate));
        }

        // Get data for selected date
        List<BudgetManager.Transaction> transactions = getTransactionsForDate(selectedDate);
        List<Event> events = eventManager.getEventsByDate(selectedDate);
        List<Holiday> holidays = holidayManager.getHolidaysForDate(selectedDate);

        // Update scheduled event
        if (tvScheduledEvent != null) {
            StringBuilder eventText = new StringBuilder();
            
            // Show holidays first
            if (!holidays.isEmpty()) {
                for (Holiday holiday : holidays) {
                    if (eventText.length() > 0) eventText.append(", ");
                    eventText.append("🎉 ").append(holiday.getName());
                }
            }
            
            // Then show custom events
            if (!events.isEmpty()) {
                for (Event event : events) {
                    if (eventText.length() > 0) eventText.append("\n");
                    String timeText = event.getTime() != null && !event.getTime().isEmpty() ? 
                                     " (" + event.getTime() + ")" : "";
                    eventText.append("📅 ").append(event.getTitle()).append(timeText);
                }
            }
            
            if (eventText.length() == 0) {
                tvScheduledEvent.setText("No Event Scheduled.");
            } else {
                tvScheduledEvent.setText(eventText.toString());
            }
        }

        // Calculate daily totals
        double dailyIncome = 0.0;
        double dailyExpenses = 0.0;

        for (BudgetManager.Transaction transaction : transactions) {
            if (transaction.getType() == BudgetManager.Transaction.TransactionType.INCOME) {
                dailyIncome += transaction.getAmount();
            } else {
                dailyExpenses += transaction.getAmount();
            }
        }

        double dailyRemaining = dailyIncome - dailyExpenses;

        // Update daily totals
        if (tvDailyIncome != null) {
            tvDailyIncome.setText(currencyFormat.format(dailyIncome));
        }
        if (tvDailyExpenses != null) {
            tvDailyExpenses.setText(currencyFormat.format(dailyExpenses));
        }
        if (tvDailyRemaining != null) {
            tvDailyRemaining.setText(currencyFormat.format(dailyRemaining));
            // Color code the remaining amount
            if (dailyRemaining >= 0) {
                tvDailyRemaining.setTextColor(getResources().getColor(R.color.sage_green_darker));
            } else {
                tvDailyRemaining.setTextColor(getResources().getColor(R.color.error));
            }
        }
    }


    private void showEventManagementDialog(Date date) {
        try {
            android.util.Log.d("CalendarFragment", "showEventManagementDialog called with date: " + date);
            
            if (eventManager == null || date == null) {
                android.util.Log.e("CalendarFragment", "Event manager or date is null");
                Toast.makeText(getContext(), "Error: Event manager not initialized", Toast.LENGTH_SHORT).show();
                return;
            }
            
            List<Event> events = eventManager.getEventsByDate(date);
            android.util.Log.d("CalendarFragment", "Found " + events.size() + " events for date");
            
            if (events.isEmpty()) {
                // No event exists, show add dialog
                android.util.Log.d("CalendarFragment", "No events found, showing add dialog");
                showAddEventDialog(date);
            } else {
                // Event exists, show edit dialog
                android.util.Log.d("CalendarFragment", "Events found, showing edit dialog");
                showEditEventDialog(events.get(0));
            }
        } catch (Exception e) {
            android.util.Log.e("CalendarFragment", "Error in showEventManagementDialog", e);
            Toast.makeText(getContext(), "Error managing events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddEventDialog(Date date) {
        try {
            android.util.Log.d("CalendarFragment", "showAddEventDialog called with date: " + date);
            
            if (getContext() == null) {
                android.util.Log.e("CalendarFragment", "Context is null");
                return;
            }
            
            // Create and setup the dialog in one go
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_event, null);
            
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            
            // Setup the dialog content after creating the dialog
            setupEventDialogContent(dialogView, dialog, date);
            
            dialog.show();
            
            android.util.Log.d("CalendarFragment", "Dialog created and shown successfully");
        } catch (Exception e) {
            android.util.Log.e("CalendarFragment", "Error in showAddEventDialog", e);
            Toast.makeText(getContext(), "Error creating dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditEventDialog(Event event) {
        try {
            if (getContext() == null) {
                Toast.makeText(getContext(), "Context not available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_event, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            
            // Setup the dialog content after creating the dialog
            setupEditEventDialogContent(dialogView, dialog, event);
            
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error creating edit event dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupEventDialogContent(View dialogView, AlertDialog dialog, Date date) {
        android.util.Log.d("CalendarFragment", "setupEventDialogContent called");
        
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_event);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_event);
        
        android.util.Log.d("CalendarFragment", "Buttons found - Cancel: " + (btnCancel != null) + ", Save: " + (btnSave != null));
        
        if (btnCancel == null || btnSave == null) {
            android.util.Log.e("CalendarFragment", "Buttons not found!");
            return;
        }

        // Simple button setup
        btnCancel.setOnClickListener(v -> {
            android.util.Log.d("CalendarFragment", "Cancel button clicked");
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            android.util.Log.d("CalendarFragment", "Save button clicked");
            
            // Simple event creation for testing
            try {
                TextInputEditText etTitle = dialogView.findViewById(R.id.et_event_title);
                String title = etTitle != null ? etTitle.getText().toString().trim() : "Test Event";
                
                if (title.isEmpty()) {
                    title = "Test Event";
                }
                
                if (eventManager != null) {
                    Event event = new Event(title, "Test description", date, null, "Personal", "#2196F3", true);
                    eventManager.addEvent(event);
                    loadCalendarData();
                    Toast.makeText(getContext(), "Event created: " + title, Toast.LENGTH_SHORT).show();
                }
                
                dialog.dismiss();
            } catch (Exception e) {
                android.util.Log.e("CalendarFragment", "Error creating event", e);
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupEditEventDialogContent(View dialogView, AlertDialog dialog, Event event) {
        android.util.Log.d("CalendarFragment", "setupEditEventDialogContent called for event: " + event.getTitle());
        
        TextInputEditText etTitle = dialogView.findViewById(R.id.et_event_title);
        TextInputEditText etDescription = dialogView.findViewById(R.id.et_event_description);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_event);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_event);
        MaterialButton btnDelete = dialogView.findViewById(R.id.btn_delete_event);
        
        // Populate fields with existing event data
        if (etTitle != null) {
            etTitle.setText(event.getTitle());
        }
        if (etDescription != null) {
            etDescription.setText(event.getDescription());
        }
        
        android.util.Log.d("CalendarFragment", "Event data populated - Title: " + event.getTitle() + ", Description: " + event.getDescription());

        // Setup buttons - simplified approach
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                android.util.Log.d("CalendarFragment", "Cancel button clicked in edit dialog");
                dialog.dismiss();
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                android.util.Log.d("CalendarFragment", "Save button clicked in edit dialog");
                
                try {
                    String title = etTitle != null ? etTitle.getText().toString().trim() : event.getTitle();
                    String description = etDescription != null ? etDescription.getText().toString().trim() : event.getDescription();
                    
                    if (title.isEmpty()) {
                        title = event.getTitle();
                    }
                    
                    if (eventManager != null) {
                        event.setTitle(title);
                        event.setDescription(description);
                        eventManager.updateEvent(event);
                        loadCalendarData();
                        Toast.makeText(getContext(), "Event updated: " + title, Toast.LENGTH_SHORT).show();
                    }
                    
                    dialog.dismiss();
                } catch (Exception e) {
                    android.util.Log.e("CalendarFragment", "Error updating event", e);
                    Toast.makeText(getContext(), "Error updating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                android.util.Log.d("CalendarFragment", "Delete button clicked in edit dialog");
                
                try {
                    if (eventManager != null) {
                        eventManager.deleteEvent(event);
                        loadCalendarData();
                        Toast.makeText(getContext(), "Event deleted: " + event.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                } catch (Exception e) {
                    android.util.Log.e("CalendarFragment", "Error deleting event", e);
                    Toast.makeText(getContext(), "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCalendarData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}