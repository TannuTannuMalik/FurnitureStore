package com.example.furniturestore.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.furniturestore.R;
import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.models.Product;
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
                this::showProductDetailDialog,
                null); // No delete button for customers

        recyclerView.setAdapter(adapter);

        loadAllProducts();
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

    private void showProductDetailDialog(Product product) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_product_detail, null);

        ImageView imageView = view.findViewById(R.id.imageViewProductDetail);
        TextView name = view.findViewById(R.id.textViewProductNameDetail);
        TextView category = view.findViewById(R.id.textViewProductCategoryDetail);
        TextView price = view.findViewById(R.id.textViewProductPriceDetail);
        TextView description = view.findViewById(R.id.textViewProductDescriptionDetail);
        Button addToCartBtn = view.findViewById(R.id.buttonAddToCart);

        name.setText(product.getName());
        category.setText(product.getCategory());
        price.setText(String.format("$%.2f", product.getPrice()));
        description.setText(product.getDescription());

        Glide.with(this).load(product.getImageUrl()).into(imageView);

        addToCartBtn.setOnClickListener(v -> {
            // TODO: Implement Add to Cart logic
            Toast.makeText(this, "Added to cart (not yet implemented)", Toast.LENGTH_SHORT).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Product Details")
                .setView(view)
                .setNegativeButton("Close", null)
                .show();
    }
}
