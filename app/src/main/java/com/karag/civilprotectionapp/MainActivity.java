package com.karag.civilprotectionapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void user_button(View view){
            Intent intent = new Intent(this, user_menu.class);
            startActivity(intent);

    }

    public void employee_button(View view){
        Intent intent = new Intent(this, employee_menu.class);
        startActivity(intent);

    }
}