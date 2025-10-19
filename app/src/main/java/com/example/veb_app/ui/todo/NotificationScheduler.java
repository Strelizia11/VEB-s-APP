package com.example.veb_app.ui.todo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Utility class to schedule deadline notifications
 */
public class NotificationScheduler {
    
    public static void scheduleDeadlineNotifications(Context context, TodoItem todo) {
        if (todo.getDeadline() <= 0) {
            return; // No deadline set
        }
        
        // Cancel existing notifications for this todo
        cancelDeadlineNotifications(context, todo);
        
        long deadline = todo.getDeadline();
        Calendar deadlineCal = Calendar.getInstance();
        deadlineCal.setTimeInMillis(deadline);
        
        // Schedule notification for 1 day before
        Calendar dayBeforeCal = Calendar.getInstance();
        dayBeforeCal.setTimeInMillis(deadline);
        dayBeforeCal.add(Calendar.DAY_OF_MONTH, -1);
        dayBeforeCal.set(Calendar.HOUR_OF_DAY, 9); // 9 AM
        dayBeforeCal.set(Calendar.MINUTE, 0);
        dayBeforeCal.set(Calendar.SECOND, 0);
        dayBeforeCal.set(Calendar.MILLISECOND, 0);
        
        // Schedule notification for exact day
        Calendar exactDayCal = Calendar.getInstance();
        exactDayCal.setTimeInMillis(deadline);
        exactDayCal.set(Calendar.HOUR_OF_DAY, 9); // 9 AM
        exactDayCal.set(Calendar.MINUTE, 0);
        exactDayCal.set(Calendar.SECOND, 0);
        exactDayCal.set(Calendar.MILLISECOND, 0);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Schedule day before notification
        if (dayBeforeCal.getTimeInMillis() > System.currentTimeMillis()) {
            Intent dayBeforeIntent = new Intent(context, DeadlineNotificationService.class);
            dayBeforeIntent.putExtra("todo_title", todo.getTitle());
            dayBeforeIntent.putExtra("deadline", deadline);
            dayBeforeIntent.putExtra("is_day_before", true);
            dayBeforeIntent.putExtra("todo_id", todo.getId());
            
            PendingIntent dayBeforePending = PendingIntent.getBroadcast(
                context, 
                (int) (deadline + 1), // Unique ID for day before
                dayBeforeIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, dayBeforeCal.getTimeInMillis(), dayBeforePending);
        }
        
        // Schedule exact day notification
        if (exactDayCal.getTimeInMillis() > System.currentTimeMillis()) {
            Intent exactDayIntent = new Intent(context, DeadlineNotificationService.class);
            exactDayIntent.putExtra("todo_title", todo.getTitle());
            exactDayIntent.putExtra("deadline", deadline);
            exactDayIntent.putExtra("is_day_before", false);
            exactDayIntent.putExtra("todo_id", todo.getId());
            
            PendingIntent exactDayPending = PendingIntent.getBroadcast(
                context, 
                (int) deadline, // Unique ID for exact day
                exactDayIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, exactDayCal.getTimeInMillis(), exactDayPending);
        }
    }
    
    public static void cancelDeadlineNotifications(Context context, TodoItem todo) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Cancel day before notification
        Intent dayBeforeIntent = new Intent(context, DeadlineNotificationService.class);
        PendingIntent dayBeforePending = PendingIntent.getBroadcast(
            context, 
            (int) (todo.getDeadline() + 1), 
            dayBeforeIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(dayBeforePending);
        
        // Cancel exact day notification
        Intent exactDayIntent = new Intent(context, DeadlineNotificationService.class);
        PendingIntent exactDayPending = PendingIntent.getBroadcast(
            context, 
            (int) todo.getDeadline(), 
            exactDayIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(exactDayPending);
    }
}
