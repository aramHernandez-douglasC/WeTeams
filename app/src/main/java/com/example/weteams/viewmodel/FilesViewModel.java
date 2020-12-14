package com.example.weteams.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.weteams.logic.File;
import com.example.weteams.logic.Project;
import com.example.weteams.logic.SnapshotListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class FilesViewModel extends AndroidViewModel {
    public static final String TAG = "FilesViewModel";

    private MutableLiveData<List<File>> fileList = new MutableLiveData<>();
    private ListenerRegistration fileSubscription;

    public FilesViewModel(@NonNull Application application) {
        super(application);
        Log.d(TAG, "created: " + this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        String currentProjectId = sharedPreferences.getString(MainViewModel.CURRENT_PROJECT_KEY, null);
        if (currentProjectId != null) {
            CollectionReference query = db.collection(Project.PROJECTS_COLLECTION).document(currentProjectId).collection(File.FILES_COLLECTION);
            fileSubscription = query.addSnapshotListener(new SnapshotListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    fileList.setValue(queryDocumentSnapshots.toObjects(File.class));
                    for (File file : fileList.getValue()) {
                        Log.d(TAG, "file = " + file);
                    }
                }
            });
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared: " + this);
        if (fileSubscription != null) {
            fileSubscription.remove();
        }
    }

    public LiveData<List<File>> getFileList() {
        return fileList;
    }
}
