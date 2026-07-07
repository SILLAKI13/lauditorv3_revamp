package com.digicoffer.lauditor.Invoice.Models;


public class LineItemModel {

    private String name        = "";   // description / item name
    private double unitPrice   = 0;    // rate
    private int    quantity    = 1;    // quantity

    public LineItemModel() {}

    public LineItemModel(String name, double unitPrice, int quantity) {
        this.name      = name;
        this.unitPrice = unitPrice;
        this.quantity  = quantity;
    }

    // Computed helper
    public double getAmount() {
        return unitPrice * quantity;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getName()      { return name; }
    public double getUnitPrice() { return unitPrice; }
    public int    getQuantity()  { return quantity; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setName(String name)          { this.name      = name; }
    public void setUnitPrice(double unitPrice){ this.unitPrice = unitPrice; }
    public void setQuantity(int quantity)     { this.quantity  = quantity; }
}