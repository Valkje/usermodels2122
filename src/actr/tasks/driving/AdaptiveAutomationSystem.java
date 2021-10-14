package actr.tasks.driving;
import networking.ServerMain;
import networking.Server;

import java.lang.Math;

public class AdaptiveAutomationSystem {

    private AaLevel aaLevel;
    private AaChange aaChange = AaChange.none;
    private boolean loaChanging = false;

    private Simcar simcar;
    private Env env;

    private boolean levelLocked = false;
    private double timerStart;
    private double lockTimeS = 10;
    // Tuning parameters for the MAs and MV
    private int bufferSizeShort = 150;
    private int bufferSizeLong = 2500;
    // Tuning parameter for decision
    private float decisionSensitivity = 0.5f;
    private MovingAverage ShortTermTrend;
    private MovingAverage LongTermTrend;
    private MovingPredictionErrorVariance LongTermMSE;
    private double tmpSecondBaseline = 100000.0;

    Server server = ServerMain.server;

    public AdaptiveAutomationSystem(Simcar simcar, Env env) {
        this.aaLevel = AaLevel.full;
        this.simcar = simcar;
        this.env = env;
        simcar.hud.setDisplayedAaLevel(aaLevel);

        // Initialize Moving averages and MSE
        ShortTermTrend = new MovingAverage(bufferSizeShort);
        LongTermTrend = new MovingAverage(bufferSizeLong);
        LongTermMSE = new MovingPredictionErrorVariance(bufferSizeLong);
    }

    private void decideAutomationLevel() {
        double shortPred = ShortTermTrend.getCurrentValue();
        double longPred = LongTermTrend.getCurrentValue();
        double longTermMSE = Math.sqrt(LongTermMSE.getCurrentValue());
        switch (this.aaLevel) {
            case none:
                if(shortPred > (longPred + (decisionSensitivity * longTermMSE))) {
                    // Check whether automation level should increase from 1 to 2
                    System.out.println("Model wants to increase from automation level 1 to 2");
                    prepareIncreaseAutomation();
                    // Set new baseline for 2 to 3 decisions.
                    if (!levelLocked) {
                        tmpSecondBaseline = shortPred;
                    }
                }
                break;
            case cruise:
                if(shortPred > (tmpSecondBaseline + (decisionSensitivity * longTermMSE))) {
                    // Check whether automation level should increase from 2 to 3
                    System.out.println("Model wants to increase from automation level 1 to 2");
                    prepareIncreaseAutomation();
                } else if (shortPred < (longPred - (decisionSensitivity * longTermMSE))) {
                    // Check whether automation level should decrease from 2 to 1
                    System.out.println("Model wants to increase from automation level 1 to 2");
                    prepareDecreaseAutomation();
                }
                break;
            case full:
                if (shortPred < (tmpSecondBaseline - (decisionSensitivity * longTermMSE))) {
                    // Check whether automation level should decrease from 3 to 2
                    System.out.println("Model wants to increase from automation level 1 to 2");
                    prepareDecreaseAutomation();
                }
                break;
        }
    }

    private void updateAutomationLevel(){
        if (!loaChanging) { // this is set to false by the hud after blinking
            switch (aaChange) {
                case increase:
                    increaseAutomation();
                    aaChange = AaChange.none;
                    break;
                case decrease:
                    System.out.println("decreased!!!!!!!1111");
                    decreaseAutomation();
                    aaChange = AaChange.none;
                    break;
            }
        }
    }

    public void update(Env env) {
        server.send("query/ PUPIL_SIZE");
        
		// System.out.println(server.lastPupilSample);
        if (server.lastPupilSample > 0) {
            ShortTermTrend.update(server.lastPupilSample);
            LongTermTrend.update(server.lastPupilSample);
            // One-step ahead orediction or not? If yes -> swap with line 39
            LongTermMSE.update(LongTermTrend.getCurrentValue(), server.lastPupilSample);
            server.send(String.format("plot/ LONG %f", LongTermTrend.getCurrentValue()));
            server.send(String.format("plot/ SHORT %f", ShortTermTrend.getCurrentValue()));
            server.send(String.format("plot/ UPPER %f", LongTermTrend.getCurrentValue() + (Math.sqrt(LongTermMSE.getCurrentValue()) * decisionSensitivity)));
            server.send(String.format("plot/ LOWER %f", LongTermTrend.getCurrentValue() - (Math.sqrt(LongTermMSE.getCurrentValue()) * decisionSensitivity)));
        }
        
        updateLevelLock();

        // Only start making decisions after 100 samples have arrived
        if(LongTermTrend.getCurrentSize() > 100) {
            decideAutomationLevel();
        }

        updateAutomationLevel();
        
        // Manual automation changes
        if (Controls.getAaChange() == 1) {
            prepareIncreaseAutomation();
        }
        else if (Controls.getAaChange() == -1) {
            prepareDecreaseAutomation();
        }
        Controls.setAaChange(0);

        /** Determining the source of control (human vs. model) is handled in the Driving class.
         * The methods in this class influence simcar properties. Sampling might be slower for the
         * model than the controller, but by this method everything keeps in sync. Then the simcar
         * dynamics have to be updated.*/
        simcar.update(env);
    }

    private void updateLevelLock() {
//        System.out.print("Env.time: ");
//        System.out.println(env.time);
//        System.out.println("Change timer: ");
//        System.out.println(levelChangeTimer);
        if (levelLocked && timerStart == 0) {
            timerStart = env.time;
        }
        if (env.time-timerStart > lockTimeS) {
            timerStart = 0;
            levelLocked = false;
        }
    }

    public void prepareIncreaseAutomation() {
        if (aaChange == AaChange.none && aaLevel!= AaLevel.full && !levelLocked) {
            setLoaChanging(true); // to start the blinking of the new automation level //TODO: perhaps remove the delay for increase?
            simcar.hud.increaseDisplayedAaLevel(); // display the new level
            setAaChange(AaChange.increase); // indicate that we want to switch after blinking
        }
    }

    private void increaseAutomation() {
        if (!levelLocked){
            switch (this.aaLevel) {
                case none:
                    this.aaLevel = AaLevel.cruise;
                    break;
                case cruise:
                    this.aaLevel = AaLevel.full;

                    Controls.rumble();

                    break;
                case full:
                    // no higher level
                    break;
            }
            levelLocked = true;
        }
    }

    private void prepareDecreaseAutomation(){
        if (aaChange == AaChange.none && aaLevel!=AaLevel.none && !levelLocked) {
            setLoaChanging(true); // to start the blinking of the new automation level
            simcar.hud.decreaseDisplayedAaLevel(); //display the new level
            setAaChange(AaChange.decrease);
        }
    }

    private void decreaseAutomation() {
        if (!levelLocked){
            switch (this.aaLevel) {
                case none:
                    // no lower level
                    break;
                case cruise:
                    this.aaLevel = AaLevel.none;
                    break;
                case full:
                    this.aaLevel = AaLevel.cruise;
                    break;
            }
            levelLocked = true;
        }
    }

    /** GETTERS AND SETTERS **/

    public AaLevel getAaLevel() {
        return aaLevel;
    }

    private void setAaLevel(AaLevel aaLevel) {
        this.aaLevel = aaLevel;
    }

    public boolean isLoaChanging() {
        return loaChanging;
    }

    public void setLoaChanging(boolean loaChanging) {
        System.out.println("setLoaChanging()");
        System.out.println(env.time);
        this.loaChanging = loaChanging;
    }

    public AaChange getAaChange() {
        return aaChange;
    }

    public void setAaChange(AaChange aaChange) {
        this.aaChange = aaChange;
    }

}
