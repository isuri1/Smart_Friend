/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import smartfriend.handGesture.HandGestureRecongnition;
import smartfriend.util.general.Consts;

/**
 *
 * @author Meuru
 */
public class HandGestureGraphicRenderer implements Runnable {

    private JFrame infoPanel;
    private Graphics2D infoPanelGraphics2D;
    private HandGestureDisplayPanel screenPanel;
//    private GUIForm basePanel;

    public HandGestureGraphicRenderer(GUIForm base) {
        infoPanel = setUpInfoPanel();
//        basePanel = base;
        infoPanelGraphics2D = (Graphics2D) infoPanel.getGraphics();

        Container c = base.getContentPane();

        JPanel testPanel = new TestPanel();
        screenPanel = new HandGestureDisplayPanel(); 
        base.setGlassPane(screenPanel);
        base.getGlassPane().setVisible(true);
        c.add(testPanel, -1);

        new Thread(this).start();
    }

    private JFrame setUpInfoPanel() {
        JFrame infoPanel = new JFrame("Info Panel");
        infoPanel.getContentPane().setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        infoPanel.getContentPane().add(jPanel, BorderLayout.CENTER);
        infoPanel.setSize(1280, 600);
        infoPanel.setVisible(true);
        infoPanel.setLayout(new BorderLayout());
        return infoPanel;
    }

    public void wipeScreen() {
        screenPanel.setVisible(false);
    }

    public void resetScreen() {
        screenPanel.setVisible(true);
    }

    public void drawPointerOnScreen(Point point) {
        screenPanel.drawPointer(point);
//        drawCircleOnScreen(point);
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

    public void drawCircleOnScreen(Point point) {
        if (point != null) {
            screenPanel.getGraphics().setColor(Color.RED);
            screenPanel.getGraphics().drawOval((int) point.x - 150, (int) point.y - 150, 300, 300);
        }
    }

    public void drawPointsOnInfoPanel(Mat image, List<Point> points, Color color, int downScale) {
        infoPanelGraphics2D.drawImage(getImage(image), 0, 0, Consts.CAMERA_WIDTH / downScale, Consts.CAMERA_HEIGHT / downScale, null);
        for (Point pt : points) {
            infoPanelGraphics2D.setColor(color);
            infoPanelGraphics2D.fillOval((int) pt.x / downScale - 5, (int) pt.y / downScale - 5, 10, 10);
        }
    }

    public void drawImageOnInfoPanel(Mat image, int downScale) {
        infoPanelGraphics2D.drawImage(getImage(image), 0, 0, Consts.CAMERA_WIDTH / downScale, Consts.CAMERA_HEIGHT / downScale, null);
    }

    public void drawImageOnInfoPanel(Mat image, int x, int y, int downScale) {
        infoPanelGraphics2D.drawImage(getImage(image), x, y, Consts.CAMERA_WIDTH / downScale, Consts.CAMERA_HEIGHT / downScale, null);
    }

    public void drawImageOnInfoPanel(BufferedImage image, int downScale) {
        infoPanelGraphics2D.drawImage(image, 0, 0, Consts.CAMERA_WIDTH / downScale, Consts.CAMERA_HEIGHT / downScale, null);
    }

    public void drawPoint(Point pt, Color color) {
        infoPanelGraphics2D.setColor(color);
        infoPanelGraphics2D.fillOval((int) pt.x - 5, (int) pt.y - 5, 20, 20);
    }

    public void drawShape(List<Point> points, Color color, int x, int y, int downScale) {
        infoPanelGraphics2D.setColor(Color.YELLOW);
        infoPanelGraphics2D.fillRect(x, y, screenPanel.getSize().width/downScale, screenPanel.getSize().height/downScale);
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

    @Override
    public void run() {
        while (true) {
            try {
                screenPanel.repaint();
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                Logger.getLogger(HandGestureDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
