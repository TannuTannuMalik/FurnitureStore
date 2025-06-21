package com.example.furniturestore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.furniturestore.R;
import com.example.furniturestore.models.OrderItem;

import java.util.List;

public class SellerOrderAdapter extends RecyclerView.Adapter<SellerOrderAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderItem> orderItems;

    private static final String DEFAULT_SELLER_ID = "D7KYKRmEHmfSS0VP9yRp8241OoB3";

    public SellerOrderAdapter(Context context, List<OrderItem> orderItems) {
        this.context = context;
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public SellerOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seller_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerOrderAdapter.ViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);

        holder.productName.setText(item.getName());
        holder.productPrice.setText("Price: $" + item.getPrice());
        holder.productQuantity.setText("Qty: " + item.getQuantity());

        String sellerId = item.getSellerId();
        if (sellerId == null || sellerId.trim().isEmpty()) {
            sellerId = DEFAULT_SELLER_ID;
        }

        // Optional: display sellerId if you want to add TextView for it
        // holder.sellerIdTextView.setText(sellerId);

        Glide.with(context).load(item.getImageUrl()).into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;
        ImageView productImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.textProductName);
            productPrice = itemView.findViewById(R.id.textProductPrice);
            productQuantity = itemView.findViewById(R.id.textProductQuantity);
            productImage = itemView.findViewById(R.id.imageProduct);
        }
    }
}