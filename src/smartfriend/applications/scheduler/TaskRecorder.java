package smartfriend.applications.scheduler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import smartfriend.util.general.MainConfiguration;

/**
 *
 * @author Isuri
 */
public class TaskRecorder {

    AudioFormat audioFormat;
    TargetDataLine targetDataLine;
    private int recordingNo;
    File audioFile;

    public TaskRecorder(int recordingNo) {
        try {
            this.recordingNo = recordingNo;
            audioFile = new File(MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("recordingFilePath")+ recordingNo + ".wav");
        } catch (IOException ex) {
            Logger.getLogger(TaskRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void captureAudio() {
        try {
            //Get things set up for capture
            audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo
                    = new DataLine.Info(
                            TargetDataLine.class,
                            audioFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);

            //Create a thread to capture the microphone
            // data into an audio file and start the
            // thread running.  It will run until the
            // Stop button is clicked.  This method
            // will return after starting the thread.
            new CaptureThread().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate,
                sampleSizeInBits,
                channels,
                signed,
                bigEndian);
    }

    class CaptureThread extends Thread {

        public void run() {
            AudioFileFormat.Type fileType = null;

            //Set the file type and the file extension
            // based on the selected radio button.
            fileType = AudioFileFormat.Type.WAVE;

            try {
                targetDataLine.open(audioFormat);
                targetDataLine.start();
                AudioSystem.write(
                        new AudioInputStream(targetDataLine),
                        fileType,
                        audioFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void stopAudio() {
        System.out.println("stop");
        targetDataLine.stop();
        targetDataLine.close();
    }
}
