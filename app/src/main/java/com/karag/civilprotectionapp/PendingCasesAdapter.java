package com.karag.civilprotectionapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.karag.civilprotectionapp.models.CompositeIncident;
import com.karag.civilprotectionapp.models.MyIncident;

import java.util.List;

public class PendingCasesAdapter extends RecyclerView.Adapter<PendingCasesAdapter.MyViewHolder> {
    private List<CompositeIncident> compositeIncidents;

    // Constructor
    public PendingCasesAdapter(List<CompositeIncident> compositeIncidents) {
        this.compositeIncidents = compositeIncidents;
    }

    // ViewHolder class
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Button checkButton;
        Button discardButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            // Initialize views
            checkButton = itemView.findViewById(R.id.check_button);
            discardButton = itemView.findViewById(R.id.discard_button);
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
