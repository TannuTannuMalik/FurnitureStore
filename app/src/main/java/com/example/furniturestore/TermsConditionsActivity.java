package com.example.furniturestore;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TermsConditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions);

        View rootLayout = findViewById(R.id.rootLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rootLayout.setOnApplyWindowInsetsListener((v, insets) -> {
                int statusBarHeight = insets.getSystemWindowInsetTop();
                v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
                return insets.consumeSystemWindowInsets();
            });
            rootLayout.requestApplyInsets();
        }

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> finish());

        TextView termsTextView = findViewById(R.id.textViewTermsContent);
        termsTextView.setText("Welcome to Recozy Living Furniture!\n\n" +
                "By using our services, you agree to the following terms and conditions:\n\n" +
                "1. All products are subject to availability.\n" +
                "2. Prices and specifications are subject to change without notice.\n" +
                "3. Warranty claims must be made within the specified warranty period.\n" +
                "4. Returns and exchanges are governed by our return policy.\n\n" +
                "Thank you for choosing Recozy Living Furniture. We are committed to providing quality products and excellent customer service.");
    }
}
