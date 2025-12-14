package com.example.veb_app.ui.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HolidayManager {
    private static HolidayManager instance;
    private final List<Holiday> holidays;
    
    private HolidayManager() {
        holidays = new ArrayList<>();
        initializePhilippineHolidays();
    }
    
    public static synchronized HolidayManager getInstance() {
        if (instance == null) {
            instance = new HolidayManager();
        }
        return instance;
    }
    
    private void initializePhilippineHolidays() {
        // Philippine Holidays - will be generated dynamically for any year
        // This method now just sets up the holiday structure
        // Actual holidays will be generated in getHolidaysForDate()
    }
    
    private List<Holiday> generateHolidaysForYear(int year) {
        List<Holiday> yearHolidays = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        
        // Regular Holidays (Red)
        cal.set(year, Calendar.JANUARY, 1);
        yearHolidays.add(new Holiday("New Year's Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.APRIL, 9);
        yearHolidays.add(new Holiday("Araw ng Kagitingan (Day of Valor)", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.MAY, 1);
        yearHolidays.add(new Holiday("Labor Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.JUNE, 12);
        yearHolidays.add(new Holiday("Independence Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        // National Heroes Day - Last Monday of August
        cal.set(year, Calendar.AUGUST, 1);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        // Find the last Monday
        while (cal.get(Calendar.MONTH) == Calendar.AUGUST) {
            cal.add(Calendar.DAY_OF_MONTH, 7);
        }
        cal.add(Calendar.DAY_OF_MONTH, -7);
        yearHolidays.add(new Holiday("National Heroes Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.NOVEMBER, 30);
        yearHolidays.add(new Holiday("Bonifacio Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.DECEMBER, 25);
        yearHolidays.add(new Holiday("Christmas Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.DECEMBER, 30);
        yearHolidays.add(new Holiday("Rizal Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        // Special Non-Working Days (Blue)
        
        
        cal.set(year, Calendar.AUGUST, 21);
        yearHolidays.add(new Holiday("Ninoy Aquino Day", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.NOVEMBER, 1);
        yearHolidays.add(new Holiday("All Saints' Day", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.NOVEMBER, 2);
        yearHolidays.add(new Holiday("All Souls' Day", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.DECEMBER, 8);
        yearHolidays.add(new Holiday("Feast of the Immaculate Conception", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.DECEMBER, 24);
        yearHolidays.add(new Holiday("Christmas Eve", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.DECEMBER, 31);
        yearHolidays.add(new Holiday("Last Day of the Year", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        return yearHolidays;
    }
    
    public List<Holiday> getHolidaysForDate(Date date) {
        List<Holiday> holidaysForDate = new ArrayList<>();
        
        if (date == null) return holidaysForDate;
        
        // Get the year of the requested date
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        int year = dateCal.get(Calendar.YEAR);
        
        // Generate holidays for that year
        List<Holiday> yearHolidays = generateHolidaysForYear(year);
        
        // Check if any of the year's holidays match the requested date
        for (Holiday holiday : yearHolidays) {
            if (holiday.isSameDate(date)) {
                holidaysForDate.add(holiday);
            }
        }
        
        return holidaysForDate;
    }
    
    public boolean isHoliday(Date date) {
        return !getHolidaysForDate(date).isEmpty();
    }
    
    public List<Holiday> getAllHolidays() {
        return new ArrayList<>(holidays);
    }
}
