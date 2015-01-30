/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Meuru
 */
public class Test extends JFrame {

    static JPanel welcomeScreen , mainScreen;

    public static void main(String[] args) {
        Test frame = new Test();
        frame.setUndecorated(true);
        //f.setOpacity(0.5f);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcomeScreen = (JPanel) (new WelcomeScreen(null));
        mainScreen = (JPanel) (new MainScreen());
        frame.add(welcomeScreen);
        frame.add(mainScreen);
        mainScreen.setVisible(false);

//        f.add(new MainScreen(),-1);
        frame.setSize(welcomeScreen.getSize());
        //f.setLocation(100, 100);
        frame.setVisible(true);
    }

    public Test() {
    }

    public void addPanel() {
        mainScreen.setVisible(true);
        remove(welcomeScreen);
//        welcomeScreen.setVisible(false);
    }
}
