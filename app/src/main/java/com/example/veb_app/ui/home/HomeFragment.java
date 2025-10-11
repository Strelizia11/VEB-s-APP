package com.example.veb_app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.veb_app.R;
import com.example.veb_app.databinding.FragmentHomeBinding;
import com.example.veb_app.ui.notes.NotesManager;
import com.example.veb_app.ui.notes.NotesFragment;
import com.example.veb_app.ui.checklist.ChecklistManager;
import com.example.veb_app.ui.checklist.ChecklistFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        
        // Setup featured note and checklist display
        setupFeaturedNote(root);
        setupFeaturedChecklist(root);
        
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh featured note and checklist when returning to home
        if (binding != null) {
            setupFeaturedNote(binding.getRoot());
            setupFeaturedChecklist(binding.getRoot());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupFeaturedNote(View root) {
        MaterialCardView cardFeaturedNote = root.findViewById(R.id.card_featured_note);
        TextView tvFeaturedNoteTitle = root.findViewById(R.id.tv_featured_note_title);
        TextView tvFeaturedNoteContent = root.findViewById(R.id.tv_featured_note_content);
        MaterialButton btnViewAllNotes = root.findViewById(R.id.btn_view_all_notes);
        TextView tvNoNotesMessage = root.findViewById(R.id.tv_no_notes_message);

        // Get featured note (pinned or most recent)
        NotesManager notesManager = NotesManager.getInstance();
        NotesFragment.Note featuredNote = notesManager.getFeaturedNote();

        if (featuredNote != null) {
            // Display featured note
            tvFeaturedNoteTitle.setText(featuredNote.getTitle());
            
            // Truncate content for preview
            String content = featuredNote.getBody();
            if (content.length() > 150) {
                content = content.substring(0, 147) + "...";
            }
            tvFeaturedNoteContent.setText(content);
            
            // Show featured note card, hide no notes message
            cardFeaturedNote.setVisibility(View.VISIBLE);
            tvNoNotesMessage.setVisibility(View.GONE);
        } else {
            // No notes available
            cardFeaturedNote.setVisibility(View.GONE);
            tvNoNotesMessage.setVisibility(View.VISIBLE);
        }

        // Setup View All Notes button
        btnViewAllNotes.setOnClickListener(v -> {
            // Trigger the NavigationView's built-in navigation mechanism
            if (getActivity() != null) {
                com.google.android.material.navigation.NavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null) {
                    android.view.MenuItem notesMenuItem = navView.getMenu().findItem(R.id.nav_notes);
                    if (notesMenuItem != null) {
                        // Trigger the menu item's click event which will use NavigationUI
                        notesMenuItem.setChecked(true);
                        
                        // Get the NavController and navigate using NavigationUI
                        androidx.navigation.fragment.NavHostFragment navHostFragment = 
                            (androidx.navigation.fragment.NavHostFragment) getActivity().getSupportFragmentManager()
                                .findFragmentById(R.id.nav_host_fragment_content_main);
                        if (navHostFragment != null) {
                            NavController navController = navHostFragment.getNavController();
                            // Use NavigationUI to handle the navigation properly
                            androidx.navigation.ui.NavigationUI.onNavDestinationSelected(notesMenuItem, navController);
                        }
                    }
                }
            }
        });
    }

    private void setupFeaturedChecklist(View root) {
        MaterialCardView cardFeaturedChecklist = root.findViewById(R.id.card_featured_checklist);
        TextView tvFeaturedChecklistTitle = root.findViewById(R.id.tv_featured_checklist_title);
        TextView tvFeaturedChecklistProgress = root.findViewById(R.id.tv_featured_checklist_progress);
        CircularProgressIndicator progressCircle = root.findViewById(R.id.progress_circle_checklist);
        MaterialButton btnViewAllChecklists = root.findViewById(R.id.btn_view_all_checklists);
        TextView tvNoChecklistsMessage = root.findViewById(R.id.tv_no_checklists_message);

        // Get featured checklist (pinned or most recent)
        ChecklistManager checklistManager = ChecklistManager.getInstance();
        ChecklistFragment.Checklist featuredChecklist = checklistManager.getFeaturedChecklist();

        if (featuredChecklist != null) {
            // Display featured checklist
            tvFeaturedChecklistTitle.setText(featuredChecklist.getTitle());
            
            // Calculate completion stats
            int completedCount = 0;
            int totalTasks = featuredChecklist.getTasks().size();
            
            for (ChecklistFragment.Checklist.Task task : featuredChecklist.getTasks()) {
                if (task.isCompleted()) {
                    completedCount++;
                }
            }
            
            int progress = totalTasks > 0 ? (completedCount * 100) / totalTasks : 0;
            
            // Set progress text and circle
            tvFeaturedChecklistProgress.setText(completedCount + " of " + totalTasks + " completed");
            progressCircle.setProgress(progress);
            
            // Show featured checklist card, hide no checklists message
            cardFeaturedChecklist.setVisibility(View.VISIBLE);
            tvNoChecklistsMessage.setVisibility(View.GONE);
        } else {
            // No checklists available
            cardFeaturedChecklist.setVisibility(View.GONE);
            tvNoChecklistsMessage.setVisibility(View.VISIBLE);
        }

        // Setup View All Checklists button
        btnViewAllChecklists.setOnClickListener(v -> {
            // Trigger the NavigationView's built-in navigation mechanism
            if (getActivity() != null) {
                com.google.android.material.navigation.NavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null) {
                    android.view.MenuItem checklistMenuItem = navView.getMenu().findItem(R.id.nav_checklist);
                    if (checklistMenuItem != null) {
                        // Trigger the menu item's click event which will use NavigationUI
                        checklistMenuItem.setChecked(true);
                        
                        // Get the NavController and navigate using NavigationUI
                        androidx.navigation.fragment.NavHostFragment navHostFragment = 
                            (androidx.navigation.fragment.NavHostFragment) getActivity().getSupportFragmentManager()
                                .findFragmentById(R.id.nav_host_fragment_content_main);
                        if (navHostFragment != null) {
                            NavController navController = navHostFragment.getNavController();
                            // Use NavigationUI to handle the navigation properly
                            androidx.navigation.ui.NavigationUI.onNavDestinationSelected(checklistMenuItem, navController);
                        }
                    }
                }
            }
        });
    }
}
