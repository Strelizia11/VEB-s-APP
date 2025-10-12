package com.example.veb_app.ui.calendar;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class EventManager {
    private static EventManager instance;
    private final List<Event> events;
    private SharedPreferences prefs;

    private EventManager() {
        events = new ArrayList<>();
        initializeDefaultEvents();
    }

    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        prefs = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE);
        loadData();
    }

    private void initializeDefaultEvents() {
        // Add some default event categories and colors
        // These will be used as suggestions when creating events
    }

    // Event Management
    public void addEvent(Event event) {
        events.add(event);
        saveData();
    }

    public void updateEvent(Event updatedEvent) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId() == updatedEvent.getId()) {
                events.set(i, updatedEvent);
                break;
            }
        }
        saveData();
    }

    public void deleteEvent(Event event) {
        events.remove(event);
        saveData();
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }

    public List<Event> getEventsByDate(Date date) {
        List<Event> eventsForDate = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        Calendar eventCal = Calendar.getInstance();
        
        cal.setTime(date);
        int targetYear = cal.get(Calendar.YEAR);
        int targetMonth = cal.get(Calendar.MONTH);
        int targetDay = cal.get(Calendar.DAY_OF_MONTH);

        for (Event event : events) {
            eventCal.setTime(event.getDate());
            if (eventCal.get(Calendar.YEAR) == targetYear &&
                eventCal.get(Calendar.MONTH) == targetMonth &&
                eventCal.get(Calendar.DAY_OF_MONTH) == targetDay) {
                eventsForDate.add(event);
            }
        }

        // Sort events by time (all-day events first, then by time)
        Collections.sort(eventsForDate, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                if (e1.isAllDay() && !e2.isAllDay()) return -1;
                if (!e1.isAllDay() && e2.isAllDay()) return 1;
                if (e1.isAllDay() && e2.isAllDay()) return 0;
                
                // Compare by time
                String time1 = e1.getTime() != null ? e1.getTime() : "00:00";
                String time2 = e2.getTime() != null ? e2.getTime() : "00:00";
                return time1.compareTo(time2);
            }
        });

        return eventsForDate;
    }

    public List<Event> getEventsByMonth(int year, int month) {
        List<Event> eventsForMonth = new ArrayList<>();
        Calendar eventCal = Calendar.getInstance();

        for (Event event : events) {
            eventCal.setTime(event.getDate());
            if (eventCal.get(Calendar.YEAR) == year && eventCal.get(Calendar.MONTH) == month) {
                eventsForMonth.add(event);
            }
        }

        return eventsForMonth;
    }

    // Event Categories
    public List<String> getEventCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Personal");
        categories.add("Work");
        categories.add("Health");
        categories.add("Finance");
        categories.add("Social");
        categories.add("Education");
        categories.add("Travel");
        categories.add("Other");
        return categories;
    }

    public List<String> getEventColors() {
        List<String> colors = new ArrayList<>();
        colors.add("#FF6B6B"); // Red
        colors.add("#4ECDC4"); // Teal
        colors.add("#45B7D1"); // Blue
        colors.add("#96CEB4"); // Green
        colors.add("#FECA57"); // Yellow
        colors.add("#FF9FF3"); // Pink
        colors.add("#54A0FF"); // Light Blue
        colors.add("#5F27CD"); // Purple
        colors.add("#9CAF88"); // Sage Green
        colors.add("#FFA726"); // Orange
        return colors;
    }

    // Persistence
    private void saveData() {
        if (prefs == null) return;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("event_count", events.size());
        editor.apply();

        // Save events
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            String prefix = "event_" + i;
            editor.putLong(prefix + "_id", event.getId());
            editor.putString(prefix + "_title", event.getTitle());
            editor.putString(prefix + "_description", event.getDescription());
            editor.putLong(prefix + "_date", event.getDate().getTime());
            editor.putString(prefix + "_time", event.getTime());
            editor.putString(prefix + "_category", event.getCategory());
            editor.putString(prefix + "_color", event.getColor());
            editor.putBoolean(prefix + "_isAllDay", event.isAllDay());
        }
        editor.apply();
    }

    private void loadData() {
        if (prefs == null) return;

        int eventCount = prefs.getInt("event_count", 0);
        events.clear();

        for (int i = 0; i < eventCount; i++) {
            String prefix = "event_" + i;
            long id = prefs.getLong(prefix + "_id", 0);
            String title = prefs.getString(prefix + "_title", "");
            String description = prefs.getString(prefix + "_description", "");
            long dateTime = prefs.getLong(prefix + "_date", System.currentTimeMillis());
            String time = prefs.getString(prefix + "_time", "");
            String category = prefs.getString(prefix + "_category", "Personal");
            String color = prefs.getString(prefix + "_color", "#9CAF88");
            boolean isAllDay = prefs.getBoolean(prefix + "_isAllDay", false);

            Event event = new Event(id, title, description, new Date(dateTime), time, category, color, isAllDay);
            events.add(event);
        }
    }
}
