package actr.env;

import java.awt.*;
import java.util.*;

import javax.swing.SwingWorker;
import javax.swing.text.*;

import actr.model.Model;

class Document extends DefaultStyledDocument {
	private Frame frame;
	private Preferences prefs;
	private MutableAttributeSet styleCommand, styleParameter, styleProduction, styleChunk, styleBuffer, styleComment;
	private UndoQueue undos, redos;
	private boolean extrasEnabled = true;
	private int undoKeystrokes = 0;
	private boolean changed = false;
	private boolean suppressStyling = false;
	private int undoCountAtLastSave = 0;
	private SwingWorker<Object, Object> restyleThread = null;
	private final int maxUndos = 50, maxUndoKeystrokes = 10;
	private final int restyleDelay = 100;
	private Vector<Marker> commands, productions, chunks;

	static MutableAttributeSet styleNormal;

	private class UndoState {
		String text;
		int selectionStart;
		int selectionEnd;
	}

	private class UndoQueue {
		int max;
		private Vector<UndoState> v;

		UndoQueue(int max) {
			this.max = max;
			v = new Vector<UndoState>();
		}

		int size() {
			return v.size();
		}

		void push(UndoState ui) {
			v.add(ui);
			if (v.size() > max)
				v.removeElementAt(0);
		}

		UndoState pop() {
			UndoState ui = v.lastElement();
			v.removeElementAt(v.size() - 1);
			return ui;
		}

		void clear() {
			v.clear();
		}

		int lastOffset() {
			return (v.size() == 0) ? 0 : v.lastElement().selectionStart;
		}
	}

	Document(Frame frame, Preferences prefs, boolean suppressStyling) {
		this.frame = frame;
		this.prefs = prefs;
		this.suppressStyling = suppressStyling;

		styleNormal = new SimpleAttributeSet();
		styleCommand = new SimpleAttributeSet();
		styleParameter = new SimpleAttributeSet();
		styleProduction = new SimpleAttributeSet();
		styleChunk = new SimpleAttributeSet();
		styleBuffer = new SimpleAttributeSet();
		styleComment = new SimpleAttributeSet();
		resetStyles();

		undos = new UndoQueue(maxUndos);
		redos = new UndoQueue(maxUndos);

		productions = new Vector<Marker>();
		chunks = new Vector<Marker>();
		commands = new Vector<Marker>();
	}

	void resetStyles() {
		StyleConstants.setFontFamily(styleNormal, prefs.font);
		StyleConstants.setFontSize(styleNormal, prefs.fontSize);
		StyleConstants.setForeground(styleNormal, Color.black);

		StyleConstants.setForeground(styleCommand, prefs.commandColor);
		StyleConstants.setBold(styleCommand, true);

		StyleConstants.setForeground(styleParameter, prefs.parameterColor);
		StyleConstants.setBold(styleParameter, true);

		StyleConstants.setForeground(styleProduction, prefs.productionColor);
		StyleConstants.setBold(styleProduction, true);

		StyleConstants.setForeground(styleChunk, prefs.chunkColor);
		StyleConstants.setBold(styleChunk, false);

		StyleConstants.setForeground(styleBuffer, prefs.bufferColor);
		StyleConstants.setBold(styleBuffer, true);

		StyleConstants.setForeground(styleComment, prefs.commentColor);
	}

	Document createCopy() {
		try {
			Document copy = new Document(frame, prefs, suppressStyling);
			copy.insertString(0, getText(0, getLength()), styleNormal);
			return copy;
		} catch (BadLocationException e) {
			return null;
		}
	}

	void disableExtras() {
		extrasEnabled = false;
	}

	void enableExtras() {
		extrasEnabled = true;
		restyle();
	}

	boolean isChanged() {
		return changed;
	}

	void setChanged(Boolean b) {
		changed = b;
	}

