package com.example.weteams.fragments.schedule;

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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weteams.MainActivity;
import com.example.weteams.R;
import com.example.weteams.fragments.dashboard.DashboardFragment;
import com.example.weteams.logic.Event;
import com.example.weteams.logic.Project;
import com.example.weteams.logic.SortEvents;
import com.example.weteams.viewmodel.EventsViewModel;
import com.example.weteams.viewmodel.MainViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleFragment extends Fragment implements OnDateSelectedListener, ScheduleDialog.ScheduleDialogListener {
    public static final int EVENT_DEADLINE_DUE = 0, EVENT_DEADLINE_NORMAL = 1, EVENT_DEADLINE_NEAR = 2;

    private FloatingActionButton fabNewEvent;
    private MaterialCalendarView calendarView;
    private Calendar calendar;
    private int mYear, mMonth, mDay;
    private List<Event> eventList, selectedDayEvent;
    private MainViewModel mainViewModel;
    private Project currentProject;
    private EventsViewModel eventsViewModel;
    private RecyclerView recyclerView;
    private ScheduleAdapter scheduleAdapter;
    private TextView placeHolderTextView;
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
                Log.wtf("PROJECT", project.getName());
                currentProject = project;
                eventsViewModel = new ViewModelProvider(getActivity()).get(EventsViewModel.class);
                eventsViewModel.setCurrentProject(currentProject);
                eventsViewModel.init();
                eventsViewModel.getEventList().observe(ScheduleFragment.this, new Observer<List<Event>>() {
                    @Override
                    public void onChanged(List<Event> events) {
                        eventList = events;
                        setEvents();
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        fabNewEvent = view.findViewById(R.id.fabNewEvent);
        calendarView = view.findViewById(R.id.calendarView);
        placeHolderTextView = view.findViewById(R.id.placeHolderText);
        calendar = Calendar.getInstance();

        if (preferences.contains(DashboardFragment.SELECTED_DATE_KEY)) {
            String[] date = preferences.getString(DashboardFragment.SELECTED_DATE_KEY, "").split("/");
            mMonth = Integer.parseInt(date[0]);
            mDay = Integer.parseInt(date[1]);
            mYear = Integer.parseInt(date[2]);
        } else {
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
        }
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);
        calendarView.setOnDateChangedListener(this);
        calendarView.setCurrentDate(CalendarDay.from(mYear, mMonth + 1, mDay), true);
        calendarView.setDateSelected(CalendarDay.from(mYear, mMonth + 1, mDay), true);

        fabNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onScheduleBtnClick(v);
            }
        });

        recyclerView = view.findViewById(R.id.recyclerSchedule);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleAdapter = new ScheduleAdapter(new OnScheduleClickListener() {
            @Override
            public void onScheduleClick(final Event e) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Change event status")
                        .setMessage("Mark event as " + (e.isCompleted() ? "IN PROGRESS?" : "COMPLETED?"))
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                updateEventStatus(e);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        }, new OnScheduleLongClickListener() {
            @Override
            public void onScheduleLongClick(final Event e) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete event?")
                        .setMessage("Are you sure you want to delete this event?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteEvent(e);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });
        recyclerView.setAdapter(scheduleAdapter);
        placeHolderTextView.setVisibility(View.INVISIBLE);
        recyclerView.setNestedScrollingEnabled(false);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //preferences = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Schedule");
    }


    public void onScheduleBtnClick(final View v) {
        DialogFragment scheduleDialog = new ScheduleDialog(calendarView.getSelectedDate());
        scheduleDialog.show(getChildFragmentManager(), null);
    }

    public void decorateDay(final int inputYear, final int inputMonth, final int inputDay) {
        final CalendarDay temp = CalendarDay.from(inputYear, inputMonth, inputDay);
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return temp.equals(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                //view.addSpan(new ForegroundColorSpan(Color.RED));
                view.addSpan(new DotSpan(5, Color.RED));
            }
        });
        calendarView.invalidateDecorators();

    }

    public void setEvents() {
        calendarView.removeDecorators();
        SortEvents.quickSort(eventList, 0, eventList.size() - 1);
        for (Event e : eventList) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(e.getDeadline());
            int y = cal.get(Calendar.YEAR), m = cal.get(Calendar.MONTH) + 1, d = cal.get(Calendar.DAY_OF_MONTH);
            decorateDay(y, m, d);
        }
        ((ScheduleAdapter) recyclerView.getAdapter()).syncEventsOnSelectedDate();
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        //Toast.makeText(getContext(), date.getYear() + "/" + date.getMonth() + "/" + date.getDay(), Toast.LENGTH_LONG).show();
        selectedDayEvent = getEventsFromDate(date.getYear(), date.getMonth(), date.getDay());
        String d = date.getMonth() - 1 + "/" + date.getDay() + "/" + date.getYear();
        preferences.edit().putString(DashboardFragment.SELECTED_DATE_KEY, d).apply();
        if (selectedDayEvent.size() == 0) {
            placeHolderTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            placeHolderTextView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            scheduleAdapter = new ScheduleAdapter(new OnScheduleClickListener() {
                @Override
                public void onScheduleClick(final Event e) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Change event status")
                            .setMessage("Mark event as " + (e.isCompleted() ? "IN PROGRESS?" : "COMPLETED?"))
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    updateEventStatus(e);
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                }
            }, new OnScheduleLongClickListener() {
                @Override
                public void onScheduleLongClick(final Event e) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Delete event?")
                            .setMessage("Are you sure you want to delete this event?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteEvent(e);
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                }
            }, selectedDayEvent);
            recyclerView.setAdapter(scheduleAdapter);
        }
    }

    public List<Event> getEventsFromDate(int year, int month, int day) {
        List<Event> results = new ArrayList<>();
        for (Event e : eventList) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(e.getDeadline());
            int y = cal.get(Calendar.YEAR), m = cal.get(Calendar.MONTH) + 1, d = cal.get(Calendar.DAY_OF_MONTH);
            if (y == year && m == month && d == day) {
                results.add(e);
            }
        }
        return results;
    }

    public void deleteEvent(Event e) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference query = db.collection(Project.PROJECTS_COLLECTION).document(currentProject.getId()).collection(Event.EVENT_COLLECTION);

        query.document(e.getId()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        ((ScheduleAdapter) recyclerView.getAdapter()).syncEventsOnSelectedDate();
                        Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.wtf("DELETE SCHEDULE", e.getMessage());
                Toast.makeText(getContext(), "Failed deleting event", Toast.LENGTH_SHORT).show();
            }
        });
        ;
    }

    public void updateEventStatus(Event e) {
        Calendar c = Calendar.getInstance();
        Date d = c.getTime();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference query = db.collection(Project.PROJECTS_COLLECTION).document(currentProject.getId()).collection(Event.EVENT_COLLECTION);
        Map<String, Object> updatedEvent = new HashMap<>();
        updatedEvent.put(Event.COMPLETED_KEY, !e.isCompleted());
        updatedEvent.put(Event.DEADLINE_KEY, e.getDeadline());
        updatedEvent.put(Event.DESCRIPTION_KEY, e.getDescription());
        updatedEvent.put(Event.COMPLETED_DATE_KEY, !e.isCompleted() ? d : null);

        query.document(e.getId()).set(updatedEvent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        ((ScheduleAdapter) recyclerView.getAdapter()).syncEventsOnSelectedDate();
                        Toast.makeText(getContext(), "Event updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.wtf("UPDATE SCHEDULE", e.getMessage());
                        Toast.makeText(getContext(), "Failed updating status", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onAddSchedule(final Event e) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference query = db.collection(Project.PROJECTS_COLLECTION).document(currentProject.getId()).collection(Event.EVENT_COLLECTION);

        query.add(e)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(), "Event Added!", Toast.LENGTH_SHORT).show();
                        ((ScheduleAdapter) recyclerView.getAdapter()).syncEventsOnSelectedDate();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.wtf("EVENT ADD", "Failed adding\n" + e.getMessage());
                        Toast.makeText(getContext(), "Failed adding event", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public interface OnScheduleClickListener {
        void onScheduleClick(Event e);
    }

    public interface OnScheduleLongClickListener {
        void onScheduleLongClick(Event e);
    }

    private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {
        private OnScheduleClickListener listener;
        private OnScheduleLongClickListener longClickListener;
        private List<Event> adapterEvents;


        public ScheduleAdapter(OnScheduleClickListener listener, OnScheduleLongClickListener longClickListener, List<Event> adapterEvents) {
            this.listener = listener;
            this.longClickListener = longClickListener;
            this.adapterEvents = adapterEvents;
        }

        public ScheduleAdapter(OnScheduleClickListener listener, OnScheduleLongClickListener longClickListener) {
            this.listener = listener;
            this.longClickListener = longClickListener;
            this.adapterEvents = eventList;
        }


        @NonNull
        @Override
        public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.listitem_event, parent, false);

            return new ScheduleViewHolder(view, listener, longClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
            holder.bindEvent(adapterEvents.get(position));
        }


        @Override
        public int getItemCount() {
            return (adapterEvents == null ? 0 : adapterEvents.size());
        }

        public void syncEventsOnSelectedDate() {
            onDateSelected(calendarView, calendarView.getSelectedDate(), true);
        }
    }

    private class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTime, txtDescription, txtStatus;
        private Event event;

        public ScheduleViewHolder(@NonNull View itemView, final OnScheduleClickListener listener, final OnScheduleLongClickListener longClickListener) {
            super(itemView);

            txtTime = itemView.findViewById(R.id.txtEventTime);
            txtDescription = itemView.findViewById(R.id.txtEventDescription);
            txtStatus = itemView.findViewById(R.id.txtEventStatus);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onScheduleClick(event);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    longClickListener.onScheduleLongClick(event);
                    return true;
                }
            });

        }

        public void bindEvent(Event e) {
            this.event = e;
            Date d = e.getDeadline();
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            String dateStr = df.format(d);

            txtTime.setText(dateStr);
            txtDescription.setText(e.getDescription());
            txtStatus.setText(e.isCompleted() ? ("Completed on " + df.format(e.getCompletedDate())): "In Progress");

            int status = checkDeadline(e);

            if (status == EVENT_DEADLINE_DUE) {
                txtDescription.setTextColor(Color.RED);
                txtTime.setTextColor(Color.RED);
            } else if (status == EVENT_DEADLINE_NEAR) {
                txtTime.setTextColor(Color.rgb(255, 153, 0));
                txtDescription.setTextColor(Color.rgb(255, 153, 0));
            }
            txtStatus.setTextColor(e.isCompleted() ? Color.rgb(8, 144, 0) : Color.RED);
        }

        public int checkDeadline(Event e) {
            Calendar current = Calendar.getInstance();
            Calendar event = Calendar.getInstance();
            event.setTime(e.getDeadline());

            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");

            if (event.before(current) || event.equals(current))
                return EVENT_DEADLINE_DUE;
            if (current.get(Calendar.YEAR) == event.get(Calendar.YEAR)
                    && current.get(Calendar.MONTH) == event.get(Calendar.MONTH)
                    && (event.get(Calendar.DAY_OF_MONTH) - current.get(Calendar.DAY_OF_MONTH)) <= 1)
                return EVENT_DEADLINE_NEAR;
            return EVENT_DEADLINE_NORMAL;
        }
    }
}
