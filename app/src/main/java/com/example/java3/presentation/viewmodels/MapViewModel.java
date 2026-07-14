package com.example.java3.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.java3.data.repository.FishingRepository;
import com.example.java3.domain.model.FishingPoint;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class MapViewModel extends ViewModel {
    private final FishingRepository fishingRepository;
    private final MutableLiveData<List<FishingPoint>> fishingPointsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private ListenerRegistration registration;

    public MapViewModel() {
        this.fishingRepository = new FishingRepository();
        startListening();
    }

    public LiveData<List<FishingPoint>> getFishingPointsLiveData() {
        return fishingPointsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void addFishingPoint(FishingPoint point) {
        fishingRepository.addFishingPoint(point, new FishingRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Success is handled by the real-time listener (listenFishingPoints)
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    private void startListening() {
        if (registration != null) {
            registration.remove();
        }
        registration = fishingRepository.listenFishingPoints(new FishingRepository.FirestoreCallback<List<FishingPoint>>() {
            @Override
            public void onSuccess(List<FishingPoint> result) {
                fishingPointsLiveData.setValue(result);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    @Override
    protected void onCleared() {
        if (registration != null) {
            registration.remove();
        }
        super.onCleared();
    }
}
