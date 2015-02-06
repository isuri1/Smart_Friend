/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import smartfriend.util.general.Consts;

/**
 *
 * @author Meuru
 */
public class tester {

    private static final int HAND_POINTS_APPROXIMATION = 75;
    // used for simiplifying the defects list
    private static final int MIN_FINGER_DEPTH = 20000;
    private static final int MAX_FINGER_ANGLE = 60;   // degrees
    // angle ranges of thumb and index finger of the left hand relative to its COG
    private static final int MIN_THUMB = 120;
    private static final int MAX_THUMB = 200;
    private static final int MIN_INDEX = 60;
    private static final int MAX_INDEX = 120;
    JFrame infoPanel;
    Graphics2D infoPanelGraphics2D;
    private SimplePolygon2D biggestContour;
    DisplayEngine displayEngine;
    private Point cog;
    private int contourAxisAngle;
    private ArrayList<Point> handHull;
    private int palmSize;
    private ArrayList<Point> fingerTips;
    private ArrayList<FingerName> namedFingers;
    private Point curserPoint; //point of the index finger

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

            displayEngine = new DisplayEngine(convertToMat(initialImage), new Dimension(1280, 1024));

            Mat img = convertToMat(initialImage);
            Core.absdiff(convertToMat(initialImageBlack), convertToMat(handImage), img);
            drawImageOnInfoPanel(getImage(img), 0, 280, 2);
            Core.absdiff(img, convertToMat(screenImage), img);
            drawImageOnInfoPanel(getImage(img), 320, 280, 2);

            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(img, img, 50, 255, Imgproc.THRESH_BINARY);
            drawImageOnInfoPanel(getImage(img), 640, 280, 2);
            //resize image to speedup

            //CODE FOR FINGER IDENTIFICATION
            ArrayList<MatOfPoint> contours = new ArrayList<>();
            handHull = new ArrayList<>();
            fingerTips = new ArrayList<>();
            cog = new Point();
            namedFingers = new ArrayList<FingerName>();

