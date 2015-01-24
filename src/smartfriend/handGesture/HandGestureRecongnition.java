/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

<<<<<<< HEAD
=======

>>>>>>> refactor
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Mat;
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

        graphicRenderer = new HandGestureGraphicRenderer(gUIForm, camera);
        gUIForm.setVisible(true);
        displayEngine = new DisplayEngine(camera, gUIForm.getDisplyDimentions(), graphicRenderer);
        Mat initialImage = graphicRenderer.drawShapeOnImage(displayEngine.getInitialImage(), displayEngine.getBoundryPoints());
        handDetector = new HandDetector(displayEngine, graphicRenderer, initialImage);
        systemController = new SystemController(gUIForm.getGraphicsDevice(), displayEngine.getBoundryPoints());

//        gUIForm.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                int keyCode = e.getKeyCode();
//                if ((keyCode == KeyEvent.VK_NUMPAD5) || (keyCode == KeyEvent.VK_ENTER)
//                        || (keyCode == KeyEvent.VK_SPACE)) // take a snap when press NUMPAD-5, enter, or space is pressed
//                {
//                    System.out.println("DDDDDD");
//                    System.exit(0);
//                }
//            }
//        });
        new Thread(this).start();


    }

    public static void main(String[] args) {
        new HandGestureRecongnition();
    }

    @Override
    public void run() {
        System.out.println("Started Hand Gesture Recognition Thread");
        Point handpointer;
        BufferedImage screenImage;
        int boundryX = (int) Math.min(displayEngine.getBoundryPoints().get(0).x, displayEngine.getBoundryPoints().get(1).x);
        int boundryY = (int) Math.min(displayEngine.getBoundryPoints().get(0).y, displayEngine.getBoundryPoints().get(3).y);

        while (true) {
            //gUIForm.setOpacity(0.5f);
            screenImage = systemController.getSkewedScreenShot();

            //graphicRenderer.drawImageOnInfoPanel(screenImage, 1);
            handpointer = handDetector.getHandPoint(camera.capturePhoto(), screenImage);
            graphicRenderer.drawPointerOnScreen(handpointer);
//            systemController.moveMousePointer(handpointer);

        }
    }
}
