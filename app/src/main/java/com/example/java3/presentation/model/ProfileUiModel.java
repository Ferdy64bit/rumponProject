package com.example.java3.presentation.model;

public class ProfileUiModel {
    private final String uid;
    private final String name;
    private final String email;
    private final String phone;
    private final String address;
    private final String bio;
    private final String photoUrl;
    private final long joinDate;

    public ProfileUiModel(String uid, String name, String email, String phone, String address,
                          String bio, String photoUrl, long joinDate) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.bio = bio;
        this.photoUrl = photoUrl;
        this.joinDate = joinDate;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getBio() { return bio; }
    public String getPhotoUrl() { return photoUrl; }
    public long getJoinDate() { return joinDate; }
}
