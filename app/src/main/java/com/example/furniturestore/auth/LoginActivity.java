package com.example.furniturestore.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;
import com.example.furniturestore.dashboard.AdminDashboardActivity;
import com.example.furniturestore.dashboard.CustomerDashboardActivity;
import com.example.furniturestore.dashboard.SellerDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonGoToRegister = findViewById(R.id.buttonGoToRegister);
        Button buttonGoBack = findViewById(R.id.buttonGoBack); // NEW

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonLogin.setOnClickListener(v -> loginUser());
        buttonGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        // Go Back button action
        buttonGoBack.setOnClickListener(v -> onBackPressed());
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter valid email");
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUser().getUid();

                    if (email.equals("admin@gmail.com")) {
                        db.collection("users").document(uid).update("role", "admin");
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                        finish();
                        return;
                    }

                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String role = documentSnapshot.getString("role");
                                    if (role == null) role = "customer";

                                    switch (role) {
                                        case "admin":
                                            startActivity(new Intent(this, AdminDashboardActivity.class));
                                            break;
                                        case "seller":
                                            startActivity(new Intent(this, SellerDashboardActivity.class));
                                            break;
                                        case "customer":
                                        default:
                                            startActivity(new Intent(this, CustomerDashboardActivity.class));
                                            break;
                                    }
                                    finish();
                                } else {
                                    Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
