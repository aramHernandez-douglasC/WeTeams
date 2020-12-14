package com.example.weteams.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.weteams.logic.Event;
import com.example.weteams.logic.Project;
import com.example.weteams.logic.QueryReferences;
import com.example.weteams.logic.SnapshotListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class EventsViewModel extends ViewModel {
    private String TAG = "EventsViewModel";
    private MutableLiveData<List<Event>> eventList = new MutableLiveData<>();
    private Project currentProject = null;

    public EventsViewModel() {
    }

    public void init() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query query = db.collection(Project.PROJECTS_COLLECTION).document(currentProject.getId()).collection(Event.EVENT_COLLECTION);
        query.addSnapshotListener(new SnapshotListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot value) {
                eventList.setValue(value.toObjects(Event.class));
            }
        });
    }

    public void setCurrentProject(Project project) {
        currentProject = project;
    }

    public LiveData<List<Event>> getEventList() {
        return eventList;
    }
}
