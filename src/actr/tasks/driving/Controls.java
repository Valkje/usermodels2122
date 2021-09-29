package actr.tasks.driving;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class Controls implements KeyListener {
    private static double steerAngle = 0;
    private static double accelBrake = 0.2;
    private static AaLevel aaLevel = AaLevel.full;


    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println(e);

        switch (e.getKeyCode()) {
            case 37: // left
                Controls.steerAngle = -0.5;
                break;
            case 38: // up
                Controls.accelBrake += 0.1;
                break;
            case 39: // right
                Controls.steerAngle = 0.5;
                break;
            case 40: // down
                Controls.accelBrake -= 0.1;
                break;
            case 49: // 1
                Controls.aaLevel = AaLevel.none;
                break;
            case 50: // 2
                Controls.aaLevel = AaLevel.cruise;
                break;
            case 51: //3
                Controls.aaLevel = AaLevel.full;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 37: // left
                Controls.steerAngle = 0;
                break;
            case 39: // right
                Controls.steerAngle = 0;
                break;
        }
    }

    public static void setSteerAngle(double steerAngle) {
        Controls.steerAngle = steerAngle;
    }

    public static double getSteerAngle() {
        return Controls.steerAngle;
    }

    public static void setAaLevel(AaLevel aaLevel) {
        Controls.aaLevel = aaLevel;
    }

    public static AaLevel getAaLevel() { return Controls.aaLevel; }

    public static void setAccelBrake(double accelBrake) {
        Controls.accelBrake = accelBrake;
    }

    public static double getAccelBrake() { return accelBrake; }
}
