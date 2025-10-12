package com.example.veb_app;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import com.example.veb_app.databinding.ActivityMainBinding;
import com.example.veb_app.data.DatabaseInitializer;
import com.example.veb_app.data.DatabaseManager;
import com.example.veb_app.ui.notes.NotesManager;
import com.example.veb_app.ui.checklist.ChecklistManager;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Force light mode regardless of system theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        // Initialize the database
        DatabaseInitializer.initialize(this);
        DatabaseManager.getInstance(this); // Initialize database managers
        
        // Initialize SharedPreferences managers
        NotesManager.getInstance().initialize(this);
        ChecklistManager.getInstance().initialize(this);
        
        // Request notification permission for Android 13+
        requestNotificationPermission();

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup burger menu click listener
        ImageButton burgerMenu = findViewById(R.id.btn_burger_menu);
        if (burgerMenu != null) {
            burgerMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open navigation drawer
                    if (binding.drawerLayout != null) {
                        binding.drawerLayout.openDrawer(binding.navView);
                    }
                }
            });
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationView navigationView = binding.navView;
        if (navigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home, R.id.nav_notes, R.id.nav_checklist, R.id.nav_budget, R.id.nav_calendar)
                    .setOpenableLayout(binding.drawerLayout)
                    .build();
            NavigationUI.setupWithNavController(navigationView, navController);
        }

        // Setup page title updates
        setupPageTitleUpdates(navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // No overflow menu needed since we're using navigation drawer
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Settings button functionality removed as requested
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void setupPageTitleUpdates(NavController navController) {
        TextView pageTitle = findViewById(R.id.tv_page_title);
        if (pageTitle != null) {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                String title = "Home"; // Default title
                
                if (destination.getId() == R.id.nav_home) {
                    title = "Home";
                } else if (destination.getId() == R.id.nav_notes) {
                    title = "Notes";
                } else if (destination.getId() == R.id.nav_checklist) {
                    title = "Checklist";
                } else if (destination.getId() == R.id.nav_budget) {
                    title = "Budget";
                } else if (destination.getId() == R.id.nav_calendar) {
                    title = "Calendar";
                }
                
                pageTitle.setText(title);
            });
        }
    }
    
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }
    
}