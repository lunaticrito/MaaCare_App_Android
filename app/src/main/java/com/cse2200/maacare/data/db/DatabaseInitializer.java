package com.cse2200.maacare.data.db;

import android.content.Context;
import com.cse2200.maacare.data.db.entity.BabyCareTimelineEntity;
import com.cse2200.maacare.data.db.entity.BabyEssentialItemEntity;
import com.cse2200.maacare.data.db.entity.BabyFoodLogEntity;
import com.cse2200.maacare.data.db.entity.BabyGrowthEntity;
import com.cse2200.maacare.data.db.entity.BabyMilestoneEntity;
import com.cse2200.maacare.data.db.entity.BabyVitalsEntity;
import com.cse2200.maacare.data.db.entity.DoctorEntity;
import com.cse2200.maacare.data.db.entity.FoodLogEntity;
import com.cse2200.maacare.data.db.entity.HydrationLogEntity;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.data.db.entity.UserEntity;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Pre-populates the database with default maternal profile ("Anna", Week 30),
 * full EPI/WHO immunization schedule (Weeks & Months), prescribed courses, and baseline nutrition.
 */
public class DatabaseInitializer {

    public static final String DEMO_EMAIL = "anna@maacare.org";
    public static final String DEMO_PASSWORD = "password123";

