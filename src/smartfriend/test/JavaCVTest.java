/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 *
 * @author Meuru
 */
import sun.java2d.opengl.OGLContext;
public class JavaCVTest{

    private static int WEB_CAMERA = 1;
    private VideoCapture webcam;

    public static void main(String[] args) {
        try {
            JavaCVTest pg = new JavaCVTest();
            System.out.println(pg.capturePhotos());
            Thread.sleep(100);
            System.out.println(pg.capturePhotos());
            //pg.webcamTest();
            while (true) {
                System.out.println(pg.capturePhotos());

            }
        } catch (InterruptedException ex) {
            Logger.getLogger(JavaCVTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void webcamTest() {
        System.out.println("######99999");
        //System.load("C:/opencv/build/java/x64/opencv_java249.dll");
        Mat frame = new Mat();
//        boolean play = false;
//        webcam = new VideoCapture(WEB_CAMERA);
//        webcam.open(WEB_CAMERA);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(JavaCVTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (true) {
            if (webcam.isOpened()) {

                webcam.read(frame);
                Highgui.imwrite("D:/camera.jpg", frame);
            }
        }
    }

    
    public JavaCVTest() {
        try {
            System.load(new File(".").getCanonicalPath()+"/lib/OpenCV2410/opencv_java2410.dll");
        } catch (IOException ex) {
            Logger.getLogger(JavaCVTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        webcam = new VideoCapture(WEB_CAMERA);
        webcam.open(WEB_CAMERA);
    }

    public Mat capturePhotos() {
        Mat frame = new Mat();
        if (webcam.isOpened()) {
            System.out.println("Capturing");
            webcam.read(frame);
            Highgui.imwrite("D:/camera.jpg", frame);
        }
        return frame;
    }
}
