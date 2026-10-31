package com.cse2200.maacare.data.db;

import com.cse2200.maacare.data.db.entity.FoodLogEntity;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre-populates default national EPI vaccination schedule and baseline maternal prescriptions.
 */
public class DatabaseInitializer {

    public static void populateInitialData(MaaCareDatabase db) {
        // Prepopulate EPI Schedule if empty
        if (db.vaccinationDao().getAllVaccines().isEmpty()) {
            List<VaccinationEntity> vaccines = new ArrayList<>();
            vaccines.add(new VaccinationEntity("BCG & OPV-0", "At Birth", "Prevents severe childhood Tuberculosis & early Polio", true, "Administered"));
            vaccines.add(new VaccinationEntity("Pentavalent-1 + OPV-1 + PCV-1", "6 Weeks", "Protects against Diphtheria, Pertussis, Tetanus, HepB, Hib, Pneumonia", false, "Due at 6 weeks"));
            vaccines.add(new VaccinationEntity("Pentavalent-2 + OPV-2 + PCV-2", "10 Weeks", "Second booster for DPT, HepB, Hib, and Pneumococcal infection", false, "Due at 10 weeks"));
            vaccines.add(new VaccinationEntity("Pentavalent-3 + OPV-3 + PCV-3 + IPV-1", "14 Weeks", "Third booster + Inactivated Polio Vaccine injection", false, "Due at 14 weeks"));
            vaccines.add(new VaccinationEntity("MR-1 (Measles & Rubella)", "9 Months", "Protects against Measles rash and Congenital Rubella Syndrome", false, "Due at 9 months"));
            vaccines.add(new VaccinationEntity("MR-2 (Second Dose)", "15 Months", "Lifelong immunity against Measles and Rubella complications", false, "Due at 15 months"));
            db.vaccinationDao().insertAll(vaccines);
        }

        // Prepopulate baseline prescriptions if empty
        if (db.medicineDao().getAllMedicines().isEmpty()) {
            db.medicineDao().insert(new MedicineEntity("Iron & Folic Acid", "1 tablet (60mg elemental Fe)", "09:00 AM", false, true));
            db.medicineDao().insert(new MedicineEntity("Calcium Carbonate + Vit D3", "500 mg (Take 2 hrs after Iron)", "02:00 PM", false, true));
            db.medicineDao().insert(new MedicineEntity("Prenatal Multivitamin", "1 capsule with dinner", "08:30 PM", false, true));
        }

        // Prepopulate baseline maternal food logs if empty
        if (db.foodLogDao().getAllFoodLogs().isEmpty()) {
            long now = System.currentTimeMillis();
            db.foodLogDao().insert(new FoodLogEntity("2 Boiled Eggs + 1 Glass Cow Milk", 280, 18.0, now - 14400000));
            db.foodLogDao().insert(new FoodLogEntity("Lentil Soup (Daal) + Steamed Rice + Spinach", 450, 16.5, now - 7200000));
        }
    }
}
