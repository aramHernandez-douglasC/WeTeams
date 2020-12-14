package com.example.weteams.fragments.schedule;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.weteams.R;
import com.example.weteams.fragments.projects.CreateProjectDialog;
import com.example.weteams.logic.Event;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class ScheduleDialog extends DialogFragment {
    private int mYear, mMonth, mDay, mHour, mMin;

    interface ScheduleDialogListener {
        void onAddSchedule(Event e);
    }

    public ScheduleDialog(CalendarDay day) {
        this.mYear = day.getYear();
        this.mMonth = day.getMonth()-1;
        this.mDay = day.getDay();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();

        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMin = calendar.get(Calendar.MINUTE);

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_schedule, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add new event");
        builder.setView(dialogView);

        //Set up dialog
        Button btnDate = dialogView.findViewById(R.id.btnSelDate);
        Button btnTime = dialogView.findViewById(R.id.btnSelTime);
        final EditText editDate = dialogView.findViewById(R.id.editDate);
        final EditText editTime = dialogView.findViewById(R.id.editTime);
        final EditText editDescription = dialogView.findViewById(R.id.editDescription);


        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dateDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mYear = year;
                        mMonth = month;
                        mDay = dayOfMonth;

                        editDate.setText((mMonth + 1) + "/" + (mDay) + "/" + mYear);
                        //decorateDay(mYear, mMonth, mDay);
                    }
                }, mYear, mMonth, mDay);
                dateDialog.show();
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timeDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mHour = hourOfDay;
                        mMin = minute;

                        editTime.setText(mHour + ":" + mMin);
                    }
                }, mHour, mMin, true);
                timeDialog.show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Schedule", null);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                editDate.setText((mMonth + 1) + "/" + (mDay) + "/" + mYear);
                editTime.setText(mHour + ":" + mMin);

                Button btn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.wtf("DATE TIME", editDate.getText().toString() + "\n" + editTime.getText().toString());
                        //decorateDay(mYear, mMonth + 1, mDay);
                        if (TextUtils.isEmpty(editDescription.getText().toString()))
                            Toast.makeText(getContext(), "Please enter a description", Toast.LENGTH_LONG).show();
                        else {
                            Calendar cal = Calendar.getInstance();
                            cal.set(mYear, mMonth, mDay, mHour, mMin);
                            Date d = cal.getTime();
                            Event e = new Event();
                            e.setDeadline(d);
                            e.setDescription(editDescription.getText().toString());
                            e.setCompleted(false);
                            e.setCompletedDate(null);

                            ScheduleDialogListener listener = (ScheduleDialogListener) getParentFragment();
                            listener.onAddSchedule(e);
                            dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }
}
