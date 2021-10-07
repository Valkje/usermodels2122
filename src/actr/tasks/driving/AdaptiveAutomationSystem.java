package actr.tasks.driving;
import networking.ServerMain;
import networking.Server;
import actr.tasks.driving.MovingAverage;
import actr.tasks.driving.MovingPredictionErrorVariance;

public class AdaptiveAutomationSystem {

    private AaLevel aaLevel;
    private Simcar simcar;
    private Env env;

    private boolean levelLocked = false;
    private double timerStart;
    private double lockTimeS = 3;
    // Tuning parameters for the MAs and MV
    private int bufferSizeShort = 100;
    private int bufferSizeLong = 1000;
    // Tuning parameter for decision
    private float decisionSensitivity = 0.75f;
    private MovingAverage ShortTermTrend;
    private MovingAverage LongTermTrend;
    private MovingPredictionErrorVariance LongTermMSE;
    private double tmpSecondBaseline = 5000.0;

    Server server = ServerMain.server;

    public AdaptiveAutomationSystem(Simcar simcar, Env env) {
        this.aaLevel = AaLevel.full;
        this.simcar = simcar;
        this.env = env;

        // Initialize Moving averages and MSE
        ShortTermTrend = new MovingAverage(bufferSizeShort);
        LongTermTrend = new MovingAverage(bufferSizeLong);
        LongTermMSE = new MovingPredictionErrorVariance(bufferSizeLong);
    }

    private void decideAutomationLevel() {
        double shortPred = ShortTermTrend.getCurrentValue();
        double longPred = LongTermTrend.getCurrentValue();
        double longTermMSE = LongTermMSE.getCurrentValue();
        switch (this.aaLevel) {
            case none:
                if(shortPred > (longPred + (decisionSensitivity * longTermMSE))) {
                    // Check whether automation level should increase from 1 to 2
                    System.out.println("Model wants to increase from automation level 1 to 2");
                    increaseAutomation();
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
                    increaseAutomation();
                } else if (shortPred < (longPred - (decisionSensitivity * longTermMSE))) {
                    // Check whether automation level should decrease from 2 to 1
                    System.out.println("Model wants to increase from automation level 1 to 2");
                    decreaseAutomation();
                }
                break;
            case full:
                if (shortPred < (tmpSecondBaseline - (decisionSensitivity * longTermMSE))) {
                    // Check whether automation level should decrease from 3 to 2
                    System.out.println("Model wants to increase from automation level 1 to 2");
                    decreaseAutomation();
                }
                break;
        }
    }

    public void update(Env env) {
        server.send("query/ PUPIL_SIZE");
		// System.out.println(server.lastPupilSample);
        ShortTermTrend.update(server.lastPupilSample);
        LongTermTrend.update(server.lastPupilSample);
        // One-step ahead orediction or not? If yes -> swap with line 39
        LongTermMSE.update(LongTermTrend.getCurrentValue(), server.lastPupilSample);
        updateLevelLock();

        // Only start making decisions after 100 samples have arrived
        if(LongTermTrend.getCurrentSize() > 100) {
            decideAutomationLevel();
        }
        
        // Manual automation changes
        if (Controls.getAaChange() == 1) { //TODO: make this just slightly more interesting
            increaseAutomation();
        } else if (Controls.getAaChange() == -1) {
            decreaseAutomation();
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

    private void increaseAutomation() {
        if (!levelLocked){
            switch (this.aaLevel) {
                case none:
                    this.aaLevel = AaLevel.cruise;
                    break;
                case cruise:
                    this.aaLevel = AaLevel.full;
                    break;
                case full:
                    // no higher level
                    break;
            }
            levelLocked = true;
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



}
