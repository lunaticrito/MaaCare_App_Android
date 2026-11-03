# MaaCare — Maternal & Newborn Health Companion (CSE 2200)

**MaaCare** is a production-ready Android healthcare application designed to support mothers and caregivers through pregnancy, childbirth, and postpartum newborn care.

---

## Key Modules & Architectural Highlights

### 1. Authentication & Deterministic Gestational Timeline Engine
* **User Authentication (`LoginActivity` & `RegisterActivity`):**
  - Secure credential registration and login backed by Room (`UserEntity`).
  - **Quick Demo Login:** One-tap login for "Anna" (Week 30 Pregnant, Blood Group O+).
* **Deterministic Clock-Driven Calculation (Non-Editable):**
  - Manual week editing has been eliminated. The system computes gestational age deterministically from the Last Menstrual Period (LMP) date:
    $$\text{Days Pregnant} = \frac{\text{System.currentTimeMillis}() - \text{lmpDateMillis}}{86400000}$$
    $$\text{Current Week} = \lfloor \frac{\text{Days Pregnant}}{7} \rfloor + 1, \quad \text{Day of Week} = \text{Days Pregnant} \pmod 7$$
    $$\text{Remaining Days to EDD} = \max(0, 280 - \text{Days Pregnant})$$
  - The custom view (`PregnancyProgressArcView`) dynamically renders the 250° arc gauge reflecting real-time weeks, trimester color zones, and EDD countdown.

### 2. Prescribed Medicines with Course Duration Tracking
* **Course Duration Selection:**
  - Choose durations: 3 Days, 1 Week (7 Days), 2 Weeks (14 Days), 1 Month (30 Days), 3 Months (90 Days), Throughout Pregnancy, or Custom Days.
  - Stored with `start_date_millis`, `duration_days`, and `end_date_millis`.
* **Real-Time Adherence & Auto-Cancellation:**
  - Each medicine card displays dynamic progress badges (e.g., *"Day 5 of 14 • 9 days left"* or *"COURSE COMPLETED"*).
  - `MedicationAlarmReceiver` inspects `endDateMillis`—once expired, the repeating `AlarmManager` PendingIntent is automatically cancelled so alarms stop upon finishing the course.

### 3. Fully Editable Daily Hydration & Maternal Monitor
* **Interactive Fluid Intake Controller:**
  - Real-time intake vs. daily goal (e.g., `1750 mL / 3000 mL (58%)`) with a responsive progress bar.
  - Quick adjustment buttons: `+250 mL`, `+500 mL`, and `-250 mL`.
  - Manual Volume & Goal Dialog: Allows typing any custom volume or adjusting the daily target.
* **Persistent Foreground Service:**
  - `MaternalHealthMonitorService` synchronizes with Room (`HydrationLogEntity`) and posts an ongoing notification with quick intake actions.

### 4. Comprehensive Baby & Newborn Care Module (Weeks & Months)
* **Chronological Child Age Tracker:**
  - Tracks infant age from birth date in precise weeks (Weeks 1–12) and months (Months 3–24).
* **WHO & National EPI Immunization Schedule:**
  - Structured milestones: At Birth, 6 Weeks, 10 Weeks, 14 Weeks, 9 Months, 15 Months.
  - Dynamic status badges: **COMPLETED** (Green), **DUE NOW** (Alert Red/Amber), and **UPCOMING** (Teal).
* **Age-Appropriate Feeding & Nutrition Guide:**
  - Structured guidance across:
    - 0–6 Months (Weeks 1–24): Exclusive Breastfeeding (EBF) guidelines and feeding frequency cues.
    - 6–8 Months: Complementary foods (Soft khichuri, pureed vegetables, egg yolk).
    - 9–11 Months: Chopped finger foods and iron-rich protein meals.
    - 12–24 Months: Balanced family diet and continued lactation.
* **Baby Food & Allergy Log:**
  - Track infant meals, textures, and observed allergy or digestive reactions.

### 5. Advanced System Components (CSE 2200 Syllabus)
* **Content Provider (`MaaCareContentProvider`):** Exposes `/medicines` (including course dates), `/vaccinations`, and `/hydration` for companion apps.
* **Broadcast Receivers:** `MedicationAlarmReceiver`, `BootCompletedReceiver`, and dynamic `NetworkStateChangeReceiver`.
* **Offline Fallback Repository:** Gracefully falls back to bundled `assets/maternal_health_data.json` for danger signs, advisories, and hospital directories when internet is offline.

---

## Toolchain & Runtime Compatibility

* **Target IDE:** Android Studio Dolphin | 2021.3.1 Beta 4
* **JDK:** OpenJDK 11 (`sourceCompatibility JavaVersion.VERSION_11`, `targetCompatibility JavaVersion.VERSION_11`)
* **Android Gradle Plugin (AGP):** `7.2.2` / `7.3.1`
* **Gradle Wrapper:** `7.4`
* **Compile SDK:** `33` (Android 13)
* **Min SDK:** `24` (Android 7.0)
* **Target SDK:** `33`
* **Build Tools:** `33.0.2`
* **Libraries:** AndroidX ViewBinding, Material Components 1.8.0, Room 2.5.0, Retrofit 2.9.0, Gson 2.10.1
