
package memoria.prueba;

import javax.swing.*;

public class FileManagerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileManagerUI fileManagerUI = new FileManagerUI();
            fileManagerUI.setTitle("File Manager");
            fileManagerUI.setSize(800, 600);
            fileManagerUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fileManagerUI.setVisible(true);
        });
    }
}

