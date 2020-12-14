package com.example.weteams.logic;

import com.google.firebase.firestore.DocumentId;

public class Project {
    public static final String PROJECTS_COLLECTION = "projects";

    public static final String NAME_KEY = "name";
    public static final String DESCRIPTION_KEY = "description";
    public static final String MEMBERS_KEY = "members";

    @DocumentId
    private String id;
    private String name;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Project { id: " + id + ", name: " + name + ", description: " + description + " }";
    }
}
