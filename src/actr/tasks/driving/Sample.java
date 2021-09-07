package actr.tasks.driving;

/**
 * The class that defines a collected data sample at a given point in time.
 * 
 * @author Dario Salvucci
 */
public class Sample {
    double time;
    Position simcarPos, simcarHeading;
    double simcarFracIndex, simcarSpeed;
    long simcarRoadIndex;
    Position nearPoint, farPoint, carPoint;
    double steerAngle, accelerator, brake;
    Position autocarPos, autocarHeading;
    double autocarFracIndex, autocarSpeed;
    boolean autocarBraking;;
    // LocationChunk eyeLocation;
    // LocationChunk handLocation;
    // boolean handMoving;
    // boolean listening;
    // boolean inDriveGoal;

    // mlh
    int currentspeed, event, block, followedLane;
    String imaginedSpeedlimit, visAttention;
    boolean turning;
    double lanepos;
    Coordinate eyeLocation;
    boolean signVis;
    
    public String listVars() {
        return "time" + "\t" + "simcarPos" + "\t" + "simcarHeading" + "\t" + "simcarFracIndex" + "\t" + "simcarSpeed"
                + "\t" + "simcarRoadIndex" + "\t" + "eyeLocation" + "\t" + "nearPoint" + "\t" + "farPoint" + "\t"
                + "carPoint" + "\t" + "steerAngle" + "\t" + "accelerator" + "\t" + "brake" + "\t" + "autocarPos" + "\t"
                + "autocarHeading" + "\t" + "autocarFracIndex" + "\t" + "autocarSpeed" + "\t" + "currentspeed" + "\t"
                + "imaginedSpeedlimit" + "\t" + "lanepos" + "\t" + "followedLane" + "\t" + "visAttention" + "\t"
                + "turning" + "\t" + "block" + "\t" + "signVis";
    }

    public String listVarsSep() {
        return "time" + "|" + "simcarPos" + "|" + "simcarHeading" + "|" + "simcarFracIndex" + "|" + "simcarSpeed" + "|"
                + "simcarRoadIndex" + "|" + "eyeLocation" + "|" + "nearPoint" + "|" + "farPoint" + "|" + "carPoint"
                + "|" + "steerAngle" + "|" + "accelerator" + "|" + "brake" + "|" + "autocarPos" + "|" + "autocarHeading"
                + "|" + "autocarFracIndex" + "|" + "autocarSpeed" + "|" + "currentspeed" + "|" + "imaginedSpeedlimit"
                + "|" + "lanepos" + "|" + "followedLane" + "|" + "visAttention" + "|" + "turning" + "|" + "block" + "|" + "signVis";
    }

    public String toString() {
        return +time + "\t" + simcarPos + "\t" + simcarHeading + "\t" + simcarFracIndex + "\t" + simcarSpeed + "\t"
                + simcarRoadIndex + "\t" + eyeLocation + "\t" + nearPoint + "\t" + farPoint + "\t" + carPoint + "\t"
                + steerAngle + "\t" + accelerator + "\t" + brake + "\t" + autocarPos + "\t" + autocarHeading + "\t"
                + autocarFracIndex + "\t" + autocarSpeed + "\t" + currentspeed + "\t" + imaginedSpeedlimit + "\t"
                + lanepos + "\t" + followedLane + visAttention + "\t" + turning + "\t" + block + "\t" + signVis;
    }

    public String toStringSep() {
        return +time + "|" + simcarPos + "|" + simcarHeading + "|" + simcarFracIndex + "|" + simcarSpeed + "|"
                + simcarRoadIndex + "|" + eyeLocation + "|" + nearPoint + "|" + farPoint + "|" + carPoint + "|"
                + steerAngle + "|" + accelerator + "|" + brake + "|" + autocarPos + "|" + autocarHeading + "|"
                + autocarFracIndex + "|" + autocarSpeed + "|" + currentspeed + "|" + imaginedSpeedlimit + "|" + lanepos
                + "|" + followedLane + "|" + visAttention + "|" + turning + "|" + block + "|" + signVis;
    }
}
