package com.project.lumina.rpc.entities;

import com.google.gson.annotations.SerializedName;

public class Timestamps {
    @SerializedName("start")
    private Long start;

    @SerializedName("end")
    private Long end;

    public Timestamps() {
    }

    public Timestamps(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }
}