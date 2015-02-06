/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import math.geom2d.Point2D;
import math.geom2d.polygon.SimplePolygon2D;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import smartfriend.gui.HandGestureGraphicRenderer;
import smartfriend.util.general.Consts;

/**
 *
 * @author Meuru
 */
public class HandDetector {

    private static final int IMG_THRESHOLD_VAL = 75;
    private static final int HAND_POINTS_APPROXIMATION = 75;
    private static final double POINTER_SMOOTH_RATIO = 0.5;
    // used for simiplifying the defects list
    private static final int MIN_FINGER_DEPTH = 20000;
    private static final int MAX_FINGER_ANGLE = 60;   // degrees
    // angle ranges of thumb and index finger of the left hand relative to its COG
    private static final int MIN_THUMB = 120;
    private static final int MAX_THUMB = 200;
    private static final int MIN_INDEX = 60;
    private static final int MAX_INDEX = 120;
    private HandGestureGraphicRenderer graphicRenderer;
    private DisplayEngine displayEngine;
    private Mat initialImage;
    private SimplePolygon2D biggestContour;
    private Point pointerHistoryPoint;
    private Point cog;
    private ArrayList<Point> handHull;
    private int palmSize;
    private int contourAxisAngle;
    private ArrayList<Point> handConvexHull;
    private ArrayList<Point> fingerTips;
    private ArrayList<FingerName> namedFingers;
    private Point curserPoint; //point of the index finger

    HandDetector(DisplayEngine de, HandGestureGraphicRenderer gr, Mat initialImageMat) {
        displayEngine = de;
        graphicRenderer = gr;
        graphicRenderer.wipeScreen();
        this.initialImage = initialImageMat;

        graphicRenderer.drawImageOnInfoPanel(initialImage, 960, 0 + 50, 2);

        graphicRenderer.startGraphicRendererThread();
        cog = new Point();
    }

