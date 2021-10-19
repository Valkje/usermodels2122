package actr.tasks.driving;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import networking.ServerMain;
import networking.Server;

import javax.sound.sampled.LineUnavailableException;
import java.util.Arrays;

public class EquationSpeaker {

    private String[] equations1 = {"1 times 2", "2 times 3", "4 times 5", "6 times 7", "6 times 7", "4 times 5", "2 times 3", "1 times 2"};
    private String[] equations2 = {"6 times 7", "4 times 5", "2 times 3", "1 times 2", "1 times 2", "2 times 3", "4 times 5", "6 times 7"};

    //TODO: SET PRIOR TO EXPERIMENT BLOCK
    private String[] equations = equations1; // or equations2

    int block = 1; // star block count at one for convenience
    double blockDuration = 20;
    int maxBlocks = 4;
    int qCounter = 0; //question counter
    double qDuration = 5; // half a minute, had to fit into the block duration
    double maxQuestions = blockDuration/qDuration;

    Tone tone = new Tone();
    int beepDuration = 1000; // ms
    int beepFrequency = 700; //Hz

    Boolean mute = false;
    double muteTime;

    private Voice voice;
    Env env;
 
    public EquationSpeaker(Env env) {

        this.env = env;

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

        new Thread(() -> {
            voice.speak("This voice will preesent you with multiplication problems");
        }).start();

    }

    Server server = ServerMain.server;
    boolean beeped = false;

    public void update() {

        if ( (env.time - env.aas.getStartDelay()) > block * blockDuration ) { // this is why the block count starts at 1
            block++;
            server.send("send/ NEXT BLOCK");
            qCounter = 0;
            beeped = false;

            if (block == 3) {
                try {
                    tone.sound(beepFrequency, beepDuration, 1.0);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }

        if ( (env.time - env.aas.getStartDelay()) + ((beepDuration/1000)*2) > block * blockDuration && (block == 1 || block == 3) && !beeped) {
            // sound a beep prior to the start of the algebra block
            beeped = true;
            new Thread(() -> {
                try {
                    tone.sound(beepFrequency, beepDuration, 1.0);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        if (block == 2 || block == 4) { // algebra blocks, no interaction/algebra in blocks 1 and 3

            if ( mute && (env.time - muteTime) > qDuration )  { // time reserved for a question has elapsed
                mute = false;
            }

            if (qCounter < maxQuestions && !mute) { // time to ask a new question
                String text = getQuestionText();
                qCounter++;
                new Thread(() -> {
                    voice.speak(text);
                }).start();
                mute = true;
                muteTime = env.time;
                server.send(String.format("send/ QUESTION ASKED %d", qCounter));
            }
        }

        if (block > maxBlocks) {
            //TODO: End the sim/model/env and store data.
            server.send("send/ EXPERIMENT ENDED");
            if (!beeped) {
                new Thread(() -> {
                    voice.speak("End of experiment");
                }).start();
            }
            beeped = true;
        }
    }

    private String getQuestionText(){
        String questionText;

        questionText = equations[0];
        equations = Arrays.copyOfRange(equations, 1, equations.length);

        return questionText;
    }
 
}