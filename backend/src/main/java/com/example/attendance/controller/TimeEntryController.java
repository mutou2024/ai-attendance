package com.example.attendance.controller;

import com.example.attendance.model.TimeEntry;
import com.example.attendance.repository.TimeEntryRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TimeEntryController {
    private final TimeEntryRepository repo;

    public TimeEntryController(TimeEntryRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/users/{userId}/calendar")
    public ResponseEntity<?> monthCalendar(@PathVariable Long userId, @RequestParam int year, @RequestParam int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        String startUtc = start.atStartOfDay(ZoneId.of("UTC")).toInstant().toString();
        String endUtc = end.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant().toString();

        List<TimeEntry> list = repo.findByUserBetween(userId, startUtc, endUtc);

        // Group by local date in UTC (simple approach) -> map date -> total seconds and count
        Map<LocalDate, Long> totals = new HashMap<>();
        Map<LocalDate, Integer> counts = new HashMap<>();
        for (TimeEntry t : list) {
            Instant s = t.getStartTime();
            Instant e = t.getEndTime() != null ? t.getEndTime() : Instant.now();
            long seconds = t.getDurationSeconds() != null ? t.getDurationSeconds() : (e.getEpochSecond() - s.getEpochSecond());
            LocalDate d = s.atZone(ZoneId.of("UTC")).toLocalDate();
            totals.put(d, totals.getOrDefault(d, 0L) + seconds);
            counts.put(d, counts.getOrDefault(d, 0) + 1);
        }

        List<Map<String,Object>> days = new ArrayList<>();
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            Map<String,Object> m = new HashMap<>();
            m.put("date", cur.toString());
            int weekday = cur.getDayOfWeek().getValue(); // 1-7
            m.put("weekday", weekday);
            m.put("seconds", totals.getOrDefault(cur, 0L));
            m.put("entryCount", counts.getOrDefault(cur, 0));
            days.add(m);
            cur = cur.plusDays(1);
        }
        Map<String,Object> resp = new HashMap<>();
        resp.put("year", year);
        resp.put("month", month);
        resp.put("days", days);
        resp.put("total_seconds", totals.values().stream().mapToLong(Long::longValue).sum());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/users/{userId}/range")
    public ResponseEntity<?> range(@PathVariable Long userId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        String startUtc = start.atStartOfDay(ZoneId.of("UTC")).toInstant().toString();
        String endUtc = end.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant().toString();
        List<TimeEntry> list = repo.findByUserBetween(userId, startUtc, endUtc);

        // Group by day
        Map<LocalDate, Long> totals = new HashMap<>();
        Map<LocalDate, List<TimeEntry>> entriesByDay = new HashMap<>();
        for (TimeEntry t : list) {
            Instant s = t.getStartTime();
            Instant e = t.getEndTime() != null ? t.getEndTime() : Instant.now();
            long seconds = t.getDurationSeconds() != null ? t.getDurationSeconds() : (e.getEpochSecond() - s.getEpochSecond());
            LocalDate d = s.atZone(ZoneId.of("UTC")).toLocalDate();
            totals.put(d, totals.getOrDefault(d, 0L) + seconds);
            entriesByDay.computeIfAbsent(d, k->new ArrayList<>()).add(t);
        }

        List<Map<String,Object>> rows = new ArrayList<>();
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            Map<String,Object> r = new HashMap<>();
            r.put("date", cur.toString());
            r.put("weekday", cur.getDayOfWeek().getValue());
            r.put("seconds", totals.getOrDefault(cur, 0L));
            r.put("entries", entriesByDay.getOrDefault(cur, Collections.emptyList()));
            rows.add(r);
            cur = cur.plusDays(1);
        }
        Map<String,Object> resp = new HashMap<>();
        resp.put("start", start.toString());
        resp.put("end", end.toString());
        resp.put("rows", rows);
        resp.put("total_seconds", totals.values().stream().mapToLong(Long::longValue).sum());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/users/{userId}/day/{date}")
    public ResponseEntity<?> dayEntries(@PathVariable Long userId, @PathVariable String date) {
        List<TimeEntry> list = repo.findByUserDay(userId, date);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/time-entries")
    public ResponseEntity<?> createEntry(@RequestBody Map<String,Object> body) {
        // minimal create: expects userId, startTime, endTime (ISO-8601 strings)
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            Instant s = Instant.parse(body.get("startTime").toString());
            Instant e = body.get("endTime") != null ? Instant.parse(body.get("endTime").toString()) : null;
            Long duration = null;
            if (e != null) duration = e.getEpochSecond() - s.getEpochSecond();
            TimeEntry t = new TimeEntry();
            t.setUserId(userId); t.setStartTime(s); t.setEndTime(e); t.setDurationSeconds(duration);
            t.setTag((String) body.getOrDefault("tag", null));
            t.setNote((String) body.getOrDefault("note", null));
            repo.save(t);
            return ResponseEntity.ok(Map.of("status","ok"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
