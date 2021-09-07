package actr.env;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;

class Navigator extends JPanel {
	private JComboBox commandCB, productionCB, chunkCB;
	private boolean disableAction;

	Navigator(final Frame frame) {
		super();

		disableAction = false;

		commandCB = new JComboBox();
		commandCB.putClientProperty("JComponent.sizeVariant", "small");
		commandCB.setAutoscrolls(true);
		commandCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!disableAction)
					goToSelected(frame, (Marker) commandCB.getSelectedItem());
			}
		});

		productionCB = new JComboBox();
		productionCB.putClientProperty("JComponent.sizeVariant", "small");
		productionCB.setAutoscrolls(true);
		productionCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!disableAction)
					goToSelected(frame, (Marker) productionCB.getSelectedItem());
			}
		});

		chunkCB = new JComboBox();
		chunkCB.putClientProperty("JComponent.sizeVariant", "small");
		chunkCB.setAutoscrolls(true);
		chunkCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!disableAction)
					goToSelected(frame, (Marker) chunkCB.getSelectedItem());
			}
		});

		setLayout(new GridLayout(0, 3));
		add(commandCB);
		add(productionCB);
		add(chunkCB);

		setMinimumSize(new Dimension(1, 1));
		setBorder(BorderFactory.createLineBorder(Color.gray));
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray),
				BorderFactory.createEmptyBorder(3, 9, 3, 3)));
	}

	void update(Document document) {
		disableAction = true;

		commandCB.removeAllItems();
		Vector<Marker> v = document.getCommandMarkers();
		for (int i = 0; i < v.size(); i++) {
			Marker marker = v.elementAt(i);
			if (!marker.getText().startsWith("(p ") && !marker.getText().startsWith("(spp "))
				commandCB.addItem(marker);
		}

		productionCB.removeAllItems();
		v = document.getProductionMarkers();
		for (int i = 0; i < v.size(); i++)
			productionCB.addItem(v.elementAt(i));

		chunkCB.removeAllItems();
		v = document.getChunkMarkers();
		for (int i = 0; i < v.size(); i++)
			chunkCB.addItem(v.elementAt(i));

		disableAction = false;
	}

	private void goToSelected(Frame frame, Marker marker) {
		if (marker == null)
			return;
		int pos = frame.getDocument().findPreviousParen('(', marker.getOffset());
		Editor editor = frame.getEditor();
		editor.setCaretPosition(pos);
		editor.scrollToVisible(pos);
		editor.grabFocus();
	}
}
