package com.karag.civilprotectionapp.danger_assessment;
import static android.content.ContentValues.TAG;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karag.civilprotectionapp.models.CompositeIncident;
import com.karag.civilprotectionapp.models.MyIncident;
public class IncidentManager {

    // Thresholds for grouping incidents
    private final long TIME_THRESHOLD_MS;
    private final double DISTANCE_THRESHOLD_KM;
    private final String emergencyType;
    private List<MyIncident> underReviewIncidents =new ArrayList<>();

    // Weight for number of reports criterion
    private static final double NUM_REPORTS_WEIGHT = 0.6;

    // Weight for geographical proximity criterion
    private static final double PROXIMITY_WEIGHT = 0.4;

    public IncidentManager(long time_threshold, double distance_threshold, String emergencyType) {
        this.TIME_THRESHOLD_MS=time_threshold;
        this.DISTANCE_THRESHOLD_KM=distance_threshold;
        this.emergencyType = emergencyType;

    }

    // Method to group incidents
    public List<List<MyIncident>> groupIncidents() {
        List<List<MyIncident>> incidentGroups = new ArrayList<>();

        for (MyIncident incident : underReviewIncidents) {
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
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
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
    // Method to assess the danger level of incidents
    public CompletableFuture<List<CompositeIncident>> assessDangerLevelAsync() {
        return groupIncidentReportsByEmergencyAsync()
                .thenApply(groupedIncidents -> {
                    Log.d(TAG, "SIZEEE" + groupedIncidents.size());
                    List<CompositeIncident> compositeIncidents = new ArrayList<>();
                    for (List<MyIncident> incidentGroup : groupedIncidents) {
                        CompositeIncident compositeIncident = createCompositeIncident(incidentGroup);
                        compositeIncidents.add(compositeIncident);
                    }
                    return compositeIncidents;
                })
                .exceptionally(e -> {
                    Log.e(TAG, "Error occurred while fetching incident reports", e);
                    return new ArrayList<>();
                });
    }
    // Method to calculate danger level based on number of incidents and proximity
    private double calculateDangerLevel(int numIncidents, double averageDistance) {
        if (numIncidents == 1) {
            return 1; // If there's only one incident, return 1
        } else {
            // Calculate danger level based on weights for number of reports and proximity
            double dangerLevel;
            double proximityFactor;
            double reportFactor;

            if (numIncidents >= 30) {
                reportFactor = NUM_REPORTS_WEIGHT * 10; // If there are 30 or more reports, set report factor to its max value
            } else {
                reportFactor = NUM_REPORTS_WEIGHT * numIncidents / 3;
            }

            if (averageDistance != 0) {
                double distanceRatio = DISTANCE_THRESHOLD_KM / averageDistance;
                if (distanceRatio > 10) {
                    proximityFactor = PROXIMITY_WEIGHT * 10; // Set proximity factor to its max value
                } else {
                    proximityFactor = PROXIMITY_WEIGHT * distanceRatio;
                }
            } else {
                proximityFactor = PROXIMITY_WEIGHT * 10; // If average distance is 0, set proximity factor to its max value
            }

            dangerLevel = reportFactor + proximityFactor;
            // Ensure the danger level falls within the range of 1 to 10
            return Math.max(1, Math.min(10, dangerLevel));
        }
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

    // Method to fetch incident reports from Firebase Firestore asynchronously
    public CompletableFuture<List<List<MyIncident>>> groupIncidentReportsByEmergencyAsync() {
        CompletableFuture<List<List<MyIncident>>> future = new CompletableFuture<>();

        FirebaseFirestore.getInstance().collection("incidents")
                .whereEqualTo("emergencyType", this.emergencyType)
                .whereEqualTo("status", "under review")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<List<MyIncident>> groupedIncidents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            underReviewIncidents.add(documentToIncident(document));
                        }
                        future.complete(groupIncidents());
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }
    private MyIncident documentToIncident(QueryDocumentSnapshot document) {
        // Extract data from Firestore document and create Incident object
        String id= document.getId();
        String userId = document.getString("uid");
        String description = document.getString("description");
        String emergencyType = document.getString("emergencyType");
        String imageFilename = document.getString("imageFilename");
        Date datetime = document.getDate("dateTime");
        double latitude = document.getDouble("latitude");
        double longitude = document.getDouble("longitude");

        // Create and return the Incident object
        return new MyIncident(id,userId, description, emergencyType, imageFilename, datetime,latitude, longitude);
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