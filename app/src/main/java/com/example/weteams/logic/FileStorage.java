package com.example.weteams.logic;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.weteams.viewmodel.MainViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FileStorage {
    public static final String TAG = "FileStorage";

    public static void uploadFile(Activity activity, Uri uri, String fileType, Callbacks<Void> callbacks) {
        Log.d(TAG, "uploadFile: uri = " + uri);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplication());
        String currentProjectId = sharedPreferences.getString(MainViewModel.CURRENT_PROJECT_KEY, null);
        if (currentProjectId == null) {
            Log.d(TAG, "uploadFile: currentProjectId = null");
            callbacks.onFailure(new RuntimeException("No current project"));
            return;
        }

        String filename = uri.getLastPathSegment();
        if (filename.contains("/") && !filename.endsWith("/")) {
            filename = filename.substring(filename.lastIndexOf("/") + 1);
        }
        if (!filename.contains(".") && fileType != null) {
            filename += "." + fileType;
        }

        try {
            InputStream stream = activity.getContentResolver().openInputStream(uri);
            createDocument(currentProjectId, filename, stream, callbacks);
        } catch (Exception e) {
            e.printStackTrace();
            callbacks.onFailure(e);
        }
    }

    public static void createDocument(final String projectId, String filename, final InputStream stream, final Callbacks<Void> callbacks) {
        Log.d(TAG, "createDocument: projectId = " + projectId + ", filename = " + filename);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection(User.USERS_COLLECTION).document(userId);

        Map<String, Object> file = new HashMap<>();
        file.put(File.NAME_KEY, filename);
        file.put(File.CREATOR_KEY, userRef);
        file.put(File.TIMESTAMP_KEY, FieldValue.serverTimestamp());

        db.collection(Project.PROJECTS_COLLECTION).document(projectId).collection(File.FILES_COLLECTION).add(file)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference ref) {
                        writeStorage(projectId, ref.getId(), stream, callbacks);
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

    public static void writeStorage(String projectId, String fileId, InputStream stream, final Callbacks<Void> callbacks) {
        String path = "files/" + projectId + "/" + fileId;
        Log.d(TAG, "writeStorage: path = " + path);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference fileRef = storage.getReference(path);

        fileRef.putStream(stream)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        callbacks.onSuccess(null);
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

    public static void downloadFile(Activity activity, String fileId, final Callbacks<byte[]> callbacks) {
        Log.d(TAG, "downloadFile: fileId = " + fileId);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplication());
        String currentProjectId = sharedPreferences.getString(MainViewModel.CURRENT_PROJECT_KEY, null);
        if (currentProjectId == null) {
            Log.d(TAG, "downloadFile: currentProjectId = null");
            callbacks.onFailure(new RuntimeException("No current project"));
            return;
        }

        String path = "files/" + currentProjectId + "/" + fileId;
        FirebaseStorage.getInstance().getReference(path).getBytes(256 * 1024 * 1024)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        callbacks.onSuccess(bytes);
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
