"""
Disaster Responder - National Alert Intelligence System & AI Microservice
FastAPI + SQLite + Scikit-Learn + Firebase Admin Auth + Gemini Conversational AI

Run command:
    pip install fastapi uvicorn scikit-learn pandas pydantic firebase-admin google-generativeai
    uvicorn ai_backend:app --reload --host 0.0.0.0 --port 8000
"""

import os
import json
import sqlite3
from typing import List, Optional
from datetime import datetime, timezone
from fastapi import FastAPI, HTTPException, Query, Header, Depends
from pydantic import BaseModel
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB

from database import init_db, get_db_connection, db_lock
from scheduler import HourlySyncScheduler, run_source_sync_task
from indian_geography import INDIAN_STATES_UTS, DISTRICT_CENTROIDS
from firebase_auth_utils import verify_firebase_admin_token, verify_firebase_responder_token

# Load environment variables from .env if present
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

# Initialize Database & Hourly Source Scheduler
init_db()
scheduler = HourlySyncScheduler(interval_seconds=3600)

app = FastAPI(
    title="Disaster Responder - National Alert Intelligence System",
    description="Provides real-time official alerts, historical retention, source monitoring, and AI decision support.",
    version="2.7.0"
)

@app.on_event("startup")
def startup_event():
    scheduler.start()

# ---------------------------------------------------------
# 1. AI Classifier Training Setup (Preserved)
# ---------------------------------------------------------
TRAIN_DATA = [
    ("Yamuna river overflow submerged houses near old bridge", "Flood"),
    ("Flash flood water entering ground floor apartments", "Flood"),
    ("Heavy rain causing severe waterlogging and trapped commuters", "Flood"),
    ("Transformer explosion caused fire in commercial market", "Fire"),
    ("Short circuit flame spreading rapidly near chemical warehouse", "Fire"),
    ("Thick black smoke and fire trapped 5 people on 3rd floor", "Fire"),
    ("Building tremors and wall cracks felt during earthquake", "Earthquake"),
    ("Structural collapse following 5.8 magnitude earthquake", "Earthquake"),
    ("Severe cyclone winds uprooted electric poles and trees", "Cyclone"),
    ("High storm winds blew off tin roofs in coastal village", "Cyclone")
]

df = pd.DataFrame(TRAIN_DATA, columns=["text", "category"])
vectorizer = TfidfVectorizer()
X_train = vectorizer.fit_transform(df["text"])
classifier = MultinomialNB()
classifier.fit(X_train, df["category"])

# ---------------------------------------------------------
# 2. Pydantic Schemas
# ---------------------------------------------------------
class ClassificationRequest(BaseModel):
    description: str

class ClassificationResponse(BaseModel):
    predicted_category: str
    confidence_score: float
    factual_summary: str
    recommended_actions: List[str]
    disclaimer: str

class ChatMessage(BaseModel):
    role: str  # "user" or "assistant"
    content: str

class ConversationalAiRequest(BaseModel):
    message: str
    conversationHistory: Optional[List[ChatMessage]] = []
    language: Optional[str] = "en"

class ConversationalAiResponse(BaseModel):
    response: str
    suggestedActions: List[str]
    disclaimer: str

class SosTriggerRequest(BaseModel):
    locationName: str
    latitude: Optional[float] = 28.6139
    longitude: Optional[float] = 77.2090
    emergencyProfileText: str

class AlertOut(BaseModel):
    id: str
    title: str
    description: str
    hindiTitle: Optional[str] = None
    hindiDescription: Optional[str] = None
    category: str
    hazardSubtype: Optional[str] = "General Hazard"
    severity: str
    officialSeverity: Optional[str] = "Official Advisory"
    status: str
    issuingAuthority: str
    sourceType: str
    sourceUrl: str
    publishedTime: str
    lastUpdatedTime: str
    validUntil: str
    isVerifiedOfficial: bool
    isDemoData: bool
    isCorroboratedByMultipleSources: bool
    unconfirmedReason: Optional[str] = None
    safetyInstructions: List[str] = []
    version: int = 1
    latitude: Optional[float] = 28.6139
    longitude: Optional[float] = 77.2090
    affectedArea: str

class AlertUpdateOut(BaseModel):
    version: int
    previousContent: Optional[str]
    newContent: str
    updateReason: str
    timestamp: str
    sourceReference: str

