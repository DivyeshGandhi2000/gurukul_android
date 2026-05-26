package com.gurukul;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Converts date from dd/mm/yyyy format to Hindi format
 * Example: 26/12/2025 → शुक्रवार 26 दिसंबर 2025
 */
public class DateConverterHindi {

    // Days of week in Hindi
    private static final String[] DAYS_HINDI = {
            "रविवार",      // Sunday
            "सोमवार",      // Monday
            "मंगलवार",     // Tuesday
            "बुधवार",      // Wednesday
            "गुरुवार",     // Thursday
            "शुक्रवार",    // Friday
            "शनिवार"       // Saturday
    };

    // Months in Hindi
    private static final String[] MONTHS_HINDI = {
            "जनवरी",       // January
            "फ़रवरी",      // February
            "मार्च",       // March
            "अप्रैल",      // April
            "मई",          // May
            "जून",         // June
            "जुलाई",       // July
            "अगस्त",       // August
            "सितंबर",      // September
            "अक्टूबर",     // October
            "नवंबर",       // November
            "दिसंबर"       // December
    };

    /**
     * Converts date from dd/mm/yyyy to Hindi format
     * @param dateStr Date in dd/mm/yyyy format (e.g., "26/12/2025")
     * @return Date in Hindi format (e.g., "शुक्रवार 26 दिसंबर 2025")
     */
    public static String convertToHindi(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }

        try {
            // Parse the date string (dd/mm/yyyy)
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(dateStr);

            if (date == null) {
                return dateStr; // Return original if parsing fails
            }

            // Get Calendar instance
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Extract day, month, year
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0-6 (Sunday-Saturday)
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH); // 0-11
            int year = calendar.get(Calendar.YEAR);

            // Build Hindi date string
            String dayNameHindi = DAYS_HINDI[dayOfWeek];
            String monthNameHindi = MONTHS_HINDI[month];

            return dayNameHindi + " " + dayOfMonth + " " + monthNameHindi + " " + year;

        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr; // Return original string if conversion fails
        }
    }

    /**
     * Alternative method supporting multiple date formats
     */
    public static String convertToHindiFlexible(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }

        // Try different date formats
        String[] formats = {"dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd"};

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                Date date = sdf.parse(dateStr);

                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH);
                    int year = calendar.get(Calendar.YEAR);

                    String dayNameHindi = DAYS_HINDI[dayOfWeek];
                    String monthNameHindi = MONTHS_HINDI[month];

                    return dayNameHindi + " " + dayOfMonth + " " + monthNameHindi + " " + year;
                }
            } catch (ParseException e) {
                // Try next format
                continue;
            }
        }

        return dateStr; // Return original if all formats fail
    }
}