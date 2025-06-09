package com.example.furniturestore.dashboard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private EditText editSellerName, editSellerEmail, editSellerPassword;
    private Button buttonAddSeller;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        editSellerName = findViewById(R.id.editSellerName);
        editSellerEmail = findViewById(R.id.editSellerEmail);
        editSellerPassword = findViewById(R.id.editSellerPassword);
        buttonAddSeller = findViewById(R.id.buttonAddSeller);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonAddSeller.setOnClickListener(v -> addSeller());
    }

    private void addSeller() {
        String name = editSellerName.getText().toString().trim();
        String email = editSellerEmail.getText().toString().trim();
        String password = editSellerPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Please fill all fields with valid data", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", name);
                    userMap.put("email", email);
                    userMap.put("role", "seller");

                    db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Seller added successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to add seller: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to register seller: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