            Imgproc.findContours(img, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            MatOfPoint transformedBiggestContour = getBiggestContourMatOfPoint(contours);

            //MatOfPoint handConvexHull = getConvexHull(transformedBiggestContour);
            computeHandInfo2(convertToMat(drawShapeToImage(convertToArrayList(transformedBiggestContour), new Dimension(1280, 1024))));

            findFingerTips(transformedBiggestContour);
            nameFingers(cog, contourAxisAngle, fingerTips);

            drawShape(convertToArrayList(transformedBiggestContour), handHull, cog, 1, Color.PINK, 960, 40, 4);




        } catch (IOException ex) {
            Logger.getLogger(tester.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private ArrayList<Point> convertToArrayList(MatOfPoint matOfPoint) {
        return new ArrayList<>(matOfPoint.toList());
    }

    private ArrayList<Point> getBiggestContour(ArrayList<MatOfPoint> contours) {
        ArrayList<Point> biggestPoints = null;
        SimplePolygon2D biggestPolygon = null;
        for (MatOfPoint points : contours) {
            ArrayList<Point> pointsArrayList = displayEngine.transformAndRemovePoints(new ArrayList<>(points.toList()));
            if (pointsArrayList.size() < 3) {
                continue;
            }
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(pointsArrayList.toArray(new Point[0]));
            Imgproc.approxPolyDP(matOfPoint2f, matOfPoint2f, HAND_POINTS_APPROXIMATION, true);

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

    public void drawImageOnInfoPanel(BufferedImage image, int x, int y, int downScale) {
        infoPanelGraphics2D.drawImage(image, x, y, Consts.CAMERA_WIDTH / downScale, Consts.CAMERA_HEIGHT / downScale, null);
    }

    public void drawShape(List<Point> points, List<Point> convexHull, Point cog, int distance, Color color, int x, int y, int downScale) {
        infoPanelGraphics2D.setColor(Color.WHITE);
        infoPanelGraphics2D.fillRect(x, y, 1280 / downScale, 1024 / downScale);
        infoPanelGraphics2D.setColor(color);
        if (points.size() > 0) {
            int x1Points[] = new int[points.size()];
            int y1Points[] = new int[points.size()];
            for (int i = 0; i < points.size(); i++) {
                //g2d.fillOval(pt.x - 10, pt.y - 10, 20, 20);
                Point pt = points.get(i);
                x1Points[i] = x + (int) pt.x / downScale;
                y1Points[i] = y + (int) pt.y / downScale;
            }

            GeneralPath polygon =
                    new GeneralPath(GeneralPath.WIND_EVEN_ODD,
                    x1Points.length);
            polygon.moveTo(x1Points[0], y1Points[0]);

            for (int index = 1; index < x1Points.length; index++) {
                polygon.lineTo(x1Points[index], y1Points[index]);
            }
            polygon.closePath();
            infoPanelGraphics2D.fill(polygon);

            for (Point pt : points) {
                infoPanelGraphics2D.setColor(Color.CYAN);
                infoPanelGraphics2D.fillOval(x + (int) pt.x / downScale - 5, y + (int) pt.y / downScale - 5, 10, 10);
            }
            for (Point pt : convexHull) {
                infoPanelGraphics2D.setColor(Color.black);
                infoPanelGraphics2D.fillOval(x + (int) pt.x / downScale - 5, y + (int) pt.y / downScale - 5, 10, 10);
            }

            infoPanelGraphics2D.setColor(Color.RED);
            infoPanelGraphics2D.fillOval(x + (int) cog.x / downScale - 5, y + (int) cog.y / downScale - 5, 10, 10);
            infoPanelGraphics2D.drawOval(x + (int) cog.x / downScale, y + (int) cog.y / downScale, distance, distance);

            for (int i = 0; i < fingerTips.size(); i++) {
                System.out.println("printing " + namedFingers.get(i));
                Point pt = fingerTips.get(i);
                if (namedFingers.get(i) == FingerName.UNKNOWN) {
                    infoPanelGraphics2D.setPaint(Color.RED);   // unnamed finger tip is red
                    infoPanelGraphics2D.drawOval(x + (int) pt.x / downScale - 8, y + (int) pt.y / downScale - 8, 16, 16);
                    infoPanelGraphics2D.drawString("" + i, x + (int) pt.x / downScale, y + (int) pt.y / downScale - 10);   // label it with a digit
                    System.out.println("@@@@ " + namedFingers.get(i));
                } else {   // draw yellow line to the named finger tip from COG
                    infoPanelGraphics2D.setPaint(Color.YELLOW);
                    infoPanelGraphics2D.drawLine(x + (int) cog.x / downScale, y + (int) cog.y / downScale, x + (int) pt.x / downScale, y + (int) pt.y / downScale);

                    infoPanelGraphics2D.setPaint(Color.GREEN);   // named finger tip is green
                    infoPanelGraphics2D.drawOval(x + (int) pt.x / downScale - 8, y + (int) pt.y / downScale - 8, 16, 16);
                    infoPanelGraphics2D.drawString(namedFingers.get(i).toString().toLowerCase(), x + (int) pt.x / downScale, y + (int) pt.y / downScale - 10);
                    if (namedFingers.get(i).toString().toLowerCase().contains("index")) {
                        curserPoint = pt;
                    }
                    System.out.println("@@@@ " + namedFingers.get(i));
                }
            }
        }
    }

    private void drawPoints(Point[] startPoints, Point[] endPoints, int x, int y, int downScale) {
        infoPanelGraphics2D.setColor(Color.YELLOW);

        for (Point pt : startPoints) {
//            System.out.println("Drawing start point  " + pt);
            infoPanelGraphics2D.setColor(Color.BLACK);
            infoPanelGraphics2D.fillOval(x + (int) pt.x / downScale - 5, y + (int) pt.y / downScale - 5, 10, 10);
        }
        for (Point pt : endPoints) {
//            System.out.println("Drawing end  point  " + pt);            
            infoPanelGraphics2D.setColor(Color.orange);
            infoPanelGraphics2D.fillOval(x + (int) pt.x / downScale - 3, y + (int) pt.y / downScale - 3, 6, 10);
        }
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

    /* Return integer degree angle of contour's major axis relative to the horizontal, 
     assuming that the positive y-axis goes down the screen. 
     */
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
    }  // end of calculateTilt()

    private void computeHandInfo2(Mat img) {
        Mat newMat = new Mat();
        Imgproc.cvtColor(img, newMat, Imgproc.COLOR_BGR2GRAY);
        Moments m = Imgproc.moments(newMat);
        cog.x = m.get_m10() / m.get_m00();
        cog.y = m.get_m01() / m.get_m00();
//        System.out.println(cog);
//        System.out.println(calculateTilt(m.get_m11(), m.get_m20(), m.get_m02()));
        contourAxisAngle = calculateTilt(m.get_m11(), m.get_m20(), m.get_m02());
//        /* uses fingertips information generated on the last update of
//         the hand, so will be out-of-date */
//        if (fingerTips.size() > 0) {
//            int yTotal = 0;
//            for (java.awt.Point pt : fingerTips) {
//                yTotal += pt.y;
//            }
//            int avgYFinger = yTotal / fingerTips.size();
//            if (avgYFinger > cogPt.y) // fingers below COG
//            {
//                contourAxisAngle += 180;
//            }
//        }
//        contourAxisAngle = 180 - contourAxisAngle;
//        /* this makes the angle relative to a positive y-axis that
//         runs up the screen */
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

    private void findFingerTips(MatOfPoint biggestContour) {
        MatOfInt hull = new MatOfInt();
        MatOfInt4 convexityDefects = new MatOfInt4();

        Imgproc.convexHull(biggestContour, hull);
        Imgproc.convexityDefects(biggestContour, hull, convexityDefects);
        extractContourInfo(biggestContour, hull, handHull);

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

    private ArrayList<Point> getConvexHull(ArrayList<Point> transformedBiggestContour) {
        MatOfPoint biggestMatOfPoint;
        MatOfInt hull = new MatOfInt();
        ArrayList<Point> handConvexHull = null;
        biggestMatOfPoint = new MatOfPoint(transformedBiggestContour.toArray(new Point[0]));
        Imgproc.convexHull(biggestMatOfPoint, hull);

        if (hull.toArray().length > 0) {
            handConvexHull = new ArrayList<>();
            for (int i = 0; i < hull.size().height; i++) {
                int index = (int) hull.get(i, 0)[0];
                handConvexHull.add(new Point(new double[]{
                            biggestMatOfPoint.get(index, 0)[0], biggestMatOfPoint.get(index, 0)[1]
                        }));
            }
        }
        return handConvexHull;
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

    /* Use the finger tip coordinates, and the comtour's COG and axis angle to horizontal
     to label the fingers.

     Try to label the thumb and index based on their likely angle ranges
     relative to the COG. This assumes that the thumb and index finger are on the
     left side of the hand.

     Then label the other fingers based on the order of the names in the FingerName class
     */
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

    // attempt to label all the unknown fingers in the list
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

    private void labelPrev(ArrayList<FingerName> nms, int i, FingerName name) // move backwards through fingers list labelling unknown fingers
    {
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
}
