"""
Disaster Responder - Hourly Automatic Source Scheduler
Schedules hourly checks across IMD, NDMA, CWC, INCOIS, NDRF, and SDMA source adapters.
Uses db_lock to ensure atomic, lock-free SQLite log writes.
"""

import time
import threading
from datetime import datetime, timezone
from database import get_db_connection, db_lock
from source_adapters import IMDAdapter, NDMAAdapter, CWCAdapter, INCOISAdapter, NDRFAdapter, SDMALocalAdapter
from correlation_engine import process_collected_records

ADAPTERS = [
    IMDAdapter(),
    NDMAAdapter(),
    CWCAdapter(),
    INCOISAdapter(),
    NDRFAdapter(),
    SDMALocalAdapter()
]

def run_source_sync_task():
    now_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")

    for adapter in ADAPTERS:
        try:
            res = adapter.fetch_latest_advisories()
            status = res["status"]
            records = res["records"]
            error_msg = res["error"]

            new_cnt = 0
            upd_cnt = 0
            if records:
                new_cnt, upd_cnt = process_collected_records(records, adapter.source_name, adapter.website_url)

            with db_lock:
                conn = get_db_connection()
                cursor = conn.cursor()
                try:
                    cursor.execute("""
                    INSERT INTO source_sync_logs (source_name, attempted_time, successful_time, records_collected, new_alerts, updated_alerts, failed_requests, last_error, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, (
                        adapter.source_name,
                        now_utc,
                        now_utc if status == "CONNECTED" else None,
                        len(records),
                        new_cnt,
                        upd_cnt,
                        1 if status != "CONNECTED" else 0,
                        error_msg,
                        status
                    ))
                    conn.commit()
                finally:
                    conn.close()

        except Exception as e:
            with db_lock:
                conn = get_db_connection()
                cursor = conn.cursor()
                try:
                    cursor.execute("""
                    INSERT INTO source_sync_logs (source_name, attempted_time, successful_time, records_collected, new_alerts, updated_alerts, failed_requests, last_error, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, (
                        adapter.source_name,
                        now_utc,
                        None,
                        0, 0, 0, 1,
                        str(e),
                        "ERROR"
                    ))
                    conn.commit()
                finally:
                    conn.close()

class HourlySyncScheduler:
    def __init__(self, interval_seconds=3600):
        self.interval = interval_seconds
        self.running = False
        self.thread = None

    def start(self):
        if not self.running:
            self.running = True
            self.thread = threading.Thread(target=self._run_loop, daemon=True)
            self.thread.start()

    def _run_loop(self):
        # Run initial sync on startup
        run_source_sync_task()
        while self.running:
            time.sleep(self.interval)
            run_source_sync_task()

    fun_manual_sync = run_source_sync_task
