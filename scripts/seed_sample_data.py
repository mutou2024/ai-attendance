#!/usr/bin/env python3
"""
Seed sample time_entries into the SQLite database for local testing.

Usage:
  python3 scripts/seed_sample_data.py [--db path/to/backend/data/app.db] [--user-id 1] [--days 7]

This script will create the table if missing and insert `days` sample entries (one per day)
for the given user_id. Times are local ISO-8601 strings.
"""
import sqlite3
import argparse
from datetime import datetime, timedelta
import os


def ensure_schema(conn):
    conn.executescript(r"""
    CREATE TABLE IF NOT EXISTS time_entries (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      start_time TEXT NOT NULL,
      end_time TEXT,
      duration_seconds INTEGER,
      tag TEXT,
      note TEXT,
      created_at TEXT NOT NULL,
      updated_at TEXT NOT NULL
    );
    CREATE INDEX IF NOT EXISTS idx_timeentries_user_start ON time_entries(user_id, start_time);
    CREATE INDEX IF NOT EXISTS idx_timeentries_user_end ON time_entries(user_id, end_time);
    """)


def iso(dt):
    return dt.replace(microsecond=0).isoformat()


def seed(db_path, user_id=1, days=7):
    os.makedirs(os.path.dirname(db_path), exist_ok=True)
    conn = sqlite3.connect(db_path)
    ensure_schema(conn)
    now = datetime.now()
    inserted = 0
    for i in range(days):
        day = now - timedelta(days=i)
        start = day.replace(hour=9, minute=0, second=0, microsecond=0)
        end = day.replace(hour=17, minute=30, second=0, microsecond=0)
        duration = int((end - start).total_seconds())
        created = iso(now)
        # avoid duplicate exact entries: check if exists
        cur = conn.execute(
            "SELECT COUNT(1) FROM time_entries WHERE user_id=? AND start_time=?",
            (user_id, iso(start)),
        )
        if cur.fetchone()[0] > 0:
            continue
        conn.execute(
            """
            INSERT INTO time_entries (user_id, start_time, end_time, duration_seconds, tag, note, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (user_id, iso(start), iso(end), duration, 'work', 'Sample seeded entry', created, created),
        )
        inserted += 1
    conn.commit()
    conn.close()
    print(f"Inserted {inserted} sample entries into {db_path} (user_id={user_id})")


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--db', default='data/app.db', help='Path to sqlite DB file')
    p.add_argument('--user-id', type=int, default=1)
    p.add_argument('--days', type=int, default=7)
    args = p.parse_args()
    seed(args.db, args.user_id, args.days)


if __name__ == '__main__':
    main()
