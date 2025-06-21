package com.example.furniturestore.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.furniturestore.R;
import com.example.furniturestore.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private final OnProductClickListener onProductClickListener;
    private final OnProductDeleteListener onProductDeleteListener;
    private final boolean isAdminOrSeller;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnProductDeleteListener {
        void onProductDelete(Product product);
    }

    public ProductAdapter(List<Product> productList,
                          OnProductClickListener clickListener,
                          OnProductDeleteListener deleteListener,
                          boolean isAdminOrSeller) {
        this.productList = productList;
        this.onProductClickListener = clickListener;
        this.onProductDeleteListener = deleteListener;
        this.isAdminOrSeller = isAdminOrSeller;
    }

    public void updateList(List<Product> updatedList) {
        productList.clear();
        productList.addAll(updatedList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productCategory.setText(product.getCategory());
        holder.productPrice.setText("$" + product.getPrice());
        holder.productQuantity.setText("Stock: " + product.getQuantity());
        if (product.getCreatedAt() != null) {
            holder.productCreatedAt.setText("Created: " + product.getCreatedAt());
        } else {
            holder.productCreatedAt.setText("Created: N/A");
        }

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                //.placeholder(R.drawable.placeholder)
                .into(holder.productImage);

        holder.itemView.setOnClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onProductClick(product);
            }
        });

        if (isAdminOrSeller) {
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonDelete.setOnClickListener(v -> {
                if (onProductDeleteListener != null) {
                    onProductDeleteListener.onProductDelete(product);
                }
            });
        } else {
            holder.buttonDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productCategory, productPrice, productCreatedAt, productQuantity;
        Button buttonDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productCategory = itemView.findViewById(R.id.productCategory);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCreatedAt = itemView.findViewById(R.id.productCreatedAt);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
