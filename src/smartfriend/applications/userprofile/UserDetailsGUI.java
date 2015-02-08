package smartfriend.applications.userprofile;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author Isuri
 */
public class UserDetailsGUI {

    private void initAndShowGUI() {
        // This method is invoked on Swing thread      
        JFrame frame = new JFrame("Task Schedular");
        final JFXPanel fxPanel = new JFXPanel();
        frame.setSize(1370, 700);

        // Add JavaFX panel into swing JFrame
        frame.add(fxPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
        });
    }

    private void initFX(JFXPanel fxPanel) {
        try {
            // This method is invoked on JavaFX thread
            Parent parent = FXMLLoader.load(getClass().getResource("user_details.fxml"));
            Scene scene = new Scene(parent);
            fxPanel.setScene(scene);
        } catch (IOException ex) {
            Logger.getLogger(UserDetailsGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        final UserDetailsGUI ud = new UserDetailsGUI();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ud.initAndShowGUI();
            }
        });
//        Get User Details         
//        UserDetails userdetails = new UserDetails();
//        System.out.println(userdetails.getChildName()+" "+userdetails.getParentEmail()+" "+userdetails.getChildAge());
    }
}
