package com.example.furniturestore.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.MainActivity;
import com.example.furniturestore.R;
import com.example.furniturestore.admin.RequestsSupplierActivity;
import com.example.furniturestore.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private EditText editSellerName, editSellerEmail, editSellerPassword;
    private Button buttonAddSeller, buttonRevenue;
    private TextView textLogout;
    private ImageView buttonGoBack;
    private Button buttonViewRequests;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // UI references
        editSellerName = findViewById(R.id.editSellerName);
        editSellerEmail = findViewById(R.id.editSellerEmail);
        editSellerPassword = findViewById(R.id.editSellerPassword);
        buttonAddSeller = findViewById(R.id.buttonAddSeller);
        buttonRevenue = findViewById(R.id.buttonRevenue);
        textLogout = findViewById(R.id.textLogout);
        buttonGoBack = findViewById(R.id.buttonGoBack);
        buttonViewRequests = findViewById(R.id.buttonViewRequests);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonAddSeller.setOnClickListener(v -> addSeller());

        // Go Back to previous screen
        buttonGoBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });


        // Logout with confirmation
        textLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(AdminDashboardActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        auth.signOut();
                        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
        buttonViewRequests.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, RequestsSupplierActivity.class);
            startActivity(intent);
        });

        // Navigate to Revenue page
        buttonRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, RevenueActivity.class);
            startActivity(intent);
        });
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
