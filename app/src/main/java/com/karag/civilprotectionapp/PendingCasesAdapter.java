package com.karag.civilprotectionapp;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karag.civilprotectionapp.models.ApprovedIncident;
import com.karag.civilprotectionapp.models.CompositeIncident;
import com.karag.civilprotectionapp.models.MyIncident;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.denzcoskun.imageslider.constants.ScaleTypes;
public class PendingCasesAdapter extends RecyclerView.Adapter<PendingCasesAdapter.MyViewHolder> {
    private List<CompositeIncident> compositeIncidents;
    private Context myContext;
    private FirebaseFirestore db;
    private static FirebaseStorage storage;
    // Constructor
    public PendingCasesAdapter(List<CompositeIncident> compositeIncidents, Context myContext) {
        this.compositeIncidents = compositeIncidents;
        this.myContext = myContext;
    }

    // ViewHolder class
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewArea,textViewEmergencyType,textViewFirstReported,textViewNumOfReports,textViewDangerLevel;
        Button checkButton, discardButton;
        ImageSlider imageSlider;

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
            imageSlider=itemView.findViewById(R.id.image_slider);
        }
        public void bind(CompositeIncident compositeIncident,Context context){
            textViewArea.setText(compositeIncident.getLocationName(context));
            textViewEmergencyType.setText(compositeIncident.getEmergencyType());
            textViewFirstReported.setText(compositeIncident.formatDateTime());
            textViewNumOfReports.setText(String.valueOf(compositeIncident.getNumOfReports()));
            textViewDangerLevel.setText(compositeIncident.getDangerLevel()+"/10");
            List<SlideModel> slideModels=new ArrayList<>();
            // Check if the map contains any key-value pairs
            Map<String, String> imageDescriptions = compositeIncident.getImageDescriptions();
            if (imageDescriptions != null && !imageDescriptions.isEmpty()) {
                imageDescriptions.forEach((key, value) -> {
                    Log.i(TAG,key);
                    if(!key.equals("")) {
                        // Load image from Firebase Storage using the provided URL format
                        String myKey = key.replace("/", "%2F");
                        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/civilprotectionapp-6bd54.appspot.com/o/" +
                                myKey + "?alt=media&token=83b5fe1a-6038-44f7-a414-3defeba8f0f4";
                        if(!value.equals("")) slideModels.add(new SlideModel(imageUrl, value, ScaleTypes.CENTER_INSIDE));
                        else slideModels.add(new SlideModel(imageUrl, ScaleTypes.CENTER_INSIDE));
                    }
                });
                imageSlider.setImageList(slideModels,ScaleTypes.CENTER_INSIDE);
            }else{
               imageSlider.setVisibility(View.GONE);
            }
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
        db=FirebaseFirestore.getInstance();
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
            // Update the Firebase Firestore database with the new statuses
            changeIncidentStatus(incident,"approved");
        }
        // Remove the approved composite incident from the list within a synchronized block
        synchronized (compositeIncidents) {
            compositeIncidents.remove(compositeIncident);
        }
        // Notify the adapter that the dataset has changed
        notifyDataSetChanged();
        uploadIncidentToFirebase(compositeIncident);
    }

    private void onDiscardButtonClick(CompositeIncident compositeIncident) {
        // Access the list of incidents inside the composite incident
        List<MyIncident> incidents = compositeIncident.getRelatedReports();

        // Modify the status of each incident
        for (MyIncident incident : incidents) {
            incident.setStatus("discarded"); // Modify the status as needed
            // Update the Firebase Firestore database with the new statuses
            changeIncidentStatus(incident,"discarded");
        }
        // Remove the discarded composite incident from the list within a synchronized block
        synchronized (compositeIncidents) {
            compositeIncidents.remove(compositeIncident);
        }
        // Notify the adapter that the dataset has changed
        notifyDataSetChanged();
    }

    private void uploadIncidentToFirebase(CompositeIncident compositeIncident) {
        ApprovedIncident approvedIncident=new ApprovedIncident(compositeIncident);
        Map<String, Object> approvedIncidentMap=approvedIncident.toMap() ;

        db.collection("approved_incidents")
                .add(approvedIncidentMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(myContext,"Incident approved successfully.",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());})
                .addOnFailureListener(e -> {
                    Toast.makeText(myContext,"Failed to approve incident",Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error adding document", e);});
    }
    public void changeIncidentStatus(MyIncident incident,String status) {
        // Create a map with the updated attribute and its value
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);

        // Update the document with the new attribute value
        db.collection("incidents")
                .document(incident.getId())
                .update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Document successfully updated
                        Log.d(TAG, "Document updated successfully");
                    } else {
                        // Error updating document
                        Log.w(TAG, "Error updating document", task.getException());
                    }
                });
    }

}
