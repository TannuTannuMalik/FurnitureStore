package com.example.furniturestore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.auth.LoginActivity;
import com.example.furniturestore.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView furnitureRecyclerView;
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        furnitureRecyclerView = findViewById(R.id.furnitureRecyclerView);
        buttonLogin = findViewById(R.id.buttonLogin);

        furnitureRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList, product -> {}, product -> {});
        furnitureRecyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();

        // Load public products
        loadProducts();

        // Login Button
        buttonLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading products", e);
                });
    }
}
