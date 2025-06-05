package com.example.furniturestore.dashboard;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;

public class SellerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        TextView textView = findViewById(R.id.textViewSeller);
        textView.setText("Welcome, Seller!");
    }
}
