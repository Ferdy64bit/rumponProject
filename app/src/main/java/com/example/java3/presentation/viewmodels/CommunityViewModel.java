package com.example.java3.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.java3.data.repository.CommunityRepository;
import com.example.java3.domain.model.Post;

import java.util.List;

public class CommunityViewModel extends ViewModel {
    private final LiveData<List<Post>> postsLiveData;

    public CommunityViewModel() {
        CommunityRepository repository = new CommunityRepository();
        this.postsLiveData = repository.getPosts();
    }

    public LiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }
}
