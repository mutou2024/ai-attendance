package com.example.attendance.model;

import java.time.Instant;

public class TimeEntry {
    private Long id;
    private Long userId;
    private Instant startTime;
    private Instant endTime;
    private Long durationSeconds;
    private String tag;
    private String note;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
