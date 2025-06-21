package com.example.furniturestore.dashboard;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RevenueActivity extends AppCompatActivity {

    private TextView revenueReportText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        // Back button
        ImageView buttonGoBack = findViewById(R.id.buttonGoBack);
        buttonGoBack.setOnClickListener(v -> finish());

        revenueReportText = findViewById(R.id.revenueReportText);
        db = FirebaseFirestore.getInstance();

        loadRevenue();
    }

    private void loadRevenue() {
        db.collection("orders").get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Double> sellerRevenueMap = new HashMap<>();
                    double totalRevenue = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String sellerId = doc.getString("sellerId");
                        Double amount = doc.getDouble("totalAmount");

                        if (sellerId != null && amount != null) {
                            totalRevenue += amount;
                            sellerRevenueMap.put(sellerId,
                                    sellerRevenueMap.getOrDefault(sellerId, 0.0) + amount);
                        }
                    }

                    double adminProfit = totalRevenue * 0.10;

                    StringBuilder message = new StringBuilder();
                    message.append("Revenue by each seller:\n\n");
                    for (Map.Entry<String, Double> entry : sellerRevenueMap.entrySet()) {
                        message.append("Seller: ").append(entry.getKey())
                                .append(" → $").append(String.format("%.2f", entry.getValue())).append("\n");
                    }

                    message.append("\nTotal Revenue: $").append(String.format("%.2f", totalRevenue));
                    message.append("\nAdmin Profit (10%): $").append(String.format("%.2f", adminProfit));

                    revenueReportText.setText(message.toString());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load revenue: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