# ---------------------------------------------------------
# 3. Public Health Check, AI & SOS Endpoints
# ---------------------------------------------------------
@app.get("/")
def health_check():
    return {
        "status": "online",
        "service": "National Disaster Alert Intelligence System",
        "version": "v2.7.0",
        "geography_coverage": "28 States & 8 Union Territories (Pan-India)",
        "hazard_taxonomy": ["Heavy Rainfall", "Flood", "Flash Flood", "Cyclone", "Thunderstorm", "Earthquake", "Tsunami"],
        "sources": ["IMD", "NDMA SACHET", "CWC", "INCOIS", "NDRF", "SDMA"]
    }

@app.post("/predict-category", response_model=ClassificationResponse)
def predict_category(request: ClassificationRequest):
    if not request.description.strip():
        raise HTTPException(status_code=400, detail="Description text cannot be empty.")

    text_vec = vectorizer.transform([request.description])
    prediction = classifier.predict(text_vec)[0]
    probs = classifier.predict_proba(text_vec)[0]
    confidence = float(max(probs))

    actions = {
        "Flood": ["Evacuate to higher ground / Stadium shelter.", "Avoid crossing submerged roads."],
        "Fire": ["Call 101 immediately.", "Evacuate low to ground below smoke."],
        "Earthquake": ["Drop, Cover, and Hold On.", "Evacuate to open ground once tremors stop."],
        "Cyclone": ["Stay indoors away from windows.", "Keep flashlight and emergency food ready."]
    }.get(prediction, ["Remain calm and contact emergency authorities."])

    return ClassificationResponse(
        predicted_category=prediction,
        confidence_score=round(confidence, 2),
        factual_summary=f"Parsed report: Identified {prediction} incident. Dispatched for verification.",
        recommended_actions=actions,
        disclaimer="AI DECISION SUPPORT ONLY: Verify with human command center."
    )

GEMINI_SYSTEM_INSTRUCTION = """
You are the AI Disaster & Safety Assistant for Disaster Responder, an emergency management system in India.
Provide open-ended disaster preparedness, safety guidance, first-aid advice, and emergency protocols in English, Hindi, or Hinglish as requested by the user.

STRICT MANDATORY SAFETY RULES:
1. NEVER claim that a rescue team, ambulance, or emergency unit has been dispatched.
2. NEVER invent official government alerts, shelter capacity, or hospital availability.
3. CLEARLY distinguish AI-generated guidance from verified official warnings.
4. For life-threatening emergencies, prioritize instructing the user to contact 112 (National Emergency Number) or 101 (Fire) / 102/108 (Ambulance).
5. Explain safety steps in clear, simple, actionable bullet points.
"""

@app.post("/api/ai/chat", response_model=ConversationalAiResponse)
def conversational_ai_chat(request: ConversationalAiRequest):
    if not request.message.strip():
        raise HTTPException(status_code=400, detail="Message text cannot be empty.")

    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise HTTPException(
            status_code=503,
            detail="Gemini API key not configured on backend server. Please configure GEMINI_API_KEY in backend/.env."
        )

    try:
        import google.generativeai as genai
        genai.configure(api_key=api_key)

        model = genai.GenerativeModel(
            model_name="gemini-1.5-flash",
            system_instruction=GEMINI_SYSTEM_INSTRUCTION
        )

        formatted_history = []
        if request.conversationHistory:
            for msg in request.conversationHistory[-6:]:
                role = "user" if msg.role == "user" else "model"
                formatted_history.append({"role": role, "parts": [msg.content]})

        chat = model.start_chat(history=formatted_history)
        gemini_response = chat.send_message(request.message)

        text_response = gemini_response.text.strip() if gemini_response.text else "I am unable to process your request at this time. Please ensure safety and call 112 if in danger."

        actions = [
            "Ensure personal safety immediately.",
            "Contact National Emergency Helpline 112 for urgent rescue.",
            "Follow official local disaster management advisories."
        ]

        return ConversationalAiResponse(
            response=text_response,
            suggestedActions=actions,
            disclaimer="AI DECISION SUPPORT ONLY: Verify official evacuation orders with local authorities."
        )

    except Exception as e:
        err_msg = str(e)
        if "429" in err_msg or "RESOURCE_EXHAUSTED" in err_msg or "Quota" in err_msg:
            raise HTTPException(
                status_code=429,
                detail="Gemini API free tier quota limit reached. Please wait a moment before trying again."
            )
        elif "API_KEY_INVALID" in err_msg or "API key not valid" in err_msg:
            raise HTTPException(
                status_code=503,
                detail="Invalid Gemini API key configured on backend server."
            )
        else:
            raise HTTPException(
                status_code=500,
                detail=f"Gemini API request failed: {err_msg}"
            )

