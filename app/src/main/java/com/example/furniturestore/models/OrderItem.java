package com.example.furniturestore.models;

public class OrderItem {

    private String productId;
    private String name;
    private int quantity;
    private double price;
    private String imageUrl;
    private String sellerId;

    // Buyer info fields
    private String buyerName;
    private String buyerPhone;
    private String buyerCity;

    public OrderItem() {
        // Required for Firestore
    }

    // Constructor without buyer info (for cart items)
    public OrderItem(String productId, String name, int quantity, double price, String imageUrl, String sellerId) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.sellerId = sellerId;
        this.buyerName = null;
        this.buyerPhone = null;
        this.buyerCity = null;
    }

    // Constructor with buyer info (for orders)
    public OrderItem(String productId, String name, int quantity, double price, String imageUrl, String sellerId,
                     String buyerName, String buyerPhone, String buyerCity) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.sellerId = sellerId;
        this.buyerName = buyerName;
        this.buyerPhone = buyerPhone;
        this.buyerCity = buyerCity;
    }

    // Getters and setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }

    public String getBuyerCity() { return buyerCity; }
    public void setBuyerCity(String buyerCity) { this.buyerCity = buyerCity; }
}
