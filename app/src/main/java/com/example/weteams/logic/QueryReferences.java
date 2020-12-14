package com.example.weteams.logic;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class QueryReferences<T> {
    public static final String TAG = "QueryReferences";

    public class Query<T> {
        private DocumentReference ref;
        private ListenerRegistration subscription;
        private T object;

        public Query(DocumentReference ref) {
            Log.d(TAG, "Query created: ref = " + ref);
            this.ref = ref;
            subscription = ref.addSnapshotListener(new SnapshotListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    object = (T) documentSnapshot.toObject(valueType);
                    Log.d(TAG, "Query ref = " + Query.this.ref + ", object = " + object);
                    notifyUpdate();
                }
            });
        }

        public void remove() {
            Log.d(TAG, "Query removed: ref = " + ref);
            subscription.remove();
        }
    }

    private final Class<T> valueType;
    private MutableLiveData<List<T>> liveData;
    private List<Query<T>> currentQueries;

    public QueryReferences(Class<T> valueType) {
        this.valueType = valueType;
        liveData = new MutableLiveData<>();
        currentQueries = new ArrayList<>();
    }

    public LiveData<List<T>> getLiveData() {
        return liveData;
    }

    public void removeAll() {
        for (Query<T> query : currentQueries) {
            query.remove();
        }
        currentQueries.clear();
    }

    public void queryReferences(List<DocumentReference> refs) {
        boolean changed = false;
        int index = 0;
        for (DocumentReference ref : refs) {
            int i = findRefAfter(index, ref);
            if (i != index) {
                currentQueries.add(index, i < 0 ? new Query<T>(ref) : currentQueries.remove(i));
                changed = true;
            }
            index++;
        }
        while (index < currentQueries.size()) {
            currentQueries.remove(index).remove();
            changed = true;
        }
        if (changed) {
            notifyUpdate();
        }
    }

    private int findRefAfter(int index, DocumentReference ref) {
        for (int i = index; i < currentQueries.size(); i++) {
            if (currentQueries.get(i).ref.equals(ref)) {
                return i;
            }
        }
        return -1;
    }

    private void notifyUpdate() {
        List<T> value = new ArrayList<>();
        for (Query<T> query : currentQueries) {
            if (query.object != null) {
                value.add(query.object);
            }
        }
        liveData.setValue(value);
    }
}
