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

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Product product);
    }

    private final List<Product> productList;
    private final OnItemClickListener itemClickListener;
    private final OnDeleteClickListener deleteClickListener;

    public ProductAdapter(List<Product> productList,
                          OnItemClickListener itemClickListener,
                          OnDeleteClickListener deleteClickListener) {
        this.productList = productList;
        this.itemClickListener = itemClickListener;
        this.deleteClickListener = deleteClickListener;
    }
    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
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
        holder.bind(product, itemClickListener, deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final TextView productName;
        private final TextView productCategory;
        private final TextView productPrice;
        private final ImageView productImage;
        private final Button buttonDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productCategory = itemView.findViewById(R.id.productCategory);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImage = itemView.findViewById(R.id.productImage);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(Product product, OnItemClickListener clickListener, OnDeleteClickListener deleteListener) {
            productName.setText(product.getName());
            productCategory.setText(product.getCategory());
            productPrice.setText(String.format("$%.2f", product.getPrice()));

            Glide.with(productImage.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(productImage);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(product);
            });

            buttonDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDeleteClick(product);
            });
        }
    }
}
