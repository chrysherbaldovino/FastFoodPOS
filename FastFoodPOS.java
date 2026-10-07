package FastFoodPOS;

import javax.swing.*;
import java.awt.*;

public class FastFoodPOS {
    private static Order currentOrder;
    private static JTextArea cartArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> openCustomerWindow());
    }

    private static void openCustomerWindow() {
        JFrame frame = new JFrame("Customer Self-Ordering Kiosk");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 650); 
        frame.setLayout(new BorderLayout());
        
        JPanel menuPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        cartArea = new JTextArea();
        cartArea.setEditable(false);
        cartArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        cartArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane cartScroll = new JScrollPane(cartArea);
        cartScroll.setPreferredSize(new Dimension(300, 500));

        // Buttons remain exactly as requested
        menuPanel.add(createCategoryButton(frame, "🍔 Burgers", new MenuItem[]{
            new MenuItem(1, "Single Cheeseburger", 85.00, "🍔"),
            new MenuItem(2, "Double Cheeseburger", 125.00, "🍔"),
            new MenuItem(3, "Bacon Mushroom Melt", 145.00, "🥓"),
            new MenuItem(4, "Crispy Chicken Burger", 110.00, "🍔"),
            new MenuItem(5, "Spicy Chicken Burger", 120.00, "🌶️"),
            new MenuItem(6, "Plant-Based Veggie Burger", 150.00, "🌱")
        }));
        
        menuPanel.add(createCategoryButton(frame, "🍟 Sides", new MenuItem[]{
            new MenuItem(7, "Small Fries", 50.00, "🍟"),
            new MenuItem(8, "Medium Fries", 65.00, "🍟"),
            new MenuItem(9, "Large Fries", 80.00, "🍟"),
            new MenuItem(10, "Crispy Hashbrown", 45.00, "🥔"),
            new MenuItem(11, "Onion Rings (6pc)", 75.00, "🧅"),
            new MenuItem(12, "Loaded Cheese Nachos", 95.00, "🧀")
        }));

        menuPanel.add(createCategoryButton(frame, "🍹 Beverages", new MenuItem[]{
            new MenuItem(13, "Small Cola", 35.00, "🍹"),
            new MenuItem(14, "Medium Cola", 45.00, "🍹"),
            new MenuItem(15, "Large Cola", 55.00, "🍹"),
            new MenuItem(16, "Iced Tea (Medium)", 40.00, "🍹"),
            new MenuItem(17, "Orange Juice", 50.00, "🍊"),
            new MenuItem(18, "Iced Coffee", 65.00, "☕"),
            new MenuItem(19, "Bottled Water", 30.00, "💧")
        }));

        menuPanel.add(createCategoryButton(frame, "🍗 Chicken", new MenuItem[]{
            new MenuItem(20, "1pc Chicken (Reg)", 85.00, "🍗"),
            new MenuItem(21, "1pc Chicken (Spicy)", 90.00, "🌶️"),
            new MenuItem(22, "2pc Chicken (Reg)", 160.00, "🍗"),
            new MenuItem(23, "2pc Chicken (Spicy)", 170.00, "🌶️"),
            new MenuItem(24, "Chicken Nuggets (6pc)", 75.00, "🐔"),
            new MenuItem(25, "Chicken Nuggets (9pc)", 105.00, "🐔")
        }));

        menuPanel.add(createCategoryButton(frame, "🍦 Desserts", new MenuItem[]{
            new MenuItem(26, "Vanilla Sundae", 45.00, "🍦"),
            new MenuItem(27, "Chocolate Sundae", 55.00, "🍫"),
            new MenuItem(28, "Strawberry Sundae", 55.00, "🍓"),
            new MenuItem(29, "Peach Mango Pie", 40.00, "🍰"),
            new MenuItem(30, "Apple Pie", 40.00, "🍎"),
            new MenuItem(31, "Fudge Brownie", 35.00, "🍫")
        }));

        menuPanel.add(createCategoryButton(frame, "🍝 Meals", new MenuItem[]{
            new MenuItem(32, "Spaghetti w/ Meat Sauce", 70.00, "🍝"),
            new MenuItem(33, "Spaghetti w/ 1pc Chicken", 145.00, "🍝"),
            new MenuItem(34, "Palabok Fiesta", 85.00, "🍜"),
            new MenuItem(35, "1pc Burger Steak w/ Rice", 75.00, "🍛"),
            new MenuItem(36, "2pc Burger Steak w/ Rice", 110.00, "🍛"),
            new MenuItem(37, "Chicken Fillet Rice Bowl", 95.00, "🍚")
        }));

        JPanel bottomPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        bottomPanel.setPreferredSize(new Dimension(850, 100));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton removeBtn = new JButton("Remove Item");
        removeBtn.setFont(new Font("Arial", Font.BOLD, 16));
        removeBtn.setBackground(new Color(255, 165, 0)); 
        removeBtn.addActionListener(e -> {
            if (currentOrder.getItems().isEmpty()) return;
            Object[] items = currentOrder.getItems().toArray();
            MenuItem toRemove = (MenuItem) JOptionPane.showInputDialog(frame, "Select item to remove:", "Remove Item", 
                    JOptionPane.QUESTION_MESSAGE, null, items, items[0]);
            if (toRemove != null) {
                currentOrder.removeItem(currentOrder.getItems().indexOf(toRemove));
                updateCartUI();
            }
        });

        JButton mysteryBtn = new JButton("Surprise Me!");
        mysteryBtn.setFont(new Font("Arial", Font.BOLD, 16));
        mysteryBtn.setBackground(new Color(255, 215, 0)); 
        mysteryBtn.addActionListener(e -> {
            MenuItem mystery = new MenuItem(99, "Mystery Meal", 150.00, "🎁");
            currentOrder.addItem(mystery);
            updateCartUI();
        });

        JButton cancelBtn = new JButton("Cancel Order");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 16));
        cancelBtn.setBackground(new Color(255, 102, 102));
        cancelBtn.addActionListener(e -> {
            if (currentOrder.getFinalTotal() > 0) {
                if (JOptionPane.showConfirmDialog(frame, "Clear your cart?", "Cancel", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    startNewOrder(frame);
                }
            }
        });

        JButton checkoutBtn = new JButton("Checkout & Pay");
        checkoutBtn.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutBtn.setBackground(new Color(144, 238, 144));
        checkoutBtn.addActionListener(e -> processCheckout(frame));

        bottomPanel.add(removeBtn);
        bottomPanel.add(mysteryBtn);
        bottomPanel.add(cancelBtn);
        bottomPanel.add(checkoutBtn);

        frame.add(menuPanel, BorderLayout.CENTER);
        frame.add(cartScroll, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        
     // Anchors the kiosk exactly to the left edge of the screen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(0, (screen.height - frame.getHeight()) / 2);
        
        // 1. The window becomes visible first
        frame.setVisible(true); 

        // 2. THEN the modal popup appears on top of it
        startNewOrder(frame); 
    }

    private static void startNewOrder(JFrame frame) {
        String[] options = {"Dine-In", "Take-Out"};
        
        // 1. Create the option pane manually instead of using the direct showOptionDialog method
        JOptionPane pane = new JOptionPane("Where will you be eating today?", 
            JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        
        // 2. Create a dialog and explicitly set it to DOCUMENT_MODAL
        JDialog dialog = pane.createDialog(frame, "Order Type");
        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.setVisible(true);
        
        // 3. Retrieve the user's selection after the dialog closes
        Object selectedValue = pane.getValue();
        String type = "Dine-In"; // Default fallback if they click the 'X' button
        
        if (selectedValue != null && selectedValue.equals("Take-Out")) {
            type = "Take-Out";
        }
        
        currentOrder = new Order(0, type);
        if (cartArea != null) updateCartUI();
    }

    private static JButton createCategoryButton(JFrame parent, String title, MenuItem[] variants) {
        JButton btn = new JButton("<html><center><font size='6'>" + title + "</font></center></html>");
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.addActionListener(e -> {
            MenuItem selected = (MenuItem) JOptionPane.showInputDialog(parent, "Select variety:", title, 
                JOptionPane.QUESTION_MESSAGE, null, variants, variants[0]);
            
            if (selected != null) {
                currentOrder.addItem(selected);
                updateCartUI();
            }
        });
        return btn;
    }

    private static void processCheckout(JFrame frame) {
        if (currentOrder.getFinalTotal() <= 0) {
            JOptionPane.showMessageDialog(frame, "Cart is empty!");
            return;
        }

        String[] discounts = {"None", "PWD (20% Off)", "Kid's Meal (10% Off)"};
        String selectedDiscount = (String) JOptionPane.showInputDialog(frame, "Select Discount Profile:", 
            "Discounts", JOptionPane.QUESTION_MESSAGE, null, discounts, discounts[0]);
        
        if (selectedDiscount != null) {
            if (selectedDiscount.contains("PWD")) currentOrder.applyDiscount("PWD", 0.20);
            else if (selectedDiscount.contains("Kid")) currentOrder.applyDiscount("Kid", 0.10);
            updateCartUI();
        }

        String[] options = {"Card (Pay Here)", "E-Wallet (GCash/Maya)", "Cash (Pay at Counter)"};
        int choice = JOptionPane.showOptionDialog(frame, "Total: ₱" + String.format("%.2f", currentOrder.getFinalTotal()) + "\nSelect Payment Method:", 
            "Payment", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) { 
            JOptionPane.showMessageDialog(frame, "Please tap your card on the terminal...");
            currentOrder.setPaymentMethod("CARD");
            currentOrder.processPayment(currentOrder.getFinalTotal()); 
            DatabaseHelper.saveOrder(currentOrder); // SAVES TO DB
            showReceipt(frame, currentOrder.generateReceipt());
            startNewOrder(frame);
        } else if (choice == 1) { 
            JOptionPane.showMessageDialog(frame, "[ 📱 SCAN QR CODE ]\n\nPlease scan the QR code using your GCash/Maya app to pay ₱" + String.format("%.2f", currentOrder.getFinalTotal()));
            currentOrder.setPaymentMethod("E-WALLET");
            currentOrder.processPayment(currentOrder.getFinalTotal()); 
            DatabaseHelper.saveOrder(currentOrder); // SAVES TO DB
            showReceipt(frame, currentOrder.generateReceipt());
            startNewOrder(frame);
        } else if (choice == 2) { 
            currentOrder.setPaymentMethod("CASH");
            currentOrder.setStatus("Pending (Cash)");
            DatabaseHelper.saveOrder(currentOrder); // SAVES TO DB
            JOptionPane.showMessageDialog(frame, "Order placed!\nPlease proceed to the counter to pay ₱" + String.format("%.2f", currentOrder.getFinalTotal()));
            startNewOrder(frame);
        }
    }

    private static void showReceipt(JFrame frame, String receiptText) {
        JTextArea area = new JTextArea(receiptText);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setEditable(false);
        JOptionPane.showMessageDialog(frame, new JScrollPane(area), "Transaction Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void updateCartUI() {
        cartArea.setText("Your Cart:\n\n" + currentOrder.getCartPreview());
    }
}