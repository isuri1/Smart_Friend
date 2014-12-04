/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.util.general;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author Keshani
 */
public class MainConfiguration {

    private Properties props;
    private static MainConfiguration instance = null;

    private MainConfiguration() {
        try {
            Properties props = new Properties();
        } catch (Exception e) {
        }
    }

    public static synchronized MainConfiguration getInstance() {

        if (instance == null) {
            instance = new MainConfiguration();
        }
        return instance;
    }

    public void loadXML() {
        
        try {
            File configFile = new File("config.xml");
            OutputStream outputStream = new FileOutputStream(configFile);
            props.storeToXML(outputStream, "Configuration File");
        } catch (Exception e) {
        }
    }

    public String getProperty(String prop) {
        
        String value = "";
        try {
            Properties pop = new Properties();
            FileInputStream reader = new FileInputStream(new File(getCurrentDirectory() + "/src/smartfriend/resources/config.xml"));
            pop.loadFromXML(reader);

            value = pop.getProperty(prop);
            reader.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return value;
    }

    public static String getCurrentDirectory() throws IOException {
        File currentDirectory = new File(new File(".").getAbsolutePath());
        String path = currentDirectory.getCanonicalPath().replace("\\", "/");
        return path;
    }
}