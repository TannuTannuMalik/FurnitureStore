package com.example.furniturestore.dashboard;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class RevenueActivity extends AppCompatActivity {

    private TextView revenueReportText;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private static final String DEFAULT_SELLER_ID = "D7KYKRmEHmfSS0VP9yRp8241OoB3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        revenueReportText = findViewById(R.id.revenueReportText);
        ImageView buttonGoBack = findViewById(R.id.buttonGoBack);
        buttonGoBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadDefaultSellerRevenue();
    }

    private void loadDefaultSellerRevenue() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No seller logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String displayId = user.getEmail() != null ? user.getEmail() : user.getUid();

        db.collection("checkouts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double totalRevenue = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<Map<String, Object>> products = (List<Map<String, Object>>) doc.get("products");

                        if (products != null) {
                            for (Map<String, Object> product : products) {
                                String sellerId = String.valueOf(product.get("sellerId"));
                                if (DEFAULT_SELLER_ID.equals(sellerId)) {
                                    double price = ((Number) product.get("price")).doubleValue();
                                    int qty = ((Number) product.get("quantity")).intValue();
                                    totalRevenue += price * qty;
                                }
                            }
                        }
                    }

                    if (totalRevenue == 0) {
                        revenueReportText.setText("No revenue found for default seller.");
                        return;
                    }

                    double adminCut = totalRevenue * 0.10;
                    double sellerProfit = totalRevenue - adminCut;

                    String message = "Revenue for Logged-in Seller (" + displayId + "):\n\n"
                            + "Total Sales (default seller): $" + String.format("%.2f", totalRevenue) + "\n"
                            + "Admin Cut (10%): $" + String.format("%.2f", adminCut) + "\n"
                            + "Seller Profit: $" + String.format("%.2f", sellerProfit);

                    revenueReportText.setText(message);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load revenue: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
