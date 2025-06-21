package com.example.furniturestore.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.furniturestore.models.CartItem;

import java.util.List;

@Dao
public interface CartDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCartItem(CartItem item);

    @Update
    void updateCartItem(CartItem item);

    @Delete
    void deleteCartItem(CartItem item);

    // Load cart items only for this user
    @Query("SELECT * FROM cart WHERE userId = :userId")
    List<CartItem> getCartItemsForUser(String userId);

    // Clear cart for specific user
    @Query("DELETE FROM cart WHERE userId = :userId")
    void clearCartForUser(String userId);
}
