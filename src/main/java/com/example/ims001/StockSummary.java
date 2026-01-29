package com.example.ims001;

public class StockSummary {
    private final int total;
    private final int inStock;
    private final int lowStock;
    private final int outStock;

    public StockSummary(int total, int inStock, int lowStock, int outStock) {
        this.total = total;
        this.inStock = inStock;
        this.lowStock = lowStock;
        this.outStock = outStock;
    }

    public int getTotal() { return total; }
    public int getInStock() { return inStock; }
    public int getLowStock() { return lowStock; }
    public int getOutStock() { return outStock; }
}
