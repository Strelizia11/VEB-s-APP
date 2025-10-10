package com.example.veb_app.ui.checklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.veb_app.databinding.FragmentChecklistBinding;

public class ChecklistFragment extends Fragment {

    private FragmentChecklistBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ChecklistViewModel checklistViewModel =
                new ViewModelProvider(this).get(ChecklistViewModel.class);

        binding = FragmentChecklistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textChecklist;
        checklistViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
