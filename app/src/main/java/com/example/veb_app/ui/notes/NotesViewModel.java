package com.example.veb_app.ui.notes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public NotesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Notes - Capture your thoughts and ideas");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
