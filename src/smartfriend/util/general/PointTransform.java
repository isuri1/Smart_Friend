/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.util.general;

import java.awt.Dimension;
import java.util.ArrayList;
import javax.media.jai.PerspectiveTransform;
import org.opencv.core.Point;

/**
 *
 * @author Meuru
 */
public class PointTransform {

    private static PointTransform instance = null;
    private PerspectiveTransform perspectiveTransform;

    protected PointTransform() {
    }

    protected PointTransform(ArrayList<Point> sortedBoarderPoints, Dimension displayDimension) {
        perspectiveTransform = PerspectiveTransform.getQuadToQuad(sortedBoarderPoints.get(0).x, sortedBoarderPoints.get(0).y, sortedBoarderPoints.get(1).x, sortedBoarderPoints.get(1).y,
                sortedBoarderPoints.get(2).x, sortedBoarderPoints.get(2).y, sortedBoarderPoints.get(3).x, sortedBoarderPoints.get(3).y,
                displayDimension.width, displayDimension.height, displayDimension.width, 0, 0, 0, 0, displayDimension.height);
    }

    public static PointTransform getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("Point Transform Class has not been initialized");
        }
        return instance;
    }
    
    public static PointTransform initialize(ArrayList<Point> sortedBoarderPoints, Dimension displayDimension) {
        instance = new PointTransform(sortedBoarderPoints, displayDimension);
        return instance;
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
