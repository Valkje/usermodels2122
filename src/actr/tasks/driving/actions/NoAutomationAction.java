package actr.tasks.driving.actions;

import actr.tasks.driving.AaLevel;
import actr.tasks.driving.Controls;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class NoAutomationAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("No automation!");
        //Controls.setAaLevel(AaLevel.none);
    }
}
