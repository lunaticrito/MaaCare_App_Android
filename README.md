# MaaCare — Maternal & Newborn Health Companion (CSE 2200)

**MaaCare** is an Android healthcare application designed to support mothers and caregivers through pregnancy, childbirth, and postpartum newborn care.

---

## Key Features

1. **Week-by-Week Pregnancy Tracker:**
   - Gestational age progress tracking (Weeks 1 to 40).
   - Custom Circular Arc Gauge (`PregnancyProgressArcView`) rendering Trimester color zones (T1: Amber, T2: Teal, T3: Rose) and countdown to Estimated Due Date (EDD).
   - Weekly fetal development milestones and maternal symptom guidance.
2. **Prescribed Medicine & Supplement Reminders:**
   - Adherence schedule for Iron, Folic Acid, Calcium, and prenatal vitamins.
   - Interactive taken/missed checklist with Room database persistence.
   - Scheduled notifications via `AlarmManager` and `MedicationAlarmReceiver`.
3. **Newborn Vaccination & Maternal Nutrition:**
   - National Expanded Programme on Immunization (EPI) schedule (BCG, Polio, Pentavalent, PCV, MR).
   - Maternal daily food and nutrient intake logger (Calories, Protein).
4. **Emergency Health Guidance & Rapid SOS:**
   - Obstetric danger signs checklist and offline first-aid protocols.
   - One-tap Emergency Calling (`Intent.ACTION_DIAL`) to 999 or emergency contacts.
   - Automated maternal SMS alert pre-populated with gestational week and blood group.
   - 24/7 maternal hospital directory.
5. **Foreground Hydration Monitor Service:**
   - Persistent foreground service (`MaternalHealthMonitorService`) tracking daily fluid intake with quick "+250ml" notification actions.
6. **MaaCare Content Provider:**
   - Custom `ContentProvider` (`com.cse2200.maacare.provider`) providing standard CRUD access to medication and vaccination data.

---

## Technical Specifications & Toolchain

- **Target IDE:** Android Studio Dolphin | 2021.3.1 Beta 4
- **JDK:** OpenJDK 11 (`sourceCompatibility JavaVersion.VERSION_11`, `targetCompatibility JavaVersion.VERSION_11`)
- **Android Gradle Plugin (AGP):** `7.2.2`
- **Gradle:** `7.4`
- **Compile SDK:** `33` (Android 13)
- **Min SDK:** `24` (Android 7.0)
- **Target SDK:** `33`
- **Build Tools:** `33.0.2`
- **Architecture & Libraries:** AndroidX ViewBinding, Material Components, Room 2.5.0, Retrofit 2.9.0, Gson 2.10.1
