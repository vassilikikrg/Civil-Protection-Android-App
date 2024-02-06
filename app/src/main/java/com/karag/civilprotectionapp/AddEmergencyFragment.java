package com.karag.civilprotectionapp;

import static android.content.Context.LOCATION_SERVICE;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddEmergencyFragment extends Fragment {

    private static final String TAG = "AddEmergencyFragment";
    FirebaseStorage storage;
    FirebaseAuth auth;
    private FusedLocationProviderClient fusedLocationClient;
    EditText editTextDescription;
    public ImageView imageEmergency;
    private ActivityResultLauncher<PickVisualMediaRequest> launcher;
    private FloatingActionButton selectImagebtn;
    private Button reportBtn;
    private Spinner spinner;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private FirebaseFirestore db;
    Uri imageUri=null;
    ProgressBar progressBar;

    public AddEmergencyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        // Initialize the launcher for photo picking
        launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                uri -> handleImageSelection(uri));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_emergency, container, false);

        //initialize UI components
        imageEmergency = view.findViewById(R.id.imageEmergency);
        selectImagebtn = view.findViewById(R.id.selectImageButton);
        spinner = view.findViewById(R.id.spinner1);
        reportBtn= view.findViewById(R.id.buttonReport);
        editTextDescription=view.findViewById(R.id.editTextDescription);
        progressBar = view.findViewById(R.id.progressBar);

        //initialize the spinnerArray and spinnerArrayAdapter
        List<String> spinnerArray = new ArrayList<>();
        spinnerArrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        //set onClickListener for the selectImagebtn button
        selectImagebtn.setOnClickListener(v -> launchImagePicker());
        //set onClickListener for the reportBtn button
        reportBtn.setOnClickListener(v -> uploadIncident());
        //load emergency types into the spinner
        loadTypeEmergency(new EmergencyCallback() {
            @Override
            public void onEmergencyLoaded(String[] emergencies) {
                spinnerArrayAdapter.addAll(emergencies);
                spinnerArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error: " + errorMessage);
            }
        });

        //firebase instances
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        return view;
    }

    private void handleImageSelection(Uri uri) {
        if (uri == null) {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
        } else {
            // Handle the selected image URI as needed
            imageUri=uri;
            imageEmergency.setImageURI(uri);
        }
    }

    private void launchImagePicker() {
        launcher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public interface EmergencyCallback {
        void onEmergencyLoaded(String[] emergencies);
        void onError(String errorMessage);
    }

    private void loadTypeEmergency(EmergencyCallback callback) {
        db.collection("emergencies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> emergenciesList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            emergenciesList.add(document.getString("Name"));
                        }
                        String[] emergenciesArray = emergenciesList.toArray(new String[0]);
                        callback.onEmergencyLoaded(emergenciesArray);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        callback.onError("Error loading emergencies");
                    }
                });
    }
    private String uploadImage(Date date){
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        String filename= formatter.format(date);
        String uid=auth.getCurrentUser().getUid();
        StorageReference ref=storage.getReference(uid+"/"+filename);
        ref.putFile(imageUri)
                .addOnProgressListener(taskSnapshot -> {
                    // Calculate progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressBar.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // Image upload success
                    imageEmergency.setImageURI(null);
                    imageEmergency.setBackgroundResource(R.drawable.incident);
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    // Image upload failure
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                });
        return filename;
    }
    private void uploadIncident(){
        // Check if permission for location is not granted
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission from the user
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 111);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                // Logic to handle location object
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String emergencyType = spinner.getSelectedItem().toString();
                String description = editTextDescription.getText().toString();
                Date dateNow= new Date();
                String userId=auth.getCurrentUser().getUid();
                if (imageUri!=null){
                    // Upload the image to Firebase Storage
                    String imageFilename=uploadImage(dateNow);
                    // Upload the incident to Firebase with location information
                    uploadIncidentToFirebase(emergencyType, description, dateNow, latitude, longitude,imageFilename,userId);
                }
                else{
                    // Upload the incident to Firebase with location information and without image path
                    uploadIncidentToFirebase(emergencyType, description, dateNow, latitude, longitude,"",userId);
                }
            }
            else {
                Toast.makeText(requireContext(), "Unable to retrieve location.Please give the related permission to the app", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void uploadIncidentToFirebase(String emergencyType, String description, Date dateTime, double latitude, double longitude,String imageFilename,String userId) {
        Map<String, Object> incident = new HashMap<>();
        incident.put("emergencyType", emergencyType);
        incident.put("description", description);
        incident.put("dateTime", dateTime);
        incident.put("latitude", latitude);
        incident.put("longitude", longitude);
        incident.put("imageFilename",imageFilename);
        incident.put("uid",userId);
        incident.put("status","under review");


        db.collection("incidents")
                .add(incident)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(),"Incident reported successfully.",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());})
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),"Failed to report incident",Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error adding document", e);});
    }

}
