package com.example.furniturestore.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.furniturestore.models.CartItem;

import java.util.List;

@Dao
public interface CartDao {

    @Insert
    void insertCartItem(CartItem item);

    @Update
    void updateCartItem(CartItem item);

    @Delete
    void deleteCartItem(CartItem item);

    @Query("SELECT * FROM cart_items")
    List<CartItem> getAllCartItems();

    @Query("DELETE FROM cart_items")
    void clearCart();
}
