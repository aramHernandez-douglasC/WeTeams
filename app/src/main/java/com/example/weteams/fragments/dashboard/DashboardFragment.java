package com.example.weteams.fragments.dashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weteams.MainActivity;
import com.example.weteams.R;
import com.example.weteams.fragments.schedule.ScheduleFragment;
import com.example.weteams.logic.Event;
import com.example.weteams.logic.Project;
import com.example.weteams.logic.SortEvents;
import com.example.weteams.viewmodel.EventsViewModel;
import com.example.weteams.viewmodel.MainViewModel;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DashboardFragment extends Fragment {

    private ProgressBar progressBar;
    private MainViewModel mainViewModel;
    private Project currentProject;
    private EventsViewModel eventsViewModel;
    private List<Event> eventList = new ArrayList<>(), eventsCompleted, eventsInProgress, todayEvents, upComingEvents, completedEvents;
    private TextView progressText, noOfTaskText, projectNameText;
    private RecyclerView recyclerView;

    public static final int HEADER_TODAY = 0, HEADER_UPCOMING = 1, HEADER_COMPLETED = 2;
    private int currentHeaderType;
    public static String SELECTED_DATE_KEY = "selectedDate";
    SharedPreferences preferences;
    private ActionBar actionBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelProvider.Factory factory = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication());
        mainViewModel = new ViewModelProvider(getActivity(), factory).get(MainViewModel.class);

        mainViewModel.getCurrentProject().observe(this, new Observer<Project>() {
            @Override
            public void onChanged(Project project) {
                currentProject = project;
                eventsViewModel = new ViewModelProvider(getActivity()).get(EventsViewModel.class);
                eventsViewModel.setCurrentProject(currentProject);
                eventsViewModel.init();
                eventsViewModel.getEventList().observe(DashboardFragment.this, new Observer<List<Event>>() {
                    @Override
                    public void onChanged(List<Event> events) {
                        eventList = events;
                        onDataChanged();
                    }
                });
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setScaleY(8f);
        progressText = view.findViewById(R.id.progressText);
        noOfTaskText = view.findViewById(R.id.noOfTaskText);
        projectNameText = view.findViewById(R.id.projectNameText);
        recyclerView = view.findViewById(R.id.recyclerDashboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.HORIZONTAL));

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Dashboard");
    }

    public void onDataChanged() {
        Calendar c = Calendar.getInstance();
        categorizeEvents(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));

        projectNameText.setText("Project " + currentProject.getName());
        eventsCompleted = new ArrayList<>();
        eventsInProgress = new ArrayList<>();

        if (eventList.size() == 0) {
            progressText.setText(R.string.noEventText);
            noOfTaskText.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            noOfTaskText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            for (Event e : eventList)
                if (e.isCompleted())
                    eventsCompleted.add(e);
                else
                    eventsInProgress.add(e);

            float percentageCompleted = (float) eventsCompleted.size() / eventList.size() * 100;
            DecimalFormat df = new DecimalFormat("#.##");

            progressText.setText(df.format(percentageCompleted) + "% Completed");
            noOfTaskText.setText(eventsCompleted.size() + "/" + eventList.size() + " Tasks Completed");
            if (percentageCompleted == 100f) {
                progressText.setTextColor(Color.RED);
                noOfTaskText.setTextColor(Color.RED);
            }

            progressBar.setProgress((int) percentageCompleted);

            DashboardAdapter adapter = new DashboardAdapter(new OnDashboardItemClickListener() {
                @Override
                public void onScheduleClick(final Event e) {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Go to this event on Calendar?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(e.getDeadline());
                                    String date = cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);
                                    preferences.edit().putString(SELECTED_DATE_KEY, date).apply();
                                    FragmentManager fm = getActivity().getSupportFragmentManager();
                                    fm.beginTransaction().replace(R.id.mainContent, new ScheduleFragment(), MainActivity.CURRENT_FRAGMENT).commit();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                }
            });

            recyclerView.setAdapter(adapter);
        }

    }

    //Separate today's event and upcoming event (in progress only) and sort according to time ASC
    public void categorizeEvents(int year, int month, int day) {
        todayEvents = new ArrayList<>();
        upComingEvents = new ArrayList<>();
        completedEvents = new ArrayList<>();

        for (Event e : eventList) {
            if (e.isCompleted())
                completedEvents.add(e);
            else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(e.getDeadline());
                int y = cal.get(Calendar.YEAR), m = cal.get(Calendar.MONTH), d = cal.get(Calendar.DAY_OF_MONTH);
                if (y == year && m == month && d == day) {
                    todayEvents.add(e);
                } else
                    upComingEvents.add(e);
            }
        }
        SortEvents.quickSort(todayEvents, 0, todayEvents.size() - 1);
        SortEvents.quickSort(upComingEvents, 0, upComingEvents.size() - 1);
        SortEvents.quickSort(completedEvents, 0, completedEvents.size() - 1);
    }

    public interface OnDashboardItemClickListener {
        void onScheduleClick(Event e);
    }

    private class DashboardAdapter extends RecyclerView.Adapter {
        private OnDashboardItemClickListener listener;


        public DashboardAdapter(OnDashboardItemClickListener listener) {
            currentHeaderType = HEADER_TODAY;
            this.listener = listener;
        }


        @Override
        public int getItemViewType(int position) {
            if (position == 0 || position == todayEvents.size() + 1 || position == todayEvents.size() + upComingEvents.size() + 2)
                return R.layout.list_header;
            return R.layout.listitem_event_dashboard;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(viewType, parent, false);
            if (viewType == R.layout.list_header)
                return new HeaderViewHolder(view);
            return new DashboardEventViewHolder(view, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            //IS_EVENT_TODAY_HEADER = position >= 0 && position <= todayEvents.size() ? true : false;
            if (position >= 0 && position <= todayEvents.size())
                currentHeaderType = HEADER_TODAY;
            else if (position > todayEvents.size() && position <= todayEvents.size() + upComingEvents.size() + 1)
                currentHeaderType = HEADER_UPCOMING;
            else
                currentHeaderType = HEADER_COMPLETED;
            if (position == 0) {
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.header.setText(R.string.todayEventHeaderText);
            } else if (position == (todayEvents.size() + 1)) {
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.header.setText(R.string.upcomingEventHeaderText);
            } else if (position == todayEvents.size() + upComingEvents.size() + 2) {
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.header.setText(R.string.completedEventHeaderText);
            } else {
                DashboardEventViewHolder dashboardEventViewHolder = (DashboardEventViewHolder) holder;
                if (currentHeaderType == HEADER_TODAY)
                    dashboardEventViewHolder.bindEvent(todayEvents.get(position - 1));
                else if(currentHeaderType == HEADER_UPCOMING)
                    dashboardEventViewHolder.bindEvent(upComingEvents.get(position - 2 - todayEvents.size()));
                else
                    dashboardEventViewHolder.bindEvent(completedEvents.get(position - 3 - todayEvents.size() - upComingEvents.size()));
            }
        }


        @Override
        public int getItemCount() {
            return (eventList == null ? 3 : todayEvents.size() + upComingEvents.size() + completedEvents.size() + 3);
        }

    }

    private class DashboardEventViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTaskName, txtDueDateText;
        private Event event;

        public DashboardEventViewHolder(@NonNull View itemView, final OnDashboardItemClickListener listener) {
            super(itemView);

            txtTaskName = itemView.findViewById(R.id.taskName);
            txtDueDateText = itemView.findViewById(R.id.dueDateText);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onScheduleClick(event);
                }
            });
        }

        public void bindEvent(Event e) {
            this.event = e;
            Calendar cal = Calendar.getInstance();
            Date current = cal.getTime();
            Date due = e.getDeadline();

            txtTaskName.setText(e.getDescription());
            //Calculate due date
            long diff = due.getTime() - current.getTime();
            long i;
            if (currentHeaderType == HEADER_TODAY) {
                i = (int) TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
                if (i <= 0) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(due);
                    txtDueDateText.setText("Already due at\n" + c.get(Calendar.HOUR_OF_DAY) + ":" + (c.get(Calendar.MINUTE)<10?"0" + c.get(Calendar.MINUTE):c.get(Calendar.MINUTE)));
                    txtDueDateText.setTextColor(Color.RED);
                } else {
                    txtDueDateText.setText("Due " + (i > 0 && i <= 1 ? "within 1 Hour" : "in " + (int) i + " Hours"));
                    txtDueDateText.setTextColor(Color.BLACK);
                }
            } else if(currentHeaderType == HEADER_UPCOMING) {
                i = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                if (i <= 0) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(due);
                    txtDueDateText.setText("Already due on\n" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.YEAR) + " - " + c.get(Calendar.HOUR_OF_DAY) + ":" + (c.get(Calendar.MINUTE)<10?"0" + c.get(Calendar.MINUTE):c.get(Calendar.MINUTE)));
                    txtDueDateText.setTextColor(Color.RED);
                } else {
                    txtDueDateText.setText("Due " + (i > 0 && i <= 1 ? "within 1 Day" : "in " + (int) i + " Days"));
                    txtDueDateText.setTextColor(Color.BLACK);
                }
            } else {
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                txtDueDateText.setText("Completed on " + df.format(e.getCompletedDate()));
                txtDueDateText.setTextColor(Color.rgb(8, 144, 0));
            }

        }

    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView header;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            header = itemView.findViewById(R.id.textViewHeader);
        }
    }
}
