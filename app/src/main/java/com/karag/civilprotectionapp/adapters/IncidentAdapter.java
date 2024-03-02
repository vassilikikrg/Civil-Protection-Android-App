package com.karag.civilprotectionapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karag.civilprotectionapp.R;
import com.karag.civilprotectionapp.models.Incident;

import java.util.List;
import java.util.Map;

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.ViewHolder> {

    private List<Incident> incidents;
    private Map<String, String> userMap;
    private FirebaseStorage storage;

    private Context context;
    private Location userLocation;

    public IncidentAdapter(List<Incident> incidents, Map<String, String> userMap) {
        this.incidents = incidents;
        this.userMap = userMap;
        this.storage = FirebaseStorage.getInstance();
        this.context = context;
        this.userLocation = userLocation;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incident, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Incident incident = incidents.get(position);
        holder.bind(incident);
    }

    @Override
    public int getItemCount() {
        return incidents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewUsername;
        private TextView textViewDescription;
        private TextView textViewEmergencyType;
        private ImageView imageView;
        private TextView textViewDateTime;
        private TextView textViewLocation;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewEmergencyType = itemView.findViewById(R.id.textViewEmergencyType);
            imageView = itemView.findViewById(R.id.imageView);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
        }

        public void bind(Incident incident) {
            String username = userMap.get(incident.getUserId());
            textViewUsername.setText(username != null ? username + " posted:" : "Unknown User");
            String description = incident.getDescription();
            if (description != null && !description.isEmpty()) {
                textViewDescription.setText(description);
            } else {
                textViewDescription.setText("No description available");
            }
            textViewEmergencyType.setText(incident.getEmergencyType());
            textViewDateTime.setText(incident.getDatetime());
            textViewLocation.setText(incident.getLocation());

            if (incident.getImageFilename() != null && !incident.getImageFilename().isEmpty()) {
                // Load image from Firebase Storage using the provided URL format
                String imageUrl = "https://firebasestorage.googleapis.com/v0/b/civilprotectionapp-6bd54.appspot.com/o/" +
                        incident.getUserId() + "%2F" + incident.getImageFilename() + "?alt=media&token=83b5fe1a-6038-44f7-a414-3defeba8f0f4";

                StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
                imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                    // Successfully retrieved image data
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bitmap);
                }).addOnFailureListener(exception -> {
                    // Failed to fetch image
                    Log.e("IncidentAdapter", "Failed to load image: " + exception.getMessage());
                    imageView.setImageResource(R.drawable.loading); // Placeholder image
                });
            } else {
                // No image filename provided
                imageView.setVisibility(View.GONE); // Placeholder image
            }
        }

    }
    }

