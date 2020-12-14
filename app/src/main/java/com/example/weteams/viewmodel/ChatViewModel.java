package com.example.weteams.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weteams.logic.Callbacks;
import com.example.weteams.logic.Chat;
import com.example.weteams.logic.ChatListing;
import com.example.weteams.logic.User;
import com.example.weteams.logic.UserListing;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    public static final String TAG = "ChatViewModel";
    public static final String CURRENT_PROJECT_KEY = "current_project";

    private MutableLiveData<List<Chat>> chatList;
    private MutableLiveData<List<User>> userList;

    private SharedPreferences sharedPreferences;
    private String currentProjectId;



    public ChatViewModel(@NonNull Application application) {
        super(application);
        Log.d(TAG, "created: " + this);

        chatList = new MutableLiveData<>();
        userList = new MutableLiveData<>();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        currentProjectId = sharedPreferences.getString(CURRENT_PROJECT_KEY, null);


        ChatListing.getChatList(currentProjectId, new Callbacks<List<Chat>>() {
            @Override
            public void onSuccess(List<Chat> result) {
                chatList.setValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                // Do nothing
            }
        });


        UserListing.getUserList(new Callbacks<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                userList.setValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                // Do nothing
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared: " + this);
    }


    public MutableLiveData<List<Chat>> getChatList() {
        return chatList;
    }

    public MutableLiveData<List<User>> getUserList() {
        return userList;
    }
}
