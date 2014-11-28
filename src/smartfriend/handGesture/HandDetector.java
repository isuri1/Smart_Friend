/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import math.geom2d.Point2D;
import math.geom2d.polygon.SimplePolygon2D;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import smartfriend.gui.HandGestureGraphicRenderer;

/**
 *
 * @author Meuru
 */
public class HandDetector {

    private static final int IMG_THRESHOLD_VAL = 145;
    private HandGestureGraphicRenderer graphicRenderer;
    private DisplayEngine displayEngine;
    private Mat initialImage;
    private SimplePolygon2D biggestContour;

    HandDetector(DisplayEngine de, HandGestureGraphicRenderer gr, Mat initialImage) {
        displayEngine = de;
        graphicRenderer = gr;
        this.initialImage = initialImage;
    }

    public Point getHandPoint(Mat image) {
        Point handPoint = new Point();
        Mat img = image.clone();
        //resize image to speedup
        Core.absdiff(image, initialImage, image);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(image, image, IMG_THRESHOLD_VAL, 255, Imgproc.THRESH_BINARY);
        ArrayList<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //System.out.println("!!!!!!!!!!!!!!" + contours.size());
//        for (MatOfPoint contour : contours) {
        if (contours.size() > 0) {
            List<Point> biggestContour = getBiggestContour(contours);
            graphicRenderer.drawShape(img, biggestContour, Color.RED);

            ArrayList<Point> transformedHandPoints = displayEngine.removeBoarderPooints(new ArrayList<>(biggestContour));

            Point cog = computeHandInfo(transformedHandPoints);
            graphicRenderer.drawPointsOnScreen(transformedHandPoints);
            graphicRenderer.drawPointer(cog);

            //graphicRenderer.drawPointer(computeHandInfo(transformedHandPoints));
            /// remove the boarder points

        }
        return handPoint;
    }

    private List<Point> getBiggestContour(ArrayList<MatOfPoint> contours) {
        ArrayList<Point> biggestPoints = null;
        SimplePolygon2D biggestPolygon = null;
        for (MatOfPoint points : contours) {
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(points.toArray());
            Imgproc.approxPolyDP(matOfPoint2f, matOfPoint2f, 10, true);

            ArrayList<Point2D> point2DList = new ArrayList<>();
            for (Point pt : points.toList()) {
                point2DList.add(new Point2D(pt.x, pt.y));
            }
            SimplePolygon2D polygon = new SimplePolygon2D(point2DList);
            if (biggestPolygon == null || Math.abs(biggestPolygon.area()) < Math.abs(polygon.area())) {
                biggestPolygon = polygon;
                biggestPoints = new ArrayList<>(matOfPoint2f.toList());
            }
        }
        this.biggestContour = biggestPolygon;
        return biggestPoints;

    }

    private Point computeHandInfo(ArrayList<Point> points) {
        if (points.size() > 0) {
            ArrayList<Point2D> point2DList = new ArrayList<>(points.size());
            for (Point pt : points) {
                point2DList.add(new Point2D(pt.x, pt.y));
            }
            SimplePolygon2D polygon = new SimplePolygon2D(point2DList);
            Point2D centorid = polygon.centroid();
            return new Point(centorid.x(), centorid.y());
        } else {
            return null;
        }
    }
}