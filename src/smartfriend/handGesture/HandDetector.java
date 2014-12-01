/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;
import math.geom2d.Point2D;
import math.geom2d.polygon.SimplePolygon2D;
import org.opencv.core.Core;
import org.opencv.core.CvType;
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
    private static final double POINTER_SMOOTH_RATIO = 0.5;
    private HandGestureGraphicRenderer graphicRenderer;
    private DisplayEngine displayEngine;
    private Mat initialImage;
    private SimplePolygon2D biggestContour;
    private Point pointerHistoryPoint;

    HandDetector(DisplayEngine de, HandGestureGraphicRenderer gr, Mat initialImage) {
        displayEngine = de;
        graphicRenderer = gr;
        this.initialImage = initialImage;
    }

    public Point getHandPoint(Mat image, BufferedImage screenImage) {
        Point pointer = new Point();
        Mat img = image.clone();
        //resize image to speedup
        Core.absdiff(image, initialImage, image);
        screenImage = new BufferedImage(screenImage.getWidth(), screenImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        byte[] pixels = ((DataBufferByte) screenImage.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(screenImage.getHeight(), screenImage.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        Core.absdiff(image, mat, image);

        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(image, image, IMG_THRESHOLD_VAL, 255, Imgproc.THRESH_BINARY);
        graphicRenderer.drawImageOnInfoPanel(image, 640, 0, 2);
        ArrayList<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.size() > 0) {
            List<Point> transformedBiggestContour = getBiggestContour(contours);
            if (transformedBiggestContour != null) {
                ArrayList<Point> transformedHandPoints = displayEngine.removeBoarderPoints(new ArrayList<>(transformedBiggestContour));
                graphicRenderer.drawShape(transformedHandPoints, Color.PINK,640,240,4);
                pointer = smoothenPoint(computeHandInfo(transformedHandPoints));
//                graphicRenderer.drawPointsOnScreen(transformedHandPoints);
//                if (pointer != null) {
//                    graphicRenderer.drawPointerOnScreen(pointer);
//                }
            }
        }
        return pointer;
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
            return new Point(distantPoint.x(), distantPoint.y());
        } else {
            return new Point(0, 0);
        }
    }

    private Point smoothenPoint(Point point) {

        if (pointerHistoryPoint != null) {
//            double distance = math.geom2d.Point2D.distance(pointerHistoryPoint.x, pointerHistoryPoint.y, point.x, point.y);
//            if (distance < 300) {
//                pointerHistoryPoint = point;
//                return point;
//            } else {
//                return pointerHistoryPoint;
//            }
            pointerHistoryPoint = new Point((POINTER_SMOOTH_RATIO * pointerHistoryPoint.x + (1 - POINTER_SMOOTH_RATIO) * point.x),
                    (POINTER_SMOOTH_RATIO * pointerHistoryPoint.y + (1 - POINTER_SMOOTH_RATIO) * point.y));
        } else {
            pointerHistoryPoint = point;
        }
        return pointerHistoryPoint;

    }
}
