/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.media.jai.PerspectiveTransform;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Meuru
 */
public class DisplayEngine {

    private static final int IMG_THRESHOLD_VAL = 145;
    private ArrayList<Point> boundryPoints;
    private Dimension displaySize;
    private PerspectiveTransform perspectiveTransform;
    
    public DisplayEngine(Camera camera, Dimension displaySize, Graphics2D gui) {
        this.displaySize = displaySize;
        while (true) {
            boundryPoints = findBoundaries(camera.capturePhoto());
            if (boundryPoints.size() > 0) {
                boundryPoints = sortPoints(boundryPoints);
                for (Point pt : findBoundaries(camera.capturePhoto())) {
                    System.out.println(" x : " + pt.x + "   " + pt.y);
                }
                drawBoundyPoints(camera.capturePhoto(), boundryPoints, gui);
                setBoarderPoints(boundryPoints, displaySize);
                break;
            }
        }

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
                System.out.println(matOfPoint2f.size());
                ArrayList<Point> points  = new ArrayList<>();
                 //drawBoundyPoints(image, points, gui);
                if ((int) matOfPoint2f.size().height == 4) {
                    System.out.println("Ditected corner points");
                    boundryPoints.addAll(matOfPoint2f.toList());
                    break;
                }
            }
        }
        return boundryPoints;
    }

    private void drawBoundyPoints(Mat image, ArrayList<Point> points, Graphics2D gui) {
        gui.drawImage(getImage(image), 0, 0, 640, 480, null);
        for (Point pt : points) {
            gui.setColor(Color.GREEN);
            gui.fillOval((int) pt.x - 5, (int) pt.y - 5, 10, 10);
        }
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

    private void setBoarderPoints(ArrayList<Point> sortedBoarderPoints, Dimension displayDimension) {
        perspectiveTransform = PerspectiveTransform.getQuadToQuad(sortedBoarderPoints.get(0).x, sortedBoarderPoints.get(0).y, sortedBoarderPoints.get(1).x, sortedBoarderPoints.get(1).y,
                sortedBoarderPoints.get(2).x, sortedBoarderPoints.get(2).y, sortedBoarderPoints.get(3).x, sortedBoarderPoints.get(3).y,
                0, displayDimension.height, displayDimension.width, displayDimension.height, displayDimension.width, 0, 0, 0);
    }

    public Point transfromPoint(Point point) {
        float[] src = {(float) point.x, (float) point.y};
        float[] dst = {0, 0};
        perspectiveTransform.transform(src, 0, dst, 0, 1);
        return new Point((int) dst[0], (int) dst[1]); //screen is rotated
    }

    public ArrayList<Point> transfromPoints(ArrayList<Point> points) {
        for (int i = 0; i < points.size(); i++) {
            points.add(i, transfromPoint(points.remove(i)));
        }
        return points;
    }
}
