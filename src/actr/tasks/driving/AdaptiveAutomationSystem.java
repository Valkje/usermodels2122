package actr.tasks.driving;
import networking.ServerMain;
import networking.Server;

import java.lang.Math;

public class AdaptiveAutomationSystem {

    private boolean systemActived = true;

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

    private boolean experimentStarted = false;

    private double startDelay = 0;

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
                    decreaseAutomation();
                    aaChange = AaChange.none;
                    break;
                case initial:
                    server.send("send/ AUTOMATION_DECREASE none");
                    server.send("send/ EXPERIMENT STARTED"); //TODO: ask Josh if this works
                    this.aaLevel = AaLevel.none;
                    aaChange = AaChange.none;
                    this.startDelay = env.time;
            }
        }
    }

    public void update(Env env) {
        if (env.time >= 6 && !experimentStarted) { // 3 second delay before the warnign is given that the switch to driver control is made
            prepareDriverControlMode();
            experimentStarted = true;
        }

        server.send("query/ PUPIL_SIZE");
        
		// System.out.println(server.lastPupilSample);
        if (server.lastPupilSample > 0) {
            ShortTermTrend.update(server.lastPupilSample);
            LongTermTrend.update(server.lastPupilSample);
            // One-step ahead orediction or not? If yes -> swap with line 39
            LongTermMSE.update(LongTermTrend.getCurrentValue(), server.lastPupilSample);
            
        }
        // Send plot-data over to Python
        server.send(String.format("plot/ RAW %f", server.lastPupilSample));
        server.send(String.format("plot/ LONG %f", LongTermTrend.getCurrentValue()));
        server.send(String.format("plot/ SHORT %f", ShortTermTrend.getCurrentValue()));
        server.send(String.format("plot/ UPPER %f", LongTermTrend.getCurrentValue() + (Math.sqrt(LongTermMSE.getCurrentValue()) * decisionSensitivity)));
        server.send(String.format("plot/ LOWER %f", LongTermTrend.getCurrentValue() - (Math.sqrt(LongTermMSE.getCurrentValue()) * decisionSensitivity)));
        
        updateLevelLock();

        // Only start making decisions after 100 samples have arrived and system is activated
        if(LongTermTrend.getCurrentSize() > 100 && systemActived && experimentStarted) {
            decideAutomationLevel();
        }

        updateAutomationLevel();
        
        // Manual automation changes
        if (Controls.getAaChange() == 1 && experimentStarted) {
            prepareIncreaseAutomation();
        }
        else if (Controls.getAaChange() == -1 && experimentStarted) {
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
                    server.send("send/ AUTOMATION_INCREASE cruise");
                    this.aaLevel = AaLevel.cruise;
                    break;
                case cruise:
                    server.send("send/ AUTOMATION_INCREASE full");
                    this.aaLevel = AaLevel.full;
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
                    server.send("send/ AUTOMATION_DECREASE none");
                    this.aaLevel = AaLevel.none;
                    break;
                case full:
                    server.send("send/ AUTOMATION_DECREASE cruise");
                    this.aaLevel = AaLevel.cruise;
                    break;
            }
            levelLocked = true;
        }
    }

    /** for the initial start of the experiment **/

    void prepareDriverControlMode() {
        setLoaChanging(true); // to start the blinking of the new automation level
        simcar.hud.setDisplayedAaLevel(AaLevel.none);
        setAaChange(AaChange.initial);
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

    public void setLoaChanging(boolean loaChanging) { this.loaChanging = loaChanging; }

    public AaChange getAaChange() {
        return aaChange;
    }

    public void setAaChange(AaChange aaChange) {
        this.aaChange = aaChange;
    }

    public double getStartDelay() { return startDelay; }

}