	int findNextParen(char paren, int offset) {
		int length = getLength();
		int parenLevel = 0;
		for (; offset < length; offset++) {
			char c = charAt(offset);
			if (c == ')' && parenLevel > 0)
				parenLevel--;
			if (parenLevel == 0 && c == paren)
				return offset;
			if (c == '(')
				parenLevel++;
		}
		return length - 1;
	}

	int findPreviousParen(char paren, int offset) {
		int parenLevel = 0;
		for (; offset >= 0; offset--) {
			char c = charAt(offset);
			if (c == '(' && parenLevel > 0)
				parenLevel--;
			if (parenLevel == 0 && c == paren)
				return offset;
			if (c == ')')
				parenLevel++;
		}
		return 0;
	}

	int findNextMarker(int offset, Vector<Marker> markers) {
		int i = 0;
		while (i < markers.size() && markers.elementAt(i).getOffset() <= offset)
			i++;
		if (i >= markers.size())
			return getLength();
		return markers.elementAt(i).getOffset();
	}

	int findPreviousMarker(int offset, Vector<Marker> markers) {
		int i = markers.size() - 1;
		while (i >= 0 && markers.elementAt(i).getOffset() >= offset)
			i--;
		if (i < 0)
			return 0;
		return markers.elementAt(i).getOffset();
	}

	Vector<Marker> getCommandMarkers() {
		return commands;
	}

	Vector<Marker> getProductionMarkers() {
		return productions;
	}

	Vector<Marker> getChunkMarkers() {
		return chunks;
	}

	public void insertString(int offset, String text, AttributeSet attr) throws BadLocationException {
		if (extrasEnabled) {
			changed = true;
			if (text.length() == 1)
				undoKeystrokes++;
			if (undos.size() == 0 || Math.abs(offset - undos.lastOffset()) > maxUndoKeystrokes || text.length() != 1
					|| undoKeystrokes >= maxUndoKeystrokes)
				saveState();
		}
		super.insertString(offset, text, styleNormal);
		if (extrasEnabled)
			restyle();
	}

	public void remove(int offset, int length) throws BadLocationException {
		if (extrasEnabled) {
			changed = true;
			int pos = offset;
			char c = ' ';
			while (pos >= 0 && (c = charAt(pos)) != '\n' && isWhite(c))
				pos--;
			if (c == '\n') {
				length += (offset - pos);
				offset = pos;
			}
			if (length == 1)
				undoKeystrokes++;
			if (undos.size() == 0 || Math.abs(offset - undos.lastOffset()) > 1 || length != 1
					|| undoKeystrokes >= maxUndoKeystrokes)
				saveState();
		}
		super.remove(offset, length);
		if (extrasEnabled)
			restyle();
	}

	void restyle() {
		if (suppressStyling)
			return;
		if (restyleThread != null)
			restyleThread.cancel(false);
		restyleThread = new SwingWorker<Object, Object>() {
			public Object doInBackground() {
				try {
					Thread.sleep(restyleDelay);
				} catch (Exception e) {
				}
				return null;
			}

			protected void done() {
				if (!isCancelled()) {
					restyleNow();
					restyleThread = null;
				}
			}
		};
		restyleThread.execute();
	}

