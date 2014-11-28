/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import math.geom2d.Point2D;
import math.geom2d.polygon.SimplePolygon2D;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import smartfriend.gui.HandGestureGraphicRenderer;
import smartfriend.util.general.PointTransform;

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

        if (contours.size() > 0) {
            List<Point> biggestContour = getBiggestContour(contours);
            //graphicRenderer.drawShape(img, biggestContour, Color.PINK);
            if (biggestContour != null) {
                ArrayList<Point> transformedHandPoints = displayEngine.removeBoarderPooints(new ArrayList<>(biggestContour));

                Point cog = computeHandInfo(transformedHandPoints);
                graphicRenderer.drawPointsOnScreen(transformedHandPoints);
                if (cog != null) {
                    //graphicRenderer.drawPointerOnScreen(cog);
                }
                //graphicRenderer.drawPointer(computeHandInfo(transformedHandPoints));
                /// remove the boarder points
            }
        }
        return handPoint;
    }

    private List<Point> getBiggestContour(ArrayList<MatOfPoint> contours) {
        ArrayList<Point> biggestPoints = null;
        SimplePolygon2D biggestPolygon = null;
        for (MatOfPoint points : contours) {
            ArrayList<Point> pointsArrayList = displayEngine.removeOutsidePoints(new ArrayList<>(points.toList()));
            if (pointsArrayList.size() < 3) {
                continue;
            }
            List<String> test = null;
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(pointsArrayList.toArray(new Point[0]));
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
        if (points.size() > 2) {
            ArrayList<Point2D> point2DList;
            SimplePolygon2D polygon;
            Point2D centorid, distantPoint = null;
            double distance = 0;

            point2DList = new ArrayList<>(points.size());
            for (Point pt : points) {
                point2DList.add(new Point2D(pt.x, pt.y));
            }
            polygon = new SimplePolygon2D(point2DList);
            centorid = polygon.centroid();
            for (Point2D pt : point2DList) {
                double dist = centorid.distance(pt);
                if (distance < dist) {
                    distance = dist;
                    distantPoint = pt;
                }
            }
            if (distantPoint != null) {
                graphicRenderer.drawPointerOnScreen(new Point(distantPoint.x(), distantPoint.y()));
            }
            return new Point(centorid.x(), centorid.y());
        } else {
            return null;
        }
    }
}