package actr.tasks.driving.actions;

import actr.tasks.driving.AaLevel;
import actr.tasks.driving.Controls;
import actr.tasks.driving.Hud;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class IncreaseAutomationAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Increase automation!");
        Controls.setAaChange(1);
    }
}
