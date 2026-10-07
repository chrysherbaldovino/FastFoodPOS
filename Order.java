package FastFoodPOS;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private String status;
    private String orderType;
    
    private double subtotal;
    private double finalTotal;
    private double cashTendered;
    private double change;
    
    private String paymentMethod;
    private String discountType;
    private double discountRate;
    private List<MenuItem> items;

    public Order(int id, String orderType) {
        this.id = id;
        this.orderType = orderType;
        this.status = "Ordering";
        this.subtotal = 0.0;
        this.items = new ArrayList<>();
        this.paymentMethod = "Unpaid";
        this.discountType = "None";
        this.discountRate = 0.0;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<MenuItem> getItems() { return items; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String method) { this.paymentMethod = method; }
    
    public double getFinalTotal() { return finalTotal; }
    public double getCashTendered() { return cashTendered; }
    public void setCashTendered(double cashTendered) { this.cashTendered = cashTendered; }
    public String getOrderType() { return orderType; }
    
    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }
    
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }
    
    public void applyDiscount(String type, double rate) {
        this.discountType = type;
        this.discountRate = rate;
        calculateTotals();
    }

    public void addItem(MenuItem item) {
        items.add(item);
        calculateTotals(); 
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            calculateTotals();
        }
    }

    private void calculateTotals() {
        subtotal = 0.0;
        for (MenuItem item : items) {
            subtotal += item.getPrice();
        }
        double discountAmount = subtotal * discountRate;
        finalTotal = subtotal - discountAmount;
    }
    
    public void processPayment(double tendered) {
        this.cashTendered = tendered;
        this.change = tendered - finalTotal;
        this.status = "Preparing";
    }
    
    public String getCartPreview() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order Type: ").append(orderType).append("\n\n");
        for (MenuItem item : items) {
            sb.append(item.getName()).append("\n");
        }
        sb.append("\nSubtotal: ₱").append(String.format("%.2f", subtotal));
        if (discountRate > 0) {
            sb.append("\n").append(discountType).append(" Discount: -₱").append(String.format("%.2f", subtotal * discountRate));
        }
        sb.append("\nTotal: ₱").append(String.format("%.2f", finalTotal));
        return sb.toString();
    }
    
    public String generateReceipt() {
        StringBuilder r = new StringBuilder();
        r.append("==========================\n");
        r.append("       FAST FOOD POS      \n");
        r.append("==========================\n");
        r.append("Order #").append(id).append("\n");
        r.append("Type: ").append(orderType).append("\n");
        r.append("Payment: ").append(paymentMethod).append("\n");
        
        r.append("--------------------------\n");
        for (MenuItem item : items) {
            r.append(String.format("%-18s %7.2f\n", item.getName(), item.getPrice()));
        }
        r.append("--------------------------\n");
        r.append(String.format("%-18s %7.2f\n", "Subtotal:", subtotal));
        if (discountRate > 0) {
            r.append(String.format("%-18s %7.2f\n", discountType + " Disc:", -(subtotal * discountRate)));
        }
        r.append(String.format("%-18s %7.2f\n", "FINAL TOTAL:", finalTotal));
        r.append("--------------------------\n");
        
        if (paymentMethod.equals("CASH")) {
            r.append(String.format("%-18s %7.2f\n", "Cash Tendered:", cashTendered));
            r.append(String.format("%-18s %7.2f\n", "Change:", change));
        } else if (paymentMethod.equals("CARD")) {
            r.append("Card Auth: APPROVED\n");
        } else if (paymentMethod.equals("E-WALLET")) { 
            r.append("QR Scan: VERIFIED\n");
        }
        r.append("==========================\n");
        return r.toString();
    }
    
    @Override
    public String toString() {
        return "Order #" + id + " [" + orderType + "] (" + status + ") - ₱" + String.format("%.2f", finalTotal);
    }
}