    public static void populateInitialData(Context context, MaaCareDatabase db) {
        long now = System.currentTimeMillis();

        // 1. Pre-populate Demo User "Anna" (Week 30 Pregnant -> LMP 210 days ago - 4 days = Week 30, Day 4)
        if (db.userDao().getUserByEmailOrPhone(DEMO_EMAIL) == null) {
            long lmpAnna = now - (206L * 86400000L); // 206 days pregnant = Week 30, Day 3
            long babyBirthAnna = now - (56L * 86400000L); // 8 weeks old baby baseline for infant care demos
            UserEntity anna = new UserEntity(
                    "Anna",
                    DEMO_EMAIL,
                    DEMO_PASSWORD,
                    lmpAnna,
                    babyBirthAnna,
                    "O+",
                    "+8801711223344"
            );
            long annaId = db.userDao().registerUser(anna);

            // Sync with PreferenceManager if first install
            PreferenceManager prefs = new PreferenceManager(context);
            if (!prefs.isLoggedIn()) {
                prefs.saveUserSession(annaId, "Anna", DEMO_EMAIL, lmpAnna, babyBirthAnna, "O+", "+8801711223344");
            }
        }

        // 2. Pre-populate WHO/EPI newborn vaccination schedule
        if (db.vaccinationDao().getAllVaccines().isEmpty()) {
            List<VaccinationEntity> vaccines = new ArrayList<>();

            // At Birth
            vaccines.add(new VaccinationEntity("BCG (Bacillus Calmette–Guérin)", "At Birth", 0, "Severe childhood Tuberculosis & Tuberculous Meningitis", true, "Administered at Birth"));
            vaccines.add(new VaccinationEntity("OPV-0 (Oral Polio Vaccine)", "At Birth", 0, "Immediate gut immunity against Poliovirus", true, "Administered at Birth"));
            vaccines.add(new VaccinationEntity("Hepatitis B (Birth Dose)", "At Birth", 0, "Prevents perinatal Hepatitis B transmission", true, "Administered at Birth"));

            // 6 Weeks
            vaccines.add(new VaccinationEntity("Pentavalent-1 (DTP-HepB-Hib)", "6 Weeks", 42, "Diphtheria, Pertussis, Tetanus, Hepatitis B, and Hib Pneumonia", true, "Administered at 6 Weeks"));
            vaccines.add(new VaccinationEntity("PCV-1 (Pneumococcal Conjugate)", "6 Weeks", 42, "Severe Pneumococcal pneumonia & Bacteremia", true, "Administered at 6 Weeks"));
            vaccines.add(new VaccinationEntity("OPV-1 + Rotavirus-1", "6 Weeks", 42, "Poliomyelitis & Rotavirus dehydration diarrhea", true, "Administered at 6 Weeks"));

            // 10 Weeks
            vaccines.add(new VaccinationEntity("Pentavalent-2 (Second Booster)", "10 Weeks", 70, "Second booster for DTP, HepB, and Hib", false, "Due at 10 Weeks"));
            vaccines.add(new VaccinationEntity("PCV-2 (Pneumococcal)", "10 Weeks", 70, "Second booster for invasive pneumococcal disease", false, "Due at 10 Weeks"));
            vaccines.add(new VaccinationEntity("OPV-2 + Rotavirus-2", "10 Weeks", 70, "Second dose of Oral Polio and Rotavirus drops", false, "Due at 10 Weeks"));

            // 14 Weeks
            vaccines.add(new VaccinationEntity("Pentavalent-3 + IPV-1 (Injectable Polio)", "14 Weeks", 98, "Third primary DTP series + Inactivated Poliovirus injection", false, "Due at 14 Weeks"));
            vaccines.add(new VaccinationEntity("PCV-3", "14 Weeks", 98, "Third primary Pneumococcal booster", false, "Due at 14 Weeks"));
            vaccines.add(new VaccinationEntity("OPV-3", "14 Weeks", 98, "Third primary Oral Polio dose", false, "Due at 14 Weeks"));

            // 9 Months
            vaccines.add(new VaccinationEntity("MR-1 (Measles & Rubella)", "9 Months", 270, "Protects against Measles infection and Congenital Rubella Syndrome", false, "Due at 9 Months"));
            vaccines.add(new VaccinationEntity("Vitamin A (100,000 IU)", "9 Months", 270, "Boosts mucosal immunity and prevents night blindness", false, "Due at 9 Months"));

            // 15 Months
            vaccines.add(new VaccinationEntity("MR-2 + DTP Booster", "15 Months", 450, "Lifelong booster against Measles, Rubella, and Whooping Cough", false, "Due at 15 Months"));

            db.vaccinationDao().insertAll(vaccines);
        }

        // 3. Pre-populate Medicines with Active and Completed Courses
        if (db.medicineDao().getAllMedicines().isEmpty()) {
            // Active 90-day pregnancy supplements (started 30 days ago, 60 days remaining)
            long activeStart = now - (30L * 86400000L);
            db.medicineDao().insert(new MedicineEntity("Iron & Folic Acid", "1 tablet (60mg elemental Fe)", "09:00 AM", activeStart, 90, false, true));
            db.medicineDao().insert(new MedicineEntity("Calcium Carbonate + Vit D3", "500 mg after lunch", "02:00 PM", activeStart, 90, false, true));

            // Completed 7-day course (started 15 days ago, ended 8 days ago)
            long finishedStart = now - (15L * 86400000L);
            db.medicineDao().insert(new MedicineEntity("Amoxicillin (Dental Infection)", "500 mg 3x daily", "08:00 AM", finishedStart, 7, true, false));
        }

        // 4. Pre-populate Hydration Logs for Today
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(now));
        if (db.hydrationDao().getDailyTotalVolumeMl(todayDate) == null || db.hydrationDao().getDailyTotalVolumeMl(todayDate) == 0) {
            db.hydrationDao().insert(new HydrationLogEntity(todayDate, 500, now - 14400000L));
            db.hydrationDao().insert(new HydrationLogEntity(todayDate, 750, now - 7200000L));
            db.hydrationDao().insert(new HydrationLogEntity(todayDate, 500, now - 3600000L));
            // Total = 1750 mL
        }

        // 5. Pre-populate Maternal Meals and Baby Food Logs
        if (db.foodLogDao().getAllFoodLogs().isEmpty()) {
            db.foodLogDao().insert(new FoodLogEntity("2 Boiled Eggs + 1 Glass Milk", 280, 18.0, now - 14400000L));
            db.foodLogDao().insert(new FoodLogEntity("Lentil Soup (Daal) + Brown Rice + Spinach", 450, 16.5, now - 7200000L));
        }

        if (db.babyFoodLogDao().getAllBabyFoodLogs().isEmpty()) {
            db.babyFoodLogDao().insert(new BabyFoodLogEntity(
                    "0-6 Months (EBF)",
                    "Exclusive Breastfeeding",
                    "On demand feeding (~8 to 10 feeds in 24 hours)",
                    "None - healthy latch",
                    now - 14400000L
            ));
            db.babyFoodLogDao().insert(new BabyFoodLogEntity(
                    "6-8 Months",
                    "Soft Rice & Moong Dal Khichuri",
                    "2 tablespoons warm puree with tiny drop of ghee",
                    "None - tolerated well",
                    now - 7200000L
            ));
        }

