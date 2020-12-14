package com.example.weteams.logic;

import com.google.firebase.firestore.DocumentId;

public class User {
    public static final String USERS_COLLECTION = "users";

    public static final String EMAIL_KEY = "email";
    public static final String DISPLAY_NAME_KEY = "displayName";

    @DocumentId
    private String uid;
    private String email;
    private String displayName;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "User { uid: " + uid + ", email: " + email + ", displayName: " + displayName + " }";
    }
}
