package com.example.sm_project.Models;

import android.os.Build;
import androidx.annotation.RequiresApi;

import java.util.Comparator;

@RequiresApi(api = Build.VERSION_CODES.N)
public class Group {

    private String title, language, owner, id;
    private int usersCount;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setUsersCount(int usersCount) {
        this.usersCount = usersCount;
    }

    public int getUsersCount() {
        return usersCount;
    }

    public static Comparator<Group> groupTitleComparator = Comparator.comparing(group -> group.getTitle().toLowerCase());

}
