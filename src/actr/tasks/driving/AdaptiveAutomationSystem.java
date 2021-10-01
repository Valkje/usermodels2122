package actr.tasks.driving;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

public class AdaptiveAutomationSystem {

    private AaLevel aaLevel;
    private Simcar simcar;
    private Env env;

    private boolean levelLocked = false;
    private double timerStart;
    private double lockTimeS = 3;

    public AdaptiveAutomationSystem(Simcar simcar, Env env) {
        this.aaLevel = AaLevel.full;
        this.simcar = simcar;
        this.env = env;
    }

    public void update(Env env) {
        updateLevelLock();
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
