package com.karag.civilprotectionapp;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewIncidentFragment extends Fragment {

    private List<Incident> incidents;
    private IncidentAdapter incidentAdapter;
    private Map<String, String> userMap; // Map to store userId and username
    private Location userLocation;
    private static final double MAX_DISTANCE = 10000; // Maximum distance in meters (e.g., 10 kilometers)

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        incidents = new ArrayList<>();
        userMap = new HashMap<>();
        fetchUsernamesFromFirestore(); // Fetch usernames first
        fetchIncidentsFromFirestore(); // Fetch incidents after fetching usernames
        incidentAdapter = new IncidentAdapter(incidents, userMap);
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

    private void fetchUsernamesFromFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getId();
                            String username = document.getString("username");
                            Log.d("NewIncidentFragment", "username" + username);

                            userMap.put(userId, username);
                        }
                        incidentAdapter.notifyDataSetChanged(); // Notify adapter after fetching usernames
                    } else {
                        Log.e("NewIncidentFragment", "Error fetching username");
                    }
                });
    }


    private void fetchIncidentsFromFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("incidents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getString("uid");
                            String description = document.getString("description");
                            String emergencyType = document.getString("emergencyType");
                            String imageFilename = document.getString("imageFilename");
                            Timestamp timestamp = document.getTimestamp("dateTime");
                            String dateTime = timestamp != null ? formatDateTime(timestamp.toDate().toString()) : "";



                            double latitude = document.getDouble("latitude");
                            double longitude = document.getDouble("longitude");
                            String locationName = getLocationName(latitude, longitude);
                            incidents.add(new Incident(userId, description, emergencyType, imageFilename, dateTime, locationName, longitude, latitude ));


                        }
                        incidentAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Log.e("NewIncidentFragment", "Error fetching incidents from Firestore.", task.getException());
                    }
                });
    }

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Location";
    }

    private String formatDateTime(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown Date";
        }
    }




}
