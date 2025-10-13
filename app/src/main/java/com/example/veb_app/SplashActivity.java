package com.example.veb_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500; // 1.5 seconds (reduced from 2)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // The splash screen is defined by the theme, no need to set content view
        
        // Navigate to MainActivity after delay with smooth transition
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            
            // Clear the back stack and make MainActivity the root
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            startActivity(intent);
            
            // Add smooth fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            
            finish();
        }, SPLASH_DELAY);
    }
}

