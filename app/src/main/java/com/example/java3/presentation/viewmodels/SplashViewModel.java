package com.example.java3.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.java3.data.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class SplashViewModel extends ViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    public SplashViewModel() {
        this.repository = new AuthRepository();
        checkCurrentUser();
    }

    private void checkCurrentUser() {
        userLiveData.setValue(repository.getCurrentUser());
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }
}
