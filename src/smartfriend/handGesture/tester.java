/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import math.geom2d.Point2D;
import math.geom2d.polygon.SimplePolygon2D;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import smartfriend.util.general.Consts;

/**
 *
 * @author Meuru
 */
public class tester {

    JFrame infoPanel;
    Graphics2D infoPanelGraphics2D;
    private SimplePolygon2D biggestContour;

    public tester() {

        try {
            System.load(new File(".").getCanonicalPath() + "/lib/OpenCV2410/opencv_java2410.dll");
        } catch (IOException ex) {
            Logger.getLogger(HandGestureRecongnition.class.getName()).log(Level.SEVERE, null, ex);
        }

        infoPanel = new JFrame("Info Panel");
        infoPanel.getContentPane().setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        infoPanel.getContentPane().add(jPanel, BorderLayout.CENTER);
        infoPanel.setSize(1280, 800);
        infoPanel.setVisible(true);
        infoPanel.setLayout(new BorderLayout());

        infoPanelGraphics2D = (Graphics2D) infoPanel.getGraphics();
        try {
            BufferedImage initialImage = ImageIO.read(new File("D://FYP//test//1.jpg"));
            BufferedImage initialImageBlack = ImageIO.read(new File("D://FYP//test//1_black.jpg"));
            BufferedImage handImage = ImageIO.read(new File("D://FYP//test//20150120_104506.jpg"));
            BufferedImage screenImage = ImageIO.read(new File("D://FYP//test//screenImage.jpg"));
            drawImageOnInfoPanel(initialImage, 0, 40, 2);
            drawImageOnInfoPanel(handImage, 320, 40, 2);
            drawImageOnInfoPanel(screenImage, 640, 40, 2);

            Mat img = convertToMat(initialImage);
            Core.absdiff(convertToMat(initialImageBlack), convertToMat(handImage), img);
            drawImageOnInfoPanel(getImage(img), 0, 280, 2);
            Core.absdiff(img, convertToMat(screenImage), img);
            drawImageOnInfoPanel(getImage(img), 320, 280, 2);

            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(img, img, 50, 255, Imgproc.THRESH_BINARY);
            drawImageOnInfoPanel(getImage(img), 640, 280, 2);
            //resize image to speedup


        } catch (IOException ex) {
            Logger.getLogger(tester.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void drawImageOnInfoPanel(BufferedImage image, int x, int y, int downScale) {
        infoPanelGraphics2D.drawImage(image, x, y, Consts.CAMERA_WIDTH / downScale, Consts.CAMERA_HEIGHT / downScale, null);
    }

    public static void main(String[] args) {
        new tester();
    }

    public Mat convertToMat(BufferedImage bufferedImage) {
        BufferedImage convertedImg;
        Mat mat;
        convertedImg = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        convertedImg.getGraphics().drawImage(bufferedImage, 0, 0, null);
        byte[] pixels = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();
        mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }

    private BufferedImage getImage(Mat matImage) {
        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", matImage, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }
}
