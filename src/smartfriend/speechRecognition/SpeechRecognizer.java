package smartfriend.speechRecognition;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import smartfriend.util.general.MainConfiguration;

public class SpeechRecognizer {

    private String command;
    private String correctCommand;

    public void setSpeechCommand(String command) {

        this.command = command;
    }

    public String getWord() {
        return command;
    }

    public String getSpeechCommand() {

        if (command.contains("story") || command.contains("book")) {
            if (command.contains("open") || command.contains("read")) {
                System.out.println("baby wants to open a book");
                correctCommand = "open story book";
            } else if (command.contains("close")) {
                System.out.println("baby wants to close book");
                correctCommand = "close story book";
            }
        }

        if (command.contains("dictation")) {
            if (command.contains("open") || command.contains("read")) {
                System.out.println("baby wants to open a dictation");
                correctCommand = "open dictation";
            } else if (command.contains("close")) {
                System.out.println("baby wants to close dictation");
                correctCommand = "close dictation";
            }
        }

        if (command.contains("video")) {
            if (command.contains("play")) {
                System.out.println("baby wants to play the video");
                correctCommand = "play video";
            } else if (command.contains("start")) {
                System.out.println("baby wants to start the video");
                correctCommand = "start video";
            } else if (command.contains("stop")) {
                System.out.println("baby wants to stop the video");
                correctCommand = "stop video";
            } else if (command.contains("pause")) {
                System.out.println("baby wants to pause the video");
                correctCommand = "stop video";
            } else if (command.contains("next") || command.contains("go")) {
                System.out.println("play next video");
                correctCommand = "next video";
            }
        }
        if (command.contains("yes") || command.contains("ow")) {
            System.out.println("child said yes");
            correctCommand = "yes";
        }

        if (command.contains("no") || command.contains("ow")) {
            System.out.println("child said no");
            correctCommand = "no";
        }

        if (command.contains("back") || command.contains("go")) {
            System.out.println("go back");
            correctCommand = "go back";
        }

        if (command.contains("page")) {
            if (command.contains("next")) {
                System.out.println("next page");
                correctCommand = "next page";
            } else if (command.contains("top")) {
                System.out.println("page top");
                correctCommand = "page top";
            } else if (command.contains("bottom")) {
                System.out.println("page bottom");
                correctCommand = "page bottom";
            }
        }

        if (command.contains("top") || command.contains("uda")) {
            System.out.println("top");
            correctCommand = "top";
        }

        if (command.contains("bottom") || command.contains("yata")) {
            System.out.println("bottom");
            correctCommand = "bottom";
        }

        if (command.contains("left")) {
            System.out.println("left");
            correctCommand = "left";
        }

        if (command.contains("right")) {
            System.out.println("right");
            correctCommand = "right";
        }

        if (command.contains("middle") || command.contains("mada")) {
            System.out.println("middle");
            correctCommand = "middle";
        }
        return correctCommand;
    }

    public void recognizeSpeech() {

        try {
            final JavaSoundRecorder recorder = new JavaSoundRecorder();
            Thread stopper = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    recorder.finish();
                }
            });
            stopper.start();
            // start recording
            recorder.start();

            //Set configuration for speech models
            Configuration configuration = new Configuration();
            configuration.setAcousticModelPath("file:/" + MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("acousticModelPath"));
            configuration.setDictionaryPath("file:/" + MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("dictionaryPath"));
            configuration.setLanguageModelPath("file:/" + MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("languageModelPath"));
            //Read from a recorded wave file 
            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            recognizer.startRecognition(new FileInputStream("C:\\Users\\user\\Documents\\smart_audio\\test2.wav"));

            SpeechResult result;
            System.out.println("start.....");

            while ((result = recognizer.getResult()) != null) {
                setSpeechCommand(result.getHypothesis());
                //sc.setSpeechCommand(result.getNbest(3));
            }
            //sc.getSpeechCommand();
            System.out.println("end.....");
            recognizer.stopRecognition();

            //************for live speech recognition****************//
            /*LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
             recognizer.startRecognition(true);
            
             SpeechResult result;
             System.out.println("start recording......");

             while ((result = recognizer.getResult()) != null) {
             System.out.println("start");
             System.out.println(result.getHypothesis());
             }
             System.out.println("stop recording....");
             recognizer.stopRecognition();        
             */
        } catch (IOException ex) {
            Logger.getLogger(SpeechRecognizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
