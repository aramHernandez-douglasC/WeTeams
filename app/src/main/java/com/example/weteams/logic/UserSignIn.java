package com.example.weteams.logic;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserSignIn {
    public static final String TAG = "UserSignIn";

    public static void processSignIn(
            final String email,
            String password,
            final Callbacks<FirebaseUser> callbacks
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "Sign in success: " + authResult.getUser().getUid());
                        callbacks.onSuccess(authResult.getUser());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        callbacks.onFailure(e);
                    }
                });
    }

    public static void processSignUp(
            String email,
            String password,
            final String displayName,
            final Callbacks<FirebaseUser> callbacks
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "Sign up success: " + authResult.getUser().getUid());
                        updateUserProfile(authResult.getUser(), displayName, callbacks);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        callbacks.onFailure(e);
                    }
                });
    }

    public static void updateUserProfile(
            final FirebaseUser user,
            final String displayName,
            final Callbacks<FirebaseUser> callbacks
    ) {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();
        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateUserDocument(user, displayName, callbacks);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        callbacks.onFailure(e);
                    }
                });
    }

    public static void updateUserPassword(
            final FirebaseUser user,
            final String oldPassword,
            final String newPassword,
            final Callbacks<FirebaseUser> callbacks
    ) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                callbacks.onSuccess(user);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                callbacks.onFailure(e);
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callbacks.onFailure(e);
            }
        });

    }

    public static void updateUserDocument(
            final FirebaseUser user,
            String displayName,
            final Callbacks<FirebaseUser> callbacks
    ) {
        Map<String, Object> profile = new HashMap<>();
        profile.put(User.EMAIL_KEY, user.getEmail());
        profile.put(User.DISPLAY_NAME_KEY, displayName);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(User.USERS_COLLECTION).document(user.getUid()).set(profile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callbacks.onSuccess(user);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        callbacks.onFailure(e);
                    }
                });
    }
}
