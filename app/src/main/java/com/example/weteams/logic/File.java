package com.example.weteams.logic;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;

public class File {
    public static final String FILES_COLLECTION = "files";

    public static final String NAME_KEY = "name";
    public static final String CREATOR_KEY = "creator";
    public static final String TIMESTAMP_KEY = "timestamp";

    @DocumentId
    private String id;
    private String name;
    private DocumentReference creator;
    private Timestamp timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DocumentReference getCreator() {
        return creator;
    }

    public void setCreator(DocumentReference creator) {
        this.creator = creator;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "File { id: " + id + ", name: " + name + ", creator: " + creator + ", timestamp: " + timestamp + " }";
    }
}
