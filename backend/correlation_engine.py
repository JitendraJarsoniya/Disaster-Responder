"""
Disaster Responder - Alert Processing & Verification Engine
Handles data normalization, deduplication, multi-source corroboration, and versioned SQLite persistence.
Uses db_lock to ensure atomic, lock-free SQLite writes.
"""

import json
from datetime import datetime, timezone
from database import get_db_connection, db_lock
from indian_geography import resolve_location_coordinates

def process_collected_records(records, source_name, source_url):
    now_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")
    new_count = 0
    updated_count = 0

    with db_lock:
        conn = get_db_connection()
        cursor = conn.cursor()
        try:
            for rec in records:
                external_id = rec.get("external_id")
                title = rec.get("title")
                description = rec.get("description")
                hindi_title = rec.get("hindi_title")
                hindi_description = rec.get("hindi_description")
                category = rec.get("category", "General Hazard")
                hazard_subtype = rec.get("hazard_subtype", "General Hazard")
                severity = rec.get("severity", "HIGH")
                official_severity = rec.get("official_severity", "Official Advisory")
                affected_area = rec.get("affected_area", "Pan-India")
                published_time = rec.get("published_time", now_utc)
                valid_until = rec.get("valid_until", "Valid for 24 Hours")
                safety_instructions = rec.get("safety_instructions", [])

                # 1. Resolve Location Coordinates using Indian Geography Hierarchy
                geo_info = resolve_location_coordinates(affected_area)

                # 2. Check for duplicate or existing alert
                cursor.execute("SELECT * FROM alerts WHERE id = ? OR stable_source_id = ?", (external_id, external_id))
                existing = cursor.fetchone()

                if existing:
                    # Check if updated
                    if existing["description"] != description or existing["severity"] != severity:
                        cursor.execute("SELECT COUNT(*) FROM alert_updates WHERE alert_id = ?", (existing["id"],))
                        version_count = cursor.fetchone()[0] + 1

                        cursor.execute("""
                        INSERT INTO alert_updates (alert_id, version, previous_content, new_content, update_reason, timestamp, source_reference)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """, (existing["id"], version_count, existing["description"], description, "Source updated advisory details", now_utc, source_name))

                        cursor.execute("""
                        UPDATE alerts
                        SET description = ?, hindi_title = ?, hindi_description = ?, severity = ?, version = ?, last_updated_time = ?, modified_at = ?
                        WHERE id = ?
                        """, (description, hindi_title, hindi_description, severity, version_count, now_utc, now_utc, existing["id"]))

                        updated_count += 1

                    # Record corroborating source
                    cursor.execute("""
                    INSERT INTO alert_sources (alert_id, source_authority, source_url, external_identifier, source_publication_timestamp, last_fetched_timestamp, verification_metadata_json)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, (existing["id"], source_name, source_url, external_id, published_time, now_utc, json.dumps({"status": "Corroborated"})))

                else:
                    # Insert New Alert Record into SQLite
                    alert_id = external_id
                    cursor.execute("""
                    INSERT INTO alerts (id, stable_source_id, title, description, hindi_title, hindi_description, category, hazard_subtype, severity, official_severity, status, issuing_authority, source_type, source_url, published_time, first_collected_time, last_updated_time, valid_until, is_verified_official, is_demo_data, is_corroborated_by_multiple_sources, safety_instructions_json, version, created_at, modified_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, (
                        alert_id, external_id, title, description, hindi_title, hindi_description, category, hazard_subtype, severity, official_severity, "PUBLISHED",
                        source_name, "Official Government Warning", source_url, published_time, now_utc,
                        now_utc, valid_until, 1, 0, 0, json.dumps(safety_instructions), 1, now_utc, now_utc
                    ))

                    # Insert Location Record
                    cursor.execute("""
                    INSERT INTO alert_locations (alert_id, state, district, latitude, longitude, precision_level)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, (alert_id, geo_info["state"], geo_info["district"], geo_info["latitude"], geo_info["longitude"], geo_info["precision"]))

                    # Insert Source Record
                    cursor.execute("""
                    INSERT INTO alert_sources (alert_id, source_authority, source_url, external_identifier, source_publication_timestamp, last_fetched_timestamp, verification_metadata_json)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, (alert_id, source_name, source_url, external_id, published_time, now_utc, json.dumps({"status": "Primary Source"})))

                    new_count += 1

            conn.commit()
        finally:
            conn.close()

    return new_count, updated_count
