# Disaster Responder 🚨

**Disaster Responder** is a functional academic prototype Android application designed for disaster management, resource mapping, and incident reporting in India. 

It provides real-time geographic insights, offline-capable database persistence, and conversational AI safety guidance for public users, emergency responders, and administrators.

---

## 📖 Problem Statement and Objectives
During natural disasters (floods, earthquakes, fires), citizens struggle to find verified nearby resources (shelters, hospitals, food distribution) and accurately report hazards. First responders lack a unified, verified incident feed, and generic emergency lines get clogged with non-critical queries. 

**Objectives:**
1. Provide an intuitive, multilingual interface to report incidents with precise GPS locations.
2. Present a centralized interactive map (OpenStreetMap + MapTiler Satellite) showing verified resources and active alerts.
3. Establish a Role-Based Access Control (RBAC) workflow (User $\rightarrow$ Admin Review $\rightarrow$ Responder Action).
4. Implement an AI Assistant to quickly classify incident texts, provide first-aid guidance, and summarize risks in multiple languages (Hindi, Hinglish, English).

---

## 🛠️ Technologies and Architecture

### 1. Android Client (Kotlin + Jetpack Compose)
- **UI Framework**: **Jetpack Compose** (Material 3). Used for its modern, declarative, and state-driven UI paradigm which simplifies dynamic list rendering and theming.
- **Architecture**: **MVVM (Model-View-ViewModel)**. Ensures a clean separation between UI components and business logic.
  - **View**: Jetpack Compose screens observe UI state from ViewModels.
  - **ViewModel**: Uses Kotlin Coroutines and `StateFlow` to manage asynchronous data processing (e.g., location fetching, AI network calls).
  - **Repository**: Single source of truth. Handles data fetching from Room, Retrofit, or Location Services.
- **Mapping**: **osmdroid (OpenStreetMap) + MapTiler Aerial Satellite**. Integrates policy-compliant Humanitarian OpenStreetMap (HOT), OpenTopoMap, and MapTiler Satellite v2 / Hybrid XYZ raster tile layers.
- **Local Persistence**: **Room Database (SQLite)**. Ensures incident reports are safely stored on the device across app restarts.
- **Device Sensors**: Uses `FusedLocationProviderClient` for exact GPS mapping, Android `SpeechRecognizer` for voice-to-text, and Activity Result APIs for file/image picking.

### 2. Python AI Backend (FastAPI + Scikit-Learn)
- **Framework**: **FastAPI** (Python). Chosen for its speed, automatic Swagger UI documentation, and modern async support.
- **Machine Learning**: **Scikit-Learn (TF-IDF + Multinomial Naive Bayes)**. A lightweight NLP classification pipeline trained on synthetic disaster reports to classify raw text into categories (Flood, Fire, Earthquake, Cyclone).
- **Network**: The Android app uses **Retrofit** and **Gson** to send HTTP requests to the local Python backend over local loopback (`http://10.0.2.2:8000/`).

---

## 🗺️ MapTiler Satellite & Hybrid Layer Configuration

To enable live **MapTiler Aerial Satellite v2** and **Hybrid (Satellite + Labels)** tile layers:
1. Obtain a free key from [MapTiler Cloud](https://www.maptiler.com/cloud/).
2. Add your key to `local.properties` (this file is excluded from Git):
   ```properties
   MAPTILER_API_KEY=your_maptiler_key_here
   ```
3. Rebuild the Android project. `app/build.gradle.kts` injects `BuildConfig.MAPTILER_API_KEY` securely into the build without hardcoding keys into Kotlin source code or XML resources.

---

## 🗄️ Database Tables and Relationships
The local SQLite database (managed by Room) contains the following core entity:

*   **`incidents` Table (`IncidentEntity`)**:
    *   `id` (String, Primary Key)
    *   `reporterId`, `reporterName` (String)
    *   `categoryName` (Enum/String - FLOOD, FIRE, etc.)
    *   `description` (String)
    *   `locationName` (String)
    *   `latitude`, `longitude` (Double - from GPS)
    *   `statusName` (Enum/String - SUBMITTED, VERIFIED, RESOLVED)
    *   `auditTrailJson` (Stringified JSON array of status history)
    *   `syncStateName` (Enum/String - SAVED_LOCALLY, PENDING_SYNC, FAILED)

---

## 🔄 Incident Verification Workflow

The system employs a strict state machine to prevent fake reports from clogging emergency maps:

1.  **Public User** submits an incident via the Report screen. (Status: `SUBMITTED`, `isVerified: false`).
2.  **Administrator** views the dashboard, investigates, and taps "Verify". (Status updates to `VERIFIED`, `isVerified: true`).
3.  **Administrator** assigns a rescue team and taps "Publish Alert". This generates a public `AlertItem` on the interactive map.
4.  **Responder (NDRF)** views their assigned dashboard, travels to the location, and updates the status to `IN_PROGRESS` or `RESOLVED`.
5.  All state changes append a timestamped note to the **Audit Trail**.

---

## 🤖 Rule-Based Guidance vs. ML Classification

The AI Assistant uses a hybrid approach for maximum safety and reliability:

1.  **Rule-Based First-Aid (High Precision)**: If the user types critical medical keywords ("injury", "bleeding", "चोट", "khoon"), the system *bypasses* the ML model and instantly returns medically safe, rule-based first-aid protocols alongside the 112 emergency number.
2.  **ML Classification (Pattern Recognition)**: If the user provides a vague situation report ("Trees fell down blocking the hospital road"), the Android app passes the text to the Python ML model. The model calculates feature probabilities and returns a classification (e.g., `CYCLONE`) with a confidence score and action list.

---

## 🚀 Build and Run Instructions

### 1. Start the Python AI Backend
1. Ensure Python 3.10+ is installed.
2. Open a terminal in the `backend/` directory.
3. Install dependencies: `pip install fastapi uvicorn scikit-learn pandas pydantic`
4. Run the server: `uvicorn ai_backend:app --reload --host 0.0.0.0 --port 8000`
5. The API is now listening for the Android emulator at `10.0.2.2:8000`.

### 2. Run the Android Application
1. Open the project folder in **Android Studio (Ladybug or newer)**.
2. Wait for Gradle Sync to complete. (Ensure you have an active internet connection to download `osmdroid` and `Retrofit`).
3. Select an Android Emulator (API 24+) from the Device Manager.
4. Click the green **Run 'app'** button (`Shift + F10`).
5. *Note: Ensure your emulator has location services enabled in its extended settings to test the GPS reporting functionality.*
