package com.example.ims001;

public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getProductId() { return product.getId(); }
    public String getName() { return product.getName(); }
    public double getUnitPrice() { return product.getPrice(); }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getLineTotal() {
        return product.getPrice() * quantity;
    }
}
