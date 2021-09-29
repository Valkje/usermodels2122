package actr.tasks.driving.actions;

import actr.tasks.driving.Controls;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SteerRightAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Right pressed!");
        Controls.setSteerAngle(0.5);
    }
}
