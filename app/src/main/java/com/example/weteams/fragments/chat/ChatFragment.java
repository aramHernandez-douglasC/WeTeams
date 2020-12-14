package com.example.weteams.fragments.chat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weteams.MainActivity;
import com.example.weteams.R;
import com.example.weteams.logic.Callbacks;
import com.example.weteams.logic.Chat;
import com.example.weteams.logic.ChatListing;
import com.example.weteams.logic.User;
import com.example.weteams.viewmodel.ChatViewModel;
import com.example.weteams.viewmodel.MainViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerChat;
    private EditText editChat;
    private Button btnSend;

    private User myUser;
    private List<Chat> chatList;
    private List<User> userList;
    private ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myUser = new User();
        myUser.setUid(user.getUid());
        myUser.setDisplayName(user.getDisplayName());
        myUser.setEmail(user.getEmail());

        recyclerChat = view.findViewById(R.id.recyclerChat);
        editChat = view.findViewById(R.id.editChat);
        btnSend = view.findViewById(R.id.btnSend);

        LinearLayoutManager layoutManager = new LinearLayoutManager(inflater.getContext());
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(new ChatAdapter(inflater));

        editChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btnSend.setEnabled(!TextUtils.isEmpty(editChat.getText().toString()));
            }
        });

        editChat.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                    actionBar.hide();
                else
                    actionBar.show();
            }
        });

        btnSend.setEnabled(false);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(editChat.getText().toString());
                editChat.setText("");
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Chat");

        ViewModelProvider.Factory factory = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication());
        ChatViewModel viewModel = new ViewModelProvider(this, factory).get(ChatViewModel.class);

        viewModel.getChatList().observe(this, new Observer<List<Chat>>() {
            @Override
            public void onChanged(List<Chat> chats) {
                chatList = chats;
                recyclerChat.getAdapter().notifyDataSetChanged();
                recyclerChat.scrollToPosition(chatList.size() - 1);
            }
        });
        viewModel.getUserList().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                userList = users;
                User user = findUser(myUser.getUid());
                if (user != null) {
                    myUser = user;
                }
                recyclerChat.getAdapter().notifyDataSetChanged();
            }
        });
    }


    public void sendMessage(String message) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String currentProjectId = sharedPreferences.getString(MainViewModel.CURRENT_PROJECT_KEY, null);


        ChatListing.addChatMessage(currentProjectId, myUser, message, new Callbacks<Void>() {
            @Override
            public void onSuccess(Void dummy) {
                editChat.setText("");
            }

            @Override
            public void onFailure(Exception e) {
                btnSend.setEnabled(!TextUtils.isEmpty(editChat.getText()));
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to send message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class ChatAdapter extends RecyclerView.Adapter<ChatHolder> {

        private LayoutInflater inflater;

        public ChatAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @NonNull
        @Override
        public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ChatHolder(inflater.inflate(viewType, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
            holder.bindChat(chatList.get(position));
        }

        @Override
        public int getItemCount() {
            return chatList == null ? 0 : chatList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (chatList.get(position).getSenderId().equals(myUser.getUid())) {
                return R.layout.list_right;
            } else {
                return R.layout.list_left;
            }
        }
    }

    public class ChatHolder extends RecyclerView.ViewHolder {

        private TextView txtChat1;
        private TextView txtChat2;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);

            txtChat1 = itemView.findViewById(R.id.txtChat1);
            txtChat2 = itemView.findViewById(R.id.txtChat2);
        }

        public void bindChat(Chat chat) {
            User user = findUser(chat.getSenderId());
            txtChat1.setText(user != null ? user.getDisplayName() : chat.getSender());
            txtChat2.setText(chat.getMessage());
        }
    }

    public User findUser(String uid) {
        if (userList != null) {
            for (User user : userList) {
                if (user.getUid().equals(uid)) {
                    return user;
                }
            }
        }
        return null;
    }
}
