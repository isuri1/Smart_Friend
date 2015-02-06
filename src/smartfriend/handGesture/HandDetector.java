/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import math.geom2d.Point2D;
import math.geom2d.polygon.SimplePolygon2D;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import smartfriend.gui.HandGestureGraphicRenderer;
import smartfriend.util.general.Consts;

/**
 *
 * @author Meuru
 */
public class HandDetector {

    private static final int IMG_THRESHOLD_VAL = 75;
    private static final double POINTER_SMOOTH_RATIO = 0.5;
    private HandGestureGraphicRenderer graphicRenderer;
    private DisplayEngine displayEngine;
    private Mat initialImage;
    private SimplePolygon2D biggestContour;
    private Point pointerHistoryPoint;
    private Point cog;
    private ArrayList<Point> handHull;
    private int palmSize;

    HandDetector(DisplayEngine de, HandGestureGraphicRenderer gr, Mat initialImageMat) {
        displayEngine = de;
        graphicRenderer = gr;
        graphicRenderer.wipeScreen();
        this.initialImage = initialImageMat;

        graphicRenderer.drawImageOnInfoPanel(initialImage, 960, 0 + 50, 2);

        graphicRenderer.startGraphicRendererThread();
        cog = new Point();

    }

    public Point getHandPoint(Mat image, BufferedImage screenImage) {
//        graphicRenderer.drawImageOnInfoPanel(screenImage, 960, 0, 2);

        Point pointer = new Point();
        Mat img = image.clone();
        //resize image to speedup
        Core.absdiff(image, initialImage, image);

        graphicRenderer.drawImageOnInfoPanel(screenImage, 960, 240 + 50, 2);

        //graphicRenderer.drawImageOnInfoPanel(screenImage, 960, 0, 2);
        if (Consts.saveImage) {
            Camera.saveImage(graphicRenderer.convertToMat(screenImage), "screenImage");
            Consts.saveImage = false;
        }
        Core.absdiff(image, graphicRenderer.convertToMat(screenImage), image);
        graphicRenderer.drawImageOnInfoPanel(image, 960, 480 + 50, 2);

        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(image, image, IMG_THRESHOLD_VAL, 255, Imgproc.THRESH_BINARY);
        graphicRenderer.drawImageOnInfoPanel(image, 640, 0 + 50, 2);
        ArrayList<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.size() > 0) {
            ArrayList<Point> transformedBiggestContour = getBiggestContour(contours);
            if (transformedBiggestContour != null) {
                transformedBiggestContour.removeAll(removeBoarderPoints((ArrayList<Point>) transformedBiggestContour.clone()));
//                ArrayList<Point> transformedHandPoints = displayEngine.removeBoarderPoints(new ArrayList<>(transformedBiggestContour));
                graphicRenderer.drawShape(transformedBiggestContour, handHull, cog, palmSize, Color.PINK, 640, 240 + 50, 4);
//                graphicRenderer.drawShape(handHull, cog, palmSize, Color.PINK, 640, 240 + 50, 4);

                pointer = smoothenPoint(computeHandInfo(transformedBiggestContour));
//                graphicRenderer.drawPointsOnScreen(transformedHandPoints);
//                if (pointer != null) {
//                    graphicRenderer.drawPointerOnScreen(pointer);
//                }
            }
        }
        return pointer;
    }

    private ArrayList<Point> removeBoarderPoints(ArrayList<Point> pointsList) {
        Collections.sort(pointsList, new Comparator<Point>() {
            @Override
            public int compare(Point pt1, Point pt2) {
                return Double.compare(Math.max(pt1.x / Consts.SCREEN_WIDHT, pt1.y / Consts.SCREEN_HEIGHT),
                        Math.max(pt2.x / Consts.SCREEN_WIDHT, pt2.y / Consts.SCREEN_HEIGHT));
            }
        });
        int size = pointsList.size() * 8 / 10;
        return new ArrayList<>(pointsList.subList(size, pointsList.size()));
    }

    private ArrayList<Point> getBiggestContour(ArrayList<MatOfPoint> contours) {
        ArrayList<Point> biggestPoints = null;
        SimplePolygon2D biggestPolygon = null;
        MatOfInt hull = new MatOfInt();
        MatOfPoint biggestMatOfPoint = null;
        for (MatOfPoint points : contours) {
            ArrayList<Point> pointsArrayList = displayEngine.transformAndRemovePoints(new ArrayList<>(points.toList()));
            if (pointsArrayList.size() < 3) {
                continue;
            }
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(pointsArrayList.toArray(new Point[0]));
            Imgproc.approxPolyDP(matOfPoint2f, matOfPoint2f, 10, true);

            ArrayList<Point2D> point2DList = new ArrayList<>();
            for (Point pt : points.toList()) {
                point2DList.add(new Point2D(pt.x, pt.y));
            }
            SimplePolygon2D polygon = new SimplePolygon2D(point2DList);
            if (biggestPolygon == null || Math.abs(biggestPolygon.area()) < Math.abs(polygon.area())) {
                biggestPolygon = polygon;
                biggestMatOfPoint = points;
                biggestPoints = new ArrayList<>(matOfPoint2f.toList());

            }
        }

        Imgproc.convexHull(biggestMatOfPoint, hull);

        if (hull.toArray().length > 0) {
//            System.out.println("@@@@@@@@@@@@ " + hull.toArray().length);
            handHull = new ArrayList<>();
            for (int i : hull.toArray()) {
                System.out.print("    " + i);
                // handHull.add(biggestPoints.get(i));
            }
            handHull = new ArrayList<>();
            for (int i = 0; i < hull.size().height; i++) {
                int index = (int) hull.get(i, 0)[0];
//                double[] point = new double[]{
//                    biggestMatOfPoint.get(index, 0)[0], biggestMatOfPoint.get(index, 0)[1]
//                };
                //System.out.print("   " + biggestMatOfPoint.get(index, 0)[0] + "," + biggestMatOfPoint.get(index, 0)[1]);
                handHull.add(new Point(new double[]{
                            biggestMatOfPoint.get(index, 0)[0], biggestMatOfPoint.get(index, 0)[1]
                        }));
            }
            handHull = displayEngine.transformAndRemovePoints(handHull);
        }
        this.biggestContour = biggestPolygon;
        return biggestPoints;

    }

    private Point computeHandInfo(ArrayList<Point> points) {
        if (points.size() > 2) {
            ArrayList<Point2D> point2DList;
            SimplePolygon2D polygon;
            Point2D centorid, distantPoint = new Point2D();
            double distance = 0;

            point2DList = new ArrayList<>(points.size());
            for (Point pt : points) {
                point2DList.add(new Point2D(pt.x, pt.y));
            }
            polygon = new SimplePolygon2D(point2DList);
            centorid = polygon.centroid();
            cog.x = centorid.getX();
            cog.y = centorid.getY();
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
