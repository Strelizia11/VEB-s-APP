package com.example.veb_app.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.example.veb_app.ui.calendar.Event;

import java.util.Calendar;
import java.util.Date;

public class EventNotificationScheduler {
    private static final String TAG = "EventNotificationScheduler";
    private Context context;

    public EventNotificationScheduler(Context context) {
        this.context = context;
    }

    /**
     * Schedule notifications for an event (one day before and on the event day)
     */
    public void scheduleEventNotifications(Event event) {
        if (event == null || event.getDate() == null) {
            return;
        }

        // Schedule notification one day before at 9:00 AM
        scheduleDayBeforeNotification(event);

        // Schedule notification on the event day at 9:00 AM
        scheduleEventDayNotification(event);
    }

    /**
     * Schedule notification one day before the event
     */
    private void scheduleDayBeforeNotification(Event event) {
        Calendar eventCalendar = Calendar.getInstance();
        eventCalendar.setTime(event.getDate());

        // Set to one day before at 9:00 AM
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.setTime(event.getDate());
        notificationTime.add(Calendar.DAY_OF_MONTH, -1);
        notificationTime.set(Calendar.HOUR_OF_DAY, 9);
        notificationTime.set(Calendar.MINUTE, 0);
        notificationTime.set(Calendar.SECOND, 0);

        // For testing: if notification time is in the past, schedule for 30 seconds from now
        if (notificationTime.getTimeInMillis() <= System.currentTimeMillis()) {
            notificationTime = Calendar.getInstance();
            notificationTime.add(Calendar.SECOND, 30);
            Log.d(TAG, "Day-before notification scheduled for testing (30 seconds from now)");
        }

        scheduleNotification(event, notificationTime.getTimeInMillis(), true);
        Log.d(TAG, "Scheduled day-before notification for: " + event.getTitle() + " at " + notificationTime.getTime());
    }

    /**
     * Schedule notification on the event day
     */
    private void scheduleEventDayNotification(Event event) {
        Calendar eventCalendar = Calendar.getInstance();
        eventCalendar.setTime(event.getDate());

        // Set to event day at 9:00 AM
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.setTime(event.getDate());
        notificationTime.set(Calendar.HOUR_OF_DAY, 9);
        notificationTime.set(Calendar.MINUTE, 0);
        notificationTime.set(Calendar.SECOND, 0);

        // For testing: if notification time is in the past, schedule for 60 seconds from now
        if (notificationTime.getTimeInMillis() <= System.currentTimeMillis()) {
            notificationTime = Calendar.getInstance();
            notificationTime.add(Calendar.SECOND, 60);
            Log.d(TAG, "Event-day notification scheduled for testing (60 seconds from now)");
        }

        scheduleNotification(event, notificationTime.getTimeInMillis(), false);
        Log.d(TAG, "Scheduled event-day notification for: " + event.getTitle() + " at " + notificationTime.getTime());
    }

    /**
     * Schedule a notification using AlarmManager
     */
    private void scheduleNotification(Event event, long triggerTime, boolean isDayBefore) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, EventNotificationReceiver.class);
        intent.putExtra(EventNotificationReceiver.EXTRA_EVENT_TITLE, event.getTitle());
        intent.putExtra(EventNotificationReceiver.EXTRA_EVENT_DESCRIPTION, event.getDescription());
        intent.putExtra(EventNotificationReceiver.EXTRA_IS_DAY_BEFORE, isDayBefore);

        // Create unique request code for each notification
        int requestCode = generateRequestCode(event.getId(), isDayBefore);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Schedule the alarm
        try {
            Log.d(TAG, "Scheduling notification for: " + event.getTitle() + " at " + new Date(triggerTime));
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
                Log.d(TAG, "Notification scheduled successfully (setExactAndAllowWhileIdle)");
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
                Log.d(TAG, "Notification scheduled successfully (setExact)");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule notification: " + e.getMessage());
        }
    }

    /**
     * Cancel all notifications for an event
     */
    public void cancelEventNotifications(Event event) {
        if (event == null) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // Cancel day-before notification
        cancelNotification(alarmManager, event.getId(), true);

        // Cancel event-day notification
        cancelNotification(alarmManager, event.getId(), false);

        Log.d(TAG, "Cancelled notifications for: " + event.getTitle());
    }

    /**
     * Cancel a specific notification
     */
    private void cancelNotification(AlarmManager alarmManager, long eventId, boolean isDayBefore) {
        Intent intent = new Intent(context, EventNotificationReceiver.class);
        int requestCode = generateRequestCode(eventId, isDayBefore);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    /**
     * Generate a unique request code for notifications
     */
    private int generateRequestCode(long eventId, boolean isDayBefore) {
        // Combine event ID with notification type (day before = 1, event day = 0)
        return (int) (eventId * 10 + (isDayBefore ? 1 : 0));
    }
}

