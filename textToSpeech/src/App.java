import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class App {
 
    public static void main(String[] args) {
        Voice voice;

        // Not working without this for whatever reason
        System.setProperty(
                    "freetts.voices",
                    "com.sun.speech.freetts.en.us"
                        + ".cmu_us_kal.KevinVoiceDirectory");

        // kevin16 is the 16bit version. There is also an 8bit version, but is not that amazing
        voice = VoiceManager.getInstance().getVoice("kevin16");

        if (voice != null) {
            voice.allocate();
        }

        try {
            voice.setRate(145); // rate of the voice
            voice.setPitch(140); // pitch of the voice
            voice.setVolume(5); // volume of the voice

            String text = "21 times 23";
            voice.speak(text); 

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
 
}