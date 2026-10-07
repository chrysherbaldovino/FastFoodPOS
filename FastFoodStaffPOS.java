package FastFoodPOS;

import javax.swing.*;
import java.awt.*;

public class FastFoodStaffPOS {
    
    // Tracks if the staff member has entered the correct PIN this session
    private static boolean isAuthenticated = false; 

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createStaffMenu());
    }
    
    // Authentication Helper Method
    private static boolean authenticate(JFrame parentFrame) {
        if (isAuthenticated) return true; // Skips prompt if already logged in

        JPasswordField pf = new JPasswordField();
        int pinAttempt = JOptionPane.showConfirmDialog(parentFrame, pf, 
            "Enter Staff PIN to access modules:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (pinAttempt == JOptionPane.OK_OPTION) {
            String password = new String(pf.getPassword());
            if (password.equals("0101")) { // 0101 is the default PIN
                isAuthenticated = true;
                return true;
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Invalid PIN. Access Denied.", "Security Alert", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    private static void createStaffMenu() {
        JFrame frame = new JFrame("Staff Terminal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400); 
        // Reduced to 5 rows since the Display Board button is removed
        frame.setLayout(new GridLayout(5, 1, 10, 10)); 

        JLabel title = new JLabel("Select Module", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        
        JButton btnCashier = new JButton("Cashier Terminal");
        JButton btnKitchen = new JButton("Kitchen Display System (KDS)");
        JButton btnSales = new JButton("Sales & ROI Report"); 
        
        JButton btnCutoff = new JButton("Process Cutoff"); 
        btnCutoff.setBackground(new Color(255, 102, 102)); 
        
        btnCashier.addActionListener(e -> {
            if (authenticate(frame)) openCashierWindow();
        });
        
        btnKitchen.addActionListener(e -> {
            if (authenticate(frame)) openKitchenWindow();
        });
        
        btnSales.addActionListener(e -> {
            if (authenticate(frame)) openSalesReportWindow();
        }); 
        
        btnCutoff.addActionListener(e -> {
            if (authenticate(frame)) {
                int confirm = JOptionPane.showConfirmDialog(frame, 
                    "Process cutoff? This will wipe the active queue and reset to Order #1.", 
                    "System Cutoff", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    DatabaseHelper.resetDatabase();
                    JOptionPane.showMessageDialog(frame, "Cutoff processed! The next transaction will be Order #1.");
                }
            }
        });

        frame.add(title);
        frame.add(btnCashier);
        frame.add(btnKitchen);
        frame.add(btnSales);
        frame.add(btnCutoff); 
        
        // Anchors the staff menu to the bottom-right corner
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screen.width - frame.getWidth(), screen.height - frame.getHeight() - 50);
        
        frame.setVisible(true);
    }
    
    private static void openCashierWindow() {
        JFrame frame = new JFrame("Cashier Terminal");
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        DefaultListModel<Order> listModel = new DefaultListModel<>();
        Runnable refreshList = () -> {
            listModel.clear();
            for (Order o : DatabaseHelper.getAllOrders()) {
                if (o.getStatus().contains("Pending")) listModel.addElement(o);
            }
        };
        refreshList.run();

        JList<Order> orderList = new JList<>(listModel);
        orderList.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JTextArea detailsArea = new JTextArea("Select an order to view details.");
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
 
        orderList.addListSelectionListener(e -> {
            Order selected = orderList.getSelectedValue();
            if (selected != null) detailsArea.setText(selected.generateReceipt());
        });
        
        JButton payBtn = new JButton("Process Cash Payment");
        payBtn.setBackground(new Color(173, 216, 230)); 
        payBtn.setPreferredSize(new Dimension(600, 50));
        
        payBtn.addActionListener(e -> {
            Order selected = orderList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(frame, "Select an order first.");
                return;
            }

            boolean paymentSuccess = false;
            while (!paymentSuccess) {
                String input = JOptionPane.showInputDialog(frame, "Total Due: ₱" + String.format("%.2f", selected.getFinalTotal()) + "\nEnter Cash Tendered:");
                if (input == null) break; 
                
                try {
                    double tendered = Double.parseDouble(input);
                    if (tendered < selected.getFinalTotal()) {
                        double missing = selected.getFinalTotal() - tendered;
                        JOptionPane.showMessageDialog(frame, "Insufficient funds! You need ₱" + String.format("%.2f", missing) + " more.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        selected.processPayment(tendered); 
                        DatabaseHelper.updateOrderPayment(selected.getId(), tendered, tendered - selected.getFinalTotal(), "Preparing");
                        
                        JTextArea receiptArea = new JTextArea(selected.generateReceipt());
                        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
                        JOptionPane.showMessageDialog(frame, new JScrollPane(receiptArea), "Payment Successful - Change: ₱" + String.format("%.2f", selected.getChange()), JOptionPane.INFORMATION_MESSAGE);
                        paymentSuccess = true;
                        refreshList.run(); 
                        detailsArea.setText("");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input. Please enter numbers only.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton refreshBtn = new JButton("Refresh List");
        refreshBtn.addActionListener(e -> refreshList.run());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        centerPanel.add(refreshBtn, BorderLayout.NORTH);

        frame.add(new JScrollPane(orderList), BorderLayout.WEST);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(payBtn, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private static void openKitchenWindow() {
        JFrame frame = new JFrame("Kitchen Display System");
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        DefaultListModel<Order> listModel = new DefaultListModel<>();
        Runnable refreshList = () -> {
            listModel.clear();
            for (Order o : DatabaseHelper.getAllOrders()) {
                if (o.getStatus().equals("Preparing")) listModel.addElement(o);
            }
        };
        refreshList.run();

        JList<Order> orderList = new JList<>(listModel);
        orderList.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JTextArea detailsArea = new JTextArea("Select a paid order to view prep details.");
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        orderList.addListSelectionListener(e -> {
            Order selected = orderList.getSelectedValue();
            if (selected != null) detailsArea.setText(selected.getCartPreview());
        });
        
        JButton completeBtn = new JButton("Mark Order as Ready");
        completeBtn.setBackground(new Color(144, 238, 144)); 
        completeBtn.addActionListener(e -> {
            Order selected = orderList.getSelectedValue();
            if (selected != null) {
                DatabaseHelper.updateOrderStatus(selected.getId(), "Ready for Pickup");
                JOptionPane.showMessageDialog(frame, "Order #" + selected.getId() + " is ready for pickup!");
                refreshList.run();
                detailsArea.setText("");
            }
        });

        JButton refreshBtn = new JButton("Refresh List");
        refreshBtn.addActionListener(e -> refreshList.run());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        centerPanel.add(refreshBtn, BorderLayout.NORTH);

        frame.add(new JScrollPane(orderList), BorderLayout.WEST);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(completeBtn, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    // Changed to public so MainLauncher can call it directly
    public static void openDisplayBoard() {
        JFrame frame = new JFrame("Customer Display Board");
        frame.setSize(600, 400); // Reduced size
        frame.setLayout(new GridLayout(1, 2));

        JTextArea prepArea = new JTextArea();
        prepArea.setFont(new Font("Arial", Font.BOLD, 40));
        prepArea.setBackground(Color.BLACK);
        prepArea.setForeground(Color.YELLOW);
        prepArea.setEditable(false);
        
        JTextArea readyArea = new JTextArea();
        readyArea.setFont(new Font("Arial", Font.BOLD, 40));
        readyArea.setBackground(Color.BLACK);
        readyArea.setForeground(Color.GREEN);
        readyArea.setEditable(false);

        frame.add(new JScrollPane(prepArea));
        frame.add(new JScrollPane(readyArea));

        Thread refreshThread = new Thread(() -> {
            while (true) {
                try {
                    StringBuilder prepTxt = new StringBuilder(" PREPARING:\n\n");
                    StringBuilder readyTxt = new StringBuilder(" READY:\n\n");
                    
                    for (Order o : DatabaseHelper.getAllOrders()) {
                        if (o.getStatus().equals("Preparing")) prepTxt.append("  #").append(o.getId()).append("\n");
                        else if (o.getStatus().equals("Ready for Pickup")) readyTxt.append("  #").append(o.getId()).append("\n");
                    }
                    
                    SwingUtilities.invokeLater(() -> {
                        prepArea.setText(prepTxt.toString());
                        readyArea.setText(readyTxt.toString());
                    });
                    
                    Thread.sleep(2000); 
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        refreshThread.start();

        // Anchors the display board to the top-right corner
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screen.width - frame.getWidth(), 0);
        
        frame.setVisible(true);
    }
    
    private static void openSalesReportWindow() {
        JFrame frame = new JFrame("Sales & ROI Report");
        frame.setSize(400, 400);
        frame.setLayout(new BorderLayout());

        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        reportArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        StringBuilder sb = new StringBuilder();
        sb.append("--- TOTAL SALES REPORT ---\n\n");
        sb.append(String.format("%-15s ₱%.2f\n", "Today:", DatabaseHelper.getSales("DAY")));
        sb.append(String.format("%-15s ₱%.2f\n", "This Week:", DatabaseHelper.getSales("WEEK")));
        sb.append(String.format("%-15s ₱%.2f\n", "This Month:", DatabaseHelper.getSales("MONTH")));
        sb.append(String.format("%-15s ₱%.2f\n", "This Quarter:", DatabaseHelper.getSales("QUARTER")));
        sb.append(String.format("%-15s ₱%.2f\n", "This Year:", DatabaseHelper.getSales("YEAR")));
        
        double totalRevenue = DatabaseHelper.getSales("ALL");
        sb.append("\nLifetime Sales: ₱").append(String.format("%.2f", totalRevenue));

        reportArea.setText(sb.toString());

        JButton btnROI = new JButton("Calculate System ROI");
        btnROI.setBackground(new Color(255, 215, 0));
        btnROI.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Enter Total Initial Investment / Capital (₱):");
            try {
                if (input != null && !input.trim().isEmpty()) {
                    double investment = Double.parseDouble(input);
                    if (investment > 0) {
                        double roi = ((totalRevenue - investment) / investment) * 100;
                        JOptionPane.showMessageDialog(frame, 
                            "Total Revenue: ₱" + String.format("%.2f", totalRevenue) + 
                            "\nInitial Investment: ₱" + String.format("%.2f", investment) + 
                            "\n\nReturn on Investment (ROI): " + String.format("%.2f", roi) + "%", 
                            "ROI Calculation", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        frame.add(btnROI, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}