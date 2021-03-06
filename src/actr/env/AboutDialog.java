package actr.env;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import resources.Resources;

class AboutDialog extends JDialog {
	AboutDialog dialog;

	AboutDialog(Core core) {
		super();

		dialog = this;

		setUndecorated(true);
		// setBackground (new Color(0.9f, 0.9f, 0.9f, 0.25f));

		JLabel icon = new JLabel(new ImageIcon(Resources.getImage("actr.png")));
		icon.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel text = new JLabel(
				"<html><center>" + "<font size=4><b>ACT-R</b></font><br>" + "<font size=2>" + ApplicationMain.getVersion()
						+ "</font><br><br>" + "<font size=2>Application by Dario Salvucci &copy; 2010</font><br><br>"
						+ "<font size=2>Icon image by Niels Taatgen &copy; 2007</font><br><br>"
						+ "<font size=2>Cognitive theory by John R. Anderson<br>" + "and the ACT-R community</font><br>"
						+ "</center></html>");
		text.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
		center.add(icon);
		center.add(text);

		center.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				dialog.setVisible(false);
			}
		});

		getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(center, BorderLayout.CENTER);

		pack();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(d.width / 2 - getWidth() / 2, d.height / 2 - getHeight() / 2);
		setVisible(true);
	}
}
