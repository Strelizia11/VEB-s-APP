package com.example.veb_app.ui.todo;

import android.app.Notification;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Service to handle deadline notifications for to-dos
 */
public class DeadlineNotificationService extends BroadcastReceiver {
    
    private static final String CHANNEL_ID = "deadline_notifications";
    private static final String CHANNEL_NAME = "Deadline Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for to-do deadlines";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String todoTitle = intent.getStringExtra("todo_title");
        long deadline = intent.getLongExtra("deadline", 0);
        boolean isDayBefore = intent.getBooleanExtra("is_day_before", false);
        
        createNotificationChannel(context);
        showNotification(context, todoTitle, deadline, isDayBefore);
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            
            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private void showNotification(Context context, String todoTitle, long deadline, boolean isDayBefore) {
        String title;
        String message;
        
        if (isDayBefore) {
            title = "Deadline Tomorrow";
            message = "Tomorrow is the deadline of: " + todoTitle;
        } else {
            title = "Deadline Today";
            message = "Today is the deadline of: " + todoTitle;
        }
        
        // Create intent to open the app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Create notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_checklist_24dp)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build();
        
        // Show notification
        NotificationManager notificationManager = 
            context.getSystemService(NotificationManager.class);
        notificationManager.notify((int) deadline, notification);
    }
}
