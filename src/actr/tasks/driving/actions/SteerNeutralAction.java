package actr.tasks.driving.actions;

import actr.tasks.driving.Controls;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SteerNeutralAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Right or left released!");
        Controls.setSteerAngle(0);
    }
}
