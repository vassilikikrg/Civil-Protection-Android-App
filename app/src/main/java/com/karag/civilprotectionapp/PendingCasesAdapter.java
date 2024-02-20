package com.karag.civilprotectionapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.karag.civilprotectionapp.models.CompositeIncident;
import com.karag.civilprotectionapp.models.MyIncident;

import java.util.List;

public class PendingCasesAdapter extends RecyclerView.Adapter<PendingCasesAdapter.MyViewHolder> {
    private List<CompositeIncident> compositeIncidents;
    private Context myContext;
    private FirebaseFirestore db;
    
    // Constructor
    public PendingCasesAdapter(List<CompositeIncident> compositeIncidents, Context myContext) {
        this.compositeIncidents = compositeIncidents;
        this.myContext = myContext;
    }

    // ViewHolder class
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewArea,textViewEmergencyType,textViewFirstReported,textViewNumOfReports,textViewDangerLevel;
        Button checkButton, discardButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            textViewArea=itemView.findViewById(R.id.textViewArea);
            textViewEmergencyType=itemView.findViewById(R.id.textViewEmergencyType);
            textViewFirstReported=itemView.findViewById(R.id.textViewFirstReported);
            textViewNumOfReports=itemView.findViewById(R.id.textViewNumOfReports);
            textViewDangerLevel=itemView.findViewById(R.id.textViewDangerLevel);
            checkButton = itemView.findViewById(R.id.check_button);
            discardButton = itemView.findViewById(R.id.discard_button);
        }
        public void bind(CompositeIncident compositeIncident,Context context){
            textViewArea.setText(compositeIncident.getLocationName(context));
            textViewEmergencyType.setText(compositeIncident.getEmergencyType());
            textViewFirstReported.setText(compositeIncident.formatDateTime());
            textViewNumOfReports.setText(String.valueOf(compositeIncident.getNumOfReports()));
            textViewDangerLevel.setText(compositeIncident.getDangerLevel()+"/10");
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_case_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CompositeIncident compositeIncident = compositeIncidents.get(position);

        holder.bind(compositeIncident,myContext);
        // Set click listeners for buttons
        holder.checkButton.setOnClickListener(v -> onCheckButtonClick(compositeIncident));
        holder.discardButton.setOnClickListener(v -> onDiscardButtonClick(compositeIncident));
    }

    @Override
    public int getItemCount() {
        return compositeIncidents.size();
    }

    private void onCheckButtonClick(CompositeIncident compositeIncident) {
        // Access the list of incidents inside the composite incident
        List<MyIncident> incidents = compositeIncident.getRelatedReports();

        // Modify the status of each incident
        for (MyIncident incident : incidents) {
            incident.setStatus("checked"); // Modify the status as needed
        }

        // Update the Firebase Firestore database with the new statuses
        // Call your method to update the database here
    }

    private void onDiscardButtonClick(CompositeIncident compositeIncident) {
        // Access the list of incidents inside the composite incident
        List<MyIncident> incidents = compositeIncident.getRelatedReports();

        // Modify the status of each incident
        for (MyIncident incident : incidents) {
            incident.setStatus("discarded"); // Modify the status as needed
        }

        // Update the Firebase Firestore database with the new statuses
        // Call your method to update the database here
    }
}
