package actr.tasks.driving.actions;

import actr.tasks.driving.Controls;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class IncreaseSpeedAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Up pressed!");
        Controls.setAccelBrake(Controls.getAccelBrake() + 0.1);
    }
}
