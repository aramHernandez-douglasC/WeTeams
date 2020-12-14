package com.example.weteams.logic;

import android.content.Context;
import android.os.health.TimerStat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListing {
    public static final String TAG = "ChatListing";

    public static final String SENDER_KEY = "sender";
    public static final String SENDER_ID_KEY = "senderId";
    public static final String MESSAGE_KEY = "message";
    public static final String TIMESTAMP_KEY = "timestamp";



    public static void getChatList( final String projectId, final Callbacks<List<Chat>> callbacks) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Project.PROJECTS_COLLECTION).document(projectId).collection(Chat.CHAT_COLLECTION).orderBy(TIMESTAMP_KEY)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.wtf(TAG, e.getMessage(), e);
                            callbacks.onFailure(e);
                            return;
                        }
                        List<Chat> chats = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String id = document.getId();
                            String sender = document.getString(SENDER_KEY);
                            String senderId = document.getString(SENDER_ID_KEY);
                            String message = document.getString(MESSAGE_KEY);
                            Timestamp timestamp = document.getTimestamp(TIMESTAMP_KEY);
                            Log.d(TAG, "id = " + id + ", sender = " + sender + ", message = " + message);
                            chats.add(new Chat(id, sender, senderId, message, timestamp));
                        }
                        callbacks.onSuccess(chats);
                    }
                });
    }

    public static void addChatMessage(final String projectId, final User sender,final String message, final Callbacks<Void> callbacks) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection(User.USERS_COLLECTION).document(userId);

        Map<String, Object> chat = new HashMap<>();
        chat.put(SENDER_KEY, sender.getDisplayName());
        chat.put(SENDER_ID_KEY, sender.getUid());
        chat.put(MESSAGE_KEY, message);
        chat.put(TIMESTAMP_KEY, Timestamp.now());

        db.collection(Project.PROJECTS_COLLECTION).document(projectId).collection(Chat.CHAT_COLLECTION).add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference ref) {
                        Log.e("yeeeey","WUUUUUUUUUUUUUU");
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
