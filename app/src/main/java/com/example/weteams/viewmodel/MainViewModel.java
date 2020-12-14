package com.example.weteams.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.weteams.logic.Project;
import com.example.weteams.logic.QueryReferences;
import com.example.weteams.logic.SnapshotListener;
import com.example.weteams.logic.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Collections;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    public static final String TAG = "MainViewModel";

    public static final String CURRENT_PROJECT_KEY = "current_project";

    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private ListenerRegistration userSubscription;

    private MutableLiveData<Project> currentProject = new MutableLiveData<>();
    private ListenerRegistration projectSubscription;

    private QueryReferences<User> queryMembers = new QueryReferences<>(User.class);

    private SharedPreferences sharedPreferences;
    private String currentProjectId;

    public MainViewModel(@NonNull Application application) {
        super(application);
        Log.d(TAG, "created: " + this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference query = db.collection(User.USERS_COLLECTION).document(userId);
        userSubscription = query.addSnapshotListener(new SnapshotListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUser.setValue(documentSnapshot.toObject(User.class));
                Log.d(TAG, "currentUser = " + currentUser.getValue());
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        currentProjectId = sharedPreferences.getString(CURRENT_PROJECT_KEY, null);
        if (!TextUtils.isEmpty(currentProjectId)) {
            queryCurrentProject(currentProjectId);
        }
    }

    private void queryCurrentProject(String projectId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference query = db.collection(Project.PROJECTS_COLLECTION).document(projectId);
        projectSubscription = query.addSnapshotListener(new SnapshotListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getId().equals(currentProjectId)) {
                    currentProject.setValue(documentSnapshot.toObject(Project.class));
                    Log.d(TAG, "currentProject = " + currentProject.getValue());
                    Object members = documentSnapshot.get(Project.MEMBERS_KEY);
                    Log.d(TAG, "members = " + members);
                    if (members instanceof List) {
                        queryMembers.queryReferences((List<DocumentReference>) members);
                    } else {
                        queryMembers.queryReferences(Collections.<DocumentReference>emptyList());
                    }
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared: " + this);
        userSubscription.remove();
        if (projectSubscription != null) {
            projectSubscription.remove();
        }
        queryMembers.removeAll();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Project> getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(String projectId) {
        if (TextUtils.equals(projectId, currentProjectId)) {
            return;
        }
        currentProjectId = projectId;
        if (projectSubscription != null) {
            projectSubscription.remove();
        }
        if (!TextUtils.isEmpty(projectId)) {
            sharedPreferences.edit().putString(CURRENT_PROJECT_KEY, projectId).apply();
            queryCurrentProject(projectId);
        } else {
            sharedPreferences.edit().remove(CURRENT_PROJECT_KEY).apply();
            currentProject.setValue(null);
            projectSubscription = null;
        }
    }

    public LiveData<List<User>> getProjectMembers() {
        return queryMembers.getLiveData();
    }
}
