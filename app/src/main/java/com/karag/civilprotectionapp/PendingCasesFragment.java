package com.karag.civilprotectionapp;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karag.civilprotectionapp.models.CompositeIncident;
import com.karag.civilprotectionapp.danger_assessment.IncidentManager;

import java.util.ArrayList;
import java.util.List;

public class PendingCasesFragment extends Fragment {
    private List<CompositeIncident> allIncidents;
    private PendingCasesAdapter pendingCasesAdapter;
    private IncidentManager incidentManager;
    private List<String> emergenciesList=new ArrayList<>();

    public PendingCasesFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allIncidents = new ArrayList<>();
        fetchTypeEmergency();
        pendingCasesAdapter = new PendingCasesAdapter(allIncidents);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_cases, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.pendingCasesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(pendingCasesAdapter);
        return view;
    }

    private void fetchTypeEmergency() {
        FirebaseFirestore.getInstance().collection("emergencies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // load all types of emergency(fire,earthquake,etc) from firebase
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            emergenciesList.add(document.getString("Name"));
                        }
                        for(String emergencyName:emergenciesList){
                            Log.d(TAG,emergencyName);
                            incidentManager=new IncidentManager(360000,1000,emergencyName);//initialize incident manager to then perform danger assessment
                            allIncidents.addAll(incidentManager.assessDangerLevel());
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }
}