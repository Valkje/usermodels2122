/*
* Parts are taken from a program written by Nilma Kelapanda.
* */

package actr.tasks.driving;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier;


public class Controls {
    private static double steerAngle = 0;
    private static double accelBrake = 0.0;
    private static int aaChange = 0;
    private static Controller steerPedals = null;
    private static Controller gamePad = null;

    // function to locate the steering wheel & pedals connected to PC.
    public static void startUp() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        // Find the steering wheel
        for (int i = 0; i < controllers.length && steerPedals == null; i++) {
            System.out.println(controllers[i].getType());

            if (controllers[i].getType() == Controller.Type.STICK) {
                steerPedals = controllers[i];
                break;
            }

            if (controllers[i].getType() == Controller.Type.GAMEPAD) {
                gamePad = controllers[i];
            }
        }

        if (steerPedals == null) {
            System.out.println("Found no steering wheel!");
        }
    }

    public static double getValue(Identifier identifier) {
        Controller controller = null;

        if (steerPedals != null)
            controller = steerPedals;
        else if(gamePad != null)
            controller = gamePad;

        if (controller == null)
            return 0;

        controller.poll();
        Component[] components = controller.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            Identifier ident = component.getIdentifier();
            if (ident == identifier){
                return component.getPollData();
            }
        }
        throw new java.lang.Error("These aren't the components you're looking for.");
    }

    public static void setSteerAngle(double steerAngle) {
        Controls.steerAngle = steerAngle;
    }

    public static double getSteerAngle() {
        if (Controls.steerPedals != null)
            return getValue(Component.Identifier.Axis.X);
        if (Controls.gamePad != null)
            return 0.5 * getValue(Component.Identifier.Axis.X);

        return Controls.steerAngle;
    }

    public static void setAaChange(int aaChange) {
        Controls.aaChange = aaChange;
    }

    public static int getAaChange() { return Controls.aaChange; }

    public static void setAccelBrake(double accelBrake) {
        Controls.accelBrake = accelBrake;
    }

    public static double getAccelBrake() {
        if (Controls.steerPedals != null)
            return -Controls.getValue(Component.Identifier.Axis.Y);

        if (Controls.gamePad != null) {
            if (Controls.getValue(Identifier.Button.X) == 1)
                return 1;

            if (Controls.getValue(Identifier.Button.A) == 1)
                return -1;

            return 0;
        }

        return Controls.accelBrake;
    }

    public static boolean getButtonXPressed() {
        return (getValue(Component.Identifier.Button._6) == 1.0);
    }

    public static String getIndicator() {
        String indicator;
        if (getValue(Component.Identifier.Button._4) == 1) {
            indicator = "right";
        } else if (getValue(Component.Identifier.Button._5) == 1) {
            indicator = "left";
        } else {
            indicator = "";
        }
        return indicator;
    }
}
