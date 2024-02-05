package com.karag.civilprotectionapp;

import static android.content.Context.LOCATION_SERVICE;

import static androidx.core.content.ContextCompat.getSystemService;

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
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddEmergencyFragment extends Fragment {

    private static final String TAG = "AddEmergencyFragment";
    FirebaseStorage storage;
    FirebaseAuth auth;
    LocationManager locationManager;

    EditText editTextDescription;
    public ImageView imageEmergency;
    private ActivityResultLauncher<PickVisualMediaRequest> launcher;
    private FloatingActionButton selectImagebtn;
    private Button reportBtn;
    private Spinner spinner;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private FirebaseFirestore db;
    Uri imageUri;

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
        locationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);
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
        //initialize the spinnerArray and spinnerArrayAdapter
        List<String> spinnerArray = new ArrayList<>();
        spinnerArrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        //set onClickListener for the selectImagebtn button
        selectImagebtn.setOnClickListener(v -> launchImagePicker());
        //set onClickListener for the reportBtn button
        reportBtn.setOnClickListener(v -> uploadImage());
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
    private void uploadImage(){
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date dateNow= new Date();
        String filename= formatter.format(dateNow);
        String uid=auth.getCurrentUser().getUid();
        StorageReference ref=storage.getReference(uid+"/"+filename);
        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
        imageEmergency.setImageURI(null);
        imageEmergency.setBackgroundResource(R.drawable.fire_in_a_burning_house);
        });
    }
    private void uploadIncident(){
        // Check if permission for location is not granted
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission from the user
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 111);
            return;
        }
    }
}
