package com.ocean.bluectrl;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FanControlViewModel extends ViewModel {
    private final MutableLiveData<String> textData = new MutableLiveData<>();

    public void setText(String text) {
        textData.setValue(text);
    }

    public LiveData<String> getText() {
        return textData;
    }
}
