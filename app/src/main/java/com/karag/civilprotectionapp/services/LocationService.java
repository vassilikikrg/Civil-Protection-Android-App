package com.karag.civilprotectionapp.services;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karag.civilprotectionapp.danger_assessment.IncidentManager;
import com.karag.civilprotectionapp.models.Incident;
import com.karag.civilprotectionapp.models.MyIncident;

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
        startForeground(NOTIFICATION_ID, NotificationHelper.createNotification(getApplicationContext(), "Real-time emergency alert is on", ""));
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
                        List<MyIncident> newIncidents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MyIncident incident= IncidentManager.documentToIncident(document);
                            if (IncidentManager.calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), incident.getLatitude(), incident.getLongitude()) < 10) {
                                if (!alreadyReported(incident.getId())) {
                                    newIncidents.add(incident);
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
        // You may implement your logic here, such as checking a local database or cache
        // For now, assume no incident is reported
        return false;
    }
    @Override
    public void onCloseIncidentsFound(List<MyIncident> newCloseIncidents) {
        if (!newCloseIncidents.isEmpty()) {
            // There are close incidents, create a notification
            if(newCloseIncidents.size()==1)
            NotificationHelper.createNotification(getApplicationContext(),"SOS! There is a "+newCloseIncidents.get(0).getEmergencyType().toLowerCase()+" near your area","Reported "+getTimeAgo(newCloseIncidents.get(0).getDatetime()));
            else{
                for(MyIncident incident:newCloseIncidents){
                    NotificationHelper.createNotification(getApplicationContext(),"SOS! There is a "+incident.getEmergencyType().toLowerCase()+" near your area","Reported "+getTimeAgo(incident.getDatetime()));
                }
            }
        }

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
}
