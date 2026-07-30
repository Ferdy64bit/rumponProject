package com.example.java3.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.java3.data.repository.CommunityRepository;
import com.example.java3.domain.model.CommunityComment;
import com.example.java3.domain.model.Post;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class CommunityViewModel extends ViewModel {
    private final CommunityRepository repository;
    private final LiveData<List<Post>> postsLiveData;
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public CommunityViewModel() {
        this.repository = new CommunityRepository();
        this.postsLiveData = repository.getRealtimePosts();
    }

    public LiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public void toggleLike(String postId) {
        repository.toggleLike(postId, new CommunityRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isLiked) {
                // Realtime listener will handle UI update
            }

            @Override
            public void onError(String message) {
                errorLiveData.postValue(message);
            }
        });
    }

    public void toggleFavorite(String postId) {
        repository.toggleFavorite(postId, new CommunityRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isFavorite) {
                // Realtime listener will handle UI update
            }

            @Override
            public void onError(String message) {
                errorLiveData.postValue(message);
            }
        });
    }

    public void addComment(String postId, String text, CommunityRepository.RepositoryCallback<Void> callback) {
        repository.addComment(postId, text, new CommunityRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                errorLiveData.postValue(message);
                callback.onError(message);
            }
        });
    }

    public void getComments(String postId, CommunityRepository.RepositoryCallback<List<CommunityComment>> callback) {
        repository.getComments(postId, callback);
    }

    public String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }

    public void deletePost(Post post) {
        loadingLiveData.setValue(true);
        repository.deletePost(post, new CommunityRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.postValue(false);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(message);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}
