package com.karag.civilprotectionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.karag.civilprotectionapp.databinding.ActivityUserMenuBinding;

public class user_menu extends AppCompatActivity {

    ActivityUserMenuBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        binding = ActivityUserMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new NewIncidentFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item ->{

            if (item.getItemId() == R.id.new_incident) {
                replaceFragment(new NewIncidentFragment());
            } else if (item.getItemId() == R.id.add_emergency) {
                replaceFragment(new AddEmergencyFragment());
            } else if (item.getItemId() == R.id.stats) {
                replaceFragment(new StatsFragment());
            }


            return true;
        });

        // Initialize toolbar and logout button
        setSupportActionBar(toolbar);
        ImageButton btnLogout = findViewById(R.id.btn_logout);

        // Set onClickListener for the logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform logout logic here, such as signing out the user
                FirebaseAuth.getInstance().signOut();

                // Navigate back to the login screen
                Intent intent = new Intent(user_menu.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Optional: Finish the current activity to prevent the user from navigating back to it using the back button
            }
        });
    }

    private void replaceFragment(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}