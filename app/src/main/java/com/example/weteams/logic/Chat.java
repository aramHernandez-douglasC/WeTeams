package com.example.weteams.logic;



import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;

public class Chat {

    public static final String CHAT_COLLECTION = "chat";

    public static final String SENDER_KEY = "name";
    public static final String MESSAGE_KEY = "message";
    public static final String TIMESTAMP_KEY = "timestamp";

    @DocumentId
    private String id;
    private String sender;
    private String senderId;
    private String message;
    private Timestamp timestamp;


    public Chat(String id, String sender, String senderId, String message, Timestamp timestamp) {
        this.sender = sender;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;

    }



    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Chat { id: " + id + ", sender: " + sender + ", message: " + message + ", timestamp: " + timestamp + " }";
    }
}
