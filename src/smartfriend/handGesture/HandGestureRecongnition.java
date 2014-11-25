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
import org.opencv.core.Point;
import smartfriend.gui.GUIForm;
import smartfriend.gui.HandGestureGraphicRenderer;

/**
 *
 * @author Meuru This Class handles all the communication with the components of
 * the hand gesture recognition
 */
public class HandGestureRecongnition implements Runnable {

    private static final int CAMERA_ID = 1;
    private GUIForm gUIForm;
    private JFrame mainDisplayPanel;
    private JFrame auxiluryDisplayPanel;
    private Camera camera;
    private DisplayEngine displayEngine;
    private HandDetector handDetector;
    private HandGestureGraphicRenderer graphicRenderer;

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

        JFrame infoPanel = setUpInfoPanel();
        graphicRenderer = new HandGestureGraphicRenderer(gUIForm, (Graphics2D) infoPanel.getContentPane().getComponent(0).getGraphics());
        gUIForm.setVisible(true);
        displayEngine = new DisplayEngine(camera, gUIForm.getDisplyDimentions(), graphicRenderer);

        handDetector = new HandDetector(displayEngine, graphicRenderer, displayEngine.getInitialImage());


        new Thread(this).start();
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

    @Override
    public void run() {
        System.out.println("Started Hand Gesture Recognition Thread");
        while (true) {
            Point point = handDetector.getHandPoint(camera.capturePhoto().clone());
        }
    }
}
