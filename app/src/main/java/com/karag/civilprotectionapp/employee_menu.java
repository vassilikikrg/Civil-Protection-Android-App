package com.karag.civilprotectionapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.karag.civilprotectionapp.databinding.ActivityEmployeeMenuBinding;

public class employee_menu extends AppCompatActivity {

    ActivityEmployeeMenuBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        binding = ActivityEmployeeMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new PendingCasesFragment());

        binding.bottomNavigationView2.setOnItemSelectedListener(item ->{

            if (item.getItemId() == R.id.pending_cases) {
                replaceFragment(new PendingCasesFragment());
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
                Intent intent = new Intent(employee_menu.this, LoginActivity.class);
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