package com.example.weteams.logic;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

public abstract class SnapshotListener<T> implements EventListener<T>, Callbacks<T> {

    @Override
    public void onEvent(@Nullable T querySnapshot, @Nullable FirebaseFirestoreException e) {
        if (e != null) {
            e.printStackTrace();
            onFailure(e);
            return;
        }
        onSuccess(querySnapshot);
    }

    @Override
    public void onFailure(Exception e) {
        // Do nothing
    }
}
