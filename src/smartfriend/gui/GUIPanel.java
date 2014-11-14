/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JPanel;

/**
 *
 * @author Meuru
 */
public class GUIPanel extends JPanel{
    
    private Point CureserPoint;
    private Random random;
    private ArrayList<Point> handPoints;
    int x = 700, y = 500;

    public GUIPanel() {
        random = new Random();
    }

    public void drawPointer(Point point) {
        this.CureserPoint = point;
        this.setBackground(Color.WHITE);
        repaint();
    }

    public void setHandPoints(ArrayList<Point> points) {
        this.handPoints = points;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2d.setColor(Color.RED);
        g2d.fillRect(3, 3, this.getWidth() - 6, this.getHeight() - 6);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(6, 6, this.getWidth() - 12, this.getHeight() - 12);


        if (CureserPoint != null) {
            drawRandomRectangle(g2d);
            g2d.setPaint(Color.RED);
            g2d.fillOval(CureserPoint.x - 10, CureserPoint.y - 10, 20, 20);
        }
        g2d.setColor(Color.LIGHT_GRAY);
        if (handPoints != null && handPoints.size() > 0) {
            int x1Points[] = new int[handPoints.size()];
            int y1Points[] = new int[handPoints.size()];
            for (int i = 0; i < handPoints.size(); i++) {
                //g2d.fillOval(pt.x - 10, pt.y - 10, 20, 20);
                Point pt = handPoints.get(i);
                x1Points[i] = pt.x;
                y1Points[i] = pt.y;
            }

            GeneralPath polygon =
                    new GeneralPath(GeneralPath.WIND_EVEN_ODD,
                    x1Points.length);
            polygon.moveTo(x1Points[0], y1Points[0]);

            for (int index = 1; index < x1Points.length; index++) {
                polygon.lineTo(x1Points[index], y1Points[index]);
            }
            polygon.closePath();
            g2d.fill(polygon);
        }
    }

    public void drawRandomRectangle(Graphics2D g) {
        if (CureserPoint.x > x & CureserPoint.x < x + 100 & CureserPoint.y > y & CureserPoint.y < y + 100) {
            x = (int) (random.nextInt(this.getWidth() - 100));
            y = (int) (random.nextInt(this.getHeight() - 100));
        }
        g.setColor(Color.yellow);
        g.fillRect(x, y, 100, 100);
    }

    
}
