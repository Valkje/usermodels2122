package actr.tasks.driving;

enum AaLevel {
    none,
    cruise,
    full
}

public class AdaptiveAutomationSystem {

    private AaLevel aaLevel;
    private Simcar simcar;
    private Env env;

    public AdaptiveAutomationSystem(Simcar simcar, Env env) {
        this.aaLevel = KeyHandler.getAaLevel();
        this.simcar = simcar;
        this.env = env;
    }

    public void update(Env env) {
        aaLevel = KeyHandler.getAaLevel(); //TODO: make this just slightly more interesting
        /** Determining the source of control (human vs. model) is handled in the Driving class.
         * The methods in this class influence simcar properties. Sampling might be slower for the
         * model than the controller, but by this method everything keeps in sync. Then the simcar
         * dynamics have to be updated.*/
        simcar.update(env);
    }

    /** GETTERS AND SETTERS **/

    public AaLevel getAaLevel() {
        return aaLevel;
    }

    public void setAaLevel(AaLevel aaLevel) {
        this.aaLevel = aaLevel;
    }



}
