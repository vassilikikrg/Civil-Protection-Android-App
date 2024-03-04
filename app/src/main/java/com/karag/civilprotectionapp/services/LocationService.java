package com.karag.civilprotectionapp.services;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karag.civilprotectionapp.R;
import com.karag.civilprotectionapp.danger_assessment.IncidentManager;
import com.karag.civilprotectionapp.helpers.Caching;
import com.karag.civilprotectionapp.helpers.NotificationHelper;
import com.karag.civilprotectionapp.helpers.Translator;
import com.karag.civilprotectionapp.models.ApprovedIncident;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationService extends Service implements CloseIncidentsCallback {

    private static final int NOTIFICATION_ID = 123;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private Location lastLocation;


    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        lastLocation = null;
        createLocationRequest();
        // Start foreground service with a notification
        startForeground(NOTIFICATION_ID, NotificationHelper.createNotification(getApplicationContext(), getResources().getString(R.string.default_notification), ""));
        // Start listening for location updates
        requestLocationUpdates();
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationService onStartCommand");
        startForeground(NOTIFICATION_ID, NotificationHelper.createNotification(getApplicationContext(), "Real-time emergency alert is on", ""));
        requestLocationUpdates();
        return START_STICKY;
    }*/

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,60000)
                .setMinUpdateIntervalMillis(30000)
                .build();
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf(); // Stop the service if permissions are not granted
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            Location location = locationResult.getLastLocation();
            fetchCloseIncidents(location,LocationService.this);
        }
    };

    private boolean isSignificantLocationChange(Location oldLocation, Location newLocation) {
        return IncidentManager.calculateDistance(oldLocation.getLatitude(),oldLocation.getLongitude(),newLocation.getLatitude(),newLocation.getLongitude()) > 0.1; // location changed by 100 meters (0.1 km)
    }
    private void fetchCloseIncidents(Location currentLocation, CloseIncidentsCallback callback) {
        //calculate the timestamp for 24 hours ago
        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        Date twentyFourHoursAgoDate = new Date(twentyFourHoursAgo);
        FirebaseFirestore.getInstance().collection("approved_incidents")
                .whereGreaterThanOrEqualTo("dateTime", twentyFourHoursAgoDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ApprovedIncident> newIncidents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ApprovedIncident incident= ApprovedIncident.documentToIncident(document,getApplicationContext());
                            Double range=incident.getRange();
                            Double maxDistance=range.isNaN()?10:incident.getRange()/2;
                            if (IncidentManager.calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), incident.getLatitude(), incident.getLongitude()) < maxDistance) {
                                if (!alreadyReported(incident.getId())) {
                                    newIncidents.add(incident);
                                    if(incident.getDatetime()!=null) Caching.storeIncidentInCache(this,incident.getId(),dateToTimestamp(incident.getDatetime()));
                                    else Caching.storeIncidentInCache(this,incident.getId(),dateToTimestamp(new Date()));
                                }
                            }
                        }
                        callback.onCloseIncidentsFound(newIncidents);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }
    private boolean alreadyReported(String incidentId) {
        // Check if the incident ID is already reported
        return Caching.isIncidentInCacheAndRecent(this, incidentId);
    }
    @Override
    public void onCloseIncidentsFound(List<ApprovedIncident> newCloseIncidents) {
        if (!newCloseIncidents.isEmpty()) {
            // There are close incidents, create a notification
            if(newCloseIncidents.size()==1) {
                ApprovedIncident incident=newCloseIncidents.get(0);
                String title=getResources().getString(R.string.sos_there_is)+" "+incident.getEmergencyType().toLowerCase()+" "+getResources().getString(R.string.near_your_area);
                String message=getResources().getString(R.string.first_reported_at)+incident.formatDateTime()+"\n"+getResources().getString(R.string.near_the_area)+" "+incident.getLocationName();
                NotificationHelper.createNotification(getApplicationContext(),title,message);
            }
            else if (newCloseIncidents.size()>1){
                for(ApprovedIncident incident:newCloseIncidents){
                    String title=getResources().getString(R.string.sos_there_is)+" "+incident.getEmergencyType().toLowerCase()+" "+getResources().getString(R.string.near_your_area);
                    String message=getResources().getString(R.string.first_reported_at)+incident.formatDateTime()+"\n"+getResources().getString(R.string.near_the_area)+" "+incident.getLocationName();
                    NotificationHelper.createNotification(getApplicationContext(),title,message);
                    }
                }
            }
            Caching.removeExpiredIncidentsFromCache(this);
        }
    public static String getTimeAgo(Date date) {
        long timeDifference = System.currentTimeMillis() - date.getTime();

        long seconds = timeDifference / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else if (hours > 0) {
            long remainingMinutes = minutes % 60;
            return hours + " hour" + (hours == 1 ? "" : "s") + (remainingMinutes > 0 ? " and " + remainingMinutes + " minute" + (remainingMinutes == 1 ? "" : "s") : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else {
            return "just now";
        }
    }

    public static long dateToTimestamp(Date date){
        return date.getTime();
    }
    public static Date timestampToDate(long timestamp){
        return new Date(timestamp);
    }
}
