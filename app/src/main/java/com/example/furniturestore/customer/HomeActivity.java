package com.example.furniturestore.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.auth.LoginActivity;
import com.example.furniturestore.models.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;

    private Button loginBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.furnitureRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loginBtn = findViewById(R.id.buttonLogin);
        loginBtn.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        });

        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList,
                product -> showUpdateDialog(product),
                product -> deleteProduct(product),
                true  // <-- Added this boolean argument
        );
        recyclerView.setAdapter(adapter);

        loadFurnitureItems();
    }

    private void loadFurnitureItems() {
        db.collection("furniture")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    productList.clear();
                    Log.d(TAG, "Documents fetched: " + querySnapshot.size());
                    for (DocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            product.setId(doc.getId());
                            productList.add(product);
                            Log.d(TAG, "Added product: " + product.getName());
                        } else {
                            Log.w(TAG, "Null product object from document: " + doc.getId());
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Loaded products: " + productList.size(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading furniture items", e);
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteProduct(Product product) {
        db.collection("furniture").document(product.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                    loadFurnitureItems();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Delete failed", e);
                });
    }

    private void showUpdateDialog(Product product) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_product, null);

        EditText inputName = dialogView.findViewById(R.id.editTextProductName);
        Spinner inputCategorySpinner = dialogView.findViewById(R.id.spinnerCategoryUpdate);
        EditText inputImageUrl = dialogView.findViewById(R.id.editTextImageUrl);
        EditText inputPrice = dialogView.findViewById(R.id.editTextPrice);
        EditText inputDescription = dialogView.findViewById(R.id.editTextDescription);

        inputName.setText(product.getName());
        inputImageUrl.setText(product.getImageUrl());
        inputPrice.setText(String.valueOf(product.getPrice()));
        inputDescription.setText(product.getDescription());

        // Define categories for Spinner
        String[] categories = {"TallBoy", "Couch", "Bedside Drawers", "Dining Table",
                "Beds", "Hallway Table", "Chairs", "Cupboard"};

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputCategorySpinner.setAdapter(adapterSpinner);

        // Set spinner selection to current product category
        int selectedIndex = Arrays.asList(categories).indexOf(product.getCategory());
        if (selectedIndex >= 0) {
            inputCategorySpinner.setSelection(selectedIndex);
        }

        new AlertDialog.Builder(this)
                .setTitle("Update Product")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    product.setName(inputName.getText().toString().trim());
                    product.setCategory(inputCategorySpinner.getSelectedItem().toString().trim());
                    product.setImageUrl(inputImageUrl.getText().toString().trim());

                    try {
                        product.setPrice(Double.parseDouble(inputPrice.getText().toString().trim()));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    product.setDescription(inputDescription.getText().toString().trim());

                    db.collection("furniture").document(product.getId())
                            .set(product)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                                loadFurnitureItems();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

