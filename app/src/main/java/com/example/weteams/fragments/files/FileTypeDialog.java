package com.example.weteams.fragments.files;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.weteams.R;

public class FileTypeDialog extends DialogFragment {

    public static final String[] FILE_TYPES = { "Text", "Image", "PDF" };

    public static final int FILE_TYPE_TEXT = 0;
    public static final int FILE_TYPE_IMAGE = 1;
    public static final int FILE_TYPE_PDF = 2;

    public interface FileTypeDialogListener {
        void onFileType(int fileType);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_file_type, null);
        final ListView listFileTypes = view.findViewById(R.id.listFileTypes);
        listFileTypes.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, FILE_TYPES));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("File Type");
        builder.setView(view);
        builder.setNegativeButton(android.R.string.cancel, null);

        listFileTypes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileTypeDialogListener listener = (FileTypeDialogListener) getParentFragment();
                listener.onFileType(position);
                dismiss();
            }
        });

        return builder.create();
    }
}
