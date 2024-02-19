package com.karag.civilprotectionapp.dangerassessment;
import static android.content.ContentValues.TAG;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karag.civilprotectionapp.models.CompositeIncident;
import com.karag.civilprotectionapp.models.MyIncident;
public class IncidentManager {

    // Thresholds for grouping incidents
    private final long TIME_THRESHOLD_MS;
    private final double DISTANCE_THRESHOLD_KM;

    // Weight for number of reports criterion
    private static final double NUM_REPORTS_WEIGHT = 0.6;

    // Weight for geographical proximity criterion
    private static final double PROXIMITY_WEIGHT = 0.4;

    public IncidentManager(long time_threshold,double distance_threshold) {
        this.TIME_THRESHOLD_MS=time_threshold;
        this.DISTANCE_THRESHOLD_KM=distance_threshold;
    }

    // Method to group incidents
    private List<List<MyIncident>> groupIncidents(List<MyIncident> incidents) {
        List<List<MyIncident>> incidentGroups = new ArrayList<>();

        for (MyIncident incident : incidents) {
            boolean addedToExistingGroup = false;

            // Check if this incident can be added to an existing group
            for (List<MyIncident> group : incidentGroups) {
                MyIncident firstIncident = group.get(0); // Assuming get(0) returns the first incident in the group

                // Calculate time difference in milliseconds
                long timeDiff = Math.abs(incident.getDatetime().getTime() - firstIncident.getDatetime().getTime());

                // Calculate distance between incidents
                double distance = calculateDistance(incident.getLatitude(), incident.getLongitude(), firstIncident.getLatitude(), firstIncident.getLongitude());

                if (timeDiff <= TIME_THRESHOLD_MS && distance <= DISTANCE_THRESHOLD_KM) {
                    // Add this incident to the existing group
                    group.add(incident);
                    addedToExistingGroup = true;
                    break;
                }
            }

            // If not added to any existing group, create a new group
            if (!addedToExistingGroup) {
                List<MyIncident> newGroup = new ArrayList<>();
                newGroup.add(incident);
                incidentGroups.add(newGroup);
            }
        }

        return incidentGroups;
    }

    ////////////////////////////////
    // Distance related functions //
    ///////////////////////////////

    // Method to calculate distance between two points using Haversine formula
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        // convert to kilometers
        return R * c;
    }

    // Method to calculate average distance between n locations
    public static double calculateAverageDistance(List<MyIncident> incidents) {
        int n = incidents.size();
        double totalDistance = 0.0;

        // Calculate total distance by summing distances between each pair of locations
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double distance = calculateDistance(incidents.get(i).getLatitude(), incidents.get(i).getLongitude(),
                        incidents.get(j).getLatitude(), incidents.get(j).getLongitude());
                totalDistance += distance;
            }
        }

        // Calculate average distance
        return  totalDistance / (n * (n - 1) / 2);
    }

    // Method to calculate center of n locations
    public static double[] calculateCenter(List<MyIncident> incidents) {
        double totalLat = 0.0;
        double totalLon = 0.0;

        // Calculate total latitude and longitude
        for (MyIncident incident : incidents) {
            totalLat += incident.getLatitude();
            totalLon += incident.getLongitude();
        }

        // Calculate average latitude and longitude
        double avgLat = totalLat / incidents.size();
        double avgLon = totalLon / incidents.size();

        return new double[] { avgLat, avgLon };
    }

    /////////////////////////////////////////
    // Danger's level assessment functions //
    /////////////////////////////////////////

    // Method to assess the danger level of incidents
    public void assessDangerLevel( List<List<MyIncident>> groupedIncidents) {
        for(List<MyIncident> incidentGroup:groupedIncidents){
            int numOfReports=incidentGroup.size();
            double averageDistance=calculateAverageDistance(incidentGroup);
            double dangerLevel = calculateDangerLevel(numOfReports,averageDistance);
        }

        return;
    }

    // Method to calculate danger level based on number of incidents and proximity
    private double calculateDangerLevel(int numIncidents, double averageDistance) {
        // Calculate danger level based on weights for number of reports and proximity
        double dangerLevel = (NUM_REPORTS_WEIGHT * numIncidents) + (PROXIMITY_WEIGHT * (1 - averageDistance / DISTANCE_THRESHOLD_KM));

        // Scale the danger level to fit within the range of 1 to 10
        double scaledDangerLevel = 1 + (9 * dangerLevel / (NUM_REPORTS_WEIGHT + PROXIMITY_WEIGHT));

        // Ensure the scaled danger level falls within the range of 1 to 10
        return Math.max(1, Math.min(10, scaledDangerLevel));
    }
    private CompositeIncident createCompositeIncident(List<MyIncident> incidentGroup){
        int numOfReports=incidentGroup.size();
        double averageDistance=calculateAverageDistance(incidentGroup);
        double dangerLevel = calculateDangerLevel(numOfReports,averageDistance);
        double[] centerCoordinates=calculateCenter(incidentGroup);
        Date firstDateReported=findFirstReportedTime(incidentGroup);

        return new CompositeIncident(incidentGroup.get(0).getEmergencyType(),firstDateReported,centerCoordinates[0],centerCoordinates[1],dangerLevel,numOfReports,incidentGroup);
    }

    ////////////////////////////////
    // Firebase related functions //
    ////////////////////////////////
    private void loadTypeEmergency() {
        FirebaseFirestore.getInstance().collection("emergencies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // load all types of emergency(fire,earthquake,etc) from firebase
                        List<String> emergenciesList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            emergenciesList.add(document.getString("Name"));
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }
    private void loadIncidentReportsByEmergency(String emergencyType){
        FirebaseFirestore.getInstance().collection("incidents")
                .whereEqualTo("emergencyType",emergencyType)
                .whereEqualTo("status","under review")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // load all types of emergency(fire,earthquake,etc) from firebase
                        List<MyIncident> reports = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            reports.add(documentToIncident(document));
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }
    private MyIncident documentToIncident(QueryDocumentSnapshot document) {
        // Extract data from Firestore document and create Incident object
        String id= document.getId();
        String userId = document.getString("userId");
        String description = document.getString("description");
        String emergencyType = document.getString("emergencyType");
        String imageFilename = document.getString("imageFilename");
        Date datetime = document.getDate("datetime");
        double latitude = document.getDouble("latitude");
        double longitude = document.getDouble("longitude");

        // Create and return the Incident object
        return new MyIncident(id,userId, description, emergencyType, imageFilename, datetime, longitude, latitude);
    }
    ///////////////////////////
    // Time related function //
    ///////////////////////////
    // Method to find the time of the first incident reported
    public static Date findFirstReportedTime(List<MyIncident> reportedIncidents) {
        Date firstReportedTime = null;

        for (MyIncident reportedIncident : reportedIncidents) {
            Date ReportTime = reportedIncident.getDatetime();
            if (firstReportedTime == null || ReportTime.before(firstReportedTime)) {
                firstReportedTime = ReportTime;
            }
        }

        return firstReportedTime;
    }
}