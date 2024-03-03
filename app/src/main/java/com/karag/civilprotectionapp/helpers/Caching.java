package com.karag.civilprotectionapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Caching {

    private static final String INCIDENTS_CACHE_PREF = "IncidentsCache";
    private static final String INCIDENT_ID_SET_KEY = "IncidentIdSet";

    // Store the incident ID and timestamp in cache
    public static void storeIncidentInCache(Context context,String incidentId, long timestamp) {
        SharedPreferences preferences = context.getSharedPreferences(INCIDENTS_CACHE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> incidentIdSet = preferences.getStringSet(INCIDENT_ID_SET_KEY, new HashSet<>());
        incidentIdSet.add(incidentId);
        editor.putLong(incidentId, timestamp);
        editor.putStringSet(INCIDENT_ID_SET_KEY, incidentIdSet);
        editor.apply();
    }

    // Check if the incident is stored in cache and within 24 hours
    public static boolean isIncidentInCacheAndRecent(Context context,String incidentId) {
        SharedPreferences preferences = context.getSharedPreferences(INCIDENTS_CACHE_PREF, Context.MODE_PRIVATE);
        long incidentTimestamp = preferences.getLong(incidentId, -1);
        if (incidentTimestamp != -1) {
            long currentTime = System.currentTimeMillis();
            return (currentTime - incidentTimestamp) <= (24 * 60 * 60 * 1000);
        }
        return false;
    }

    // Remove incidents from cache older than 24 hours
    public static void removeExpiredIncidentsFromCache(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(INCIDENTS_CACHE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> incidentIdSet = preferences.getStringSet(INCIDENT_ID_SET_KEY, new HashSet<>());
        long currentTime = System.currentTimeMillis();

        // Create an iterator to safely remove elements
        Iterator<String> iterator = incidentIdSet.iterator();
        while (iterator.hasNext()) {
            String incidentId = iterator.next();
            long incidentTimestamp = preferences.getLong(incidentId, -1);
            if (incidentTimestamp != -1 && (currentTime - incidentTimestamp) > (24 * 60 * 60 * 1000)) {
                // Remove the incident from cache
                editor.remove(incidentId);
                iterator.remove(); // Safely remove the element from the set
            }
        }

        // Update the set in SharedPreferences
        editor.putStringSet(INCIDENT_ID_SET_KEY, incidentIdSet);
        editor.apply();
    }


    public static List<String> getStoredIncidentIds(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(INCIDENTS_CACHE_PREF, Context.MODE_PRIVATE);
        return new ArrayList<>(sharedPreferences.getStringSet(INCIDENT_ID_SET_KEY, new HashSet<>()));
    }

}
