package actr.tasks.driving.actions;

import actr.tasks.driving.Controls;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DecreaseSpeedAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Down pressed!");
        Controls.setAccelBrake(-1);
    }
}
