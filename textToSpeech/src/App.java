import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import java.util.Random;

public class App {
 
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

    public static int[] update(int counter, int block, Voice voice) {
        int[] resp = new int[2];
        Random rand = new Random();

        // Set here how many calculations we want before switching the block
        final int maxCounter = 5;

        // Generate the spoken text here
        int number = rand.nextInt(11)+10;
        String text = Integer.toString(number);
        text = text + " times ";
        number = rand.nextInt(11)+10;
        text = text + Integer.toString(number);

        if (block == 0) {
            // Change block and say the first calculations
            block = 1;
            counter = 1;
            voice.speak(text);
        } else {
            // Continue with text to speech
            if (counter < maxCounter) {
                counter += 1;
                voice.speak(text);
            }

            // Reset to block 0 (no calculations)
            if (counter >= maxCounter) {
                counter = 0;
                block = 0;
            }
        }

        resp[0] = counter;
        resp[1] = block;
        return resp;
    }

    public static void main(String args[]) {
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

        while (true) {
            tempTime = System.currentTimeMillis();
            System.out.println(currentTime);
            System.out.println(tempTime);

            if (block == 0 && tempTime - currentTime > noCalcBlock) {
                    ans = update(counter,block,voice);
                    counter = ans[0];
                    block = ans[1];
                    currentTime = System.currentTimeMillis();
            } else if (block == 1 && tempTime - currentTime > pauseBetweenCalc) {
                ans = update(counter,block,voice);
                counter = ans[0];
                block = ans[1];
                currentTime = System.currentTimeMillis();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
 
}