package com.example.furniturestore.customer;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.furniturestore.R;

/**
 * NOTE: This activity is currently not used.
 * You may safely delete it if MainActivity handles all functionality.
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // This Activity is unused as MainActivity is now the entry point.
        // You can repurpose or delete this file.
    }
}
