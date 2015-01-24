/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.gui;

/**
 *
 * @author Meuru
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import smartfriend.util.general.Colors;
import smartfriend.util.general.Consts;

/**
 *
 * @author Meuru
 */
public class WelcomeScreen extends JPanel implements Runnable {

    BufferedImage source;
    JLabel kiteImageLabel;
    Point kitePosition;
    HandGestureGraphicRenderer graphicRenderer;
    private volatile boolean active = true;

    public WelcomeScreen(final HandGestureGraphicRenderer graphicRenderer) {
        setSize(Consts.SCREEN_WIDHT, Consts.SCREEN_HEIGHT);
        setOpaque(true);
        repaint();

        setLayout(null);
        this.graphicRenderer = graphicRenderer;
        //welcome text
        JLabel welcomeText = new JLabel("Welcome");
        final Font font = new Font("Araial", Font.BOLD, 150);
        welcomeText.setFont(font);
        welcomeText.setForeground(Color.WHITE);
        add(welcomeText);
        welcomeText.setBounds((Consts.SCREEN_WIDHT - welcomeText.getPreferredSize().width) / 2, 150, welcomeText.getPreferredSize().width, welcomeText.getPreferredSize().height);

        //kite
        kitePosition = new Point();
        try {
            BufferedImage kiteImage = ImageIO.read(new File(Consts.KITE_IMAGE));
            kiteImageLabel = new JLabel(new ImageIcon(kiteImage));
            add(kiteImageLabel);
            kiteImageLabel.setBounds(0, 0, kiteImageLabel.getPreferredSize().width, kiteImageLabel.getPreferredSize().height);
        } catch (IOException ex) {
            Logger.getLogger(WelcomeScreen.class.getName()).log(Level.SEVERE, null, ex);
        }

        //button
        Button testButton = new Button("Enter", Color.decode(Colors.YELLOW), Color.decode(Colors.DULL_PINK));
        add(testButton, 2);
        System.out.println("$$$ " + testButton.getPreferredSize());
        testButton.setBounds((Consts.SCREEN_WIDHT - testButton.getPreferredSize().width) / 2, 350, testButton.getPreferredSize().width, testButton.getPreferredSize().height);

//        Button testButton2 = new Button("Enter", Color.decode(Colors.DULL_GREEN), Color.decode(Colors.DULL_PINK));
//        //testButton2.setPreferredSize(new Dimension(100, 500));
//        add(testButton2,1);
//        System.out.println("$$$ " + testButton2.getPreferredSize());
//        
//        testButton2.setBounds((Consts.SCREEN_WIDHT - testButton.getPreferredSize().width) / 2, 550, testButton.getPreferredSize().width, testButton.getPreferredSize().height);
//       
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                graphicRenderer.showMainScreen();
                active = false;
            }
        });

        new Thread(this).start();

    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        try {
            g2.drawImage(ImageIO.read(new File(Consts.WELCOME_IMAGE)), 0, 0, this);
        } catch (IOException ex) {
            Logger.getLogger(WelcomeScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public static void main(String[] args) {
//        f = new JFrame();
//        f.setUndecorated(true);
//        //f.setOpacity(0.5f);
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        JPanel panel = new WelcomeScreen();
//        f.add(panel);
////        f.add(new MainScreen(),-1);
//        f.setSize(panel.getSize());
//        //f.setLocation(100, 100);
//        f.setVisible(true);
//
//        f.addKeyListener(new KeyAdapter() {
//            public void keyPressed(KeyEvent e) {
//                int keyCode = e.getKeyCode();
//                if ((keyCode == KeyEvent.VK_NUMPAD5) || (keyCode == KeyEvent.VK_ENTER)
//                        || (keyCode == KeyEvent.VK_SPACE)) // take a snap when press NUMPAD-5, enter, or space is pressed
//                {
//                    System.out.println("DDDDDD");
//                    System.exit(0);
//                }
//            }
//        });
//    }
    @Override
    public void run() {
        while (active) {
            if (kiteImageLabel.getBounds().x == kitePosition.x & kiteImageLabel.getBounds().y == kitePosition.y) {
                kitePosition.x = (int) (930 * Math.random());
                kitePosition.y = (int) (550 * Math.random());
            } else {
                Rectangle kiteBounds = kiteImageLabel.getBounds();
                if (kitePosition.x > kiteBounds.x) {
                    kiteBounds.x += 1;
                } else if (kitePosition.x < kiteBounds.x) {
                    kiteBounds.x -= 1;
                }
                if (kitePosition.y > kiteBounds.y) {
                    kiteBounds.y += 1;
                } else if (kitePosition.y < kiteBounds.y) {
                    kiteBounds.y -= 1;
                }
                kiteImageLabel.setBounds(kiteBounds);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    Logger.getLogger(WelcomeScreen.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
