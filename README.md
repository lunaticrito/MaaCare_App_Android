# MaaCare — Maternal & Newborn Health Companion (CSE 2200)

**MaaCare** is a production-ready, bilingual Android healthcare application designed to support mothers and caregivers through pregnancy, childbirth, and postpartum newborn care—featuring real-time Firebase synchronization, full offline SQLite/Room persistence, and a nationwide Bangladesh specialist directory.

---

## Key Modules & Architectural Highlights

### 1. UI/UX, Custom Branding, Typography & Bilingual Engine (`EN | বাংলা`)
* **Custom Maternal & Newborn App Icon:**
  - Bundled adaptive and legacy launcher icons (`ic_launcher`, `ic_launcher_round`) across `mdpi`–`xxxhdpi` mipmaps and `res/drawable/ic_app_logo.png` integrated across the splash/auth screens (`activity_login.xml`, `activity_register.xml`).
* **Global Poppins Typography:**
  - Bundled `res/font/poppins.xml` (`poppins_regular.ttf`, `poppins_medium.ttf`, `poppins_semibold.ttf`, `poppins_bold.ttf`) applied globally across `res/values/themes.xml` and `res/values-night/themes.xml`.
* **Light & Deep Magenta Dark Mode (`#88026D`):**
  - Top-bar instant Theme Toggle (`☀️ / 🌙`) backed by `AppCompatDelegate` and `SharedPreferences`.
  - Custom Dark Mode palette featuring `#88026D` (Deep Magenta/Plum) root backgrounds, `#63014F` / `#9E1480` elevated surfaces, and high-contrast `#FFFFFF` / `#FFD6F5` typography and interactive selectors.
* **100% Complete English & Bangla Localization:**
  - Top-bar Language Toggle (`EN | বাংলা`) powered by `LocaleHelper` with complete `res/values/strings.xml` and `res/values-bn/strings.xml` resources.
  - Full dynamic Bangla rendering across weekly pregnancy milestone cards (`Week 4`–`Week 40`), `PregnancyProgressArcView`, medicine durations, Baby Suite tabs/charts/cards, XML emergency danger-sign protocols, and the 64-district doctor directory (including Bangla digits and localized time/division/district labels).
* **Responsive Layout Support:** Includes landscape-adapted layouts (`layout-land/fragment_home_tracker.xml`) alongside portrait layouts.

### 2. Authentication & Deterministic Gestational Timeline Engine
* **User Authentication (`LoginActivity`, `RegisterActivity` & Firebase Auth):**
  - Secure credential registration and login backed by Room (`UserEntity`) and Firebase Authentication via `FirebaseManager`.
  - **Quick Demo Login:** One-tap login for demo profile ("Anna", Week 30 Pregnant, Blood Group O+).
* **Deterministic Clock-Driven Calculation (Non-Editable):**
  - Computes gestational age deterministically from the Last Menstrual Period (LMP) date:
    $$\text{Days Pregnant} = \frac{\text{System.currentTimeMillis}() - \text{lmpDateMillis}}{86400000}$$
    $$\text{Current Week} = \lfloor \frac{\text{Days Pregnant}}{7} \rfloor + 1, \quad \text{Day of Week} = \text{Days Pregnant} \pmod 7$$
    $$\text{Remaining Days to EDD} = \max(0, 280 - \text{Days Pregnant})$$
  - Custom view (`PregnancyProgressArcView`) dynamically renders the 250° arc gauge reflecting real-time weeks, trimester color zones, and EDD countdown in both English and Bangla.
  - Weekly fetal development cards (`FetalMilestoneAdapter`) loaded from `assets/maternal_health_data.json` with bilingual badges, mom symptoms, and clinical advice.

### 3. Prescribed Medicines with Course Duration Tracking
* **Course Duration Selection:**
  - Choose durations: 3 Days, 1 Week (7 Days), 2 Weeks (14 Days), 1 Month (30 Days), 3 Months (90 Days), Throughout Pregnancy, or Custom Days.
  - Stored with `start_date_millis`, `duration_days`, and `end_date_millis`.
* **Real-Time Adherence & Auto-Cancellation:**
  - Each medicine card displays dynamic localized progress badges (e.g., *"Day 5 of 14 • 9 days left"* or *"COURSE COMPLETED"*).
  - `MedicationAlarmReceiver` inspects `endDateMillis`—once expired, the repeating `AlarmManager` PendingIntent is automatically cancelled so alarms stop upon finishing the course.

### 4. Daily Hydration Monitor & Maternal Photo Memories
* **Interactive Fluid Intake Controller:**
  - Real-time intake vs. daily goal (e.g., `1750 mL / 3000 mL (58%)`) with responsive progress bar, quick adjustments (`+250 mL`, `+500 mL`, `-250 mL`), and custom target dialog.
* **Persistent Foreground Service:**
  - `MaternalHealthMonitorService` synchronizes with Room (`HydrationLogEntity`) and posts an ongoing notification with quick intake actions.
