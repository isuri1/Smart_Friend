/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import smartfriend.gui.HandGestureGraphicRenderer;
import smartfriend.util.general.PointTransform;

/**
 *
 * @author Meuru
 */
public class DisplayEngine {

    private static final int IMG_THRESHOLD_VAL = 145;
    private ArrayList<Point> boundryPoints;
    private Dimension displaySize;
    private Mat initialImage;

    public DisplayEngine(Camera camera, Dimension displaySize, HandGestureGraphicRenderer graphicRenderer) {
        this.displaySize = displaySize;
        while (true) {
            initialImage = camera.capturePhoto();
            boundryPoints = findBoundaries(initialImage.clone());
            if (boundryPoints.size() > 0) {
                boundryPoints = sortPoints(boundryPoints);
                for (Point pt : boundryPoints) {
                    System.out.println(" x : " + pt.x + "   " + pt.y);
                }
                graphicRenderer.drawPointsOnInfoPanel(camera.capturePhoto(), boundryPoints, Color.GREEN,1);
                PointTransform.initialize(boundryPoints, displaySize);
                break;
            }
        }
    }
    
    public Dimension getDisplayDimension(){
        return displaySize;
    }
    
    public ArrayList<Point> getBoundryPointsI(){
        return boundryPoints;
    }

    public Mat getInitialImage() {
        return initialImage;
    }

    private ArrayList<Point> findBoundaries(Mat image) {
        ArrayList<Point> boundryPoints = new ArrayList<>();
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(image, image, IMG_THRESHOLD_VAL, 255, Imgproc.THRESH_BINARY);
        ArrayList<MatOfPoint> contours = new ArrayList<>();

        Mat hierachy = new Mat();
        Imgproc.findContours(image, contours, hierachy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (MatOfPoint contour : contours) {
            Rect rec = Imgproc.boundingRect(contour);
            if (rec.area() > 100000) {
                MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contour.toArray());
                Imgproc.approxPolyDP(matOfPoint2f, matOfPoint2f, 10, true);
                if ((int) matOfPoint2f.size().height == 4) {
                    System.out.println("Ditected corner points");
                    boundryPoints.addAll(matOfPoint2f.toList());
                    break;
                }
            }
        }
        return boundryPoints;
    }

    private ArrayList<Point> sortPoints(ArrayList<Point> points) {
        Point[] sortedPoints = new Point[4];
        sortedPoints[0] = points.get(0);
        for (Point pt : points) {
            if (Math.pow(sortedPoints[0].x, 2) + Math.pow(sortedPoints[0].x, 2)
                    > Math.pow(pt.x, 2) + Math.pow(pt.y, 2)) {
                sortedPoints[0] = pt;

            }
        }
        points.remove(sortedPoints[0]);
        sortedPoints[1] = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Point pt : points) {
            if (sortedPoints[1].x > pt.x) {
                sortedPoints[1] = pt;
            }
        }
        points.remove(sortedPoints[1]);
        sortedPoints[3] = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Point pt : points) {
            if (sortedPoints[3].y > pt.y) {
                sortedPoints[3] = pt;
            }
        }
        points.remove(sortedPoints[3]);
        sortedPoints[2] = points.get(0);
        ArrayList<Point> sortedPointsArrayList = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            sortedPointsArrayList.add(i, sortedPoints[i]);
        }
        return sortedPointsArrayList;
    }

    public ArrayList<Point> removeOutsidePoints(ArrayList<Point> pointsList) {

        ArrayList<Point> points = null;
        try {
            points = PointTransform.getInstance().transfromPoints(pointsList);
        } catch (Exception ex) {
            Logger.getLogger(DisplayEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        int boaderSize = 0;
        ArrayList<Point> modifiedPoints = new ArrayList<>();
        for (Point pt : points) {
            if (pt.x < boaderSize | pt.x > (displaySize.width - boaderSize)) {
                continue;
            } else if (pt.y < boaderSize | pt.y > displaySize.height - boaderSize) {
                continue;
            } else {
                modifiedPoints.add(pt);
            }
        }
        return modifiedPoints;


    }

    public ArrayList<Point> removeBoarderPoints(ArrayList<Point> pointsList) {

        if (pointsList.size() > 4) {
            int boaderSize = 10;
            ArrayList<Point> modifiedPoints = new ArrayList<>();
            for (Point pt : pointsList) {
                if (pt.x < boaderSize | pt.x > (displaySize.width - boaderSize)) {
                    continue;
                } else if (pt.y < boaderSize | pt.y > displaySize.height - boaderSize) {
                    continue;
                } else {
                    modifiedPoints.add(pt);
                }
            }
            return modifiedPoints;
        } else {
            return pointsList;
        }

    }
}
