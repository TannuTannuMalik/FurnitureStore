package com.example.furniturestore.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.MainActivity;
import com.example.furniturestore.R;
import com.example.furniturestore.adapters.CustomerOrderAdapter;
import com.example.furniturestore.models.OrderItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomerOrderAdapter adapter;
    private final List<OrderItem> orderList = new ArrayList<>();
    private ImageView backIcon;
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        backIcon = findViewById(R.id.backIcon);
        buttonLogout = findViewById(R.id.buttonLogout);

        loadOrderHistory();
        setupHeaderActions();
    }

    private void setupHeaderActions() {
        // Back button click
        backIcon.setOnClickListener(v -> finish());

        // Logout button click
        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(OrderHistoryActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadOrderHistory() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("checkouts")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        List<Map<String, Object>> products = (List<Map<String, Object>>) doc.get("products");

                        if (products != null) {
                            for (Map<String, Object> map : products) {
                                orderList.add(new OrderItem(
                                        (String) map.get("productId"),
                                        (String) map.get("name"),
                                        ((Long) map.get("quantity")).intValue(),
                                        ((Number) map.get("price")).doubleValue(),
                                        (String) map.get("imageUrl"),
                                        (String) map.get("sellerId"),
                                        (String) doc.get("name"),
                                        (String) doc.get("phone"),
                                        (String) doc.get("city")
                                ));
                            }
                        }
                    }

                    adapter = new CustomerOrderAdapter(this, orderList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching orders", Toast.LENGTH_SHORT).show());
    }
}
