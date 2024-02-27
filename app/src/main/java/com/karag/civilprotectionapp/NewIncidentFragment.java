package com.karag.civilprotectionapp;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karag.civilprotectionapp.models.Incident;
import com.karag.civilprotectionapp.services.LocationService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequiresApi(api = 34)
public class NewIncidentFragment extends Fragment {

    private List<Incident> incidents;
    private IncidentAdapter incidentAdapter;
    private Map<String, String> userMap; // Map to store userId and username
    private Location userLocation;
    private static final double MAX_DISTANCE = 10000; // Maximum distance in meters (e.g., 10 kilometers)
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    };
    private ActivityResultContracts.RequestMultiplePermissions multiplePermissionsContract;
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        incidents = new ArrayList<>();
        userMap = new HashMap<>();
        fetchUsernamesFromFirestore(); // Fetch usernames first
        fetchIncidentsFromFirestore(); // Fetch incidents after fetching usernames
        incidentAdapter = new IncidentAdapter(incidents, userMap);

        //Permission dialog
        if(arePermissionsGranted(permissions)){
            startLocationService();
        }else{
        multiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
        multiplePermissionLauncher = registerForActivityResult(multiplePermissionsContract, isGranted -> {
            Log.d("PERMISSIONS", "Launcher result: " + isGranted.toString());
            /*if (isGranted.containsValue(false)) {
                Log.d("PERMISSIONS", "At least one of the permissions was not granted, launching again...");
                multiplePermissionLauncher.launch(permissions);
            }*/
        });

        requestPermissions(multiplePermissionLauncher);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_incident, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(incidentAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        /*if(arePermissionsGranted(permissions)){
            startLocationService();
        }*/
    }


    // Check if all permissions are granted
    private boolean arePermissionsGranted(String[] permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("PERMISSIONS", "Permission is not granted: " + permission);
                    return false;
                }
                Log.d("PERMISSIONS", "Permission already granted: " + permission);
            }
            return true;
        }
        return false;
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }


    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });
    private void requestPermissions(ActivityResultLauncher<String[]> multiplePermissionLauncher) {
        if (!arePermissionsGranted(permissions)) {
            Log.d("PERMISSIONS", "Launching multiple contract permission launcher for ALL required permissions");
            multiplePermissionLauncher.launch(permissions);
        } else {
            Log.d("PERMISSIONS", "All permissions are already granted");
        }
    }


    // Fetch usernames from Firestore
    private void fetchUsernamesFromFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getId();
                            String username = document.getString("username");
                            userMap.put(userId, username);
                        }
                        incidentAdapter.notifyDataSetChanged(); // Notify adapter after fetching usernames
                    } else {
                        Log.e("NewIncidentFragment", "Error fetching usernames", task.getException());
                    }
                });
    }

    // Fetch incidents from Firestore
    private void fetchIncidentsFromFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("incidents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Parse incident data and add it to the list
                            Incident incident = parseIncident(document);
                            incidents.add(incident);
                        }
                        incidentAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("NewIncidentFragment", "Error fetching incidents", task.getException());
                    }
                });
    }

    // Parse Firestore document to Incident object
    private Incident parseIncident(QueryDocumentSnapshot document) {
        // Extract incident data from document
        String userId = document.getString("uid");
        String description = document.getString("description");
        String emergencyType = document.getString("emergencyType");
        String imageFilename = document.getString("imageFilename");
        Timestamp timestamp = document.getTimestamp("dateTime");
        String dateTime = timestamp != null ? formatDateTime(timestamp.toDate().toString()) : "";
        double latitude = document.getDouble("latitude");
        double longitude = document.getDouble("longitude");
        String locationName = getLocationName(latitude, longitude);
        // Create and return Incident object
        return new Incident(userId, description, emergencyType, imageFilename, dateTime, locationName, longitude, latitude);
    }

    // Format datetime string
    private String formatDateTime(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown Date";
        }
    }

    // Get location name from latitude and longitude
    private String getLocationName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                if (city != null && !city.isEmpty()) {
                    return city; // Return the city name if available
                } else {
                    return "Unknown Location"; // If city is not available, return a default value
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown Location";
    }
}
