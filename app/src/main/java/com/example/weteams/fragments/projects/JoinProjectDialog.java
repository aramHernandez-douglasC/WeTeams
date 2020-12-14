package com.example.weteams.fragments.projects;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.weteams.R;
import com.example.weteams.logic.Project;
import com.example.weteams.viewmodel.ProjectsViewModel;

import java.util.ArrayList;
import java.util.List;

public class JoinProjectDialog extends DialogFragment {

    public interface JoinProjectDialogListener {
        void onJoinProject(Project project);
    }

    private ProjectsAdapter projectsAdapter = new ProjectsAdapter();
    private List<Project> joinedProjects;
    private List<Project> allProjects;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_project_join, null);
        final ListView listProjects = view.findViewById(R.id.listProjects);
        listProjects.setAdapter(projectsAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Join Project");
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
                        int position = listProjects.getCheckedItemPosition();
                        if (position >= 0) {
                            JoinProjectDialogListener listener = (JoinProjectDialogListener) getParentFragment();
                            listener.onJoinProject(projectsAdapter.projectList.get(position));
                            dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ProjectsViewModel viewModel = new ViewModelProvider(getActivity()).get(ProjectsViewModel.class);
        viewModel.getProjectList().observe(this, new Observer<List<Project>>() {
            @Override
            public void onChanged(List<Project> projects) {
                joinedProjects = projects;
                projectsAdapter.setProjects(joinedProjects, allProjects);
            }
        });
        viewModel.getAllProjectList().observe(this, new Observer<List<Project>>() {
            @Override
            public void onChanged(List<Project> projects) {
                allProjects = projects;
                projectsAdapter.setProjects(joinedProjects, allProjects);
            }
        });
    }

    public static class ProjectsAdapter extends BaseAdapter {
        private List<Project> projectList;

        public void setProjects(List<Project> joinedProjects, List<Project> allProjects) {
            if (joinedProjects != null && allProjects != null) {
                allProjects = new ArrayList<>(allProjects);
                for (Project joinedProject : joinedProjects) {
                    for (Project project : allProjects) {
                        if (project.getId().equals(joinedProject.getId())) {
                            allProjects.remove(project);
                            break;
                        }
                    }
                }
            }
            projectList = allProjects;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return projectList != null ? projectList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return projectList.get(position);
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
            Project project = projectList.get(position);
            text1.setText(project.getName());
            text2.setText(project.getDescription());
            return convertView;
        }
    }
}
