/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javaxt.io.Image;
import org.opencv.core.Point;
import smartfriend.util.general.Consts;

/**
 *
 * @author Meuru
 */
public class SystemController {

    private static Robot robot;
    private ArrayList<Point> boundryPoints;
    private int boundryX, boundryY, imgWidth, imgHeight;

    /**
     * Controls the mouse pointer
     *
     * @param graphicsDevice The screen which need to display the mouse pointer
     */
    public SystemController(GraphicsDevice graphicsDevice, ArrayList<Point> boundryPoints) {
        try {
            robot = new Robot(graphicsDevice);
            this.boundryPoints = boundryPoints;
            boundryX = (int) Math.min(boundryPoints.get(0).x, boundryPoints.get(1).x);
            boundryY = (int) Math.min(boundryPoints.get(0).y, boundryPoints.get(3).y);
            imgWidth = (int) Math.max(boundryPoints.get(2).x, boundryPoints.get(3).x) - boundryX;
            imgHeight = (int) Math.max(boundryPoints.get(1).y, boundryPoints.get(2).y) - boundryY;
//            System.out.println("X:" + boundryX + "   Y :" + boundryY + "   W:" + imgWidth + "   H:" + imgHeight);
        } catch (AWTException ex) {
            Logger.getLogger(SystemController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param pt position to which the mouse pointer needs to be moved
     */
    public void moveMousePointer(Point pt) {
        robot.mouseMove((int) pt.x, (int) pt.y);
    }

    /**
     *
     * @param pt position needs to be clicked
     */
    public void leftMouseClick(Point pt) {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    /**
     *
     * @param pt position needs to be clicked
     */
    public void rightMouseClick(Point pt) {
        robot.mousePress(InputEvent.BUTTON3_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
    }

    /**
     *
     * @param pt position needs to be clicked
     */
    public void middleMouseClick(Point pt) {
        robot.mousePress(InputEvent.BUTTON2_MASK);
        robot.mouseRelease(InputEvent.BUTTON2_MASK);
    }

    /**
     *
     * @param pt position needs to be clicked
     */
    public void doubleMouseClick(Point pt) {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    public BufferedImage getSkewedScreenShot() {
        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(0, 0, Consts.SCREEN_WIDHT, Consts.SCREEN_HEIGHT));
        Image image = new Image(screenShot);
        image.setCorners((int) boundryPoints.get(0).x, (int) boundryPoints.get(0).y, //UL
                (int) boundryPoints.get(3).x, (int) boundryPoints.get(03).y, //UR
                (int) boundryPoints.get(2).x, (int) boundryPoints.get(2).y, //LR
                (int) boundryPoints.get(1).x, (int) boundryPoints.get(1).y);         //LL
        image.crop(0, 0, imgWidth, imgHeight);
        Image anImage = new Image(640, 480);
        anImage.addImage(image, boundryX, boundryY, true);
        return anImage.getBufferedImage();
    }
}
