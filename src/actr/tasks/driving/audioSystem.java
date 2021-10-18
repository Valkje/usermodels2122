import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import java.util.Random;

public class audioSystem {

    int block = 0;
    int counter = 0;
    int[] ans =  new int[2];
    long currentTime = System.currentTimeMillis();
    long tempTime = System.currentTimeMillis();
    Voice voice = init();

    // Use this variable for the block where there are no calculations
    final int noCalcBlock = 10000;

    // Use this variable for pause between two calculations
    final int pauseBetweenCalc = 3000;
 
    public static Voice init() {
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
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return voice;
    }

    public void update() {
        Random rand = new Random();

        // Set here how many calculations we want before switching the block
        final int maxCounter = 5;

        // Generate the spoken text here
        int number = rand.nextInt(11)+10;
        String text = Integer.toString(number);
        text = text + " times ";
        number = rand.nextInt(11)+10;
        text = text + Integer.toString(number);

        if (this.block == 0) {
            // Change block and say the first calculations
            this.block = 1;
            this.counter = 1;
            this.voice.speak(text);
        } else {
            // Continue with text to speech
            if (this.counter < maxCounter) {
                this.counter += 1;
                this.voice.speak(text);
            }

            // Reset to block 0 (no calculations)
            if (this.counter >= maxCounter) {
                this.counter = 0;
                this.block = 0;
            }
        }

        this.ans[0] = counter;
        this.ans[1] = block;
    }

    public void main() {

        this.tempTime = System.currentTimeMillis();

        if (this.block == 0 && this.tempTime - this.currentTime > this.noCalcBlock) {
                update();
                this.currentTime = System.currentTimeMillis();
        } else if (this.block == 1 && this.tempTime - this.currentTime > this.pauseBetweenCalc) {
            update();
            this.currentTime = System.currentTimeMillis();
        }
        
    }
 
}