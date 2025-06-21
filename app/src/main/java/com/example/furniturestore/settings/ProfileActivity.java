package com.example.furniturestore.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.furniturestore.R;
import com.example.furniturestore.auth.LoginActivity;
import com.example.furniturestore.dashboard.AdminDashboardActivity;
import com.example.furniturestore.dashboard.CustomerDashboardActivity;
import com.example.furniturestore.dashboard.SellerDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView textUserName, textUserEmail, textUserRole;
    private ImageView backIcon;
    private Button buttonLogout, buttonDashboard, buttonEditProfile;

    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textUserName = findViewById(R.id.textUserName);
        textUserEmail = findViewById(R.id.textUserEmail);
        textUserRole = findViewById(R.id.textUserRole);
        backIcon = findViewById(R.id.backIcon);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonDashboard = findViewById(R.id.buttonDashboard);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showLoginRequiredDialog();
        } else {
            loadUserDetails(currentUser);
        }

        backIcon.setOnClickListener(v -> onBackPressed());

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        buttonEditProfile.setOnClickListener(v -> {
            if (!"admin".equalsIgnoreCase(userRole)) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            }
        });
    }

    private void loadUserDetails(FirebaseUser user) {
        String uid = user.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        userRole = documentSnapshot.getString("role");

                        textUserName.setText(name != null ? name : user.getDisplayName());
                        textUserEmail.setText(email != null ? email : user.getEmail());
                        textUserRole.setText(userRole != null ? userRole : "Unknown");

                        if ("admin".equalsIgnoreCase(userRole)) {
                            buttonDashboard.setVisibility(View.VISIBLE);
                            buttonEditProfile.setVisibility(View.GONE); // Admin can't edit
                            buttonDashboard.setOnClickListener(v -> {
                                startActivity(new Intent(ProfileActivity.this, AdminDashboardActivity.class));
                            });
                        } else if ("seller".equalsIgnoreCase(userRole)) {
                            buttonDashboard.setVisibility(View.VISIBLE);
                            buttonEditProfile.setVisibility(View.VISIBLE);
                            buttonDashboard.setOnClickListener(v ->
                                    startActivity(new Intent(ProfileActivity.this, SellerDashboardActivity.class)));
                        } else {
                            buttonDashboard.setVisibility(View.GONE);
                            buttonEditProfile.setVisibility(View.VISIBLE);
                        }

                    } else {
                        Toast.makeText(ProfileActivity.this, "User record not found in database.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoginRequiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Login Required")
                .setMessage("You must login/register first.")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }
}