    /**
     * Computes the hand gesture information from the camera image and the
     * screen-shot image
     *
     * @param image Image acquired from the Camera
     * @param screenImage Screen-shot of the display
     * @return the mouse point from the hand pointer
     */
    public Point getHandPoint(Mat image, BufferedImage screenImage) {
        Point pointer = new Point();
        ArrayList<MatOfPoint> contours = new ArrayList<>();     //uses to hold the list of contours
        MatOfPoint transformedBiggestContour;             //uses to hold the biggest contour from list of contours

        handConvexHull = new ArrayList<>();
        fingerTips = new ArrayList<>();
        namedFingers = new ArrayList<FingerName>();


        //resize image to speedup
        Core.absdiff(image, initialImage, image);
        if (Consts.GRAPHICAL_DEBUG) {
            graphicRenderer.drawImageOnInfoPanel(screenImage, 960, 240 + 50, 2);
        }
        //graphicRenderer.drawImageOnInfoPanel(screenImage, 960, 0, 2);
        if (Consts.saveImage) {
            Camera.saveImage(graphicRenderer.convertToMat(screenImage), "screenImage");
            Consts.saveImage = false;
        }
        Core.absdiff(image, graphicRenderer.convertToMat(screenImage), image);
        if (Consts.GRAPHICAL_DEBUG) {
            graphicRenderer.drawImageOnInfoPanel(image, 960, 480 + 50, 2);
        }

        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(image, image, IMG_THRESHOLD_VAL, 255, Imgproc.THRESH_BINARY);
        if (Consts.GRAPHICAL_DEBUG) {
            graphicRenderer.drawImageOnInfoPanel(image, 640, 0 + 50, 2);
        }

        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.size() > 0) {
            transformedBiggestContour = getBiggestContourMatOfPoint(contours);
            if (transformedBiggestContour != null & transformedBiggestContour.height() > 3) {

                computeHandInfo(convertToMat(drawShapeToImage(convertToArrayList(transformedBiggestContour), new Dimension(Consts.SCREEN_WIDHT, Consts.SCREEN_HEIGHT))));
                findFingerTips(transformedBiggestContour);
                nameFingers(cog, contourAxisAngle, fingerTips);
                pointer = getPointer();

                if (Consts.GRAPHICAL_DEBUG & handConvexHull != null) {
                    graphicRenderer.drawHandInfo(convertToArrayList(transformedBiggestContour), handConvexHull, cog, palmSize, fingerTips, namedFingers, 640, 240 + 50, 4);
                }




//                transformedBiggestContour.removeAll(removeBoarderPoints((ArrayList<Point>) transformedBiggestContour.clone()));
//                graphicRenderer.drawShape(transformedBiggestContour, handHull, cog, palmSize, Color.PINK, 640, 240 + 50, 4);
//
//                pointer = smoothenPoint(computeHandInfo(transformedBiggestContour));
//                graphicRenderer.drawPointsOnScreen(transformedHandPoints);
//                if (pointer != null) {
//                    graphicRenderer.drawPointerOnScreen(pointer);
//                }
            }
        }
        return pointer;
    }

    // Returns the biggest contour
    private MatOfPoint getBiggestContourMatOfPoint(ArrayList<MatOfPoint> contours) {
        MatOfPoint2f biggestPoints = null;
        SimplePolygon2D biggestPolygon = null;
        for (MatOfPoint points : contours) {
            points = displayEngine.transformAndRemovePoints(points);
            if (points.size().height < 3) {
                continue;
            }
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(points.toArray());
            Imgproc.approxPolyDP(matOfPoint2f, matOfPoint2f, HAND_POINTS_APPROXIMATION, true);
            //TODO  optimize the area calculation method
            ArrayList<Point2D> point2DList = new ArrayList<>();
            for (Point pt : points.toList()) {
                point2DList.add(new Point2D(pt.x, pt.y));
            }
            SimplePolygon2D polygon = new SimplePolygon2D(point2DList);
            if (biggestPolygon == null || Math.abs(biggestPolygon.area()) < Math.abs(polygon.area())) {
                biggestPolygon = polygon;
                biggestPoints = matOfPoint2f;
            }
        }
        this.biggestContour = biggestPolygon;
        return new MatOfPoint(biggestPoints.toArray());

    }

    private ArrayList<Point> convertToArrayList(MatOfPoint matOfPoint) {
        return new ArrayList<>(matOfPoint.toList());
    }

    public BufferedImage drawShapeToImage(List<Point> points, Dimension dimension) {
        BufferedImage image =
                new BufferedImage(dimension.width, dimension.height,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        if (points.size() > 0) {
            int x1Points[] = new int[points.size()];
            int y1Points[] = new int[points.size()];
            for (int i = 0; i < points.size(); i++) {
                //g2d.fillOval(pt.x - 10, pt.y - 10, 20, 20);
                Point pt = points.get(i);
                x1Points[i] = (int) pt.x;
                y1Points[i] = (int) pt.y;
            }

            GeneralPath polygon =
                    new GeneralPath(GeneralPath.WIND_EVEN_ODD,
                    x1Points.length);
            polygon.moveTo(x1Points[0], y1Points[0]);

            for (int index = 1; index < x1Points.length; index++) {
                polygon.lineTo(x1Points[index], y1Points[index]);
            }
            polygon.closePath();
            g2.fill(polygon);
        }
        return image;
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

    private void computeHandInfo(Mat img) {
        Mat newMat = new Mat();
        Imgproc.cvtColor(img, newMat, Imgproc.COLOR_BGR2GRAY);
        Moments m = Imgproc.moments(newMat);
        cog.x = m.get_m10() / m.get_m00();
        cog.y = m.get_m01() / m.get_m00();
//        System.out.println(cog);
//        System.out.println(calculateTilt(m.get_m11(), m.get_m20(), m.get_m02()));
        contourAxisAngle = calculateTilt(m.get_m11(), m.get_m20(), m.get_m02());
        /* uses fingertips information generated on the last update of
         the hand, so will be out-of-date */
        if (fingerTips.size() > 0) {
            int yTotal = 0;
            for (Point pt : fingerTips) {
                yTotal += pt.y;
            }
            int avgYFinger = yTotal / fingerTips.size();
            if (avgYFinger > cog.y) // fingers below COG
            {
                contourAxisAngle += 180;
            }
        }
        contourAxisAngle = 180 - contourAxisAngle;
        /* this makes the angle relative to a positive y-axis that
         runs up the screen */
    }

    private int calculateTilt(double m11, double m20, double m02) {
        double diff = m20 - m02;
        if (diff == 0) {
            if (m11 == 0) {
                return 0;
            } else if (m11 > 0) {
                return 45;
            } else // m11 < 0
            {
                return -45;
            }
        }

        double theta = 0.5 * Math.atan2(2 * m11, diff);
        int tilt = (int) Math.round(Math.toDegrees(theta));

        if ((diff > 0) && (m11 == 0)) {
            return 0;
        } else if ((diff < 0) && (m11 == 0)) {
            return -90;
        } else if ((diff > 0) && (m11 > 0)) // 0 to 45 degrees
        {
            return tilt;
        } else if ((diff > 0) && (m11 < 0)) // -45 to 0
        {
            return (180 + tilt);   // change to counter-clockwise angle measure
        } else if ((diff < 0) && (m11 > 0)) // 45 to 90
        {
            return tilt;
        } else if ((diff < 0) && (m11 < 0)) // -90 to -45
        {
            return (180 + tilt);  // change to counter-clockwise angle measure
        }
        System.out.println("Error in moments for tilt angle");
        return 0;
    }

    private void findFingerTips(MatOfPoint biggestContour) {
        MatOfInt hull = new MatOfInt();
        MatOfInt4 convexityDefects = new MatOfInt4();

        Imgproc.convexHull(biggestContour, hull);
        Imgproc.convexityDefects(biggestContour, hull, convexityDefects);
        extractContourInfo(biggestContour, hull, handConvexHull);

        int convexityDefectsSize = convexityDefects.height();
        Point[] startPoints = new Point[convexityDefectsSize];
        Point[] endPoints = new Point[convexityDefectsSize];
        double[] fixptDepths = new double[convexityDefectsSize];

        for (int i = 0; i < convexityDefects.height(); i++) {
//            System.out.println(convexityDefects.get(i, 0)[0] + " - " + convexityDefects.get(i, 0)[1] + " - " + convexityDefects.get(i, 0)[2] + " - " + convexityDefects.get(i, 0)[3]);
            int startIndex = (int) convexityDefects.get(i, 0)[0];
            int endIndex = (int) convexityDefects.get(i, 0)[2];
            int farthestPtIndex = (int) convexityDefects.get(i, 0)[1];
            fixptDepths[i] = convexityDefects.get(i, 0)[3];

            startPoints[i] = new Point(biggestContour.get(startIndex, 0)[0], biggestContour.get(startIndex, 0)[1]);
            endPoints[i] = new Point(biggestContour.get(endIndex, 0)[0], biggestContour.get(endIndex, 0)[1]);
        }

        reduceTips(startPoints, endPoints, fixptDepths);
        //(startPoints, endPoints, 960, 40, 4);
    }

    private void extractContourInfo(MatOfPoint src, MatOfInt indexList, ArrayList<Point> dest) {
        dest.clear();
        if (indexList.height() > 0) {
            for (int i = 0; i < indexList.size().height; i++) {
                int index = (int) indexList.get(i, 0)[0];
                dest.add(i, new Point(src.get(index, 0)[0], src.get(index, 0)[1]));
            }
        }
    }

    private void reduceTips(Point[] tipPts, Point[] foldPts, double[] depths) /* Narrow in on 'real' finger tips by ignoring shallow defect depths, and tips
     which have too great an angle between their neighbouring fold points.

     Store the resulting finger tip coordinates in the global fingerTips list.
     */ {
        fingerTips.clear();
        int pointsSize = depths.length;
        for (int i = 0; i < pointsSize; i++) {
            if (depths[i] < MIN_FINGER_DEPTH) // defect too shallow
            {
//                System.out.println("too shallow depth " + i + "  " + depths[i]);
                continue;
            }
            // look at fold points on either side of a tip
            int pdx = (i == 0) ? (pointsSize - 1) : (i - 1);   // predecessor of i
            int sdx = (i == pointsSize - 1) ? 0 : (i + 1);     // successor of i
            int angle = angleBetween(tipPts[i], foldPts[pdx], foldPts[sdx]);
            if (angle >= MAX_FINGER_ANGLE) // angle between finger and folds too wide
            {
//                System.out.println("too wide angle " + i + "  " + angle);
                continue;
            }
            // this point probably is a finger tip, so add to list
            fingerTips.add(tipPts[i]);
        }
    }

    private int angleBetween(Point tip, Point next, Point prev) // calulate the angle between the tip and its neigbouring folds (in integer degrees)
    {
        return Math.abs((int) Math.round(
                Math.toDegrees(
                Math.atan2(next.x - tip.x, next.y - tip.y)
                - Math.atan2(prev.x - tip.x, prev.y - tip.y))));
    }

    private void nameFingers(Point cogPt, int contourAxisAngle, ArrayList<Point> fingerTips) { // reset all named fingers to unknown
        namedFingers.clear();
        for (int i = 0; i < fingerTips.size(); i++) {
            namedFingers.add(FingerName.UNKNOWN);
        }
        labelThumbIndex(fingerTips, namedFingers);

        // printFingers("named fingers", namedFingers);
        labelUnknowns(namedFingers);
        // printFingers("revised named fingers", namedFingers);
    }

    // attempt to label the thumb and index fingers of the hand
    private void labelThumbIndex(ArrayList<Point> fingerTips, ArrayList<FingerName> nms) {
        boolean foundThumb = false;
        boolean foundIndex = false;

        /* the thumb and index fingers will most likely be stored at the end
         of the list, since the contour hull was built in a counter-clockwise 
         order by the call to cvConvexHull2() in findFingerTips(), and I am assuming
         the thumb is on the left of the hand.
         So iterate backwards through the list.
         */
        int i = fingerTips.size() - 1;
        while ((i >= 0)) {
            int angle = angleToCOG(fingerTips.get(i), cog, contourAxisAngle);

            // check for thumb
            if ((angle <= MAX_THUMB) && (angle > MIN_THUMB) && !foundThumb) {
                nms.set(i, FingerName.THUMB);
                foundThumb = true;
            }

            // check for index
            if ((angle <= MAX_INDEX) && (angle > MIN_INDEX) && !foundIndex) {
                nms.set(i, FingerName.INDEX);
                foundIndex = true;
            }
            i--;
        }
    }

    private int angleToCOG(Point tipPt, Point cogPt, int contourAxisAngle) /* calculate angle of tip relative to the COG, remembering to add the
     hand contour angle so that the hand is orientated straight up */ {
        int yOffset = (int) (cogPt.y - tipPt.y);    // make y positive up screen
        int xOffset = (int) (tipPt.x - cogPt.x);
        // Point offsetPt = new Point(xOffset, yOffset);
        double theta = Math.atan2(yOffset, xOffset);
        int angleTip = (int) Math.round(Math.toDegrees(theta));
        int offsetAngleTip = angleTip + (90 - contourAxisAngle);
        // this addition ensures that the hand is orientated straight up
        return offsetAngleTip;
    }

    private void labelUnknowns(ArrayList<FingerName> nms) {
        // find first named finger
        int i = 0;
        while ((i < nms.size()) && (nms.get(i) == FingerName.UNKNOWN)) {
            i++;
        }
        if (i == nms.size()) // no named fingers found, so give up
        {
            return;
        }

        FingerName name = nms.get(i);
        labelPrev(nms, i, name);    // fill-in backwards
        labelFwd(nms, i, name);    // fill-in forwards
    }

    // move backwards through fingers list labelling unknown fingers
    private void labelPrev(ArrayList<FingerName> nms, int i, FingerName name) {
        i--;
        while ((i >= 0) && (name != FingerName.UNKNOWN)) {
            if (nms.get(i) == FingerName.UNKNOWN) {   // unknown finger
                name = name.getPrev();
                if (!usedName(nms, name)) {
                    nms.set(i, name);
                }
            } else // finger is named already
            {
                name = nms.get(i);
            }
            i--;
        }
    }

    // move forward through fingers list labelling unknown fingers
    private void labelFwd(ArrayList<FingerName> nms, int i, FingerName name) {
        i++;
        while ((i < nms.size()) && (name != FingerName.UNKNOWN)) {
            if (nms.get(i) == FingerName.UNKNOWN) {  // unknown finger
                name = name.getNext();
                if (!usedName(nms, name)) {
                    nms.set(i, name);
                }
            } else // finger is named already
            {
                name = nms.get(i);
            }
            i++;
        }
    }

    private boolean usedName(ArrayList<FingerName> nms, FingerName name) // does the fingers list contain name already?
    {
        for (FingerName fn : nms) {
            if (fn == name) {
                return true;
            }
        }
        return false;
    }

    private Point getPointer() {
        for (int i = 0; i < fingerTips.size(); i++) {
            if (namedFingers.get(i).toString().toLowerCase().contains("index")) {
                return fingerTips.get(i);
            }
        }
        return null;
    }

    //-----------------------------------------
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
