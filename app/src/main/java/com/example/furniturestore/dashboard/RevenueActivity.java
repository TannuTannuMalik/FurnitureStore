package com.example.furniturestore.dashboard;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class RevenueActivity extends AppCompatActivity {

    private TextView revenueReportText;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        revenueReportText = findViewById(R.id.revenueReportText);
        ImageView buttonGoBack = findViewById(R.id.buttonGoBack);
        buttonGoBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadSellerRevenue();
    }

    private void loadSellerRevenue() {
        String sellerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (sellerId == null) {
            Toast.makeText(this, "Seller not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("checkouts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double totalRevenue = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<Map<String, Object>> products = (List<Map<String, Object>>) doc.get("products");

                        if (products != null) {
                            for (Map<String, Object> product : products) {
                                String pid = String.valueOf(product.get("sellerId"));
                                if (sellerId.equals(pid)) {
                                    double price = ((Number) product.get("price")).doubleValue();
                                    int qty = ((Number) product.get("quantity")).intValue();
                                    totalRevenue += price * qty;
                                }
                            }
                        }
                    }

                    if (totalRevenue == 0) {
                        revenueReportText.setText("No revenue found. You may not have any completed checkouts yet.");
                        return;
                    }

                    double adminCut = totalRevenue * 0.10;
                    double sellerProfit = totalRevenue - adminCut;

                    String message = "Your Revenue Summary:\n\n"
                            + "Total Sales: $" + String.format("%.2f", totalRevenue) + "\n"
                            + "Admin Cut (10%): $" + String.format("%.2f", adminCut) + "\n"
                            + "Your Profit: $" + String.format("%.2f", sellerProfit);

                    revenueReportText.setText(message);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load revenue: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
