package com.example.furniturestore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.example.furniturestore.models.CartItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartItemListener {
        void onQuantityChanged();
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
        void onRemoveItem(CartItem item);
    }

    private Context context;
    private List<CartItem> cartItems;
    private CartItemListener listener;

    public CartAdapter(Context context, List<CartItem> cartItems, CartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.textName.setText(item.getProductName());
        holder.textPrice.setText(String.format("$%.2f", item.getPrice()));
        holder.textQuantity.setText(String.valueOf(item.getQuantity()));
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(android.R.drawable.ic_menu_report_image)
                            .error(android.R.drawable.ic_delete))
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }



        holder.btnIncrease.setOnClickListener(v -> {
            listener.onIncreaseQuantity(item);
            listener.onQuantityChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            listener.onDecreaseQuantity(item);
            listener.onQuantityChanged();
        });

        holder.btnRemove.setOnClickListener(v -> {
            listener.onRemoveItem(item);
            listener.onQuantityChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textPrice, textQuantity;
        ImageView imageView;
        Button btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textViewProductName);
            textPrice = itemView.findViewById(R.id.textViewProductPrice);
            textQuantity = itemView.findViewById(R.id.textViewQuantity);
            imageView = itemView.findViewById(R.id.imageViewProduct);
            btnIncrease = itemView.findViewById(R.id.buttonIncrease);
            btnDecrease = itemView.findViewById(R.id.buttonDecrease);
            btnRemove = itemView.findViewById(R.id.buttonRemove);
        }
    }
}
