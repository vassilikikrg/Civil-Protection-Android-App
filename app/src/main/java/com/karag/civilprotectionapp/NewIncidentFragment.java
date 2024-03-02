package com.karag.civilprotectionapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karag.civilprotectionapp.adapters.IncidentAdapter;
import com.karag.civilprotectionapp.danger_assessment.IncidentManager;
import com.karag.civilprotectionapp.helpers.Caching;
import com.karag.civilprotectionapp.models.ApprovedIncident;
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

    private List<ApprovedIncident> incidents;
    private IncidentAdapter incidentAdapter;
    //private Map<String, String> userMap; // Map to store userId and username
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

        //Permission dialog
        if(arePermissionsGranted(permissions)){
            startLocationService();
            incidents = new ArrayList<>();
            List<String> incidentIds = Caching.getStoredIncidentIds(requireContext());
            // userMap = new HashMap<>();
            fetchIncidentsFromFirestore(incidentIds); // Fetch incidents after fetching usernames
            incidentAdapter = new IncidentAdapter(incidents);
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

    // Fetch incidents from Firestore
    /*private void fetchIncidentsFromFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("approved_incidents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Parse incident data and add it to the list
                            if (isClosetoUser(document.getDouble("latitude"), document.getDouble("longitude"), userLocation.getLatitude(), userLocation.getLongitude(), document.getDouble("range")))
                            {
                                ApprovedIncident incident = parseIncident(document);
                                incidents.add(incident);
                            }
                        }
                        incidentAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("NewIncidentFragment", "Error fetching incidents", task.getException());
                    }
                });
    }*/

    private void fetchIncidentsFromFirestore(List<String> incidentIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String incidentId : incidentIds) {
            db.collection("approved_incidents")
                    .document(incidentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        // Check if the document exists
                        if (documentSnapshot.exists()) {
                            // Convert the document data to your Incident model
                            ApprovedIncident incident = parseIncident(documentSnapshot);
                            incidents.add(incident);

                        } else {
                            Log.d(TAG, "No such document with ID: " + incidentId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching incident with ID: " + incidentId, e);
                    });
        }
        incidentAdapter.notifyDataSetChanged();
    }

    // Parse Firestore document to Incident object
    private ApprovedIncident parseIncident(DocumentSnapshot document) {
        return ApprovedIncident.documentToIncident(document, requireContext());
    }




}
