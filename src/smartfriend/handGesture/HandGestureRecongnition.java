/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import smartfriend.gui.GUIForm;

/**
 *
 * @author Meuru This Class handles all the communication with the components of
 * the hand gesture recognition
 */
public class HandGestureRecongnition {

    private static final int CAMERA_ID = 1;
    private GUIForm gUIForm;
    private JFrame mainDisplayPanel;
    private JFrame auxiluryDisplayPanel;
    private Camera camera;
    private DisplayEngine displayEngine;
    private HandDetector handDetector;

    public HandGestureRecongnition() {

        try {
            System.load(new File(".").getCanonicalPath() + "/lib/OpenCV2410/opencv_java2410.dll");
        } catch (IOException ex) {
            Logger.getLogger(HandGestureRecongnition.class.getName()).log(Level.SEVERE, null, ex);
        }

        gUIForm = new GUIForm();
        mainDisplayPanel = new JFrame();
        auxiluryDisplayPanel = new JFrame();
        camera = new Camera(CAMERA_ID);

        gUIForm.setVisible(true);
        JFrame infoPanel = setUpInfoPanel();
        displayEngine = new DisplayEngine(camera, gUIForm.getDisplyDimentions(), (Graphics2D) infoPanel.getContentPane().getComponent(0).getGraphics());
        handDetector = new HandDetector();

    }

    public static void main(String[] args) {
        new HandGestureRecongnition();
    }

    private JFrame setUpInfoPanel() {
        JFrame infoPanel = new JFrame("Info Panel");
        infoPanel.getContentPane().setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        infoPanel.getContentPane().add(jPanel, BorderLayout.CENTER);
        infoPanel.setSize(800, 600);
        infoPanel.setVisible(true);
        infoPanel.setLayout(new BorderLayout());
        return infoPanel;
    }
}
