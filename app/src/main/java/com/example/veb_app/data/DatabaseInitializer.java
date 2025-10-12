package com.example.veb_app.data;

import android.content.Context;
import android.util.Log;

public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";
    private static boolean isInitialized = false;
    
    public static synchronized void initialize(Context context) {
        if (isInitialized) {
            Log.d(TAG, "Database already initialized, skipping...");
            return;
        }
        
        Log.d(TAG, "Initializing database...");
        
        try {
            // Initialize the database (this will create the database file if it doesn't exist)
            AppDatabase database = AppDatabase.getDatabase(context);
            
            Log.d(TAG, "Database is ready - no migration needed for fresh start");
            
            isInitialized = true;
            Log.d(TAG, "Database initialization completed successfully!");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    public static boolean isInitialized() {
        return isInitialized;
    }
}
