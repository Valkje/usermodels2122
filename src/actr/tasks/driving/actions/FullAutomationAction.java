package actr.tasks.driving.actions;

import actr.tasks.driving.AaLevel;
import actr.tasks.driving.Controls;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FullAutomationAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Full automation!");
        Controls.setAaLevel(AaLevel.full);
    }
}
