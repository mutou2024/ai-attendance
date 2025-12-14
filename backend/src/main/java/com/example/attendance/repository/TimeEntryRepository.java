package com.example.attendance.repository;

import com.example.attendance.model.TimeEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Repository
public class TimeEntryRepository {
    private final JdbcTemplate jdbc;

    public TimeEntryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<TimeEntry> mapper = new RowMapper<>() {
        @Override
        public TimeEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            TimeEntry t = new TimeEntry();
            t.setId(rs.getLong("id"));
            t.setUserId(rs.getLong("user_id"));
            String s = rs.getString("start_time");
            String e = rs.getString("end_time");
            if (s != null) t.setStartTime(Instant.parse(s));
            if (e != null) t.setEndTime(Instant.parse(e));
            long d = rs.getLong("duration_seconds");
            if (!rs.wasNull()) t.setDurationSeconds(d);
            t.setTag(rs.getString("tag"));
            t.setNote(rs.getString("note"));
            return t;
        }
    };

    public List<TimeEntry> findByUserBetween(Long userId, String startUtc, String endUtc) {
        String sql = "SELECT * FROM time_entries WHERE user_id = ? AND (start_time BETWEEN ? AND ? OR (end_time IS NOT NULL AND end_time BETWEEN ? AND ?) OR (start_time <= ? AND (end_time IS NULL OR end_time >= ?))) ORDER BY start_time";
        return jdbc.query(sql, new Object[]{userId, startUtc, endUtc, startUtc, endUtc, startUtc, endUtc}, mapper);
    }

    public List<TimeEntry> findByUserDay(Long userId, String date) {
        String sql = "SELECT * FROM time_entries WHERE user_id = ? AND date(start_time) = ? ORDER BY start_time";
        return jdbc.query(sql, new Object[]{userId, date}, mapper);
    }

    public int save(TimeEntry t) {
        if (t.getId() == null) {
            String sql = "INSERT INTO time_entries (user_id, start_time, end_time, duration_seconds, tag, note, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))";
            return jdbc.update(sql, t.getUserId(), t.getStartTime().toString(), t.getEndTime() != null ? t.getEndTime().toString() : null, t.getDurationSeconds(), t.getTag(), t.getNote());
        } else {
            String sql = "UPDATE time_entries SET start_time = ?, end_time = ?, duration_seconds = ?, tag = ?, note = ?, updated_at = datetime('now') WHERE id = ?";
            return jdbc.update(sql, t.getStartTime().toString(), t.getEndTime() != null ? t.getEndTime().toString() : null, t.getDurationSeconds(), t.getTag(), t.getNote(), t.getId());
        }
    }

}