        // 6. Pre-populate Baby Daily Timeline Events
        if (db.babyCareDao().getAllEvents().isEmpty()) {
            db.babyCareDao().insert(new BabyCareTimelineEntity(
                    todayDate, "07:30 AM", now - 18000000L,
                    "FEEDING", "Morning Breastfeeding", "Left breast 15m, right breast 12m. Baby burped well.", "#E6F2FF"
            ));
            db.babyCareDao().insert(new BabyCareTimelineEntity(
                    todayDate, "08:15 AM", now - 15000000L,
                    "DIAPER", "Wet Diaper Change", "Wet diaper changed, no diaper rash observed.", "#E6FAEA"
            ));
            db.babyCareDao().insert(new BabyCareTimelineEntity(
                    todayDate, "09:00 AM", now - 12000000L,
                    "SLEEP", "Morning Nap", "Fell asleep in crib. Slept peacefully for 1 hour 45 minutes.", "#FFF1E0"
            ));
            db.babyCareDao().insert(new BabyCareTimelineEntity(
                    todayDate, "11:30 AM", now - 6000000L,
                    "BATH", "Warm Tub Bathing", "Gentle lukewarm bath with baby soap, followed by coconut oil massage.", "#FDECEF"
            ));
            db.babyCareDao().insert(new BabyCareTimelineEntity(
                    todayDate, "12:30 PM", now - 3600000L,
                    "FEEDING", "Afternoon Feed", "Bottle feed - 120ml expressed breastmilk.", "#E6F2FF"
            ));
        }

        // 7. Pre-populate Developmental Milestones
        if (db.babyMilestoneDao().getAllMilestones().isEmpty()) {
            db.babyMilestoneDao().insert(new BabyMilestoneEntity(
                    "Motor Skills", "Does baby hold head up steadily when supported upright?", true, "Good neck tone observed", now - 86400000L
            ));
            db.babyMilestoneDao().insert(new BabyMilestoneEntity(
                    "Motor Skills", "Pushes up on arms when lying on tummy (tummy time)?", true, "Lifts chest comfortably", now - 86400000L
            ));
            db.babyMilestoneDao().insert(new BabyMilestoneEntity(
                    "Social & Communication", "Smiles spontaneously in response to mother's voice and smile?", true, "Frequent social smiling", now - 86400000L
            ));
            db.babyMilestoneDao().insert(new BabyMilestoneEntity(
                    "Social & Communication", "Makes cooing sounds (gurgling, vocalizing vowel sounds)?", true, "Says 'ooo' and 'ahh'", now - 86400000L
            ));
            db.babyMilestoneDao().insert(new BabyMilestoneEntity(
                    "Cognitive & Vision", "Follows moving objects smoothly with eyes across midline?", true, "Tracks colorful toys", now - 86400000L
            ));
            db.babyMilestoneDao().insert(new BabyMilestoneEntity(
                    "Cognitive & Vision", "Brings hands to mouth and explores hands/fingers?", false, "Starting to grasp fingers", now - 86400000L
            ));
        }

        // 8. Pre-populate Baby Growth Metrics (Anthropometrics)
        if (db.babyGrowthDao().getAllGrowthRecords().isEmpty()) {
            db.babyGrowthDao().insert(new BabyGrowthEntity(0, 50.0, 3.2, now - (56L * 86400000L), true));
            db.babyGrowthDao().insert(new BabyGrowthEntity(1, 54.0, 4.3, now - (28L * 86400000L), false));
            db.babyGrowthDao().insert(new BabyGrowthEntity(2, 57.5, 5.4, now, false));
        }

        // 9. Pre-populate Baby Vitals
        if (db.babyVitalsDao().getAllVitals().isEmpty()) {
            db.babyVitalsDao().insert(new BabyVitalsEntity(
                    false, "Baby is energetic, active and feeding well.",
                    98.4, 98.6, "98.6°F - Normal",
                    124, 99, now - 7200000L
            ));
        }

