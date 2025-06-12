package com.example.furniturestore.customer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.furniturestore.R;
import com.example.furniturestore.database.AppDatabase;
import com.example.furniturestore.models.CartItem;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView nameText, categoryText, priceText, descriptionText;
    private Button addToCartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        imageView = findViewById(R.id.imageViewProductDetail);
        nameText = findViewById(R.id.textViewProductNameDetail);
        categoryText = findViewById(R.id.textViewProductCategoryDetail);
        priceText = findViewById(R.id.textViewProductPriceDetail);
        descriptionText = findViewById(R.id.textViewProductDescriptionDetail);
        addToCartBtn = findViewById(R.id.buttonAddToCart);

        String productId = getIntent().getStringExtra("productId");
        String name = getIntent().getStringExtra("productName");
        String category = getIntent().getStringExtra("productCategory");
        double price = getIntent().getDoubleExtra("productPrice", 0.0);
        String description = getIntent().getStringExtra("productDescription");
        String imageUrl = getIntent().getStringExtra("productImageUrl");

        nameText.setText(name);
        categoryText.setText(category);
        priceText.setText(String.format("$%.2f", price));
        descriptionText.setText(description);
        Glide.with(this).load(imageUrl).into(imageView);

        addToCartBtn.setOnClickListener(v -> {
            CartItem cartItem = new CartItem(
                    productId,
                    name,
                    1, // default quantity
                    price,
                    imageUrl
            );

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                db.cartDao().insertCartItem(cartItem);
                runOnUiThread(() -> Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show());
            }).start();
        });
    }
}
