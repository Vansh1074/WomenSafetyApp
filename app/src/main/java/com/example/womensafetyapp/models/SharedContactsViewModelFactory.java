package com.example.womensafetyapp.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SharedContactsViewModelFactory implements ViewModelProvider.Factory {
    private Application application;

    public SharedContactsViewModelFactory(Application application) {
        this.application = application;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SharedContactsViewModel.class)) {
            return (T) new SharedContactsViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
