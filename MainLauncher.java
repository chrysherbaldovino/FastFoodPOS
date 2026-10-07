package FastFoodPOS;

import javax.swing.SwingUtilities;

public class MainLauncher {
    public static void main(String[] args) {
        FastFoodPOS.main(args); 
        FastFoodStaffPOS.main(args); 
        SwingUtilities.invokeLater(() -> FastFoodStaffPOS.openDisplayBoard());
    }
}