@app.post("/api/sos/trigger")
def trigger_sos_event(request: SosTriggerRequest):
    now_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")
    sos_id = f"sos_{int(datetime.now().timestamp())}"

    with db_lock:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("""
        INSERT INTO sos_events (id, timestamp, latitude, longitude, location_name, emergency_profile_text, status, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """, (sos_id, now_utc, request.latitude, request.longitude, request.locationName, request.emergencyProfileText, "ACTIVE_DISTRESS", now_utc))
        conn.commit()
        conn.close()

    return {
        "status": "SOS_DISPATCHED",
        "sosId": sos_id,
        "message": "Emergency SOS distress signal received and logged for Command Center dispatch."
    }

# ---------------------------------------------------------
# 4. Public REST API Endpoints (Alerts, History, Geography, Timeline)
# ---------------------------------------------------------
@app.get("/api/alerts", response_model=List[AlertOut])
def get_public_alerts(
    state: Optional[str] = None,
    category: Optional[str] = None,
    severity: Optional[str] = None,
    limit: int = 50
):
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
    SELECT a.*, l.state, l.district, l.latitude, l.longitude
    FROM alerts a
    LEFT JOIN alert_locations l ON a.id = l.alert_id
    WHERE a.status = 'PUBLISHED'
    """
    params = []

    if state:
        query += " AND l.state = ?"
        params.append(state)
    if category:
        query += " AND a.category = ?"
        params.append(category)
    if severity:
        query += " AND a.severity = ?"
        params.append(severity)

    query += " ORDER BY a.created_at DESC LIMIT ?"
    params.append(limit)

    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    result = []
    for r in rows:
        safety_inst = json.loads(r["safety_instructions_json"]) if r["safety_instructions_json"] else []
        result.append(AlertOut(
            id=r["id"],
            title=r["title"],
            description=r["description"],
            hindiTitle=r["hindi_title"] if "hindi_title" in r.keys() else None,
            hindiDescription=r["hindi_description"] if "hindi_description" in r.keys() else None,
            category=r["category"],
            hazardSubtype=r["hazard_subtype"] if "hazard_subtype" in r.keys() else "General Hazard",
            severity=r["severity"],
            officialSeverity=r["official_severity"] if "official_severity" in r.keys() else "Official Advisory",
            status=r["status"],
            issuingAuthority=r["issuing_authority"],
            sourceType=r["source_type"],
            sourceUrl=r["source_url"],
            publishedTime=r["published_time"],
            lastUpdatedTime=r["last_updated_time"],
            validUntil=r["valid_until"] or "Valid for 24 Hours",
            isVerifiedOfficial=bool(r["is_verified_official"]),
            isDemoData=bool(r["is_demo_data"]),
            isCorroboratedByMultipleSources=bool(r["is_corroborated_by_multiple_sources"]),
            unconfirmedReason=r["unconfirmed_reason"],
            safetyInstructions=safety_inst,
            version=r["version"] if "version" in r.keys() and r["version"] else 1,
            latitude=r["latitude"],
            longitude=r["longitude"],
            affectedArea=f"{r['district'] or 'Pan-India'}, {r['state'] or ''}".strip(", ")
        ))

    return result

@app.get("/api/alerts/history", response_model=List[AlertOut])
def get_alerts_history(
    state: Optional[str] = None,
    category: Optional[str] = None,
    limit: int = 100
):
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
    SELECT a.*, l.state, l.district, l.latitude, l.longitude
    FROM alerts a
    LEFT JOIN alert_locations l ON a.id = l.alert_id
    """
    params = []

    if state:
        query += " WHERE l.state = ?"
        params.append(state)
        if category:
            query += " AND a.category = ?"
            params.append(category)
    elif category:
        query += " WHERE a.category = ?"
        params.append(category)

    query += " ORDER BY a.created_at DESC LIMIT ?"
    params.append(limit)

    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    result = []
    for r in rows:
        safety_inst = json.loads(r["safety_instructions_json"]) if r["safety_instructions_json"] else []
        result.append(AlertOut(
            id=r["id"],
            title=r["title"],
            description=r["description"],
            hindiTitle=r["hindi_title"] if "hindi_title" in r.keys() else None,
            hindiDescription=r["hindi_description"] if "hindi_description" in r.keys() else None,
            category=r["category"],
            hazardSubtype=r["hazard_subtype"] if "hazard_subtype" in r.keys() else "General Hazard",
            severity=r["severity"],
            officialSeverity=r["official_severity"] if "official_severity" in r.keys() else "Official Advisory",
            status=r["status"],
            issuingAuthority=r["issuing_authority"],
            sourceType=r["source_type"],
            sourceUrl=r["source_url"],
            publishedTime=r["published_time"],
            lastUpdatedTime=r["last_updated_time"],
            validUntil=r["valid_until"] or "Expired / Archived",
            isVerifiedOfficial=bool(r["is_verified_official"]),
            isDemoData=bool(r["is_demo_data"]),
            isCorroboratedByMultipleSources=bool(r["is_corroborated_by_multiple_sources"]),
            unconfirmedReason=r["unconfirmed_reason"],
            safetyInstructions=safety_inst,
            version=r["version"] if "version" in r.keys() and r["version"] else 1,
            latitude=r["latitude"],
            longitude=r["longitude"],
            affectedArea=f"{r['district'] or 'Pan-India'}, {r['state'] or ''}".strip(", ")
        ))

    return result

