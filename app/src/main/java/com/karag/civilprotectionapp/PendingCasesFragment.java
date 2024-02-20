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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class PendingCasesFragment extends Fragment {
    private List<CompositeIncident> allIncidents;
    private PendingCasesAdapter pendingCasesAdapter;
    private List<String> emergenciesList = new ArrayList<>();

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
                            emergenciesList.add(document.getString("Name"));
                        }
                        CompletableFuture<Void> allIncidentsFuture = CompletableFuture.completedFuture(null);
                        for (String emergencyName : emergenciesList) {
                            allIncidentsFuture = allIncidentsFuture.thenComposeAsync(
                                    v -> fetchIncidentsForEmergency(emergencyName)
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

    private CompletableFuture<Void> fetchIncidentsForEmergency(String emergencyName) {
        return CompletableFuture.supplyAsync(() -> {
            IncidentManager incidentManager = new IncidentManager(360000, 1000, emergencyName);
            return incidentManager.assessDangerLevelAsync();
        }).thenAccept(incidents -> {
            try {
                allIncidents.addAll(incidents.get());
            } catch (InterruptedException | ExecutionException e) {
                // Handle exceptions
                Log.e(TAG, "Error occurred while fetching incidents for emergency: " + emergencyName, e);
            }
        });
    }


}