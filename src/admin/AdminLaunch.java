package admin;

import javax.swing.SwingUtilities;

public class AdminLaunch {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminModel model = new AdminModel("localhost", 12345);
            AdminGUI gui = new AdminGUI();
            AdminController controller = new AdminController(model, gui);
            controller.connect();
        });
    }
}
