/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import com.sun.corba.se.impl.interceptors.PICurrent;
import java.awt.Dimension;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 *
 * @author
 */
public class Camera {

    private int cameraID;
    private VideoCapture webcam;

    /**
     * This class controls the camera and related actions
     *
     * @param id Camera ID
     */
    public Camera(int id) {
        cameraID = id;
        webcam = new VideoCapture(cameraID);
        webcam.open(cameraID);
        runCamera();        // allow camera to adjust itself
        System.out.println("Camera " + cameraID + " is ready");
    }

    public Dimension getCameraDimension() {
        Size photoSize = capturePhoto().size();
        return new Dimension((int) photoSize.width, (int) photoSize.height);
    }

    /**
     * Capture the photo from camera
     *
     * @return Captured frame as
     */
    public Mat capturePhoto() {
        Mat frame = new Mat();
        if (webcam.isOpened()) {
            webcam.read(frame);
        }
        return frame.clone();
    }

    private void runCamera() {
        for (int i = 0; i < 30; i++) {
            capturePhoto();
        }
    }

    public void saveImage(Mat image) {
        Highgui.imwrite("D:/camera.jpg", image);
    }
}
