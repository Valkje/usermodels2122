package actr.env;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.text.*;
import resources.Resources;

class Actions {
	private Core core;
	private Frame frame;

	Action newAction, openAction, closeAction, saveAction, saveAsAction, printAction, aboutAction, quitAction;
	Action undoAction, redoAction, cutAction, copyAction, pasteAction;
	Action findAction, findNextAction, findPreviousAction, findHideAction, prefsAction;
	Action runAction, runAnalysisAction, stopAction, resumeAction;
	Action outputBuffersAction, outputWhyNotAction, outputDMAction, outputPRAction, outputVisiconAction,
			outputTasksAction, saveDataAction;
	Action saveTraceAction, saveBoldAction, saveBehaviorAction;

	Actions(final Core core, final Frame frame) {
		this.core = core;
		this.frame = frame;

		newAction = new AbstractAction("New...", Resources.getIcon("jlfNew16.gif")) {
			public void actionPerformed(ActionEvent e) {
				core.newFrame();
			}
		};
		openAction = new AbstractAction("Open...", Resources.getIcon("jlfOpen16.gif")) {
			public void actionPerformed(ActionEvent e) {
				core.openFrame();
			}
		};
		closeAction = new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				core.closeFrame(frame);
			}
		};
		saveAction = new AbstractAction("Save", Resources.getIcon("jlfSave16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.save(false);
			}
		};
		saveAsAction = new AbstractAction("Save As...") {
			public void actionPerformed(ActionEvent e) {
				frame.save(true);
			}
		};
		printAction = new AbstractAction("Print...", Resources.getIcon("jlfPrint16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.print();
			}
		};
		aboutAction = new AbstractAction("About ACT-R") {
			public void actionPerformed(ActionEvent e) {
				core.openAboutDialog();
			}
		};
		quitAction = new AbstractAction("Quit") {
			public void actionPerformed(ActionEvent e) {
				core.quit();
			}
		};

		undoAction = new AbstractAction("Undo", Resources.getIcon("jlfUndo16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.getDocument().undo();
				update();
			}
		};
		redoAction = new AbstractAction("Redo", Resources.getIcon("jlfRedo16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.getDocument().redo();
				update();
			}
		};

		cutAction = new StyledEditorKit.CutAction();
		cutAction.putValue(Action.NAME, "Cut");
		cutAction.putValue(Action.LARGE_ICON_KEY, Resources.getIcon("jlfCut16.gif"));
		copyAction = new StyledEditorKit.CopyAction();
		copyAction.putValue(Action.NAME, "Copy");
		copyAction.putValue(Action.LARGE_ICON_KEY, Resources.getIcon("jlfCopy16.gif"));
		pasteAction = new StyledEditorKit.PasteAction();
		pasteAction.putValue(Action.NAME, "Paste");
		pasteAction.putValue(Action.LARGE_ICON_KEY, Resources.getIcon("jlfPaste16.gif"));

		findAction = new AbstractAction("Find", Resources.getIcon("jlfFind16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.find();
			}
		};
		findNextAction = new AbstractAction("Find Next", Resources.getIcon("jlfFindAgain16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.findNext();
			}
		};
		findPreviousAction = new AbstractAction("Find Previous", Resources.getIcon("FindPrevious16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.findPrevious();
			}
		};
		findHideAction = new AbstractAction("Find Hide") {
			public void actionPerformed(ActionEvent e) {
				frame.findHide();
			}
		};
		prefsAction = new AbstractAction("Preferences...") {
			public void actionPerformed(ActionEvent e) {
				core.openPreferencesDialog();
			}
		};

		runAction = new AbstractAction("Run", Resources.getIcon("jlfPlay16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.run();
			}
		};
		runAnalysisAction = new AbstractAction("Run Analysis", Resources.getIcon("jlfFastForward16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.runAnalysis();
			}
		};
		stopAction = new AbstractAction("Stop", Resources.getIcon("jlfStop16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.stop();
			}
		};
		resumeAction = new AbstractAction("Resume", Resources.getIcon("jlfStepForward16.gif")) {
			public void actionPerformed(ActionEvent e) {
				frame.resume();
			}
		};

		outputBuffersAction = new AbstractAction("Buffers") {
			public void actionPerformed(ActionEvent e) {
				frame.outputBuffers();
			}
		};
		outputWhyNotAction = new AbstractAction("\"Why Not\"") {
			public void actionPerformed(ActionEvent e) {
				frame.outputWhyNot();
			}
		};
		outputDMAction = new AbstractAction("Declarative Memory") {
			public void actionPerformed(ActionEvent e) {
				frame.outputDeclarative();
			}
		};
		outputPRAction = new AbstractAction("Production Rules") {
			public void actionPerformed(ActionEvent e) {
				frame.outputProcedural();
			}
		};
		outputVisiconAction = new AbstractAction("Visual Objects") {
			public void actionPerformed(ActionEvent e) {
				frame.outputVisualObjects();
			}
		};

		saveTraceAction = new AbstractAction ("Save Trace"){
			public void actionPerformed(ActionEvent e){
				frame.saveTrace();
			}
		};

		saveBehaviorAction = new AbstractAction ("Save Behavior"){
			public void actionPerformed(ActionEvent e){
				frame.saveBehavior();
			}
		};
	}

	Action createOpenRecentAction(final File file) {
		return new AbstractAction(file.getName()) {
			public void actionPerformed(ActionEvent e) {
				core.openFrame(file);
			}
		};
	}

	void update() {
		if (frame == null)
			return;

		newAction.setEnabled(true);
		openAction.setEnabled(true);
		frame.getMenus().updateOpenRecent();
		closeAction.setEnabled(true);
		saveAction.setEnabled(frame.getDocument() != null && frame.getDocument().isChanged());
		saveAsAction.setEnabled(frame.getDocument() != null);

		undoAction.setEnabled(
				frame.getDocument() != null && frame.getEditor().hasFocus() && frame.getDocument().canUndo());
		redoAction.setEnabled(
				frame.getDocument() != null && frame.getEditor().hasFocus() && frame.getDocument().canRedo());
		cutAction.setEnabled(frame.getEditor() != null
				// && frame.getEditor().hasFocus()
				&& frame.getEditor().getSelectedText() != null);
		copyAction.setEnabled(frame.getEditor() != null
				// && frame.getEditor().hasFocus()
				&& frame.getEditor().getSelectedText() != null);

		pasteAction.setEnabled(frame.getEditor() != null
				// && frame.getEditor().hasFocus()
				&& (Main.inApplet() || Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this) != null));

		findAction.setEnabled(true);
		findNextAction.setEnabled(frame.isFindNextPossible());
		findPreviousAction.setEnabled(frame.isFindNextPossible());

		boolean runEnabled = !core.isAnyModelRunning();
		runAction.setEnabled(runEnabled); // && frame.isModelFile());
		runAnalysisAction.setEnabled(runEnabled);
		stopAction.setEnabled(core.hasLock(frame));
		resumeAction.setEnabled(runEnabled && frame.getModel() != null && !frame.getModel().isDone());

		boolean outputEnabled = frame.getModel() != null;
		outputBuffersAction.setEnabled(outputEnabled);
		outputWhyNotAction.setEnabled(outputEnabled);
		outputDMAction.setEnabled(outputEnabled);
		outputPRAction.setEnabled(outputEnabled);
		outputVisiconAction.setEnabled(outputEnabled);

		boolean dataEnabled = !core.isAnyModelRunning();
		saveTraceAction.setEnabled(dataEnabled);
		saveBehaviorAction.setEnabled(dataEnabled);

		boolean changed = frame.getDocument().isChanged();
		frame.getRootPane().putClientProperty("Window.documentModified", changed);
		String title = frame.getFileName();
		if (changed)
			title = "*" + title;
		frame.setTitle(title);
	}

	Action createAppletFileAction(final String name) {
		return new AbstractAction(name) {
			public void actionPerformed(ActionEvent e) {
				try {
					frame.open(new URL(Main.getApplet().getCodeBase(), "models/" + name));
				} catch (Exception ex) {
				}
			}
		};
	}
}
