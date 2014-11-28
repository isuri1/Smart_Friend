/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;

/**
 *
 * @author Meuru
 */
public class HandGestureGraphicRenderer {
    
    private Graphics2D infoPanel;
    private HandGestureDisplayPanel screenPanel;
    private GUIForm basePanel;
    
    public HandGestureGraphicRenderer(GUIForm base, Graphics2D graphics2D) {
        basePanel = base;
        infoPanel = graphics2D;
        
        Container c = base.getContentPane();
        //.setLayout(new BorderLayout());
        screenPanel = new HandGestureDisplayPanel(); // the webcam pictures and drums appear here
        c.add(screenPanel, BorderLayout.CENTER);
//        c.add(screenPanel);
    }
    
    public void wipeScreen() {
        screenPanel.setVisible(false);
    }
    
    public void resetScreen() {
        screenPanel.setVisible(true);
    }
    
    public void drawPointerOnScreen(Point point) {
        screenPanel.drawPointer(point);
        //mouse.moveMousePointer((point));
        //System.out.println("Drawing points at x :" + point.x + "  y: " + point.y);
        //infoPanel.add(new JLabel(new ImageIcon("C:\\Users\\Meuru\\Desktop\\Untitled-1.png")));
        //image = transformPicture(mouse.getScreenShot());
        //jPanel.repaint();
    }
    
    public void drawPointsOnScreen(ArrayList<Point> points) {
        if (points != null) {
            screenPanel.setHandPoints(points);
        }
    }
    
    public void drawPoints(Mat image, List<Point> points, Color color) {
        infoPanel.drawImage(getImage(image), 0, 0, 640, 480, null);
        for (Point pt : points) {
            infoPanel.setColor(color);
            infoPanel.fillOval((int) pt.x - 5, (int) pt.y - 5, 10, 10);
        }
    }
    
    public void drawImage(Mat image) {
        infoPanel.drawImage(getImage(image), 0, 0, 640, 480, null);
    }
    
    public void drawPoint(Point pt, Color color) {
        infoPanel.setColor(color);
        infoPanel.fillOval((int) pt.x - 5, (int) pt.y - 5, 20, 20);
    }
    
    public void drawShape(Mat image, List<Point> points, Color color) {
        infoPanel.setColor(color);
        infoPanel.drawImage(getImage(image), 0, 0, 640, 480, null);
        
        
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
            infoPanel.fill(polygon);
            
            for (Point pt : points) {
                infoPanel.setColor(Color.CYAN);
                infoPanel.fillOval((int) pt.x - 5, (int) pt.y - 5, 10, 10);
            }
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
}
