package com.karag.civilprotectionapp.helpers;

import android.content.Context;

import com.karag.civilprotectionapp.R;
import com.karag.civilprotectionapp.models.Emergency;

import java.util.List;
import java.util.Locale;

public class Translator {

    public static String getNameLocale(Context context, Emergency emergency) {
        Locale currentLocale = context.getResources().getConfiguration().getLocales().get(0);
        String languageCode = currentLocale.getLanguage();
        if (languageCode.equals("el")) {
            return emergency.getGreekName();
        } else return emergency.getName();
    }

    public static boolean isEnglish(String word) {
        for (char c : word.toCharArray()) {
            // Check if the character falls within the Unicode range of English characters or is a space
            if (!((c >= '\u0041' && c <= '\u005A') || // Uppercase English letters
                    (c >= '\u0061' && c <= '\u007A') || // Lowercase English letters
                    (c == ' '))) {                      // Space
                return false; // If any non-English character is found, return false
            }
        }
        return true; // If all characters are English or spaces, return true
    }


    public static String translateNameToEnglish(String emergencyName, List<Emergency> emergencies) {
        for (Emergency emergency : emergencies) {
            if (emergency.getGreekName().equals(emergencyName)) {
                return emergency.getName();
            }
        }
        return null;
    }

    public static String translateEmergency(String emergency, Context context) {
        String[] stringArray = context.getResources().getStringArray(R.array.emergency_array);

        for (String item : stringArray) {
            // Get the resource ID of the item
            int resourceId = context.getResources().getIdentifier(item.substring(1), "string", context.getPackageName());
            // Get the translated string using the resource ID
            String translatedString = context.getString(resourceId);
            // Compare the translated string with the input emergency
            if (translatedString.equals(emergency)) {
                return translatedString;
            }
        }

        return emergency;
    }



}
