package smartfriend.applications;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import smartfriend.speechRecognition.SpeechRecognizer;
import smartfriend.tts.TextToSpeech;
import smartfriend.util.general.ImageXMLParser;
import smartfriend.util.general.MainConfiguration;

public class NumberApp extends JFrame {

    private JPanel foregroundPanel;
    private JLabel jLabel1, jLabel2, jLabel3, jLabel4, jLabel5, jLabel6;
    private JButton[] l = new JButton[12];
    private JTextField textField;
     String[] cardsArray = new String[11];
    private static int i = 1;
    private static int cellCount = 0;
    private ImageXMLParser xml;
     TextToSpeech talk;

    public NumberApp() {

        xml = new ImageXMLParser();
        talk = new TextToSpeech();
        
        JWindow window = new JWindow();
        window.getContentPane().add(
                new JLabel("", new ImageIcon(Toolkit.getDefaultToolkit().createImage(xml.getImageLocation(35))), SwingConstants.CENTER));
        window.setBounds(462, 170, 460, 420);
        window.setVisible(true);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        window.setVisible(false);
        
        try {
            foregroundPanel = new JPanel();
            foregroundPanel.setLayout(null);
            foregroundPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            foregroundPanel.setOpaque(false);

            JPanel panel2 = new JPanel();
            panel2.setLayout(null);
            foregroundPanel.add(panel2);
            panel2.setOpaque(false);
            panel2.setBounds(0, 0, 1350, 700);

            jLabel1 = new JLabel();
            jLabel1.setBounds(0, 0, 1500, 500);
            jLabel1.setLocation(5, -96);
            panel2.add(jLabel1);
            jLabel1.setIcon(new ImageIcon(ImageIO.read(new File(xml.getImageLocation(1)))));

            JPanel panel3 = new JPanel();
            panel3.setLayout(new GridLayout(2, 5));
            panel2.add(panel3);
            panel3.setOpaque(false);
            panel3.setBounds(0, 0, 180, 70);
            panel3.setLocation(700, 476);
            panel3.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

            jLabel4 = new JLabel();
            jLabel4.setBounds(0, 0, 140, 200);
            jLabel4.setLocation(77, 500);
            panel2.add(jLabel4);

            jLabel5 = new JLabel();
            jLabel5.setBounds(0, 0, 240, 175);
            jLabel5.setLocation(65, 530);
            panel2.add(jLabel5);

            jLabel6 = new JLabel();
            jLabel6.setBounds(0, 0, 240, 175);
            jLabel6.setLocation(1100, 10);
            panel2.add(jLabel6);

            for (int k = 1; k <= 10; k++) {
                l[k] = new JButton();
                l[k].setSize(100, 100);
                l[k].setBorder(BorderFactory.createLineBorder(Color.GRAY));
                panel3.add(l[k]);
                l[k].setBackground(Color.WHITE);
                final int h = k;
                l[k].addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        Random random = new Random();
                        final float hue = random.nextFloat();
                        final float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
                        final float luminance = 1.0f; //1.0 for brighter, 0.0 for black
                        Color color = Color.getHSBColor(hue, saturation, luminance);
                        if (!(l[h].getBackground().equals(Color.WHITE))) {
                            l[h].setBackground(Color.WHITE);
                        } else {
                            l[h].setBackground(color);
                        }
                        cellCount++;
                        if (cellCount == i) {
                            jLabel4.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(xml.getImageLocation(33))));
                            jLabel5.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(xml.getImageLocation(32))));
                            jLabel6.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(xml.getImageLocation(34))));
                        }
                    }
                });
            }

            jLabel2 = new JLabel();
            jLabel2.setBounds(0, 0, 700, 700);
            jLabel2.setLocation(437, 30);
            panel2.add(jLabel2);
            jLabel2.setIcon(new ImageIcon(ImageIO.read(new File(xml.getImageLocation(11)))));

            jLabel3 = new JLabel();
            jLabel3.setBounds(0, 0, 700, 700);
            jLabel3.setLocation(1030, 140);
            panel2.add(jLabel3);
            jLabel3.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(xml.getImageLocation(20))));

            textField = new JTextField();
            textField.setFont(new Font("Orator Std", 1, 40)); // NOI18N
            textField.setOpaque(false);
            textField.setBounds(340, 280, 300, 70);
            textField.setLocation(200, 460);
            textField.setBorder(null);
            panel2.add(textField);

            JButton leftButton = new JButton();
            panel2.add(leftButton);
            leftButton.setSize(90, 90);
            leftButton.setLocation(40, 310);
            leftButton.setOpaque(false);
            leftButton.setContentAreaFilled(false);
            leftButton.setFocusPainted(false);
            leftButton.setBorderPainted(false);
            leftButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(xml.getImageLocation(30))));
            leftButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    leftButtonActionPerformed(ae);
                }
            });

            JButton rightButton = new JButton();
            panel2.add(rightButton);
            rightButton.setSize(90, 90);
            rightButton.setLocation(180, 310);
            rightButton.setOpaque(false);
            rightButton.setContentAreaFilled(false);
            rightButton.setFocusPainted(false);
            rightButton.setBorderPainted(false);
            rightButton.setIcon(new ImageIcon(ImageIO.read(new File(xml.getImageLocation(31)))));
            rightButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    rightButtonActionPerformed(ae);
                }
            });

            JButton speakButton = new JButton();
            panel2.add(speakButton);
            speakButton.setSize(150, 150);
            speakButton.setLocation(50, 380);
            speakButton.setOpaque(false);
            speakButton.setContentAreaFilled(false);
            speakButton.setFocusPainted(false);
            speakButton.setBorderPainted(false);
            speakButton.setIcon(new ImageIcon(ImageIO.read(new File(xml.getImageLocation(36)))));
            speakButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    speakButtonActionPerformed(ae);
                }
            });

        } catch (IOException ex) {
            Logger.getLogger(NumberApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void leftButtonActionPerformed(ActionEvent ae) {
        try {
            if (i > 0) {
                i--;
            }
            cardsArray[i] = MainConfiguration.getCurrentDirectory() +"/src/smartfriend/resources/images/speech/n" + i + ".jpg";
            jLabel1.setIcon(new ImageIcon(ImageIO.read(new File(cardsArray[i]))));
            jLabel2.setIcon(new ImageIcon(ImageIO.read(new File(MainConfiguration.getCurrentDirectory()+"/src/smartfriend/resources/images/speech/a" + i + ".png"))));
            jLabel3.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(MainConfiguration.getCurrentDirectory()+"/src/smartfriend/resources/images/speech/animated" + i + ".gif")));

            for (int x = 1; x <= 10; x++) {
                l[x].setBackground(Color.WHITE);
            }
            cellCount = 0;
            jLabel4.setIcon(null);
            jLabel5.setIcon(null);
            jLabel6.setIcon(null);
            textField.setText("");
            Timer timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    talk.voiceOutput("How many " + xml.getImageDescription(i) + "do you see?");
                }
            });
            timer.setRepeats(false); // Only execute once
            timer.start();
        } catch (IOException ex) {
            Logger.getLogger(NumberApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void rightButtonActionPerformed(ActionEvent ae) {
        try {
            if (i < 10) {
                i++;
            }
            cardsArray[i] = MainConfiguration.getCurrentDirectory() + "/src/smartfriend/resources/images/speech/n" + i + ".jpg";
            jLabel1.setIcon(new ImageIcon(ImageIO.read(new File(cardsArray[i]))));
            jLabel2.setIcon(new ImageIcon(ImageIO.read(new File(MainConfiguration.getCurrentDirectory() + "/src/smartfriend/resources/images/speech/a" + i + ".png"))));
            jLabel3.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(MainConfiguration.getCurrentDirectory() + "/src/smartfriend/resources/images/speech/animated" + i + ".gif")));

            for (int x = 1; x <= 10; x++) {
                l[x].setBackground(Color.WHITE);
            }
            cellCount = 0;
            jLabel4.setIcon(null);
            jLabel5.setIcon(null);
            jLabel6.setIcon(null);
            textField.setText("");
            Timer timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    talk.voiceOutput("How many " + xml.getImageDescription(i) + "do you see?");
                }
            });
            timer.setRepeats(false); // Only execute once
            timer.start();

        } catch (IOException ex) {
            Logger.getLogger(NumberApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void speakButtonActionPerformed(ActionEvent ae) {
        SpeechRecognizer test = new SpeechRecognizer();
        test.recognizeSpeech();
        System.out.println("**************" + test.getWord());
        textField.setText(test.getWord());
        checkAccuracy(test.getWord(), i);
    }

    public void playSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("C:/Users/user/Documents/NetBeansProjects/Speechmvn/src/main/resources/sounds/Kids_cheering.wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    }

    // Set up contraints so that the user supplied component and the
    // background image label overlap and resize identically
    private static final GridBagConstraints gbc;

    static {
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
    }

    public static JPanel wrapInBackgroundImage(JComponent component,
            Icon backgroundIcon) {
        return wrapInBackgroundImage(
                component,
                backgroundIcon,
                JLabel.TOP,
                JLabel.LEADING);
    }

    public static JPanel wrapInBackgroundImage(JComponent component,
            Icon backgroundIcon,
            int verticalAlignment,
            int horizontalAlignment) {

        // make the passed in swing component transparent
        component.setOpaque(false);

        // create wrapper JPanel
        JPanel backgroundPanel = new JPanel(new GridBagLayout());

        // add the passed in swing component first to ensure that it is in front
        backgroundPanel.add(component, gbc);

        // create panel2 label to paint the background image
        JLabel backgroundImage = new JLabel(backgroundIcon);

        // set minimum and preferred sizes so that the size of the image
        // does not affect the layout size
        backgroundImage.setPreferredSize(new Dimension(1350, 700));
        backgroundImage.setMinimumSize(new Dimension(1, 1));

        // align the image as specified.
        backgroundImage.setVerticalAlignment(verticalAlignment);
        backgroundImage.setHorizontalAlignment(horizontalAlignment);

        // add the background label
        backgroundPanel.add(backgroundImage, gbc);
        return backgroundPanel;
    }

    public boolean checkAccuracy(String spokenWord, int number) {

        if (spokenWord.equals(xml.getImageDescription(number))) {
            System.out.println(number + "correct");
            return true;
        } else {
            System.out.println(number + "wrong");
            return false;
        }
    }

    public static void main(String[] args) {

        try {
            NumberApp frame = new NumberApp();
            frame.setContentPane(wrapInBackgroundImage(frame.foregroundPanel,
                    new ImageIcon(
                            ImageIO.read(new File(frame.xml.getImageLocation(37))))));
            frame.setTitle("Lets Learn Numbers");
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            //frame.playSound();
            frame.talk.voiceOutput("How many rabbits do you see?");
        } catch (IOException ex) {
            Logger.getLogger(NumberApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
