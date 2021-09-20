package actr.tasks.driving;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.security.Key;

public class KeyHandler implements KeyListener {
    private static double steerAngle = 0;
    private static double accelBrake = 0;

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
            case 39: // right
                KeyHandler.steerAngle = 0.5;
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
}
