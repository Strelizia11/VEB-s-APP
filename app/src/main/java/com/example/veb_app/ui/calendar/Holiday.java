package com.example.veb_app.ui.calendar;

import java.util.Calendar;
import java.util.Date;

public class Holiday {
    private String name;
    private Date date;
    private HolidayType type;
    
    public enum HolidayType {
        REGULAR,    // Red numbers
        SPECIAL     // Blue numbers
    }
    
    public Holiday(String name, Date date, HolidayType type) {
        this.name = name;
        this.date = date;
        this.type = type;
    }
    
    // Getters
    public String getName() { return name; }
    public Date getDate() { return date; }
    public HolidayType getType() { return type; }
    
    // Check if a given date is this holiday
    public boolean isSameDate(Date checkDate) {
        if (checkDate == null || this.date == null) return false;
        
        Calendar holidayCal = Calendar.getInstance();
        holidayCal.setTime(this.date);
        Calendar checkCal = Calendar.getInstance();
        checkCal.setTime(checkDate);
        
        return holidayCal.get(Calendar.YEAR) == checkCal.get(Calendar.YEAR) &&
               holidayCal.get(Calendar.MONTH) == checkCal.get(Calendar.MONTH) &&
               holidayCal.get(Calendar.DAY_OF_MONTH) == checkCal.get(Calendar.DAY_OF_MONTH);
    }
}
