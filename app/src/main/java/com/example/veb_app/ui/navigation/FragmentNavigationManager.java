package com.example.veb_app.ui.navigation;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;

import com.example.veb_app.R;
import com.example.veb_app.ui.home.HomeFragment;
import com.example.veb_app.ui.notes.NotesFragment;
import com.example.veb_app.ui.checklist.ChecklistFragment;
import com.example.veb_app.ui.budget.BudgetFragment;
import com.example.veb_app.ui.calendar.CalendarFragment;

/**
 * Custom navigation manager that uses show/hide instead of replace
 * to preserve fragment state and prevent recreation
 */
public class FragmentNavigationManager {
    private static FragmentNavigationManager instance;
    private FragmentManager fragmentManager;
    private int containerId;
    
    // Fragment instances
    private HomeFragment homeFragment;
    private NotesFragment notesFragment;
    private ChecklistFragment checklistFragment;
    private BudgetFragment budgetFragment;
    private CalendarFragment calendarFragment;
    
    private Fragment currentFragment;
    
    private FragmentNavigationManager() {
        // Private constructor for singleton
    }
    
    public static synchronized FragmentNavigationManager getInstance() {
        if (instance == null) {
            instance = new FragmentNavigationManager();
        }
        return instance;
    }
    
    public void initialize(FragmentManager fragmentManager, int containerId) {
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
        Log.d("FragmentNavigationManager", "Initialized with container ID: " + containerId);
    }
    
    /**
     * Navigate to a specific fragment using show/hide pattern
     */
    public void navigateToFragment(String fragmentTag) {
        if (fragmentManager == null) {
            Log.e("FragmentNavigationManager", "FragmentManager not initialized");
            return;
        }
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Hide current fragment if it exists
        if (currentFragment != null && currentFragment.isVisible()) {
            transaction.hide(currentFragment);
            Log.d("FragmentNavigationManager", "Hiding current fragment: " + currentFragment.getClass().getSimpleName());
        }
        
        // Get or create the target fragment
        Fragment targetFragment = getOrCreateFragment(fragmentTag);
        
        if (targetFragment != null) {
            if (targetFragment.isAdded()) {
                // Fragment already exists, just show it
                transaction.show(targetFragment);
                Log.d("FragmentNavigationManager", "Showing existing fragment: " + fragmentTag);
            } else {
                // Fragment doesn't exist, add it
                transaction.add(containerId, targetFragment, fragmentTag);
                Log.d("FragmentNavigationManager", "Adding new fragment: " + fragmentTag);
            }
            
            currentFragment = targetFragment;
        }
        
        transaction.commit();
    }
    
    /**
     * Get or create a fragment instance
     */
    private Fragment getOrCreateFragment(String fragmentTag) {
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
        
        if (fragment == null) {
            // Create new fragment instance
            switch (fragmentTag) {
                case "home":
                    if (homeFragment == null) {
                        homeFragment = new HomeFragment();
                    }
                    fragment = homeFragment;
                    break;
                case "notes":
                    if (notesFragment == null) {
                        notesFragment = new NotesFragment();
                    }
                    fragment = notesFragment;
                    break;
                case "checklist":
                    if (checklistFragment == null) {
                        checklistFragment = new ChecklistFragment();
                    }
                    fragment = checklistFragment;
                    break;
                case "budget":
                    if (budgetFragment == null) {
                        budgetFragment = new BudgetFragment();
                    }
                    fragment = budgetFragment;
                    break;
                case "calendar":
                    if (calendarFragment == null) {
                        calendarFragment = new CalendarFragment();
                    }
                    fragment = calendarFragment;
                    break;
                default:
                    Log.e("FragmentNavigationManager", "Unknown fragment tag: " + fragmentTag);
                    return null;
            }
        }
        
        return fragment;
    }
    
    /**
     * Get current fragment
     */
    public Fragment getCurrentFragment() {
        return currentFragment;
    }
    
    /**
     * Check if a fragment is currently visible
     */
    public boolean isFragmentVisible(String fragmentTag) {
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
        return fragment != null && fragment.isVisible();
    }
    
    /**
     * Get fragment instance by tag
     */
    public Fragment getFragment(String fragmentTag) {
        return fragmentManager.findFragmentByTag(fragmentTag);
    }
}

