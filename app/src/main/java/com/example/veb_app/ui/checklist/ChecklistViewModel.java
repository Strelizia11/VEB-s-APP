package com.example.veb_app.ui.checklist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChecklistViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ChecklistViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Checklist - Organize your tasks and goals");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
