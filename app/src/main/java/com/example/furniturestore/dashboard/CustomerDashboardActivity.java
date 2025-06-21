package com.example.furniturestore.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;
import com.example.furniturestore.customer.CartActivity;
import com.example.furniturestore.customer.OrderHistoryActivity;
import com.example.furniturestore.customer.SupplierRequestActivity;
import com.example.furniturestore.settings.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        // Find views
        Button buttonEditProfile = findViewById(R.id.buttonEditProfile);
        Button buttonOrderHistory = findViewById(R.id.buttonOrderHistory);
        Button buttonApplySupplier = findViewById(R.id.buttonApplySupplier);
        ImageView homeIcon = findViewById(R.id.homeIcon);
        Button buttonLogout = findViewById(R.id.buttonLogout);

        ImageView cartIcon = findViewById(R.id.cartIcon);

        // Button listeners
        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        buttonOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        buttonApplySupplier.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, SupplierRequestActivity.class);
            startActivity(intent);
        });

        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, com.example.furniturestore.MainActivity.class);
            startActivity(intent);
        });

        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, CartActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // Redirect to login screen after logout
            Intent intent = new Intent(CustomerDashboardActivity.this, com.example.furniturestore.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish();
        });
    }
}
