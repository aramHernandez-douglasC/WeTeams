package com.example.weteams.fragments.projects;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.weteams.R;

public class CreateProjectDialog extends DialogFragment {

    public interface CreateProjectDialogListener {
        void onCreateProject(String name, String description);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_project_create, null);
        final EditText editProjectName = view.findViewById(R.id.editProjectName);
        final EditText editProjectDescription = view.findViewById(R.id.editProjectDescription);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create New Project");
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence name = editProjectName.getText();
                        CharSequence description = editProjectDescription.getText();
                        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(description)) {
                            CreateProjectDialogListener listener = (CreateProjectDialogListener) getParentFragment();
                            listener.onCreateProject(name.toString(), description.toString());
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Project name and description must not be empty!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }
}
