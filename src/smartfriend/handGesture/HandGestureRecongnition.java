/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.opencv.core.Point;
import smartfriend.gui.GUIForm;
import smartfriend.gui.HandGestureGraphicRenderer;
import smartfriend.util.general.Consts;

/**
 *
 * @author Meuru This Class handles all the communication with the components of
 * the hand gesture recognition
 */
public class HandGestureRecongnition implements Runnable {

    private GUIForm gUIForm;
    private JFrame infoPanel;
    private Camera camera;
    private DisplayEngine displayEngine;
    private HandDetector handDetector;
    private HandGestureGraphicRenderer graphicRenderer;
    private SystemController systemController;

    public HandGestureRecongnition() {

        try {
            System.load(new File(".").getCanonicalPath() + "/lib/OpenCV2410/opencv_java2410.dll");
        } catch (IOException ex) {
            Logger.getLogger(HandGestureRecongnition.class.getName()).log(Level.SEVERE, null, ex);
        }

        gUIForm = new GUIForm();
        camera = new Camera(Consts.CAMERA_ID);

        infoPanel = setUpInfoPanel();
        graphicRenderer = new HandGestureGraphicRenderer(gUIForm);
        gUIForm.setVisible(true);
        displayEngine = new DisplayEngine(camera, gUIForm.getDisplyDimentions(), graphicRenderer);

        handDetector = new HandDetector(displayEngine, graphicRenderer, displayEngine.getInitialImage());
        systemController = new SystemController(gUIForm.getGraphicsDevice(), displayEngine.getBoundryPointsI());

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
        infoPanel.setSize(1280, 600);
        infoPanel.setVisible(true);
        infoPanel.setLayout(new BorderLayout());
        return infoPanel;
    }

    @Override
    public void run() {
        System.out.println("Started Hand Gesture Recognition Thread");
        Point handpointer;
        BufferedImage screenImage;
        int boundryX = (int) Math.min(displayEngine.getBoundryPointsI().get(0).x, displayEngine.getBoundryPointsI().get(1).x);
        int boundryY = (int) Math.min(displayEngine.getBoundryPointsI().get(0).y, displayEngine.getBoundryPointsI().get(3).y);

        while (true) {
            //gUIForm.setOpacity(0.5f);
            screenImage = systemController.getSkewedScreenShot();
            graphicRenderer.drawImageOnInfoPanel(screenImage, 1);
            handpointer = handDetector.getHandPoint(camera.capturePhoto().clone(), screenImage);
            graphicRenderer.drawPointerOnScreen(handpointer);
            systemController.moveMousePointer(handpointer);
            infoPanel.repaint();
        }
    }
}
