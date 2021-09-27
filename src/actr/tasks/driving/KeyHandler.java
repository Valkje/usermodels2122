package actr.tasks.driving;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class KeyHandler implements KeyListener {
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
                KeyHandler.steerAngle = -0.5;
                break;
            case 38: // up
                KeyHandler.accelBrake += 0.1;
                break;
            case 39: // right
                KeyHandler.steerAngle = 0.5;
                break;
            case 40: // down
                KeyHandler.accelBrake -= 0.1;
                break;
            case 49: // 1
                KeyHandler.aaLevel = AaLevel.none;
                break;
            case 50: // 2
                KeyHandler.aaLevel = AaLevel.cruise;
                break;
            case 51: //3
                KeyHandler.aaLevel = AaLevel.full;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 37: // left
                KeyHandler.steerAngle = 0;
                break;
            case 39: // right
                KeyHandler.steerAngle = 0;
                break;
        }
    }

    public static double getSteerAngle() {
        return KeyHandler.steerAngle;
    }

    public static AaLevel getAaLevel() { return KeyHandler.aaLevel; }

    public static double getAccelBrake() { return accelBrake; }
}
