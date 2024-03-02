package com.karag.civilprotectionapp.adapters;

import static android.content.ContentValues.TAG;

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

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karag.civilprotectionapp.R;
import com.karag.civilprotectionapp.models.ApprovedIncident;
import com.karag.civilprotectionapp.models.Incident;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.ViewHolder> {

    private List<ApprovedIncident> incidents;

    private FirebaseStorage storage;

    private Context context;
    private Location userLocation;

    public IncidentAdapter(List<ApprovedIncident> incidents) {
        this.incidents = incidents;
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
        ApprovedIncident incident = incidents.get(position);
        holder.bind(incident);
    }

    @Override
    public int getItemCount() {
        return incidents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewArea;
        private TextView textViewFirstReported;
        private TextView textViewEmergencyType;
        private ImageSlider imageSlider;
        private TextView textViewNumOfReports;
        private TextView textViewDangerLevel;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewArea=itemView.findViewById(R.id.textViewArea);
            textViewEmergencyType=itemView.findViewById(R.id.textViewEmergencyType);
            textViewFirstReported=itemView.findViewById(R.id.textViewFirstReported);
            textViewNumOfReports=itemView.findViewById(R.id.textViewNumOfReports);
            textViewDangerLevel=itemView.findViewById(R.id.textViewDangerLevel);
            imageSlider=itemView.findViewById(R.id.image_slider);
        }

        public void bind(ApprovedIncident incident) {
            textViewArea.setText(incident.getLocationName());
            textViewEmergencyType.setText(incident.getEmergencyType());
            textViewFirstReported.setText(incident.formatDateTime());
            textViewNumOfReports.setText(String.valueOf(incident.getNumOfReports()));
            textViewDangerLevel.setText(incident.getDangerLevel()+"/10");
            List<SlideModel> slideModels=new ArrayList<>();
            // Check if the map contains any key-value pairs
            Map<String, String> imageDescriptions = incident.getImageDescriptions();
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
    }

