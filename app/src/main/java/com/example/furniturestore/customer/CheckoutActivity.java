package com.example.furniturestore.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;
import com.example.furniturestore.auth.LoginActivity;
import com.example.furniturestore.database.AppDatabase;
import com.example.furniturestore.models.CartItem;
import com.example.furniturestore.models.OrderItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextCity;
    private Button buttonPlaceOrder, buttonLogout;
    private ImageButton buttonBack;

    private final List<OrderItem> cartItems = new ArrayList<>();
    private AppDatabase db;

    private static final String DEFAULT_SELLER_ID = "D7KYKRmEHmfSS0VP9yRp8241OoB3";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextCity = findViewById(R.id.editTextCity);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonBack = findViewById(R.id.buttonBack);

        db = AppDatabase.getInstance(getApplicationContext());

        loadCartItems();

        buttonBack.setOnClickListener(v -> finish());

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        buttonPlaceOrder.setOnClickListener(v -> {
            if (validateInputs()) {
                placeOrder();
            }
        });
    }

    private void loadCartItems() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        new Thread(() -> {
            List<CartItem> cartItemList = db.cartDao().getCartItemsForUser(userId);
            cartItems.clear();
            for (CartItem c : cartItemList) {
                Log.d("CheckoutDebug", "CartItem SellerID: " + c.getSellerId());

                String sellerId = c.getSellerId();
                if (sellerId == null || sellerId.trim().isEmpty()) {
                    sellerId = DEFAULT_SELLER_ID;
                }
                cartItems.add(new OrderItem(
                        c.getProductId(),
                        c.getProductName(),
                        c.getQuantity(),
                        c.getPrice(),
                        c.getImageUrl(),
                        sellerId
                ));
            }
        }).start();
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(editTextName.getText())) {
            editTextName.setError("Enter your name");
            return false;
        }
        if (TextUtils.isEmpty(editTextPhone.getText())) {
            editTextPhone.setError("Enter phone number");
            return false;
        }
        if (TextUtils.isEmpty(editTextCity.getText())) {
            editTextCity.setError("Enter city");
            return false;
        }
        return true;
    }

    private void placeOrder() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String city = editTextCity.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long orderTimestamp = System.currentTimeMillis();

        List<Map<String, Object>> productList = new ArrayList<>();
        for (OrderItem item : cartItems) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("productId", item.getProductId());
            productMap.put("name", item.getName());
            productMap.put("quantity", item.getQuantity());
            productMap.put("price", item.getPrice());
            productMap.put("imageUrl", item.getImageUrl());

            String sellerId = item.getSellerId();
            if (sellerId == null || sellerId.trim().isEmpty()) {
                sellerId = DEFAULT_SELLER_ID;
            }
            productMap.put("sellerId", sellerId);

            productList.add(productMap);
        }

        Map<String, Object> checkoutData = new HashMap<>();
        checkoutData.put("name", name);
        checkoutData.put("phone", phone);
        checkoutData.put("city", city);
        checkoutData.put("products", productList);
        checkoutData.put("orderTimestamp", orderTimestamp);
        checkoutData.put("userId", userId);

        FirebaseFirestore.getInstance()
                .collection("checkouts")
                .add(checkoutData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Order placed!", Toast.LENGTH_LONG).show();
                    clearCart();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void clearCart() {
        cartItems.clear();
        new Thread(() -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.cartDao().clearCartForUser(userId);
        }).start();
    }
}