@app.get("/api/alerts/{alert_id}/timeline", response_model=List[AlertUpdateOut])
def get_alert_timeline(alert_id: str):
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("""
    SELECT version, previous_content, new_content, update_reason, timestamp, source_reference
    FROM alert_updates
    WHERE alert_id = ?
    ORDER BY version ASC
    """, (alert_id,))

    rows = cursor.fetchall()
    conn.close()

    timeline = []
    for r in rows:
        timeline.append(AlertUpdateOut(
            version=r["version"],
            previousContent=r["previous_content"],
            newContent=r["new_content"],
            updateReason=r["update_reason"],
            timestamp=r["timestamp"],
            sourceReference=r["source_reference"]
        ))

    return timeline

@app.get("/api/alerts/locations")
def get_administrative_locations():
    return {
        "states_and_uts": INDIAN_STATES_UTS,
        "districts_sample": list(DISTRICT_CENTROIDS.keys())
    }

# ---------------------------------------------------------
# 5. Protected Admin Endpoints
# ---------------------------------------------------------
@app.get("/api/admin/sources/status", dependencies=[Depends(verify_firebase_admin_token)])
def get_admin_sources_status():
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("""
    SELECT source_name, MAX(attempted_time) as last_attempted, MAX(successful_time) as last_success,
           SUM(records_collected) as total_collected, SUM(failed_requests) as total_failed,
           last_error, status
    FROM source_sync_logs
    GROUP BY source_name
    """)
    rows = cursor.fetchall()
    conn.close()

    sources = []
    for r in rows:
        sources.append({
            "sourceName": r["source_name"],
            "lastAttempted": r["last_attempted"],
            "lastSuccess": r["last_success"] or "N/A",
            "totalCollected": r["total_collected"] or 0,
            "totalFailed": r["total_failed"] or 0,
            "lastError": r["last_error"],
            "status": r["status"]
        })

    return {"sources": sources}

@app.get("/api/admin/sos/events", dependencies=[Depends(verify_firebase_admin_token)])
def get_admin_sos_events():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM sos_events ORDER BY created_at DESC LIMIT 50")
    rows = cursor.fetchall()
    conn.close()

    events = []
    for r in rows:
        events.append({
            "id": r["id"],
            "timestamp": r["timestamp"],
            "latitude": r["latitude"],
            "longitude": r["longitude"],
            "locationName": r["location_name"],
            "emergencyProfileText": r["emergency_profile_text"],
            "status": r["status"]
        })
    return {"sosEvents": events}

@app.post("/api/admin/sources/{source_id}/sync", dependencies=[Depends(verify_firebase_admin_token)])
def trigger_manual_source_sync(source_id: str):
    run_source_sync_task()
    return {"message": f"Manual synchronization triggered across official adapters including {source_id}."}

@app.post("/api/admin/alerts/{alert_id}/withdraw", dependencies=[Depends(verify_firebase_admin_token)])
def withdraw_alert(alert_id: str, reason: str = "Withdrawn by issuing authority"):
    with db_lock:
        conn = get_db_connection()
        cursor = conn.cursor()
        now_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")

        cursor.execute("UPDATE alerts SET status = 'WITHDRAWN', unconfirmed_reason = ?, modified_at = ? WHERE id = ?", (f"WITHDRAWN: {reason}", now_utc, alert_id))
        conn.commit()
        conn.close()

    return {"message": f"Alert {alert_id} withdrawn successfully."}
