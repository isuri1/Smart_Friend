package smartfriend.tts;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TextToSpeech {

    public void voiceOutput(String text) {

        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice voice;
        voice = voiceManager.getVoice("kevin16");
        voice.setPitch((float) (110));
        voice.setPitchShift((float) (1));
       
        voice.setRate(100);
        voice.allocate();
        voice.speak(text);
    }
}
