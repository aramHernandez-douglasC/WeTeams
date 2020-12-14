package com.example.weteams.fragments.files;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.weteams.R;
import com.example.weteams.logic.Callbacks;
import com.example.weteams.logic.File;
import com.example.weteams.logic.FileStorage;
import com.example.weteams.logic.User;
import com.example.weteams.viewmodel.FilesViewModel;
import com.example.weteams.viewmodel.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class FilesFragment extends Fragment implements FileTypeDialog.FileTypeDialogListener {

    public static final int REQUEST_CODE_TEXT = 1000;
    public static final int REQUEST_CODE_IMAGE = 1001;
    public static final int REQUEST_CODE_PDF = 1002;

    private MainViewModel mainViewModel;
    private FilesViewModel filesViewModel;
    private ListView filesList;
    private FilesAdapter filesAdapter;
    private ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        filesList = view.findViewById(R.id.filesList);
        filesAdapter = new FilesAdapter();
        filesList.setAdapter(filesAdapter);
        filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = (File) filesAdapter.getItem(position);
                Intent intent = new Intent(getContext(), FileViewerActivity.class);
                intent.putExtra(FileViewerActivity.FILE_ID_KEY, file.getId());
                intent.putExtra(FileViewerActivity.FILENAME_KEY, file.getName());
                startActivity(intent);
            }
        });

        FloatingActionButton fabAddFile = view.findViewById(R.id.fabAddFile);
        fabAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new FileTypeDialog();
                dialog.show(getChildFragmentManager(), null);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Files");

        ViewModelProvider.Factory factory = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication());

        mainViewModel = new ViewModelProvider(getActivity(), factory).get(MainViewModel.class);
        mainViewModel.getProjectMembers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> members) {
                filesAdapter.setMemberList(members);
            }
        });

        filesViewModel = new ViewModelProvider(this, factory).get(FilesViewModel.class);
        filesViewModel.getFileList().observe(this, new Observer<List<File>>() {
            @Override
            public void onChanged(List<File> files) {
                filesAdapter.setFileList(files);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_TEXT || requestCode == REQUEST_CODE_IMAGE || requestCode == REQUEST_CODE_PDF) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String fileType = null;
                if (requestCode == REQUEST_CODE_TEXT) {
                    fileType = "txt";
                } else if (requestCode == REQUEST_CODE_IMAGE) {
                    fileType = "jpg";
                } else if (requestCode == REQUEST_CODE_PDF) {
                    fileType = "pdf";
                }
                FileStorage.uploadFile(getActivity(), data.getData(), fileType, new Callbacks<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        Toast.makeText(getContext(), "Upload succeeded.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Sorry, upload failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFileType(int fileType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        switch (fileType) {
            case FileTypeDialog.FILE_TYPE_TEXT:
                intent.setType("text/plain");
                startActivityForResult(intent, REQUEST_CODE_TEXT);
                break;
            case FileTypeDialog.FILE_TYPE_IMAGE:
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
                break;
            case FileTypeDialog.FILE_TYPE_PDF:
                intent.setType("application/pdf");
                startActivityForResult(intent, REQUEST_CODE_PDF);
                break;
        }
    }

    public static class FilesAdapter extends BaseAdapter {

        private List<File> fileList;
        private List<User> memberList;

        public void setFileList(List<File> fileList) {
            this.fileList = fileList;
            notifyDataSetChanged();
        }

        public void setMemberList(List<User> memberList) {
            this.memberList = memberList;
            notifyDataSetChanged();
        }

        public User findMember(String uid) {
            for (User member : memberList) {
                if (TextUtils.equals(member.getUid(), uid)) {
                    return member;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return fileList != null ? fileList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return fileList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);
            File file = fileList.get(position);
            User creator = findMember(file.getCreator().getId());
            text1.setText(file.getName());
            text2.setText("Created by " + (creator != null ? creator.getDisplayName() : "unknown user"));
            return convertView;
        }
    }
}
