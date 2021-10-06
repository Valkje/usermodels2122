package networking;

import actr.env.ApplicationMain;

/**
 * Prepares the experiment on the eye-tracking side by sending messages to the Python component. Starts up the main
 * application after that is done.
 *
 * @author Gilles Lijnzaad
 */
public class Participant {
    Server s = ServerMain.server;
    private final int participantNumber;
    private boolean endedAlready = false;

    private static final short dimX = 1920;
    private static final short dimY = 1080;

    public Participant(int participantNumber) {
        this.participantNumber = participantNumber;
        ApplicationMain.startApplication();
    }

    public void prepareExperiment() {
        String edfFileName = "UM" + participantNumber + ".EDF";
        s.send("info/ EDF " + edfFileName);

        s.send("info/ DIM_X " + dimX);
        s.send("info/ DIM_Y " + dimY);

        s.send("do/ PREPARE EXPERIMENT");
    }

    public void prepareTrial(int trialNumber, boolean construction, int nBack) {
        s.send("info/ TRIAL_NUMBER " + trialNumber);
        String con = (construction? "T" : "F");
        s.send("info/ TRIAL_ID CON " + con + " NBACK " + nBack);
        s.send("do/ PREPARE TRIAL");
    }

    public void doDriftCorrection() {
        s.send("do/ DRIFT CORRECT");
    }

    public void startRecording() {
        s.send("do/ START RECORDING");
    }

    public void stopRecording(){
        s.send("do/ STOP RECORDING");
    }

    public void sendSpeedSign() {
        s.send("send/ SPEED SIGN");
    }

    public void endTrial() {
        s.send("do/ END TRIAL");
    }

    public void startTrial(){
        s.send("do/ START TRIAL");
    }

    public void endExperiment() {
        if (!endedAlready) {
            s.send("do/ END EXPERIMENT");
            endedAlready = true;
        }
    }

}