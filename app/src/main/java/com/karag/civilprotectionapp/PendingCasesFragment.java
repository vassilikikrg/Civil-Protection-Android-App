package com.karag.civilprotectionapp;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karag.civilprotectionapp.models.CompositeIncident;
import com.karag.civilprotectionapp.danger_assessment.IncidentManager;
import com.karag.civilprotectionapp.models.Emergency;
import com.karag.civilprotectionapp.models.MyIncident;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class PendingCasesFragment extends Fragment {
    private List<CompositeIncident> allIncidents;
    private PendingCasesAdapter pendingCasesAdapter;
    private List<Emergency> emergenciesList = new ArrayList<>();

    public PendingCasesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allIncidents = new ArrayList<>();
        fetchTypeEmergency();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_cases, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.pendingCasesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingCasesAdapter = new PendingCasesAdapter(allIncidents,requireContext());
        recyclerView.setAdapter(pendingCasesAdapter);
        return view;
    }

    private void fetchTypeEmergency() {
        FirebaseFirestore.getInstance().collection("emergencies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            emergenciesList.add(documentToEmergency(document));
                        }
                        CompletableFuture<Void> allIncidentsFuture = CompletableFuture.completedFuture(null);
                        for (Emergency emergency : emergenciesList) {
                            allIncidentsFuture = allIncidentsFuture.thenComposeAsync(
                                    v -> fetchIncidentsForEmergency(emergency)
                            );
                        }
                        allIncidentsFuture.thenRun(() -> {
                            getActivity().runOnUiThread(() -> {
                                pendingCasesAdapter.notifyDataSetChanged();
                            });
                        });
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private CompletableFuture<Void> fetchIncidentsForEmergency(Emergency emergency) {
        return CompletableFuture.supplyAsync(() -> {
            IncidentManager incidentManager = new IncidentManager(emergency.timespanToMillieSeconds(), emergency.getRange(), emergency.getName());
            return incidentManager.assessDangerLevelAsync();
        }).thenAccept(incidents -> {
            try {
                allIncidents.addAll(incidents.get());
                Collections.sort(allIncidents, new DangerLevelComparator());//display incidents sorted by danger level
            } catch (InterruptedException | ExecutionException e) {
                // Handle exceptions
                Log.e(TAG, "Error occurred while fetching incidents for emergency: " + emergency.getName(), e);
            }
        });
    }

    private Emergency documentToEmergency(QueryDocumentSnapshot document) {
        // Extract data from Firestore document and create Emergency object
        String name=document.getString("Name");
        Log.i(TAG,name);
        String greekName = document.getString("GreekName");
        long timespan = document.getLong("timespan");
        int range = ((Long) document.get("range")).intValue();
        // Create and return the Incident object
        return new Emergency(name,greekName,range,timespan);
    }
// Custom comparator for sorting by danger level in descending order
    private static class DangerLevelComparator implements Comparator<CompositeIncident> {
        @Override
        public int compare(CompositeIncident incident1, CompositeIncident incident2) {
            // Sort in descending order
            return Double.compare(incident2.getDangerLevel(), incident1.getDangerLevel());
        }
    }

}