package com.example.furniturestore.seller;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {

    private EditText editName, editPrice, editDescription, editQuantity, editCategory;
    private Button buttonUpdate;

    private FirebaseFirestore db;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        editName = findViewById(R.id.editProductName);
        editPrice = findViewById(R.id.editProductPrice);
        editDescription = findViewById(R.id.editProductDescription);
        editQuantity = findViewById(R.id.editProductQuantity);
        editCategory = findViewById(R.id.editProductCategory);
        buttonUpdate = findViewById(R.id.buttonUpdateProduct);

        db = FirebaseFirestore.getInstance();
        productId = getIntent().getStringExtra("productId");

        if (productId == null) {
            Toast.makeText(this, "No product ID passed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductDetails();

        buttonUpdate.setOnClickListener(v -> updateProduct());
    }

    private void loadProductDetails() {
        db.collection("products").document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        editName.setText(doc.getString("name"));
                        editPrice.setText(String.valueOf(doc.getDouble("price")));
                        editDescription.setText(doc.getString("description"));
                        editQuantity.setText(String.valueOf(doc.getLong("quantity")));
                        editCategory.setText(doc.getString("category"));
                    } else {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateProduct() {
        String name = editName.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String quantityStr = editQuantity.getText().toString().trim();
        String category = editCategory.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)
                || TextUtils.isEmpty(description) || TextUtils.isEmpty(quantityStr)
                || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int quantity = Integer.parseInt(quantityStr);

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", name);
        updatedData.put("price", price);
        updatedData.put("description", description);
        updatedData.put("quantity", quantity);
        updatedData.put("category", category);

        db.collection("products").document(productId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
