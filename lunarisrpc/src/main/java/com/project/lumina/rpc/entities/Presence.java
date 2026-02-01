package com.project.lumina.rpc.entities;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Presence {
    @SerializedName("activities")
    private List<Activity> activities;

    @SerializedName("afk")
    private boolean afk;

    @SerializedName("since")
    private Long since;

    @SerializedName("status")
    private String status;

    public Presence() {
    }

    public Presence(List<Activity> activities, boolean afk, Long since, String status) {
        this.activities = activities;
        this.afk = afk;
        this.since = since;
        this.status = status;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public boolean isAfk() {
        return afk;
    }

    public void setAfk(boolean afk) {
        this.afk = afk;
    }

    public Long getSince() {
        return since;
    }

    public void setSince(Long since) {
        this.since = since;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void validate() {
        if (activities != null) {
            for (Activity activity : activities) {
                if (activity != null) {
                    activity.validate();
                }
            }
        }
    }
}