package com.example.veb_app.ui.calendar;

import java.util.Date;

public class Event {
    private long id;
    private String title;
    private String description;
    private Date date;
    private String time; // Optional time (e.g., "14:30")
    private String category;
    private String color; // Hex color for event indicator
    private boolean isAllDay;

    public Event(String title, String description, Date date, String time, String category, String color, boolean isAllDay) {
        this.id = System.currentTimeMillis();
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.category = category;
        this.color = color;
        this.isAllDay = isAllDay;
    }

    public Event(long id, String title, String description, Date date, String time, String category, String color, boolean isAllDay) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.category = category;
        this.color = color;
        this.isAllDay = isAllDay;
    }

    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Date getDate() { return date; }
    public String getTime() { return time; }
    public String getCategory() { return category; }
    public String getColor() { return color; }
    public boolean isAllDay() { return isAllDay; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(Date date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setCategory(String category) { this.category = category; }
    public void setColor(String color) { this.color = color; }
    public void setAllDay(boolean allDay) { isAllDay = allDay; }
}
