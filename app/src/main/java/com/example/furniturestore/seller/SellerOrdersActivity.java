package com.example.furniturestore.seller;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.example.furniturestore.adapters.SellerOrderAdapter;
import com.example.furniturestore.models.OrderItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SellerOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SellerOrderAdapter adapter;
    private final String defaultSellerId = "D7KYKRmEHmfSS0VP9yRp8241OoB3";  // Fallback default seller

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_orders);

        recyclerView = findViewById(R.id.recyclerViewSellerOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadOrdersWithFallback();
    }

    private void loadOrdersWithFallback() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        String sellerId = user.getUid();
        loadOrdersForSeller(sellerId, true);
    }

    private void loadOrdersForSeller(String sellerId, boolean tryFallback) {
        FirebaseFirestore.getInstance()
                .collection("checkouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OrderItem> orderItems = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        List<Map<String, Object>> products = (List<Map<String, Object>>) doc.get("products");

                        // Get buyer info from the order document itself
                        String buyerName = doc.getString("name");
                        String buyerPhone = doc.getString("phone");
                        String buyerCity = doc.getString("city");

                        if (products != null) {
                            for (Map<String, Object> map : products) {
                                String itemSellerId = (String) map.get("sellerId");

                                if (sellerId.equals(itemSellerId)) {
                                    orderItems.add(new OrderItem(
                                            (String) map.get("productId"),
                                            (String) map.get("name"),
                                            ((Long) map.get("quantity")).intValue(),
                                            ((Number) map.get("price")).doubleValue(),
                                            (String) map.get("imageUrl"),
                                            itemSellerId,
                                            buyerName,
                                            buyerPhone,
                                            buyerCity
                                    ));
                                }
                            }
                        }
                    }

                    if (orderItems.isEmpty() && tryFallback && !sellerId.equals(defaultSellerId)) {
                        // Retry with default seller ID if no orders found for current seller
                        loadOrdersForSeller(defaultSellerId, false);
                    } else {
                        adapter = new SellerOrderAdapter(this, orderItems);
                        recyclerView.setAdapter(adapter);
                        if (orderItems.isEmpty()) {
                            Toast.makeText(this, "No orders available.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
