package com.example.weteams.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.weteams.logic.Project;
import com.example.weteams.logic.SnapshotListener;
import com.example.weteams.logic.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

public class ProjectsViewModel extends ViewModel {
    public static final String TAG = "ProjectsViewModel";

    private MutableLiveData<List<Project>> projectList = new MutableLiveData<>();
    private ListenerRegistration projectSubscription;
    private MutableLiveData<List<Project>> allProjectList = new MutableLiveData<>();
    private ListenerRegistration allProjectSubscription;

    public ProjectsViewModel() {
        Log.d(TAG, "created: " + this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection(User.USERS_COLLECTION).document(userId);
        Query query = db.collection(Project.PROJECTS_COLLECTION).whereArrayContains(Project.MEMBERS_KEY, userRef);
        projectSubscription = query.addSnapshotListener(new SnapshotListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                projectList.setValue(queryDocumentSnapshots.toObjects(Project.class));
                for (Project project : projectList.getValue()) {
                    Log.d(TAG, "project = " + project);
                }
            }
        });

        Query allQuery = db.collection(Project.PROJECTS_COLLECTION);
        allProjectSubscription = allQuery.addSnapshotListener(new SnapshotListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                allProjectList.setValue(queryDocumentSnapshots.toObjects(Project.class));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared: " + this);
        projectSubscription.remove();
        allProjectSubscription.remove();
    }

    public LiveData<List<Project>> getProjectList() {
        return projectList;
    }

    public LiveData<List<Project>> getAllProjectList() {
        return allProjectList;
    }
}
