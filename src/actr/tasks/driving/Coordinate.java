package actr.tasks.driving;

/**
 * A simple class for x,y coordinates plus depth.
 * 
 * @author Dario Salvucci
 */
public class Coordinate {
	int x, y;
	double d;

	Coordinate(int xArg, int yArg) {
		x = xArg;
		y = yArg;
		d = 0;
	}

	Coordinate(int xArg, int yArg, double dArg) {
		x = xArg;
		y = yArg;
		d = dArg;
	}

	Coordinate myclone() {
		return new Coordinate(x, y);
	}

	public String toString() {
		return "(" + Utilities.df2.format(x) + "," + Utilities.df2.format(y) + "," + Utilities.df2.format(d) + ")";
	}

};