        // 10. Pre-populate Hospital Bag & Recovery Essentials
        if (db.babyEssentialsDao().getAllItems().isEmpty()) {
            List<BabyEssentialItemEntity> items = new ArrayList<>();
            // Baby Essentials
            items.add(new BabyEssentialItemEntity("Baby Essentials", "Soft cotton newborn onesies & baby rompers (4-6 sets)", true));
            items.add(new BabyEssentialItemEntity("Baby Essentials", "Newborn swaddle blankets / muslin cloths", true));
            items.add(new BabyEssentialItemEntity("Baby Essentials", "Newborn gentle diapers (size 0/1) & fragrance-free wipes", true));
            items.add(new BabyEssentialItemEntity("Baby Essentials", "Baby hats, mittens, and warm socks", true));
            items.add(new BabyEssentialItemEntity("Baby Essentials", "Infant grooming kit: gentle brush, nail scissors", false));

            // Labor & Delivery Bag
            items.add(new BabyEssentialItemEntity("Labor & Delivery Bag", "Mother's NID, insurance card, medical records & prenatal folder", true));
            items.add(new BabyEssentialItemEntity("Labor & Delivery Bag", "Comfortable front-open nightgowns / nursing gowns (3 sets)", true));
            items.add(new BabyEssentialItemEntity("Labor & Delivery Bag", "Warm non-skid slippers and comfortable cotton socks", false));
            items.add(new BabyEssentialItemEntity("Labor & Delivery Bag", "Phone chargers (extra-long cable) and power bank", true));
            items.add(new BabyEssentialItemEntity("Labor & Delivery Bag", "Lip balm, hair ties, and gentle toiletries travel kit", false));

            // Postpartum Recovery Kit
            items.add(new BabyEssentialItemEntity("Postpartum Recovery Kit", "High-absorbency maternity sanitary pads / disposable underwear", true));
            items.add(new BabyEssentialItemEntity("Postpartum Recovery Kit", "Soothing peri-rinse bottle for perineal comfort", true));
            items.add(new BabyEssentialItemEntity("Postpartum Recovery Kit", "Nursing bras (comfortable, wire-free) & breast pads", true));
            items.add(new BabyEssentialItemEntity("Postpartum Recovery Kit", "Nipple soothing balm (pure medical-grade lanolin)", false));
            items.add(new BabyEssentialItemEntity("Postpartum Recovery Kit", "Postpartum belly support band / binder", false));

            db.babyEssentialsDao().insertAll(items);
        }

        // 11. Pre-populate Verified Doctors Directory (Bangladesh Specialist Hub)
        if (db.doctorDao().getAllDoctors().isEmpty()) {
            List<DoctorEntity> docs = new ArrayList<>();
            docs.add(new DoctorEntity(
                    "Prof. Dr. Rashida Begum", "Obstetrician & Gynecologist",
                    "Dhaka Medical College Hospital", 2.4, "Today, 05:00 PM", true, "+8801712345671", 4.9
            ));
            docs.add(new DoctorEntity(
                    "Dr. Farzana Huq", "Senior Consultant - Obstetrics & Fetal Medicine",
                    "Square Hospitals Ltd, Panthapath", 3.8, "Today, 06:30 PM", true, "+8801712345672", 4.8
            ));
            docs.add(new DoctorEntity(
                    "Dr. Shahedur Rahman", "Chief Consultant Pediatrician & Neonatologist",
                    "Evercare Hospital Dhaka, Bashundhara", 5.2, "Tomorrow, 10:00 AM", false, "+8801712345673", 4.9
            ));
            docs.add(new DoctorEntity(
                    "Prof. Dr. Nazneen Akhter", "Maternal-Fetal Medicine & High-Risk Pregnancy",
                    "BSMMU (PG Hospital), Shahbagh", 3.1, "Today, 04:00 PM", true, "+8801712345674", 4.7
            ));
            docs.add(new DoctorEntity(
                    "Dr. Kazi Tanvir Ahmed", "Pediatric Critical Care & Infant Health",
                    "United Hospital, Gulshan 2", 6.5, "Tomorrow, 11:30 AM", false, "+8801712345675", 4.8
            ));
            docs.add(new DoctorEntity(
                    "Dr. Syeda Nusrat Jahan", "Gynecology & Reproductive Endocrinologist",
                    "Labaid Specialized Hospital, Dhanmondi", 1.8, "Today, 07:00 PM", true, "+8801712345676", 4.8
            ));

            db.doctorDao().insertAll(docs);
        }
    }
}
