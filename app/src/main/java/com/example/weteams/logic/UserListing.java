package com.example.weteams.logic;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserListing {

    public static final String TAG = "UserListing";

    public static final String USERS_COLLECTION = "users";
    public static final String EMAIL_KEY = "email";
    public static final String DISPLAY_NAME_KEY = "displayName";
    public static final String AVATAR_KEY = "avatar";

    public static void getUserList(final Callbacks<List<User>> callbacks) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION).orderBy(DISPLAY_NAME_KEY)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.wtf(TAG, e.getMessage(), e);
                            callbacks.onFailure(e);
                            return;
                        }
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String uid = document.getId();
                            String email = document.getString(EMAIL_KEY);
                            String displayName = document.getString(DISPLAY_NAME_KEY);
                            Log.d(TAG, "uid = " + uid + ", email = " + email + ", displayName = " + displayName);
                            User mUser = new User();
                            mUser.setUid(uid);
                            mUser.setEmail(email);
                            mUser.setDisplayName(displayName);
                            users.add(mUser);
                        }
                        callbacks.onSuccess(users);
                    }
                });
    }
}
