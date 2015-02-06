/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.util.general;

/**
 *
 * @author Meuru
 */
public final class Consts {

    //Disply Constants
    public static final int GRAPHIC_DIVICE_NO = 1;
    public static final int SCREEN_WIDHT = 1366;
    public static final int SCREEN_HEIGHT = 768;
//    public static final int SCREEN_WIDHT = 1280;
//    public static final int SCREEN_HEIGHT = 1024;
    //Hand Gesture Recognition Constants
    public static final int CAMERA_ID = 1;
    public static final int CAMERA_WIDTH = 640;
    public static final int CAMERA_HEIGHT = 480;
    //image locations
    private static final String IMAGE_SOURCE_DIRECTORY = "src\\smartfriend\\resources\\images\\main\\";
    public static final String WELCOME_IMAGE = IMAGE_SOURCE_DIRECTORY + "welcome.jpg";
    public static final String MAIN_IMAGE = IMAGE_SOURCE_DIRECTORY + "main.jpg";
    
    public static final String KITE_IMAGE = IMAGE_SOURCE_DIRECTORY + "kite.png";
    public static final String ENTER_ICON = IMAGE_SOURCE_DIRECTORY + "enter_icon.png";
    
    public static boolean saveImage = false;
    
    public static boolean GRAPHICAL_DEBUG = true;
}
