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
        // 2024 Philippine Holidays
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        
        // Regular Holidays (Red)
        cal.set(year, Calendar.JANUARY, 1);
        holidays.add(new Holiday("New Year's Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.APRIL, 9);
        holidays.add(new Holiday("Araw ng Kagitingan (Day of Valor)", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.MAY, 1);
        holidays.add(new Holiday("Labor Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.JUNE, 12);
        holidays.add(new Holiday("Independence Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
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
        holidays.add(new Holiday("National Heroes Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.NOVEMBER, 30);
        holidays.add(new Holiday("Bonifacio Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.DECEMBER, 25);
        holidays.add(new Holiday("Christmas Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        cal.set(year, Calendar.DECEMBER, 30);
        holidays.add(new Holiday("Rizal Day", cal.getTime(), Holiday.HolidayType.REGULAR));
        
        // Special Non-Working Days (Blue)
        // Chinese New Year - February 10, 2024 (simplified, should use lunar calendar)
        cal.set(year, Calendar.FEBRUARY, 10);
        holidays.add(new Holiday("Chinese New Year", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        // Easter dates for 2024 (simplified)
        cal.set(year, Calendar.MARCH, 28);
        holidays.add(new Holiday("Maundy Thursday", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.MARCH, 29);
        holidays.add(new Holiday("Good Friday", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.MARCH, 30);
        holidays.add(new Holiday("Black Saturday", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        // Eid dates for 2024 (simplified)
        cal.set(year, Calendar.APRIL, 10);
        holidays.add(new Holiday("Eid'l Fitr", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.JUNE, 16);
        holidays.add(new Holiday("Eid'l Adha", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.AUGUST, 21);
        holidays.add(new Holiday("Ninoy Aquino Day", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.NOVEMBER, 1);
        holidays.add(new Holiday("All Saints' Day", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.NOVEMBER, 2);
        holidays.add(new Holiday("All Souls' Day", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.DECEMBER, 8);
        holidays.add(new Holiday("Feast of the Immaculate Conception", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.DECEMBER, 24);
        holidays.add(new Holiday("Christmas Eve", cal.getTime(), Holiday.HolidayType.SPECIAL));
        
        cal.set(year, Calendar.DECEMBER, 31);
        holidays.add(new Holiday("Last Day of the Year", cal.getTime(), Holiday.HolidayType.SPECIAL));
    }
    
    public List<Holiday> getHolidaysForDate(Date date) {
        List<Holiday> holidaysForDate = new ArrayList<>();
        for (Holiday holiday : holidays) {
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
