package smartfriend.applications.scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import smartfriend.util.general.MainConfiguration;

/**
 *
 * @author Isuri
 */
public class Scheduler{

    HashMap<String, ArrayList<String>> dateMap = new HashMap<String, ArrayList<String>>();
    HashMap<String, ArrayList<String>> hoursMap = new HashMap<String, ArrayList<String>>();
    int waveNo, year1, month1, day1, hours1, minuts1, repeat1;
    String am_pm;

    public void runScheduler() {

        System.out.println("check check");
        readFile();
        GregorianCalendar currtime = new GregorianCalendar();

        if (true) {

            Set<Entry<String, ArrayList<String>>> setMap = dateMap.entrySet();
            Iterator<Entry<String, ArrayList<String>>> iteratorMap = setMap.iterator();
            while (iteratorMap.hasNext()) {

                try {
                    Map.Entry<String, ArrayList<String>> entry
                            = (Map.Entry<String, ArrayList<String>>) iteratorMap.next();
                    String key = entry.getKey();
                    ArrayList<String> values = entry.getValue();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss aa");
                    Date date = sdf.parse(values.get(0));
                    Calendar calendar3 = new GregorianCalendar();
                    calendar3.setTime(date);
                    if (calendar3.get(Calendar.HOUR) == currtime.get(Calendar.HOUR)) {
                        if (calendar3.get(Calendar.AM_PM) == currtime.get(Calendar.AM_PM)) {
                            System.out.println("same hour");
                            hoursMap.put(key, values);
                        }
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            Set<Entry<String, ArrayList<String>>> setMap2 = hoursMap.entrySet();
            Iterator<Entry<String, ArrayList<String>>> iteratorMap2 = setMap2.iterator();
            while (iteratorMap2.hasNext()) {

                Map.Entry<String, ArrayList<String>> entry2
                        = (Map.Entry<String, ArrayList<String>>) iteratorMap2.next();
                String key2 = entry2.getKey();
                ArrayList<String> values2 = entry2.getValue();
                int repeat_count = Integer.parseInt(values2.get(1));
                for (int r = 1; r <= repeat_count; r++) {
                    playSound(Integer.parseInt(key2));
                    System.out.println("repeat " + r + " play************" + key2);
                    try {
//                        int threadSleep1 = 60 / repeat_count;
                        Thread.sleep(12 * 60 * 1000);
                    } catch (InterruptedException e) {
                    }
                }
                try {
                    int threadSleep = 60 / hoursMap.size();
                    Thread.sleep(threadSleep * 60 * 1000);
                } catch (InterruptedException e) {
                }
            }
        }
//        }
    }

    public void playSound(int key) {

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("recordingFilePath") + key + ".wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    }

    public void readFile() {

        BufferedReader br = null;

        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("schedulerFilePath")));

            while ((sCurrentLine = br.readLine()) != null) {

                String words[] = sCurrentLine.split(" ");

                SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss aa");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                waveNo = Integer.parseInt(words[0].trim());
                year1 = Integer.parseInt(words[1].trim());
                month1 = Integer.parseInt(words[2].trim());
                day1 = Integer.parseInt(words[3].trim());
                hours1 = Integer.parseInt(words[4].trim());
                minuts1 = Integer.parseInt(words[5].trim());
                repeat1 = Integer.parseInt(words[6].trim());
                am_pm = words[7].trim();

                String dateInString = day1 + "-" + month1 + "-" + year1 + " " + hours1 + ":" + minuts1 + ":00 " + am_pm;

                Date date = sdf.parse(dateInString);
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(date);
//                System.out.println("from file : " + sdf.format(date));

                //get current date time with Date()
                Date currentDate = new Date();
                Calendar calendar2 = new GregorianCalendar();
                calendar2.setTime(currentDate);
//                System.out.println("current   : " + sdf.format(currentDate));

                if (calendar.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)) {
                    if (calendar.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)) {
                        if (calendar.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)) {
//                            System.out.println("equal dates");
                            ArrayList<String> listOne = new ArrayList<String>();
                            listOne.add(dateInString);
                            listOne.add(String.valueOf(repeat1));
                            dateMap.put(String.valueOf(waveNo), listOne);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
