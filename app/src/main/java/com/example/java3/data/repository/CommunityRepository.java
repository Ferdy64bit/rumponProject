package com.example.java3.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.java3.domain.model.Post;
import com.example.java3.core.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

/**
 * Repository for managing community posts.
 */
public class CommunityRepository {
    private final FirebaseFirestore firestore;

    public CommunityRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Post>> getPosts() {
        MutableLiveData<List<Post>> liveData = new MutableLiveData<>();
        
        firestore.collection(Constants.COL_COMMUNITY_POSTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((queryDocumentSnapshots, error) -> {
                if (error != null || queryDocumentSnapshots == null) {
                    liveData.setValue(new java.util.ArrayList<>());
                    return;
                }
                liveData.setValue(queryDocumentSnapshots.toObjects(Post.class));
            });

        return liveData;
    }
}
