package com.karag.civilprotectionapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.karag.civilprotectionapp.models.Emergency.documentToEmergency;

import android.Manifest;
import android.content.ContentValues;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karag.civilprotectionapp.adapters.IncidentAdapter;
import com.karag.civilprotectionapp.danger_assessment.IncidentManager;
import com.karag.civilprotectionapp.helpers.Caching;
import com.karag.civilprotectionapp.helpers.NetworkUtils;
import com.karag.civilprotectionapp.models.ApprovedIncident;
import com.karag.civilprotectionapp.models.CompositeIncident;
import com.karag.civilprotectionapp.models.Emergency;
import com.karag.civilprotectionapp.models.Incident;
import com.karag.civilprotectionapp.services.LocationService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = 34)
public class NewIncidentFragment extends Fragment {

    private List<ApprovedIncident> incidents;
    private IncidentAdapter incidentAdapter;
    private List<Emergency> emergenciesList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
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
        fetchTypeEmergency(); // fetch emergencies
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        incidents = new ArrayList<>();
        incidentAdapter = new IncidentAdapter(emergenciesList,incidents,requireContext());

        //Permission dialog
        if (arePermissionsGranted(permissions)) {
            startLocationService();
            // Fetch incidents and update adapter
            fetchIncidentsFromFirestore();
            //incidentAdapter = new IncidentAdapter(emergenciesList,incidents,requireContext());
        } else {
            multiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
            multiplePermissionLauncher = registerForActivityResult(multiplePermissionsContract, isGranted -> {
                if (arePermissionsGranted(permissions))
                {
                    startLocationService();
                    // Fetch incidents and update adapter
                    fetchIncidentsFromFirestore();
                    //incidentAdapter = new IncidentAdapter(emergenciesList,incidents,requireContext());
                }
            });
            requestPermissions(multiplePermissionLauncher);
        }
        if(!NetworkUtils.isInternetAvailable(requireContext())){
            Snackbar.make(requireActivity().findViewById(android.R.id.content), getResources().getString(R.string.no_internet),Snackbar.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_incident, container, false);
        // Set-up recycler view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(incidentAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
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
    //Request all permissions that are not granted from permissions array
    private void requestPermissions(ActivityResultLauncher<String[]> multiplePermissionLauncher) {
        if (!arePermissionsGranted(permissions)) {
            Log.d("PERMISSIONS", "Launching multiple contract permission launcher for ALL required permissions");
            multiplePermissionLauncher.launch(permissions);
        } else {
            Log.d("PERMISSIONS", "All permissions are already granted");
        }
    }
    private void startLocationService() {
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }

    // Fetch incidents from Firestore
    private void fetchIncidentsFromFirestore() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), getResources().getString(R.string.don_t_have_location_permission), Toast.LENGTH_SHORT).show();
            return;
        }
        //calculate the timestamp for 24 hours ago
        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        Date twentyFourHoursAgoDate = new Date(twentyFourHoursAgo);
        fusedLocationClient
                .getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection("approved_incidents")
                                .whereGreaterThanOrEqualTo("dateTime", twentyFourHoursAgoDate)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().isEmpty()) {
                                            Log.d("NewIncidentFragment", "No incidents found.");
                                        } else {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                // Parse incident data and add it to the list
                                                if (isClosetoUser(document.getDouble("latitude"), document.getDouble("longitude"), location.getLatitude(), location.getLongitude(), document.getDouble("range"))) {
                                                    ApprovedIncident incident = parseIncident(document);
                                                    incidents.add(incident);
                                                }
                                            }
                                            Collections.sort(incidents, new NewIncidentFragment.DateComparator());//display incidents sorted by date

                                            // Notify the adapter that the data set has changed
                                            incidentAdapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        Log.e("NewIncidentFragment", "Error fetching incidents", task.getException());
                                    }
                                });
                    } else {
                        Toast.makeText(requireContext(), getResources().getString(R.string.can_t_access_location), Toast.LENGTH_LONG).show();
                    }
                });
    }


    //Check if incident is close to user
    private boolean isClosetoUser(Double latitude, Double longitude, double latitude1, double longitude1, Double range) {
        return IncidentManager.calculateDistance(latitude, longitude, latitude1, longitude1) <=( range / 2 );
    }
    // Parse Firestore document to Incident object
    private ApprovedIncident parseIncident(DocumentSnapshot document) {
        return ApprovedIncident.documentToIncident(document, requireContext());
    }


    private void fetchTypeEmergency() {
        FirebaseFirestore.getInstance().collection("emergencies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            emergenciesList.add(documentToEmergency(document));
                        }
                    }
                });
    }

    // Custom comparator for sorting by date in descending order
    private static class DateComparator implements Comparator<ApprovedIncident> {
        @Override
        public int compare(ApprovedIncident incident1, ApprovedIncident incident2) {
            // Sort in descending order by date
            return incident2.getDatetime().compareTo(incident1.getDatetime());
        }
    }
}
