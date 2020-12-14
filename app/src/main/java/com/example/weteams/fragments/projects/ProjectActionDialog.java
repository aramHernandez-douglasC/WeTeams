package com.example.weteams.fragments.projects;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.DialogFragment;

import com.example.weteams.R;
import com.example.weteams.logic.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectActionDialog extends DialogFragment {
    private Project currentProject;

    public interface ProjectActionDialogListener {
        void onProjectActionClick(int pos, Project project);
    }

    public ProjectActionDialog(Project currentProject) {
        super();
        this.currentProject = currentProject;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_project_action, null);
        final ListView listView = view.findViewById(R.id.listViewProjectAction);


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Project " + currentProject.getName());
        builder.setView(view);
        builder.setNegativeButton(R.string.close, null);
        listView.setAdapter(new CustomAdapter());

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        ProjectActionDialogListener listener = (ProjectActionDialogListener) getParentFragment();
                        listener.onProjectActionClick(i, currentProject);
                        dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }

    class CustomAdapter extends BaseAdapter {
        List<Integer> pics;
        List<String> text;

        public CustomAdapter() {
            pics = new ArrayList<>(Arrays.asList(R.drawable.edit_icon, R.drawable.remove_icon, R.drawable.delete_icon));
            text = new ArrayList<>(Arrays.asList("Edit Project", "Un-join From Project", "Delete Project"));
        }

        @Override
        public int getCount() {
            return text.size();
        }

        @Override
        public Object getItem(int position) {
            return text.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, null);

            Drawable d = AppCompatResources.getDrawable(convertView.getContext(), pics.get(position));
            TextView text = convertView.findViewById(R.id.listText);
            ImageView i = convertView.findViewById(R.id.listImg);

            i.setImageDrawable(d);
            text.setText(this.text.get(position));

            return convertView;
        }
    }
}
