package com.example.java3.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.java3.data.repository.ProfileRepository;
import com.example.java3.domain.model.FishingPoint;
import com.example.java3.domain.model.Post;
import com.example.java3.presentation.model.ProfileStatsUiModel;
import com.example.java3.presentation.model.ProfileUiModel;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private final ProfileRepository repository = new ProfileRepository();
    private final MutableLiveData<ProfileUiModel> profileLiveData = new MutableLiveData<>();
    private final MutableLiveData<ProfileStatsUiModel> statsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> uploadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> myPostsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<FishingPoint>> mySpotsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<FishingPoint>> favoriteSpotsLiveData = new MutableLiveData<>();

    public ProfileViewModel() {
        repository.listenProfile(new ProfileRepository.Callback<ProfileUiModel>() {
            @Override
            public void onSuccess(ProfileUiModel result) {
                profileLiveData.postValue(result);
            }

            @Override
            public void onError(String message) {
                messageLiveData.postValue(message);
            }
        });
        repository.listenStats(new ProfileRepository.Callback<ProfileStatsUiModel>() {
            @Override
            public void onSuccess(ProfileStatsUiModel result) {
                statsLiveData.postValue(result);
            }

            @Override
            public void onError(String message) {
                messageLiveData.postValue(message);
            }
        });
    }

    public LiveData<ProfileUiModel> getProfileLiveData() { return profileLiveData; }
    public LiveData<ProfileStatsUiModel> getStatsLiveData() { return statsLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }
    public LiveData<Boolean> getUploadingLiveData() { return uploadingLiveData; }
    public LiveData<String> getMessageLiveData() { return messageLiveData; }
    public LiveData<List<Post>> getMyPostsLiveData() { return myPostsLiveData; }
    public LiveData<List<FishingPoint>> getMySpotsLiveData() { return mySpotsLiveData; }
    public LiveData<List<FishingPoint>> getFavoriteSpotsLiveData() { return favoriteSpotsLiveData; }

    public void updateProfile(String name, String phone, String address, String bio) {
        loadingLiveData.setValue(true);
        repository.updateProfile(name, phone, address, bio, new ProfileRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.postValue(false);
                messageLiveData.postValue("Profil berhasil diperbarui");
            }

            @Override
            public void onError(String message) {
                loadingLiveData.postValue(false);
                messageLiveData.postValue(message);
            }
        });
    }

    public void uploadProfilePhoto(byte[] imageData) {
        uploadingLiveData.setValue(true);
        repository.uploadProfilePhoto(imageData, new ProfileRepository.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                uploadingLiveData.postValue(false);
                messageLiveData.postValue("Foto profil berhasil diperbarui");
            }

            @Override
            public void onError(String message) {
                uploadingLiveData.postValue(false);
                messageLiveData.postValue(message);
            }
        });
    }

    public void sendPasswordReset() {
        repository.sendPasswordReset(new ProfileRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                messageLiveData.postValue("Link reset password sudah dikirim. Cek Inbox atau folder Spam email Anda.");
            }

            @Override
            public void onError(String message) {
                messageLiveData.postValue(message);
            }
        });
    }

    public void loadMyPosts() {
        repository.loadMyPosts(new ProfileRepository.Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                myPostsLiveData.postValue(result);
            }

            @Override
            public void onError(String message) {
                messageLiveData.postValue(message);
            }
        });
    }

    public void loadMySpots() {
        repository.loadMySpots(new ProfileRepository.Callback<List<FishingPoint>>() {
            @Override
            public void onSuccess(List<FishingPoint> result) {
                mySpotsLiveData.postValue(result);
            }

            @Override
            public void onError(String message) {
                messageLiveData.postValue(message);
            }
        });
    }

    public void loadFavoriteSpots() {
        repository.loadFavoriteSpots(new ProfileRepository.Callback<List<FishingPoint>>() {
            @Override
            public void onSuccess(List<FishingPoint> result) {
                favoriteSpotsLiveData.postValue(result);
            }

            @Override
            public void onError(String message) {
                messageLiveData.postValue(message);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}
