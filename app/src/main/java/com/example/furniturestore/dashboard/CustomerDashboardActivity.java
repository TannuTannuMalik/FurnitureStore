package com.example.furniturestore.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.customer.CartActivity;
import com.example.furniturestore.models.Product;
import com.example.furniturestore.customer.ProductDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CustomerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewCustomer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(productList,
                this::openProductDetailActivity,
                null); // No add-to-cart click here

        recyclerView.setAdapter(adapter);

        loadAllProducts();
        Button buttonCart = findViewById(R.id.buttonCart);
        buttonCart.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, CartActivity.class);
            startActivity(intent);
        });

    }

    private void loadAllProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(snapshot -> {
                    productList.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                });
    }

    private void openProductDetailActivity(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("productName", product.getName());
        intent.putExtra("productCategory", product.getCategory());
        intent.putExtra("productPrice", product.getPrice());
        intent.putExtra("productDescription", product.getDescription());
        intent.putExtra("productImageUrl", product.getImageUrl());
        startActivity(intent);
    }
}
