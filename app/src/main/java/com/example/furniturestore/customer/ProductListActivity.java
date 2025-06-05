package com.example.furniturestore.customer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.models.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        productList = new ArrayList<>();

        // Create adapter with listeners
        adapter = new ProductAdapter(productList,
                product -> {
                    // On item click
                    Toast.makeText(ProductListActivity.this, "Clicked: " + product.getName(), Toast.LENGTH_SHORT).show();
                },
                product -> {
                    // On delete click
                    Toast.makeText(ProductListActivity.this, "Deleted: " + product.getName(), Toast.LENGTH_SHORT).show();
                    productList.remove(product);
                    adapter.notifyDataSetChanged();
                });

        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        loadProductsFromFirestore();
    }

    private void loadProductsFromFirestore() {
        firestore.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setId(doc.getId());
                                productList.add(product);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load products.", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error: ", task.getException());
                    }
                });
    }
}