	void restyleNow() {
		if (suppressStyling)
			return;
		setCharacterAttributes(0, getLength(), styleNormal, true);
		try {
			recolor();
			if (prefs.autoIndent)
				reindent();

			frame.getEditor().clearMarkers();
			Model model = Model.compile(getText(0, getLength()), frame);
			frame.getEditor().addMarkers(model.getErrors(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private char charAt(int pos) {
		try {
			return getText(pos, 1).charAt(0);
		} catch (Exception e) {
			return ' ';
		}
	}

	private boolean isWhite(int c) {
		return Character.isWhitespace(c);
	}

	private boolean notWhiteOrSpecial(int c) {
		return !(Character.isWhitespace(c) || c == '(' || c == ')');
	}

	private void recolor() throws BadLocationException {
		commands.clear();
		productions.clear();
		chunks.clear();

		if (getLength() == 0)
			return;
		String text = getText(0, getLength());
		int pos = 0;
		int length = getLength();
		int parenLevel = 0;
		String lastCommand = "";
		boolean inProductionRule = false;
		if (prefs.autoHilite)
			setCharacterAttributes(0, length, styleNormal, true);
		while (pos < length) {
			while (pos < length && isWhite(charAt(pos)))
				pos++;
			if (pos >= length)
				break;
			final char c = charAt(pos);
			if (c == '(') {
				int parenPos = pos;
				pos++;
				parenLevel++;
				while (pos < length && isWhite(charAt(pos)))
					pos++;
				int start = pos;
				while (pos < length && notWhiteOrSpecial(charAt(pos)))
					pos++;
				String keyword = getText(start, pos - start);
				if (parenLevel == 1) {
					if (prefs.autoHilite)
						setCharacterAttributes(start, pos - start, styleCommand, false);
					commands.add(new Marker("(" + keyword + " ...)", createPosition(parenPos)));
					lastCommand = keyword;
				} else if (lastCommand.equals("add-dm")) {
					if (prefs.autoHilite)
						setCharacterAttributes(start, pos - start, styleChunk, false);
					chunks.add(new Marker(keyword, createPosition(parenPos)));
				}

				if (keyword.toLowerCase().equals("p")) {
					inProductionRule = true;
					while (pos < length && isWhite(charAt(pos)))
						pos++;
					start = pos;
					while (pos < length && notWhiteOrSpecial(charAt(pos)))
						pos++;
					if (prefs.autoHilite)
						setCharacterAttributes(start, pos - start, styleProduction, false);
					productions.add(new Marker(getText(start, pos - start), createPosition(parenPos)));
				}
			} else if (c == ')') {
				pos++;
				parenLevel--;
				if (parenLevel < 0)
					parenLevel = 0;
				if (parenLevel == 0)
					inProductionRule = false;
			} else if (c == ':' && !inProductionRule) {
				int start = pos;
				while (pos < length && notWhiteOrSpecial(charAt(pos)))
					pos++;
				if (prefs.autoHilite)
					setCharacterAttributes(start, pos - start, styleParameter, false);
			} else if (c == '*') {
				int start = pos;
				while (pos < length && notWhiteOrSpecial(charAt(pos)))
					pos++;
				if (prefs.autoHilite && pos > start + 1)
					setCharacterAttributes(start, pos - start, styleParameter, false);
			} else if (c == '#' && pos + 1 < length && text.charAt(pos + 1) == '|') {
				int start = pos;
				while (pos + 1 < length && (charAt(pos) != '|' || text.charAt(pos + 1) != '#'))
					pos++;
				if (pos < length)
					pos++;
				if (pos < length)
					pos++;
				if (prefs.autoHilite)
					setCharacterAttributes(start, pos - start, styleComment, false);
			} else if (c == ';' || c == '#') {
				int start = pos;
				while (pos < length && charAt(pos) != '\n')
					pos++;
				if (prefs.autoHilite)
					setCharacterAttributes(start, pos - start, styleComment, false);
			} else if (c == '=' || c == '+' || c == '-' || c == '?') {
				int start = pos;
				String buffer = "";
				while (pos < length && notWhiteOrSpecial(charAt(pos))) {
					buffer += charAt(pos);
					pos++;
				}
				if (buffer.endsWith(">") && buffer.length() > 3)
					if (prefs.autoHilite)
						setCharacterAttributes(start, pos - start, styleBuffer, false);
			} else if (c == '!') {
				int start = pos;
				while (pos < length && notWhiteOrSpecial(charAt(pos)))
					pos++;
				if (text.charAt(pos - 1) == '!')
					if (prefs.autoHilite)
						setCharacterAttributes(start, pos - start, styleBuffer, false);
			} else if (pos < length) {
				while (pos < length && notWhiteOrSpecial(charAt(pos)))
					pos++;
			}
		}

		frame.getNavigator().update(this);
	}

	private void reindent() throws BadLocationException {
		if (getLength() == 0)
			return;
		int pos = 0;
		int parenLevel = 0;
		boolean inProduction = false;
		while (pos < getLength()) {
			int start = pos;
			while (pos < getLength() && isWhite(charAt(pos)) && charAt(pos) != '\n')
				pos++;
			if (pos >= getLength())
				pos = getLength() - 1;
			int curSpaces = pos - start;

			String firstToken = "";
			for (int i = pos; i < getLength() && !isWhite(charAt(i)); i++)
				firstToken += charAt(i);

			int startParenLevel = parenLevel;
			while (pos < getLength() && charAt(pos) != '\n') {
				if (charAt(pos) == '(')
					parenLevel++;
				else if (charAt(pos) == ')') {
					parenLevel--;
					if (parenLevel < 0)
						parenLevel = 0;
				}
				pos++;
			}
			pos++;

			int realParenLevel = parenLevel;
			if (firstToken.startsWith("("))
				realParenLevel = startParenLevel;
			else if (parenLevel < startParenLevel && !firstToken.startsWith(")"))
				realParenLevel = startParenLevel;

			if (firstToken.toLowerCase().equals("(p")) {
				inProduction = true;
				realParenLevel = 0;
			} else if (inProduction) {
				if (firstToken.equals(")"))
					realParenLevel = 0;
				else if (firstToken.equals("==>"))
					realParenLevel = 0;
				else if (firstToken.endsWith(">") && (firstToken.startsWith("=") || firstToken.startsWith("+")
						|| firstToken.startsWith("-") || firstToken.startsWith("?")))
					realParenLevel = 1;
				else if (firstToken.startsWith("!") && firstToken.endsWith("!"))
					realParenLevel = 1;
				else
					realParenLevel = 2;
			}

			int newSpaces = prefs.indentSpaces * realParenLevel;
			if (newSpaces != curSpaces) {
				if (curSpaces > 0)
					super.remove(start, curSpaces);
				if (newSpaces > 0) {
					String spacer = "";
					for (int i = 0; i < newSpaces; i++)
						spacer += ' ';
					super.insertString(start, spacer, styleNormal);
				}
			}
			pos += newSpaces - curSpaces;
			if (parenLevel == 0)
				inProduction = false;
		}
	}

	void changeFontSize(int dpoints) {
		SimpleAttributeSet styleSmall = new SimpleAttributeSet();
		StyleConstants.setFontSize(styleSmall, prefs.fontSize + dpoints);
		setCharacterAttributes(0, getLength(), styleSmall, false);
	}

	UndoState getState() {
		UndoState ui = new UndoState();
		try {
			ui.text = getText(0, getLength());
			ui.selectionStart = frame.getEditor().getSelectionStart();
			ui.selectionEnd = frame.getEditor().getSelectionEnd();
		} catch (Exception e) {
		}
		return ui;
	}

	void saveState() {
		changed = true;
		undos.push(getState());
		redos.clear();
		undoKeystrokes = 0;
	}

	void restoreState(UndoState ui) {
		try {
			disableExtras();
			super.replace(0, getLength(), ui.text, styleNormal);
			frame.getEditor().setCaretPosition(ui.selectionStart);
			frame.getEditor().moveCaretPosition(ui.selectionEnd);
			restyleNow();
			enableExtras();
		} catch (Exception e) {
		}
	}

	void undo() {
		if (undos.size() == 0)
			return;
		redos.push(getState());
		restoreState(undos.pop());
		changed = (undos.size() != undoCountAtLastSave);
	}

	void redo() {
		if (redos.size() == 0)
			return;
		undos.push(getState());
		restoreState(redos.pop());
		changed = (undos.size() != undoCountAtLastSave);
	}

	boolean canUndo() {
		return undos.size() > 0;
	}

	boolean canRedo() {
		return redos.size() > 0;
	}

	void resetUndo() {
		undos.clear();
		redos.clear();
	}

	void noteSave() {
		undoCountAtLastSave = undos.size();
		changed = false;
	}
}
