package com.karag.civilprotectionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private EditText emailEditText, passwordEditText;

    private ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogout = findViewById(R.id.btn_logout);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    loginUser(email, password);
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.fill_in_all_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.signUpText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        if (auth.getCurrentUser() != null) {
            startMenu(auth.getCurrentUser().getUid());
            // User is logged in, show the logout button
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            // User is not logged in, hide the logout button
            btnLogout.setVisibility(View.GONE);
        }
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful, check user role
                        String userId = auth.getCurrentUser().getUid();

                        startMenu(userId);
                    } else {
                        // If login fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMenu(String userId){
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        DocumentSnapshot document = task1.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            if ("Citizen".equals(role)) {
                                startCitizenMenuActivity();
                            } else if ("Employee".equals(role)) {
                                startEmployeeMenuActivity();
                            }
                        }
                    }
                });
    }
    private void startCitizenMenuActivity() {
        Intent intent = new Intent(LoginActivity.this, user_menu.class);
        startActivity(intent);
        finish();
    }

    private void startEmployeeMenuActivity() {
        Intent intent = new Intent(LoginActivity.this, employee_menu.class);
        startActivity(intent);
        finish();
    }

    public void logoutUser(View view) {
        // Add your logout logic here, such as signing out the user, clearing session data, etc.
        // For example, if you're using Firebase Authentication:
        FirebaseAuth.getInstance().signOut();

        // After logging out, you may want to navigate the user back to the login screen or perform any other appropriate action.
        // For example, navigate back to the login activity:
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        this.finish(); // Optional: Finish the current activity to prevent the user from navigating back to it using the back button
    }
}

