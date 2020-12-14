package com.example.weteams.logic;

import com.google.firebase.firestore.DocumentId;

import java.util.Calendar;
import java.util.Date;

public class Event {
    public static final String EVENT_COLLECTION = "events";

    public static final String DESCRIPTION_KEY = "description";
    public static final String DEADLINE_KEY = "deadline";
    public static final String COMPLETED_KEY = "completed";
    public static final String COMPLETED_DATE_KEY = "completedDate";


    @DocumentId
    private String id;
    private String description;
    private Date deadline;
    private boolean completed;
    private Date completedDate;

    public Event() {

    }


    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDeadline() {
        return deadline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public String toString() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(deadline);
        return id + "\n" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR) + " - " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + "\n" + getDescription() + "\n" + isCompleted();
    }
}
