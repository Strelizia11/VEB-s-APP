package com.example.veb_app.ui.checklist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import java.util.ArrayList;

public class ChecklistViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<List<ChecklistFragment.Checklist>> mChecklists;
    private boolean isDataLoaded = false;
    private boolean isFragmentLoaded = false;

    public ChecklistViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("");
        mChecklists = new MutableLiveData<>();
        mChecklists.setValue(new ArrayList<>());
    }

    public LiveData<String> getText() {
        return mText;
    }
    
    public LiveData<List<ChecklistFragment.Checklist>> getChecklists() {
        return mChecklists;
    }
    
    public void setChecklists(List<ChecklistFragment.Checklist> checklists) {
        mChecklists.setValue(checklists);
        isDataLoaded = true;
    }
    
    public boolean isDataLoaded() {
        return isDataLoaded;
    }
    
    public void setDataLoaded(boolean loaded) {
        isDataLoaded = loaded;
    }
    
    public boolean isFragmentLoaded() {
        return isFragmentLoaded;
    }
    
    public void setFragmentLoaded(boolean loaded) {
        isFragmentLoaded = loaded;
    }
}
