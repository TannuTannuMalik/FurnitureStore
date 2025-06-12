package com.example.furniturestore.customer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.example.furniturestore.adapters.CartAdapter;
import com.example.furniturestore.database.AppDatabase;
import com.example.furniturestore.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();
    private static final String TAG = "CartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItemList);
        recyclerView.setAdapter(cartAdapter);

        loadCartItems();
        Button buttonCheckout = findViewById(R.id.buttonCheckout);
        buttonCheckout.setOnClickListener(v -> handleCheckout());
    }

    private void handleCheckout() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to proceed with checkout", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<CartItem> cartItems = db.cartDao().getAllCartItems();

            if (cartItems.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show());
                return;
            }

            List<Map<String, Object>> cartData = new ArrayList<>();
            for (CartItem item : cartItems) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", item.getProductId());
                itemMap.put("name", item.getName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getPrice());
                itemMap.put("imageUrl", item.getImageUrl());
                cartData.add(itemMap);
            }

            Map<String, Object> order = new HashMap<>();
            order.put("userId", user.getUid());
            order.put("userEmail", user.getEmail());
            order.put("items", cartData);
            order.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("orders")
                    .add(order)
                    .addOnSuccessListener(documentReference -> {
                        // Clear cart on a background thread
                        new Thread(() -> {
                            db.cartDao().clearCart();
                        }).start();

                        runOnUiThread(() ->
                                Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show()
                        );
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() ->
                                Toast.makeText(CartActivity.this, "Checkout failed. Try again.", Toast.LENGTH_LONG).show()
                        );
                    });

        }).start();
    }

    private void loadCartItems() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<CartItem> items = db.cartDao().getAllCartItems();
            runOnUiThread(() -> {
                if (items != null && !items.isEmpty()) {
                    cartItemList.clear();
                    cartItemList.addAll(items);
                    cartAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CartActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
