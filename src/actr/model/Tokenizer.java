package actr.model;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * A tokenizer that breaks an ACT-R model file into relevant tokens for the
 * parser.
 * 
 * @author Dario Salvucci
 */
class Tokenizer {
	private Reader reader = null;
	private int c = 0;
	private int offset = 0;
	private int line = 1;
	private int lastOffset = 0, lastLine = 1;
	private String token = "";
	private Vector<String> putbacks = new Vector<String>();
	private Map<String, String> variables = new HashMap<String, String>();

	boolean caseSensitive = false;

	Tokenizer(File file) throws FileNotFoundException {
		reader = new FileReader(file);
		readChar();
		while (c != -1 && Character.isWhitespace(c))
			readChar();
		advance();
	}

	Tokenizer(URL url) throws IOException {
		reader = new BufferedReader(new InputStreamReader(url.openStream()));
		readChar();
		while (c != -1 && Character.isWhitespace(c))
			readChar();
		advance();
	}

	Tokenizer(String s) {
		reader = new StringReader(s);
		readChar();
		while (c != -1 && Character.isWhitespace(c))
			readChar();
		advance();
	}

	boolean hasMoreTokens() {
		return (c != -1) || !putbacks.isEmpty();
	}

	String getToken() {
		return token;
	}

	boolean isLetterToken() {
		return !token.isEmpty() && Character.isLetter(token.charAt(0));
	}

	int getLine() {
		return line;
	}

	int getOffset() {
		return offset;
	}

	int getLastLine() {
		return lastLine;
	}

	int getLastOffset() {
		return lastOffset;
	}

	void readChar() {
		try {
			c = reader.read();
		} catch (IOException exc) {
			System.err.println("IOException: " + exc.getMessage());
		}
		offset++;
		if (c == '\n' || c == '\r')
			line++;
	}

	boolean isSpecial(int c2) {
		return c2 == '(' || c2 == ')';
	}

	void advance() {
		if (!hasMoreTokens()) {
			token = "";
			return;
		}

		lastOffset = offset;
		lastLine = line;

		if (!putbacks.isEmpty()) {
			token = putbacks.elementAt(0);
			putbacks.removeElementAt(0);
			return;
		}

		StringWriter sr = new StringWriter();

		while (c != -1 && (c == ';' || c == '#')) {
			if (c == ';') {
				while (c != -1 && c != '\n' && c != '\r')
					readChar();
			} else if (c == '#') {
				if (c != -1)
					readChar(); // '#'
				if (c != -1)
					readChar(); // '|'
				while (c != -1 && c != '|')
					readChar();
				if (c != -1)
					readChar(); // '|'
				if (c != -1)
					readChar(); // '#'
			}
			while (c != -1 && Character.isWhitespace(c))
				readChar();
		}

		if (isSpecial(c)) {
			sr.write(c);
			readChar();
		} else if (c == '"') {
			sr.write(c);
			readChar();
			while (c != -1 && c != '"') {
				sr.write(c);
				readChar();
			}
			sr.write(c);
			readChar();
		} else {
			while (c != -1 && !Character.isWhitespace(c) && !isSpecial(c)) {
				sr.write(c);
				readChar();
			}
		}
		while (c != -1 && Character.isWhitespace(c))
			readChar();

		token = sr.toString();

		if (!caseSensitive && !token.startsWith("\""))
			token = token.toLowerCase();

		String value = variables.get(token);
		if (value != null)
			token = value;

		// System.out.println ("-" + token + "-");
	}

	void pushBack(String old) {
		putbacks.add(token);
		token = old;
	}

	void addVariable(String variable, String value) {
		variables.put(variable, value);
	}
}