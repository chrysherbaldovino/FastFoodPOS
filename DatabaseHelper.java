package FastFoodPOS;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;

public class DatabaseHelper {

    public static Connection connect() throws SQLException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
            return DriverManager.getConnection(
                props.getProperty("db.url"), 
                props.getProperty("db.user"), 
                props.getProperty("db.password")
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Failed to load database credentials from db.properties.");
        }
    }

    public static void saveOrder(Order order) {
        String queueQuery = "SELECT COALESCE(MAX(QueueNo), 0) + 1 FROM `Order` WHERE IsArchived = FALSE";
        String insertOrder = "INSERT INTO `Order` (Status, Channel, Total, EmployeeId, CustomerId, QueueNo) VALUES (?, ?, ?, 1, 1, ?)";
        String insertPayment = "INSERT INTO Payment (Amount, Method, PaymentStatus, OrderId) VALUES (?, ?, ?, ?)";
        String insertItem = "INSERT INTO MenuItem (MenuName, Category, Price, OrderId) VALUES (?, 'Standard', ?, ?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false); 

            try {
                // 1. Calculate the daily Queue Number
                int nextQueueNo = 1;
                try (Statement stmt = conn.createStatement(); ResultSet rsQueue = stmt.executeQuery(queueQuery)) {
                    if (rsQueue.next()) nextQueueNo = rsQueue.getInt(1);
                }

                // 2. Insert the Order
                try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtOrder.setString(1, order.getStatus());
                    pstmtOrder.setString(2, order.getOrderType());
                    pstmtOrder.setDouble(3, order.getFinalTotal());
                    pstmtOrder.setInt(4, nextQueueNo); // Save the queue number
                    pstmtOrder.executeUpdate();

                    ResultSet rs = pstmtOrder.getGeneratedKeys();
                    if (rs.next()) {
                        int realOrderId = rs.getInt(1);
                        
                        // Trick the Java UI into displaying the Queue Number instead of the absolute DB ID
                        order.setId(nextQueueNo); 

                        // 3. Insert Payment
                        try (PreparedStatement pstmtPay = conn.prepareStatement(insertPayment)) {
                            pstmtPay.setDouble(1, order.getFinalTotal());
                            pstmtPay.setString(2, order.getPaymentMethod());
                            pstmtPay.setString(3, order.getStatus().contains("Pending") ? "Pending" : "Completed");
                            pstmtPay.setInt(4, realOrderId);
                            pstmtPay.executeUpdate();
                        }

                        // 4. Insert Menu Items
                        try (PreparedStatement pstmtItem = conn.prepareStatement(insertItem)) {
                            for (MenuItem item : order.getItems()) {
                                pstmtItem.setString(1, item.getName());
                                pstmtItem.setDouble(2, item.getPrice());
                                pstmtItem.setInt(3, realOrderId);
                                pstmtItem.addBatch();
                            }
                            pstmtItem.executeBatch();
                        }
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        // Fetch both OrderId (for DB links) and QueueNo (for the UI)
        String queryOrders = "SELECT o.OrderId, o.QueueNo, o.Channel, o.Status, o.Total, p.Method FROM `Order` o " +
                             "LEFT JOIN Payment p ON o.OrderId = p.OrderId " +
                             "WHERE o.Status != 'Completed' AND o.IsArchived = FALSE";
        String queryItems = "SELECT * FROM MenuItem WHERE OrderId = ?";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rsOrders = stmt.executeQuery(queryOrders)) {

            while (rsOrders.next()) {
                int realOrderId = rsOrders.getInt("OrderId");
                int queueNo = rsOrders.getInt("QueueNo");
                
                // Map the QueueNo to the Java object so the Cashier and Display Board show #1, #2...
                Order order = new Order(queueNo, rsOrders.getString("Channel"));
                order.setStatus(rsOrders.getString("Status"));
                order.setPaymentMethod(rsOrders.getString("Method") != null ? rsOrders.getString("Method") : "Unpaid");
                
                try (PreparedStatement pstmtItems = conn.prepareStatement(queryItems)) {
                    pstmtItems.setInt(1, realOrderId); // Fetch items using the real DB ID
                    ResultSet rsItems = pstmtItems.executeQuery();
                    while (rsItems.next()) {
                        MenuItem item = new MenuItem(
                            rsItems.getInt("MenuId"),
                            rsItems.getString("MenuName"),
                            rsItems.getDouble("Price"),
                            "🍔" 
                        );
                        order.addItem(item); 
                    }
                }
                orders.add(order);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public static void updateOrderStatus(int queueNo, String newStatus) {
        // UI passes the QueueNo back to us, so we update based on QueueNo + Active Shift
        String sql = "UPDATE `Order` SET Status = ? WHERE QueueNo = ? AND IsArchived = FALSE";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, queueNo);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void updateOrderPayment(int queueNo, double tendered, double change, String newStatus) {
        String sqlOrder = "UPDATE `Order` SET Status = ? WHERE QueueNo = ? AND IsArchived = FALSE";
        // Use an INNER JOIN to update the Payment table using only the QueueNo
        String sqlPayment = "UPDATE Payment p INNER JOIN `Order` o ON p.OrderId = o.OrderId " +
                            "SET p.PaymentStatus = 'Completed' WHERE o.QueueNo = ? AND o.IsArchived = FALSE";
        
        try (Connection conn = connect()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sqlOrder)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, queueNo);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt2 = conn.prepareStatement(sqlPayment)) {
                pstmt2.setInt(1, queueNo);
                pstmt2.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    public static double getSales(String interval) {
        String query = "";
        switch (interval) {
            case "DAY": 
                query = "SELECT COALESCE(SUM(Total), 0) FROM `Order` WHERE IsArchived = FALSE"; 
                break;
            case "WEEK": 
                query = "SELECT COALESCE(SUM(Total), 0) FROM `Order` WHERE YEARWEEK(OrderDate, 1) = YEARWEEK(CURDATE(), 1)"; 
                break;
            case "MONTH": 
                query = "SELECT COALESCE(SUM(Total), 0) FROM `Order` WHERE MONTH(OrderDate) = MONTH(CURDATE()) AND YEAR(OrderDate) = YEAR(CURDATE())"; 
                break;
            case "QUARTER": 
                query = "SELECT COALESCE(SUM(Total), 0) FROM `Order` WHERE QUARTER(OrderDate) = QUARTER(CURDATE()) AND YEAR(OrderDate) = YEAR(CURDATE())"; 
                break;
            case "YEAR": 
                query = "SELECT COALESCE(SUM(Total), 0) FROM `Order` WHERE YEAR(OrderDate) = YEAR(CURDATE())"; 
                break;
            case "ALL": 
                query = "SELECT COALESCE(SUM(Total), 0) FROM `Order`"; 
                break;
        }

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }
    
    public static void resetDatabase() {
        String sql = "UPDATE `Order` SET IsArchived = TRUE WHERE IsArchived = FALSE";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
}