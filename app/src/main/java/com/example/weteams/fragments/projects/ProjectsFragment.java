package com.example.weteams.fragments.projects;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weteams.R;
import com.example.weteams.logic.Project;
import com.example.weteams.logic.User;
import com.example.weteams.viewmodel.MainViewModel;
import com.example.weteams.viewmodel.ProjectsViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectsFragment extends Fragment
        implements CreateProjectDialog.CreateProjectDialogListener, JoinProjectDialog.JoinProjectDialogListener, EditProjectDialog.EditProjectDialogListener, ProjectActionDialog.ProjectActionDialogListener {

    public static final String TAG = "ProjectsFragment";

    private Button btnCreateProject;
    private Button btnJoinProject;

    private MainViewModel mainViewModel;
    private RecyclerView recyclerProjects;
    private ProjectAdapter projectAdapter;
    private Project currentProject;
    private List<Project> projects;
    private ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        btnCreateProject = view.findViewById(R.id.btnCreateProject);
        btnCreateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new CreateProjectDialog();
                dialog.show(getChildFragmentManager(), null);
            }
        });

        btnJoinProject = view.findViewById(R.id.btnJoinProject);
        btnJoinProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new JoinProjectDialog();
                dialog.show(getChildFragmentManager(), null);
            }
        });

        recyclerProjects = view.findViewById(R.id.recyclerProjects);
        recyclerProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        projectAdapter = new ProjectAdapter(new OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project) {
                mainViewModel.setCurrentProject(project.getId());
            }
        }, new OnProjectLongClickListener() {
            @Override
            public void onProjectLongClick(final Project project) {
                DialogFragment dialog = new ProjectActionDialog(project);
                dialog.show(getChildFragmentManager(), null);
            }
        });
        recyclerProjects.setAdapter(projectAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Projects");

        ViewModelProvider.Factory factory = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication());
        mainViewModel = new ViewModelProvider(getActivity(), factory).get(MainViewModel.class);
        mainViewModel.getCurrentProject().observe(this, new Observer<Project>() {
            @Override
            public void onChanged(Project project) {
                currentProject = project;
                projectAdapter.setProjects(currentProject, projects);
            }
        });

        ProjectsViewModel viewModel = new ViewModelProvider(this).get(ProjectsViewModel.class);
        viewModel.getProjectList().observe(this, new Observer<List<Project>>() {
            @Override
            public void onChanged(List<Project> projects) {
                ProjectsFragment.this.projects = projects;
                projectAdapter.setProjects(currentProject, projects);
            }
        });
    }

    @Override
    public void onCreateProject(String name, String description) {
        Log.d(TAG, "onCreateProject: name = " + name + ", description = " + description);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection(User.USERS_COLLECTION).document(userId);
        Map<String, Object> project = new HashMap<>();
        project.put(Project.NAME_KEY, name);
        project.put(Project.DESCRIPTION_KEY, description);
        project.put(Project.MEMBERS_KEY, Arrays.asList(userRef));

        db.collection(Project.PROJECTS_COLLECTION).add(project)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference ref) {
                        mainViewModel.setCurrentProject(ref.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to create project", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onJoinProject(final Project project) {
        Log.d(TAG, "onJoinProject: project = " + project);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection(User.USERS_COLLECTION).document(userId);
        DocumentReference projectRef = db.collection(Project.PROJECTS_COLLECTION).document(project.getId());
        projectRef.update(Project.MEMBERS_KEY, FieldValue.arrayUnion(userRef))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mainViewModel.setCurrentProject(project.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to join project", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEditProject(Project project, String name, String description) {
        Map<String, Object> values = new HashMap<>();
        values.put(Project.NAME_KEY, name);
        values.put(Project.DESCRIPTION_KEY, description);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference projectRef = db.collection(Project.PROJECTS_COLLECTION).document(project.getId());

        projectRef.update(values)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Project Updated", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to update Project", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void deleteProject(Project project) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference projectRef = db.collection(Project.PROJECTS_COLLECTION).document(project.getId());

        projectRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Project Deleted", Toast.LENGTH_LONG).show();
                        currentProject = null;
                        mainViewModel.setCurrentProject(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to delete Project", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void unJoinProject(Project project) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference projectRef = db.collection(Project.PROJECTS_COLLECTION).document(project.getId());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        projectRef.update(Project.MEMBERS_KEY, userId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Successfully removed from project!", Toast.LENGTH_LONG).show();
                        currentProject = null;
                        mainViewModel.setCurrentProject(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to Un-join from Project", Toast.LENGTH_LONG).show();
                    }
                });


    }

    @Override
    public void onProjectActionClick(int pos, final Project project) {
        switch (pos) {
            case 0:
                DialogFragment dialog = new EditProjectDialog(project);
                dialog.show(getChildFragmentManager(), null);
                break;
            case 1:
                new AlertDialog.Builder(getContext())
                        .setTitle("Un-join project?")
                        .setMessage("Are you sure you want to remove yourself from project " + project.getName() + "?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                unJoinProject(project);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                break;
            case 2:
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete project?")
                        .setMessage("Are you sure you want to delete project " + project.getName() + "?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Really Sure?")
                                        .setMessage("Deleting project means all of the project's data, including logs, events, chats and files will be lost!")
                                        .setPositiveButton("I'M SURE!", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                deleteProject(project);
                                            }
                                        })
                                        .setNegativeButton("ON SECOND THOUGHT...", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        }).show();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                break;
        }
    }

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public interface OnProjectLongClickListener {
        void onProjectLongClick(Project project);
    }

    public static class ProjectAdapter extends RecyclerView.Adapter<ProjectViewHolder> {

        private OnProjectClickListener listener;
        private OnProjectLongClickListener longClickListener;

        private Project currentProject;
        private List<Project> projectList;

        public ProjectAdapter(OnProjectClickListener listener, OnProjectLongClickListener longClickListener) {
            this.listener = listener;
            this.longClickListener = longClickListener;
        }

        public void setProjects(Project currentProject, List<Project> projects) {
            this.currentProject = currentProject;
            projectList = new ArrayList<>();
            if (currentProject != null) {
                projectList.add(currentProject);
            }

            if (projects != null) {
                for (Project project : projects) {
                    if (currentProject == null || !project.getId().equals(currentProject.getId())) {
                        projectList.add(project);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return projectList == null ? 0 : projectList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (currentProject != null && position == 0) {
                return R.layout.listitem_project_current;
            } else {
                return R.layout.listitem_project;
            }
        }

        @NonNull
        @Override
        public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ProjectViewHolder(inflater.inflate(viewType, parent, false),
                    viewType == R.layout.listitem_project ? listener : null, viewType == R.layout.listitem_project ? null : longClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
            holder.bindProject(projectList.get(position));
        }
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private TextView txtProjectName;
        private TextView txtProjectDescription;

        private Project project;

        public ProjectViewHolder(@NonNull View itemView, final OnProjectClickListener listener, final OnProjectLongClickListener longClickListener) {
            super(itemView);
            txtProjectName = itemView.findViewById(R.id.txtProjectName);
            txtProjectDescription = itemView.findViewById(R.id.txtProjectDescription);

            if (listener != null)
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onProjectClick(project);
                    }
                });

            if (longClickListener != null)
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        longClickListener.onProjectLongClick(project);
                        return true;
                    }
                });
        }

        public void bindProject(Project project) {
            this.project = project;
            txtProjectName.setText(project.getName());
            txtProjectDescription.setText(project.getDescription());
        }
    }
}