* **Bump / Newborn Photo Memories:**
  - Consumes `MediaStore` via device gallery selection and uploads/syncs photo memories to Firebase Storage (`FirebaseManager`).

### 5. Interactive Baby & Newborn Care Suite (Full CRUD)
* **Date-Filtered & Editable Baby Care Timeline (`Yesterday` | `Today` | `Tomorrow`):**
  - Real date-based filtering across `BabyCareTimelineEntity` entries with Quick Log target-date selection (`Yesterday`, `Today`, `Tomorrow`).
  - Full per-card Edit (`TimePickerDialog`, category chips, title, notes) and Delete with **Snackbar Undo**, synced across Room and Cloud Firestore.
* **Interactive Baby Growth Chart (`0m–12m`):**
  - Custom `BabyGrowthChartView` with tappable `At Birth (Day 0)` and `Current` summary cards plus `+ Add / Edit Weight & Height` dialog.
  - Supports upsert and deletion of non-birth monthly measurements (`BabyGrowthDao`) with live chart redrawing and Room/Firestore/Realtime DB sync.
* **Health & Vitals Logs (`BabyVitalsDao` & `BabyVitalsAdapter`):**
  - Save vitals records (`btn_save_vitals`) with reverse-chronological history list (`rv_vitals_history`), inline Edit/Delete (+ Snackbar Undo), and live Firebase Realtime Database + Firestore sync.
* **WHO & National EPI Immunization Schedule, Milestones & Feeding Guide:**
  - Structured milestones (`At Birth`, `6 Weeks`, `10 Weeks`, `14 Weeks`, `9 Months`, `15 Months`) with dynamic localized status badges (**COMPLETED**, **DUE NOW**, **UPCOMING**), baby developmental milestones (`BabyMilestoneAdapter`), essentials checklist (`EssentialsAdapter`), and Baby Food & Allergy Log.

### 6. 64-District Bangladesh Doctor Directory & XML Emergency Protocols
* **Complete Nationwide Specialist Directory (129 Doctors across 8 Divisions & 64 Districts):**
  - Bundled `assets/doctors_bd.json` seeded into Room (`DoctorEntity`, `DoctorDao`, Room `version = 5`).
  - Interactive filtering by **8 Divisions**, dynamic **64 Districts**, **Specialty** (`All Specialists`, `Obs & Gynae`, `Pediatrics`), and live text search with live specialist counter.
  - One-tap bilingual emergency dialing (`Intent.ACTION_DIAL`) and **Set as Emergency Contact** assignment, plus **Import Caregiver from Contacts** via `ContactsContract`.
* **XML Emergency Health Protocols (`assets/emergency_health_protocols.xml`):**
  - Parsed natively with `XmlPullParser` in `MaaCareRepository.getEmergencyProtocolsFromXml()`, rendering bilingual danger signs, severity badges (`অতি জরুরি` / `CRITICAL`, `উচ্চ ঝুঁকি` / `HIGH RISK`), and recommended immediate actions.

### 7. Firebase Suite & Core Android System Components (CSE 2200 Rubric)
* **Firebase Suite (BoM `31.5.0`):**
  - Integrated `firebase-auth`, `firebase-database` (Realtime Database for live baby vitals), `firebase-firestore` (Cloud Firestore for care logs & growth sync), and `firebase-storage` (Media/Picture storage) inside `FirebaseManager` with safe initialization and automatic Room/SharedPreferences offline fallback.
* **Room Database (`version = 5`, 12 Entities):**
  - Configured with `.fallbackToDestructiveMigration()` for seamless schema upgrades and automatic re-seeding of bilingual protocols and the 64-district doctor directory.
* **Content Providers:**
  - Exposes `/medicines`, `/vaccinations`, and `/hydration` via `MaaCareContentProvider`, and consumes device `ContactsContract` & `MediaStore`.
* **Broadcast Receivers:**
  - Static receivers (`MedicationAlarmReceiver`, `BootCompletedReceiver`) and dynamic `SystemBroadcastReceiver` in `MainActivity` handling `Intent.ACTION_AIRPLANE_MODE_CHANGED` (offline emergency vs. cloud-sync mode banner) and `Intent.ACTION_BATTERY_LOW` (one-tap caregiver status SMS alert).

---

## Toolchain & Runtime Compatibility

* **Target IDE:** Android Studio Dolphin | 2021.3.1
* **JDK:** OpenJDK 11 (`sourceCompatibility JavaVersion.VERSION_11`, `targetCompatibility JavaVersion.VERSION_11`)
* **Android Gradle Plugin (AGP):** `7.2.2`
* **Gradle Wrapper:** `7.4`
* **Compile SDK:** `33` (Android 13)
* **Min SDK:** `24` (Android 7.0)
* **Target SDK:** `33`
* **Build Tools:** `33.0.2`
* **Key Dependencies:** Firebase BoM `31.5.0` (`auth`, `database`, `firestore`, `storage`), Room `2.5.0`, AndroidX ViewBinding, Material Components `1.8.0`, Retrofit `2.9.0`, Gson `2.10.1`
