package com.cse2200.maacare.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Manages runtime locale changes (English and Bangla) across activities,
 * plus Bangla digit conversion and dynamic translation helpers for seeded models.
 */
public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_BANGLA = "bn";

    private static final Map<String, String> DIVISION_BN = new HashMap<>();
    private static final Map<String, String> DISTRICT_BN = new HashMap<>();
    private static final Map<String, String> DYNAMIC_TEXT_BN = new HashMap<>();

    static {
        // 8 Divisions
        DIVISION_BN.put("Dhaka", "ঢাকা");
        DIVISION_BN.put("Chattogram", "চট্টগ্রাম");
        DIVISION_BN.put("Khulna", "খুলনা");
        DIVISION_BN.put("Rajshahi", "রাজশাহী");
        DIVISION_BN.put("Sylhet", "সিলেট");
        DIVISION_BN.put("Barishal", "বরিশাল");
        DIVISION_BN.put("Rangpur", "রংপুর");
        DIVISION_BN.put("Mymensingh", "ময়মনসিংহ");

        // All 64 Districts
        DISTRICT_BN.put("Dhaka", "ঢাকা");
        DISTRICT_BN.put("Gazipur", "গাজীপুর");
        DISTRICT_BN.put("Narayanganj", "নারায়ণগঞ্জ");
        DISTRICT_BN.put("Tangail", "টাঙ্গাইল");
        DISTRICT_BN.put("Faridpur", "ফরিদপুর");
        DISTRICT_BN.put("Kishoreganj", "কিশোরগঞ্জ");
        DISTRICT_BN.put("Manikganj", "মানিকগঞ্জ");
        DISTRICT_BN.put("Munshiganj", "মুন্সীগঞ্জ");
        DISTRICT_BN.put("Narsingdi", "নরসিংদী");
        DISTRICT_BN.put("Gopalganj", "গোপালগঞ্জ");
        DISTRICT_BN.put("Madaripur", "মাদারীপুর");
        DISTRICT_BN.put("Rajbari", "রাজবাড়ী");
        DISTRICT_BN.put("Shariatpur", "শরীয়তপুর");

        DISTRICT_BN.put("Chattogram", "চট্টগ্রাম");
        DISTRICT_BN.put("Cumilla", "কুমিল্লা");
        DISTRICT_BN.put("Cox's Bazar", "কক্সবাজার");
        DISTRICT_BN.put("Noakhali", "নোয়াখালী");
        DISTRICT_BN.put("Feni", "ফেনী");
        DISTRICT_BN.put("Brahmanbaria", "ব্রাহ্মণবাড়িয়া");
        DISTRICT_BN.put("Chandpur", "চাঁদপুর");
        DISTRICT_BN.put("Lakshmipur", "লক্ষ্মীপুর");
        DISTRICT_BN.put("Bandarban", "বান্দরবান");
        DISTRICT_BN.put("Rangamati", "রাঙ্গামাটি");
        DISTRICT_BN.put("Khagrachhari", "খাগড়াছড়ি");

        DISTRICT_BN.put("Khulna", "খুলনা");
        DISTRICT_BN.put("Jashore", "যশোর");
        DISTRICT_BN.put("Kushtia", "কুষ্টিয়া");
        DISTRICT_BN.put("Satkhira", "সাতক্ষীরা");
        DISTRICT_BN.put("Jhenaidah", "ঝিনাইদহ");
        DISTRICT_BN.put("Chuadanga", "চুয়াডাঙ্গা");
        DISTRICT_BN.put("Bagerhat", "বাগেরহাট");
        DISTRICT_BN.put("Magura", "মাগুরা");
        DISTRICT_BN.put("Meherpur", "মেহেরপুর");
        DISTRICT_BN.put("Narail", "নড়াইল");

        DISTRICT_BN.put("Rajshahi", "রাজশাহী");
        DISTRICT_BN.put("Bogura", "বগুড়া");
        DISTRICT_BN.put("Pabna", "পাবনা");
        DISTRICT_BN.put("Sirajganj", "সিরাজগঞ্জ");
        DISTRICT_BN.put("Naogaon", "নওগাঁ");
        DISTRICT_BN.put("Natore", "নাটোর");
        DISTRICT_BN.put("Chapai Nawabganj", "চাঁপাইনবাবগঞ্জ");
        DISTRICT_BN.put("Joypurhat", "জয়পুরহাট");

        DISTRICT_BN.put("Sylhet", "সিলেট");
        DISTRICT_BN.put("Moulvibazar", "মৌলভীবাজার");
        DISTRICT_BN.put("Habiganj", "হবিগঞ্জ");
        DISTRICT_BN.put("Sunamganj", "সুনামগঞ্জ");

        DISTRICT_BN.put("Barishal", "বরিশাল");
        DISTRICT_BN.put("Patuakhali", "পটুয়াখালী");
        DISTRICT_BN.put("Bhola", "ভোলা");
        DISTRICT_BN.put("Pirojpur", "পিরোজপুর");
        DISTRICT_BN.put("Barguna", "বরগুনা");
        DISTRICT_BN.put("Jhalokathi", "ঝালকাঠি");

        DISTRICT_BN.put("Rangpur", "রংপুর");
        DISTRICT_BN.put("Dinajpur", "দিনাজপুর");
        DISTRICT_BN.put("Kurigram", "কুড়িগ্রাম");
        DISTRICT_BN.put("Gaibandha", "গাইবান্ধা");
        DISTRICT_BN.put("Thakurgaon", "ঠাকুরগাঁও");
        DISTRICT_BN.put("Nilphamari", "নীলফামারী");
        DISTRICT_BN.put("Lalmonirhat", "লালমনিরহাট");
        DISTRICT_BN.put("Panchagarh", "পঞ্চগড়");

        DISTRICT_BN.put("Mymensingh", "ময়মনসিংহ");
        DISTRICT_BN.put("Jamalpur", "জামালপুর");
        DISTRICT_BN.put("Netrokona", "নেত্রকোণা");
        DISTRICT_BN.put("Sherpur", "শেরপুর");

        // Timeline Seeded Titles & Details
        DYNAMIC_TEXT_BN.put("Afternoon Feed", "দুপুরের খাবার");
        DYNAMIC_TEXT_BN.put("Bottle feed - 120ml expressed breastmilk.", "বোতল ফিডিং - ১২০ মি.লি. মায়ের দুধ।");
        DYNAMIC_TEXT_BN.put("Warm Tub Bathing", "হালকা গরম পানিতে গোসল");
        DYNAMIC_TEXT_BN.put("Gentle lukewarm bath with baby soap, followed by coconut oil massage.", "বেবি সাবান দিয়ে কুসুম গরম পানিতে গোসল এবং নারিকেল তেল মালিশ।");
        DYNAMIC_TEXT_BN.put("Morning Nap", "সকালের ঘুম");
        DYNAMIC_TEXT_BN.put("Fell asleep in crib. Slept peacefully for 1 hour 45 minutes.", "দোলনায় শান্তিতে ১ ঘণ্টা ৪৫ মিনিট ঘুমিয়েছে।");
        DYNAMIC_TEXT_BN.put("Wet Diaper Change", "ভেজা ডায়াপার পরিবর্তন");
        DYNAMIC_TEXT_BN.put("Wet diaper changed, no diaper rash observed.", "ভেজা ডায়াপার পরিবর্তন করা হয়েছে, কোনো র্যাশ দেখা যায়নি।");
        DYNAMIC_TEXT_BN.put("Morning Breastfeeding", "সকালের বুকের দুধ খাওয়ানো");
        DYNAMIC_TEXT_BN.put("Left breast 15m, right breast 12m. Baby burped well.", "বাম পাশে ১৫ মি., ডান পাশে ১২ মি. খাওয়ানো হয়েছে ও ঢেকুর তুলেছে।");

        DYNAMIC_TEXT_BN.put("Early Morning Breastfeeding", "ভোরের বুকের দুধ খাওয়ানো");
        DYNAMIC_TEXT_BN.put("12 mins left side, calm feeding session.", "বাম পাশে ১২ মিনিট শান্তিতে দুধ পান করেছে।");
        DYNAMIC_TEXT_BN.put("Morning Diaper & Outfit Change", "সকালের ডায়াপার ও পোশাক পরিবর্তন");
        DYNAMIC_TEXT_BN.put("Fresh cotton onesie after clean diaper change.", "পরিষ্কার ডায়াপার পরিয়ে সুতির আরামদায়ক পোশাক পরানো হয়েছে।");
        DYNAMIC_TEXT_BN.put("Afternoon Deep Nap", "দুপুরের গভীর ঘুম");
        DYNAMIC_TEXT_BN.put("Slept soundly for 2 hours after afternoon feed.", "দুপুরের খাবারের পর ২ ঘণ্টা নির্বিঘ্নে ঘুমিয়েছে।");
        DYNAMIC_TEXT_BN.put("Evening Sponge Bath", "সন্ধ্যার স্পঞ্জ গোসল");
        DYNAMIC_TEXT_BN.put("Gentle sponge cleanse and moisturizer before dusk.", "সন্ধ্যার আগে কুসুম গরম পানিতে গা মুছে ময়েশ্চারাইজার দেওয়া হয়েছে।");

        DYNAMIC_TEXT_BN.put("Scheduled Morning Feed & Vitamin D", "নির্ধারিত সকালের খাবার ও ভিটামিন ডি");
        DYNAMIC_TEXT_BN.put("Give prescribed 400 IU Vitamin D drops with morning feed.", "সকালের খাবারের সাথে নির্ধারিত ৪০০ আইইউ ভিটামিন ডি ড্রপস দিন।");
        DYNAMIC_TEXT_BN.put("Pre-Checkup Morning Bath", "চেকআপ পূর্ববর্তী সকালের গোসল");
        DYNAMIC_TEXT_BN.put("Warm bath before pediatric well-baby weight follow-up.", "শিশু বিশেষজ্ঞের ওজন পরীক্ষার আগে কুসুম গরম পানিতে গোসল।");
        DYNAMIC_TEXT_BN.put("Planned Quiet Nap Window", "নির্ধারিত নিরিবিলি ঘুমের সময়");
        DYNAMIC_TEXT_BN.put("Dim nursery lights & swaddle for afternoon rest.", "ঘরের আলো কমিয়ে বিকেলের বিশ্রামের জন্য সোয়াডল করে শোন।");

        // Quick Log Default Titles & Details
        DYNAMIC_TEXT_BN.put("Baby Feeding Recorded", "শিশুর খাবার রেকর্ড করা হয়েছে");
        DYNAMIC_TEXT_BN.put("Logged via 1-tap quick action", "১-ট্যাপ কুইক অ্যাকশনের মাধ্যমে সংরক্ষিত");
        DYNAMIC_TEXT_BN.put("Diaper Change Recorded", "ডায়াপার পরিবর্তন রেকর্ড করা হয়েছে");
        DYNAMIC_TEXT_BN.put("Clean diaper logged via quick action", "কুইক অ্যাকশনে পরিষ্কার ডায়াপার সংরক্ষিত");
        DYNAMIC_TEXT_BN.put("Baby Sleep / Nap Recorded", "শিশুর ঘুম রেকর্ড করা হয়েছে");
        DYNAMIC_TEXT_BN.put("Peaceful sleep interval logged", "শান্তিপূর্ণ ঘুমের সময়কাল সংরক্ষিত");
        DYNAMIC_TEXT_BN.put("Tub Bathing Recorded", "গোসল রেকর্ড করা হয়েছে");
        DYNAMIC_TEXT_BN.put("Warm bath and hygiene logged", "উষ্ণ গোসল ও পরিচ্ছন্নতা সংরক্ষিত");

        // Milestones
        DYNAMIC_TEXT_BN.put("Motor Skills", "শারীরিক ও মোটর দক্ষতা");
        DYNAMIC_TEXT_BN.put("Social & Communication", "সামাজিক ও যোগাযোগ দক্ষতা");
        DYNAMIC_TEXT_BN.put("Cognitive & Vision", "মানসিক ও দৃষ্টিশক্তি বিকাশ");
        DYNAMIC_TEXT_BN.put("Does baby hold head up steadily when supported upright?", "শিশু সোজা করে ধরলে কি স্থিরভাবে মাথা তুলে রাখতে পারে?");
        DYNAMIC_TEXT_BN.put("Pushes up on arms when lying on tummy (tummy time)?", "উপুড় হয়ে শুয়ে থাকার সময় হাতের ওপর ভর দিয়ে বুক উঁচু করে?");
        DYNAMIC_TEXT_BN.put("Smiles spontaneously in response to mother's voice and smile?", "মায়ের কণ্ঠস্বর ও হাসির উত্তরে কি স্বতঃস্ফূর্তভাবে হাসে?");
        DYNAMIC_TEXT_BN.put("Makes cooing sounds (gurgling, vocalizing vowel sounds)?", "আদর করলে কি কু-কু বা মিষ্টি স্বরের শব্দ করে সাড়া দেয়?");
        DYNAMIC_TEXT_BN.put("Follows moving objects smoothly with eyes across midline?", "নড়াচড়া করা বস্তু বা খেলনার দিকে চোখ ঘুরিয়ে তাকিয়ে থাকে?");
        DYNAMIC_TEXT_BN.put("Brings hands to mouth and explores hands/fingers?", "হাত ও আঙুল মুখে নিয়ে খেলা বা পরীক্ষা করে?");
        DYNAMIC_TEXT_BN.put("Good neck tone observed", "ঘাড়ের পেশির শক্তি ভালো দেখা গেছে");
        DYNAMIC_TEXT_BN.put("Lifts chest comfortably", "স্বাচ্ছন্দ্যে বুক উঁচু করতে পারে");
        DYNAMIC_TEXT_BN.put("Frequent social smiling", "নিয়মিত হাসিমুখে সাড়া দেয়");
        DYNAMIC_TEXT_BN.put("Says 'ooo' and 'ahh'", "'উউ' এবং 'আআ' শব্দ করে");
        DYNAMIC_TEXT_BN.put("Tracks colorful toys", "রঙিন খেলনা অনুসরণ করে");
        DYNAMIC_TEXT_BN.put("Starting to grasp fingers", "আঙুল ধরার চেষ্টা শুরু করেছে");

        // Vitals Notes
        DYNAMIC_TEXT_BN.put("Baby is energetic, active and feeding well.", "শিশু চনমনে, সক্রিয় এবং ভালোভাবে দুধ পান করছে।");
        DYNAMIC_TEXT_BN.put("Symptoms observed; monitoring closely.", "লক্ষণ পর্যবেক্ষণ করা হচ্ছে; নিবিড় নজরে রাখা হয়েছে।");

        // Vaccines & Ages
        DYNAMIC_TEXT_BN.put("At Birth", "জন্মের সময়");
        DYNAMIC_TEXT_BN.put("6 Weeks", "৬ সপ্তাহ");
        DYNAMIC_TEXT_BN.put("10 Weeks", "১০ সপ্তাহ");
        DYNAMIC_TEXT_BN.put("14 Weeks", "১৪ সপ্তাহ");
        DYNAMIC_TEXT_BN.put("9 Months", "৯ মাস");
        DYNAMIC_TEXT_BN.put("15 Months", "১৫ মাস");
        DYNAMIC_TEXT_BN.put("BCG (Bacillus Calmette–Guérin)", "বিসিজি (যক্ষ্মা প্রতিরোধী টিকা)");
        DYNAMIC_TEXT_BN.put("OPV-0 (Oral Polio Vaccine)", "ওপিভি-০ (মুখে খাওয়ার পোলিও টিকা)");
        DYNAMIC_TEXT_BN.put("Hepatitis B (Birth Dose)", "হেপাটাইটিস বি (জন্মকালীন ডোজ)");
        DYNAMIC_TEXT_BN.put("Pentavalent-1 (DTP-HepB-Hib)", "পেন্টাভ্যালেন্ট-১ (৫ রোগের সমন্বিত টিকা)");
        DYNAMIC_TEXT_BN.put("PCV-1 (Pneumococcal Conjugate)", "পিসিভি-১ (নিউমোনিয়া প্রতিরোধী টিকা)");
        DYNAMIC_TEXT_BN.put("OPV-1 + Rotavirus-1", "ওপিভি-১ ও রোটাভাইরাস-১");
        DYNAMIC_TEXT_BN.put("Pentavalent-2 (Second Booster)", "পেন্টাভ্যালেন্ট-২ (২য় ডোজ)");
        DYNAMIC_TEXT_BN.put("PCV-2 (Pneumococcal)", "পিসিভি-২ (নিউমোনিয়া ২য় ডোজ)");
        DYNAMIC_TEXT_BN.put("OPV-2 + Rotavirus-2", "ওপিভি-২ ও রোটাভাইরাস-২");
        DYNAMIC_TEXT_BN.put("Pentavalent-3 + IPV-1 (Injectable Polio)", "পেন্টাভ্যালেন্ট-৩ ও আইপিভি-১ (ইনজেকশন পোলিও)");
        DYNAMIC_TEXT_BN.put("PCV-3", "পিসিভি-৩ (নিউমোনিয়া ৩য় ডোজ)");
        DYNAMIC_TEXT_BN.put("OPV-3", "ওপিভি-৩ (পোলিও ৩য় ডোজ)");
        DYNAMIC_TEXT_BN.put("MR-1 (Measles & Rubella)", "এমআর-১ (হাম ও রুবেলা টিকা)");
        DYNAMIC_TEXT_BN.put("Vitamin A (100,000 IU)", "ভিটামিন এ ক্যাপসুল (১ লক্ষ আইইউ)");
        DYNAMIC_TEXT_BN.put("MR-2 + DTP Booster", "এমআর-২ ও ডিটিপি বুস্টার");

        DYNAMIC_TEXT_BN.put("Severe childhood Tuberculosis & Tuberculous Meningitis", "শিশুর মারাত্মক যক্ষ্মা ও মস্তিষ্কের যক্ষ্মা প্রতিরোধ করে");
        DYNAMIC_TEXT_BN.put("Immediate gut immunity against Poliovirus", "পোলিও ভাইরাসের বিরুদ্ধে তাৎক্ষণিক সুরক্ষা প্রদান করে");
        DYNAMIC_TEXT_BN.put("Prevents perinatal Hepatitis B transmission", "জন্মের সময় হেপাটাইটিস বি সংক্রমণ প্রতিরোধ করে");
        DYNAMIC_TEXT_BN.put("Diphtheria, Pertussis, Tetanus, Hepatitis B, and Hib Pneumonia", "ডিপথেরিয়া, হুপিং কাশি, ধনুষ্টংকার, হেপাটাইটিস বি ও হিব নিউমোনিয়া প্রতিরোধ");
        DYNAMIC_TEXT_BN.put("Severe Pneumococcal pneumonia & Bacteremia", "গুরুতর নিউমোকক্কাল নিউমোনিয়া প্রতিরোধ করে");
        DYNAMIC_TEXT_BN.put("Poliomyelitis & Rotavirus dehydration diarrhea", "পোলিও ও রোটাভাইরাসজনিত ডায়রিয়া প্রতিরোধ করে");
        DYNAMIC_TEXT_BN.put("Second booster for DTP, HepB, and Hib", "ডিটিপি, হেপ-বি এবং হিব-এর দ্বিতীয় ডোজ");
        DYNAMIC_TEXT_BN.put("Second booster for invasive pneumococcal disease", "নিউমোকক্কাল সংক্রমণের দ্বিতীয় ডোজ");
        DYNAMIC_TEXT_BN.put("Second dose of Oral Polio and Rotavirus drops", "মৌখিক পোলিও এবং রোটাভাইরাসের দ্বিতীয় ডোজ");
        DYNAMIC_TEXT_BN.put("Third primary DTP series + Inactivated Poliovirus injection", "ডিটিপি ৩য় ডোজ ও ইনজেকশন পোলিও সুরক্ষা");
        DYNAMIC_TEXT_BN.put("Third primary Pneumococcal booster", "নিউমোকক্কাল টিকার তৃতীয় ডোজ");
        DYNAMIC_TEXT_BN.put("Third primary Oral Polio dose", "মৌখিক পোলিও টিকার তৃতীয় ডোজ");
        DYNAMIC_TEXT_BN.put("Protects against Measles infection and Congenital Rubella Syndrome", "হাম এবং রুবেলা সংক্রমণ থেকে সুরক্ষা দেয়");
        DYNAMIC_TEXT_BN.put("Boosts mucosal immunity and prevents night blindness", "রোগ প্রতিরোধ ক্ষমতা বাড়ায় এবং রাতকানা রোগ প্রতিরোধ করে");
        DYNAMIC_TEXT_BN.put("Lifelong booster against Measles, Rubella, and Whooping Cough", "হাম, রুবেলা ও হুপিং কাশির বিরুদ্ধে দীর্ঘমেয়াদী বুস্টার সুরক্ষা");

        // Medicines Seeded
        DYNAMIC_TEXT_BN.put("Iron & Folic Acid", "আয়রন ও ফলিক অ্যাসিড");
        DYNAMIC_TEXT_BN.put("1 tablet (60mg elemental Fe)", "১টি ট্যাবলেট (৬০ মি.গ্রা. আয়রন)");
        DYNAMIC_TEXT_BN.put("Calcium Carbonate + Vit D3", "ক্যালসিয়াম কার্বনেট + ভিটামিন ডি৩");
        DYNAMIC_TEXT_BN.put("500 mg after lunch", "দুপুরের খাবারের পর ৫০০ মি.গ্রা.");
        DYNAMIC_TEXT_BN.put("Amoxicillin (Dental Infection)", "অ্যামোক্সিসিলিন (দাঁতের সংক্রমণ)");
        DYNAMIC_TEXT_BN.put("500 mg 3x daily", "৫০০ মি.গ্রা. দিনে ৩ বার");

        // Hospital Essentials Categories & Items
        DYNAMIC_TEXT_BN.put("Baby Essentials", "নবজাতকের সামগ্রী");
        DYNAMIC_TEXT_BN.put("Labor & Delivery Bag", "লেবার ও ডেলিভারি ব্যাগ");
        DYNAMIC_TEXT_BN.put("Postpartum Recovery Kit", "প্রসবোত্তর রিকভারি কিট");
        DYNAMIC_TEXT_BN.put("Soft cotton newborn onesies & baby rompers (4-6 sets)", "নবজাতকের নরম সুতির পোশাক ও রম্পার (৪-৬ সেট)");
        DYNAMIC_TEXT_BN.put("Newborn swaddle blankets / muslin cloths", "শিশু মোড়ানোর নরম কাঁথা ও মসলিন কাপড়");
        DYNAMIC_TEXT_BN.put("Newborn gentle diapers (size 0/1) & fragrance-free wipes", "নবজাতকের ডায়াপার (সাইজ ০/১) ও সুগন্ধিমুক্ত ওয়াইপস");
        DYNAMIC_TEXT_BN.put("Baby hats, mittens, and warm socks", "শিশুর টুপি, হাতমোজা ও গরম মোজা");
        DYNAMIC_TEXT_BN.put("Infant grooming kit: gentle brush, nail scissors", "শিশুর নেইল কাটার ও নরম ব্রাশ কিট");
        DYNAMIC_TEXT_BN.put("Mother's NID, insurance card, medical records & prenatal folder", "মায়ের জাতীয় পরিচয়পত্র, প্রেসক্রিপশন ও মেডিকেল ফাইল");
        DYNAMIC_TEXT_BN.put("Comfortable front-open nightgowns / nursing gowns (3 sets)", "সামনে খোলা আরামদায়ক নার্সিং গাউন (৩ সেট)");
        DYNAMIC_TEXT_BN.put("Warm non-skid slippers and comfortable cotton socks", "পিছলে না যাওয়া স্লিপার ও সুতির মোজা");
        DYNAMIC_TEXT_BN.put("Phone chargers (extra-long cable) and power bank", "লম্বা কেবলের ফোন চার্জার ও পাওয়ার ব্যাংক");
        DYNAMIC_TEXT_BN.put("Lip balm, hair ties, and gentle toiletries travel kit", "লিপ বাম, চুলের ব্যান্ড ও টয়লেট্রিজ ব্যাগ");
        DYNAMIC_TEXT_BN.put("High-absorbency maternity sanitary pads / disposable underwear", "ম্যাটারনিটি স্যানিটারি প্যাড ও ডিসপোজেবল আন্ডারওয়্যার");
        DYNAMIC_TEXT_BN.put("Soothing peri-rinse bottle for perineal comfort", "পেরিনিয়াল পরিচ্ছন্নতার জন্য পেরি-ওয়াশ বোতল");
        DYNAMIC_TEXT_BN.put("Nursing bras (comfortable, wire-free) & breast pads", "আরামদায়ক নার্সিং ব্রা ও ব্রেস্ট প্যাড");
        DYNAMIC_TEXT_BN.put("Nipple soothing balm (pure medical-grade lanolin)", "স্তনবৃন্তের যত্নে খাঁটি ল্যানোলিন বাম");
        DYNAMIC_TEXT_BN.put("Postpartum belly support band / binder", "প্রসবোত্তর পেটের সাপোর্ট বেল্ট / বাইন্ডার");
    }

    public static boolean isBangla(Context context) {
        return LANGUAGE_BANGLA.equals(getLanguage(context));
    }

    public static String toBanglaDigits(String input) {
        if (input == null) return "";
        char[] banglaDigits = {'০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯'};
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(banglaDigits[c - '0']);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String translateDynamicText(Context context, String englishText) {
        if (englishText == null) return "";
        if (!isBangla(context)) return englishText;
        String mapped = DYNAMIC_TEXT_BN.get(englishText);
        return mapped != null ? mapped : toBanglaDigits(englishText);
    }

    public static String translateDivision(Context context, String division) {
        if (division == null) return "";
        if (!isBangla(context)) return division;
        String bn = DIVISION_BN.get(division);
        return bn != null ? bn : division;
    }

    public static String translateDistrict(Context context, String district) {
        if (district == null) return "";
        if (!isBangla(context)) return district;
        String bn = DISTRICT_BN.get(district);
        return bn != null ? bn : district;
    }

    public static String translateSpecialtyCategory(Context context, String specialtyCategory) {
        if (specialtyCategory == null) return "";
        if (!isBangla(context)) return specialtyCategory;
        if ("Obs & Gynae".equalsIgnoreCase(specialtyCategory) || specialtyCategory.toLowerCase().contains("obs") || specialtyCategory.toLowerCase().contains("gyn")) {
            return "গাইনি ও প্রসূতি বিশেষজ্ঞ";
        }
        if ("Pediatrics".equalsIgnoreCase(specialtyCategory) || specialtyCategory.toLowerCase().contains("ped") || specialtyCategory.toLowerCase().contains("child")) {
            return "শিশু বিশেষজ্ঞ";
        }
        return specialtyCategory;
    }

    public static String translateTimelineCategory(Context context, String type) {
        if (type == null) return "";
        if (!isBangla(context)) return type;
        switch (type.toUpperCase(Locale.ROOT)) {
            case "FEEDING": return "খাবার / ফিডিং";
            case "BATH": return "গোসল";
            case "SLEEP": return "ঘুম";
            case "DIAPER": return "ডায়াপার";
            default: return type;
        }
    }

    public static String localizeTime(Context context, String timeStr) {
        if (timeStr == null) return "";
        if (!isBangla(context)) return timeStr;
        String out = timeStr.replace("Today", "আজ").replace("Tomorrow", "আগামীকাল").replace("Yesterday", "গতকাল")
                .replace("AM", "সকাল").replace("PM", "বিকাল/রাত");
        return toBanglaDigits(out);
    }

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        if (!lang.equals(LANGUAGE_ENGLISH) && !lang.equals(LANGUAGE_BANGLA)) {
            lang = LANGUAGE_ENGLISH;
        }
        return setLocale(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }
        return updateResourcesLegacy(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = context.getSharedPreferences("maacare_user_prefs", Context.MODE_PRIVATE);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences("maacare_user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
}
