package com.karag.civilprotectionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private EditText emailEditText, passwordEditText, nameEditText, surnameEditText, usernameEditText;
    private Spinner roleSpinner;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);



        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nameEditText = findViewById(R.id.nameEditText);
        surnameEditText = findViewById(R.id.surnameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        roleSpinner = findViewById(R.id.roleSpinner);
        signUpButton = findViewById(R.id.signUpButton);

        // Populate the spinner with roles
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String name = nameEditText.getText().toString().trim();
                String surname = surnameEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();
                String selectedRole = roleSpinner.getSelectedItem().toString();
                if (!email.isEmpty() && !password.isEmpty() && !name.isEmpty() && !surname.isEmpty() && !username.isEmpty()) {
                    if(selectedRole.equals(getResources().getString(R.string.citizen))){
                        signUpUser(email, password, name, surname, username, "Citizen");
                    }else{
                        signUpUser(email, password, name, surname, username, "Employee");
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, getResources().getString(R.string.fill_in_all_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.loginText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }

    private void signUpUser(String email, String password, final String name, final String surname, final String username, final String role) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, add user data to Firestore
                            String userId = auth.getCurrentUser().getUid();
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("surname", surname);
                            user.put("username", username);
                            user.put("email", email);
                            user.put("role", role);

                            firestore.collection("users")
                                    .document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        // User data added successfully
                                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                        finish();
                                        Toast.makeText(SignUpActivity.this, getResources().getString(R.string.signed_up_successful), Toast.LENGTH_LONG).show();

                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle errors while adding user data to Firestore
                                        Toast.makeText(SignUpActivity.this, getResources().getString(R.string.failed_sign_up), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, getResources().getString(R.string.failed_sign_up), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

