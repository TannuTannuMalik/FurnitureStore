package com.example.furniturestore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.auth.LoginActivity;
import com.example.furniturestore.customer.CartActivity;
import com.example.furniturestore.customer.ProductDetailActivity;
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
    private Button buttonCart;  // Added buttonCart here
    private SearchView searchView;
    private ImageView homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        furnitureRecyclerView = findViewById(R.id.furnitureRecyclerView);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCart = findViewById(R.id.buttonCart);  // Initialize here
        searchView = findViewById(R.id.searchView);
        homeIcon = findViewById(R.id.homeIcon);

        furnitureRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList,
                product -> {
                    Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                    intent.putExtra("productId", product.getId());
                    intent.putExtra("productName", product.getName());
                    intent.putExtra("productCategory", product.getCategory());
                    intent.putExtra("productPrice", product.getPrice());
                    intent.putExtra("productDescription", product.getDescription());
                    intent.putExtra("productImageUrl", product.getImageUrl());
                    startActivity(intent);
                },
                null // no delete button needed
        );
        furnitureRecyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();
        loadProducts();

        buttonLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        buttonCart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        homeIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
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
                    productAdapter.updateList(new ArrayList<>(productList));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading products", e);
                });
    }

    private void filterProducts(String query) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : productList) {
            if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(p);
            }
        }
        productAdapter.updateList(filtered);
    }
}
