package com.karag.civilprotectionapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.karag.civilprotectionapp.databinding.ActivityEmployeeMenuBinding;

public class employee_menu extends AppCompatActivity {

    ActivityEmployeeMenuBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    private void replaceFragment(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}