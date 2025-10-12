package com.example.veb_app.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.veb_app.MainActivity;
import com.example.veb_app.R;

public class EventNotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "event_notifications";
    private static final String CHANNEL_NAME = "Event Reminders";
    public static final String EXTRA_EVENT_TITLE = "event_title";
    public static final String EXTRA_EVENT_DESCRIPTION = "event_description";
    public static final String EXTRA_IS_DAY_BEFORE = "is_day_before";

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE);
        String eventDescription = intent.getStringExtra(EXTRA_EVENT_DESCRIPTION);
        boolean isDayBefore = intent.getBooleanExtra(EXTRA_IS_DAY_BEFORE, false);

        if (eventTitle == null) {
            return;
        }

        createNotificationChannel(context);
        showNotification(context, eventTitle, eventDescription, isDayBefore);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for upcoming events");
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(Context context, String title, String description, boolean isDayBefore) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent to open the app when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build notification message
        String message;
        String notificationTitle;
        if (isDayBefore) {
            notificationTitle = "Tomorrow's Event Reminder";
            message = "Don't forget: " + title;
        } else {
            notificationTitle = "Event Today!";
            message = title;
        }

        if (description != null && !description.isEmpty()) {
            message += "\n" + description;
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_schedule_24dp)
                .setContentTitle(notificationTitle)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 200, 500});

        // Generate unique notification ID based on title and time
        int notificationId = (title + System.currentTimeMillis()).hashCode();

        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
}

