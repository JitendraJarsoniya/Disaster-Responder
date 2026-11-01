"""
Disaster Responder - SQLite Database Module
Manages persistent, normalized tables for Alerts, Sources, Updates, Locations, Verifications, Sync Logs, and SOS Events.
Includes backward-compatible database migration logic for Hindi fields, versioning, and SOS distress events.
"""

import sqlite3
import json
import os
import threading
from datetime import datetime

DB_PATH = os.path.join(os.path.dirname(__file__), "alerts.db")
db_lock = threading.Lock()

def get_db_connection():
    conn = sqlite3.connect(DB_PATH, timeout=15.0)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL;")
    conn.execute("PRAGMA busy_timeout=15000;")
    return conn

def init_db():
    with db_lock:
        conn = get_db_connection()
        cursor = conn.cursor()

        # 1. Alerts Table
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS alerts (
            id TEXT PRIMARY KEY,
            stable_source_id TEXT,
            title TEXT NOT NULL,
            description TEXT NOT NULL,
            hindi_title TEXT,
            hindi_description TEXT,
            category TEXT NOT NULL,
            hazard_subtype TEXT DEFAULT 'General Hazard',
            severity TEXT NOT NULL,
            official_severity TEXT DEFAULT 'Official Advisory',
            status TEXT NOT NULL,
            issuing_authority TEXT NOT NULL,
            source_type TEXT NOT NULL,
            source_url TEXT NOT NULL,
            published_time TEXT NOT NULL,
            first_collected_time TEXT,
            last_updated_time TEXT NOT NULL,
            valid_from TEXT,
            valid_until TEXT,
            is_verified_official INTEGER DEFAULT 1,
            is_demo_data INTEGER DEFAULT 0,
            is_corroborated_by_multiple_sources INTEGER DEFAULT 0,
            unconfirmed_reason TEXT,
            safety_instructions_json TEXT,
            version INTEGER DEFAULT 1,
            created_at TEXT NOT NULL,
            modified_at TEXT NOT NULL
        )
        """)

        # Backward-compatible migrations for existing alerts table columns
        migration_columns = [
            ("hindi_title", "TEXT"),
            ("hindi_description", "TEXT"),
            ("hazard_subtype", "TEXT DEFAULT 'General Hazard'"),
            ("official_severity", "TEXT DEFAULT 'Official Advisory'"),
            ("first_collected_time", "TEXT"),
            ("version", "INTEGER DEFAULT 1")
        ]

        for col_name, col_type in migration_columns:
            try:
                cursor.execute(f"ALTER TABLE alerts ADD COLUMN {col_name} {col_type}")
            except sqlite3.OperationalError:
                pass  # Column already exists

        # 2. Alert Sources Table
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS alert_sources (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            alert_id TEXT NOT NULL,
            source_authority TEXT NOT NULL,
            source_url TEXT NOT NULL,
            external_identifier TEXT,
            source_publication_timestamp TEXT,
            last_fetched_timestamp TEXT NOT NULL,
            verification_metadata_json TEXT,
            FOREIGN KEY (alert_id) REFERENCES alerts (id)
        )
        """)

        # 3. Alert Version Updates Audit Log
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS alert_updates (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            alert_id TEXT NOT NULL,
            version INTEGER NOT NULL,
            previous_content TEXT,
            new_content TEXT,
            update_reason TEXT NOT NULL,
            timestamp TEXT NOT NULL,
            source_reference TEXT,
            FOREIGN KEY (alert_id) REFERENCES alerts (id)
        )
        """)

        # 4. Alert Locations Table
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS alert_locations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            alert_id TEXT NOT NULL,
            state TEXT NOT NULL,
            district TEXT NOT NULL,
            sub_district TEXT,
            village_locality TEXT,
            latitude REAL,
            longitude REAL,
            precision_level TEXT NOT NULL,
            FOREIGN KEY (alert_id) REFERENCES alerts (id)
        )
        """)

        # 5. Alert Verifications Table
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS alert_verifications (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            alert_id TEXT NOT NULL,
            verification_status TEXT NOT NULL,
            evidence_references_json TEXT,
            verification_method TEXT NOT NULL,
            reviewer_id TEXT,
            timestamp TEXT NOT NULL,
            review_notes TEXT,
            FOREIGN KEY (alert_id) REFERENCES alerts (id)
        )
        """)

        # 6. Source Sync Logs Table
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS source_sync_logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            source_name TEXT NOT NULL,
            attempted_time TEXT NOT NULL,
            successful_time TEXT,
            records_collected INTEGER DEFAULT 0,
            new_alerts INTEGER DEFAULT 0,
            updated_alerts INTEGER DEFAULT 0,
            failed_requests INTEGER DEFAULT 0,
            last_error TEXT,
            status TEXT NOT NULL
        )
        """)

        # 7. SOS Events Table
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS sos_events (
            id TEXT PRIMARY KEY,
            timestamp TEXT NOT NULL,
            latitude REAL,
            longitude REAL,
            location_name TEXT NOT NULL,
            emergency_profile_text TEXT NOT NULL,
            status TEXT NOT NULL DEFAULT 'ACTIVE_DISTRESS',
            created_at TEXT NOT NULL
        )
        """)

        # Indices for efficient historical retention and pagination queries
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_alerts_status ON alerts(status)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_alerts_category ON alerts(category)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_alerts_created_at ON alerts(created_at)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_locations_state ON alert_locations(state)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_locations_district ON alert_locations(district)")

        conn.commit()
        conn.close()

if __name__ == "__main__":
    init_db()
    print("SQLite WAL Mode, Migrations & Indexing initialized successfully.")
