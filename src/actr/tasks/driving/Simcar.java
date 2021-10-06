package actr.tasks.driving;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * The driver's own vehicle and its controls.
 * 
 * @author Dario Salvucci
 */
public class Simcar extends Vehicle {
	Driver driver;

	double steerAngle;
	double accelerator;
	double brake;
	long roadIndex;
	Position nearPoint;
	Position farPoint;
	Position carPoint;
	int lane;
	double dist_to_nearest_lane;
	double diffDist;
	boolean turning;

	public Simcar(Driver driver, Env env) {
		super();

		this.driver = driver;

		steerAngle = 0;
		accelerator = 0;
		brake = 0;
		speed = 0;
		lane = 2;
	}

	Hud hud = new Hud(this);

	int order = 6;
	int max_order = 10;
	double gravity = 9.8;
	double air_drag_coeff = .25;
	double engine_max_watts = 106000;
	double brake_max_force = 8000;
	double f_surface_friction = .2;
	double lzz = 2618;
	double ms = 1175;
	double a = .946;
	double b = 1.719;
	double caf = 48000;
	double car = 42000;
	double[] y = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	double[] dydx = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	double[] yout = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	double heading = -999;
	double heading1 = -999;
	double heading2 = -999;
	double car_heading;
	double car_accel_pedal;
	double car_brake_pedal;
	double car_deltaf;
	double car_steer;
	double car_speed;
	double car_ke;

	void derivs(double y[], double dydx[]) {
		double phi = y[1];
		double r = y[2];
		double beta = y[3];
		double ke = y[4];
		double u = (ke > 0) ? Math.sqrt(ke * 2 / ms) : 0;
		double deltar = 0;
		double deltaf = car_deltaf;
		dydx[1] = r;
		if (u > 5) {
			dydx[2] = (2.0 * a * caf * deltaf - 2.0 * b * car * deltar - 2.0 * (a * caf - b * car) * beta
					- (2.0 * (a * a * caf + b * b * car) * r / u)) / lzz;
			dydx[3] = (2.0 * caf * deltaf + 2.0 * car * deltar - 2.0 * (caf + car) * beta
					- (ms * u + (2.0 * (a * caf - b * car) / u)) * r) / (ms * u);
		} else {
			dydx[1] = 0.0;
			dydx[2] = 0.0;
			dydx[3] = 0.0;
		}
		double pengine = car_accel_pedal * engine_max_watts;
		double fbrake = car_brake_pedal * brake_max_force;
		double fdrag = (f_surface_friction * ms * gravity) + (air_drag_coeff * u * u);
		dydx[4] = pengine - fdrag * u - fbrake * u;
		dydx[5] = u * Math.cos(phi);
		dydx[6] = u * Math.sin(phi);
	}

	void rk4(int n, double x, double h) {
		double dym[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double dyt[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double yt[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double hh = h * 5;
		double h6 = h / 6;
		int i;

		for (i = 1; i <= n; i++)
			yt[i] = y[i] + hh * dydx[i];
		derivs(yt, dyt);
		for (i = 1; i <= n; i++)
			yt[i] = y[i] + hh * dyt[i];
		derivs(yt, dym);
		for (i = 1; i <= n; i++) {
			yt[i] = y[i] + h * dym[i];
			dym[i] += dyt[i];
		}
		derivs(yt, dyt);
		for (i = 1; i <= n; i++)
			yout[i] = y[i] + h6 * (dydx[i] + dyt[i] + 2.0 * dym[i]);
	}

	void updateDynamics(Env env) {
		Road road = env.road;
		double time = env.time;
		double sampleTime = Env.sampleTime;

		if (heading2 == -999.0) {
			heading = heading1 = heading2 = Math.atan2(h.z, h.x);
			yout[1] = y[1] = car_heading = heading;
			yout[2] = y[2] = 0.0;
			yout[3] = y[3] = 0.0;
			yout[4] = y[4] = car_ke = 50000; // 0.0; // kinetic energy > 0, otherwise unstable at start
			yout[5] = y[5] = p.x;
			yout[6] = y[6] = p.z;
			if (car_ke > 0.0)
				car_speed = Math.sqrt(2.0 * car_ke / ms);
			else
				car_speed = 0.0;
		}

		car_steer = steerAngle;
		car_accel_pedal = accelerator;
		car_brake_pedal = brake;

		// original had lines below; changing to linear steering function
		// if (car_steer < 0.0) car_deltaf = -0.0423 * Math.pow(-1.0*car_steer, 1.3);
		// else car_deltaf = 0.0423 * Math.pow(car_steer,1.3);
		car_deltaf = 0.0423 * car_steer;

		// drift -mh
		double forcing = 0.125 * (0.01 * Math.sin(2.0 * 3.14 * 0.13 * time + 1.137)
				+ 0.005 * Math.sin(2.0 * 3.14 * 0.47 * time + 0.875));
		car_deltaf += forcing;

		derivs(y, dydx);
		rk4(order, time, sampleTime);

		y[1] = car_heading = yout[1];
		y[2] = yout[2];
		y[3] = yout[3];
		y[4] = car_ke = yout[4];
		y[5] = p.x = yout[5];
		y[6] = p.z = yout[6];

		if (car_ke > 0.0)
			car_speed = Math.sqrt(2.0 * car_ke / ms);
		else
			car_speed = 0.0;

		h.x = Math.cos(car_heading);
		h.z = Math.sin(car_heading);

		heading2 = heading1;
		heading1 = heading;
		heading = car_heading;

		speed = car_speed;

		long i = Math.max(1, roadIndex);
		long newi = i;
		Position nearloc = (road.middle(i)).subtract(p);
		double norm = (nearloc.x * nearloc.x) + (nearloc.z * nearloc.z); // error in lisp!
		double mindist = norm;
		boolean done = false;
		while (!done) {
			i += 1;
			nearloc = (road.middle(i)).subtract(p);
			norm = (nearloc.x * nearloc.x) + (nearloc.z * nearloc.z); // error in lisp!
			if (norm < mindist) {
				mindist = norm;
				newi = i;
			} else
				done = true;
		}
		Position vec1 = (road.middle(newi)).subtract(p);
		Position vec2 = (road.middle(newi)).subtract(road.middle(newi - 1));
		double dotprod = -((vec1.x * vec2.x) + (vec1.z * vec2.z));
		double fracdelta;
		if (dotprod < 0) {
			newi--;
			fracdelta = 1.0 + dotprod;
		} else
			fracdelta = dotprod;

		fracIndex = newi + fracdelta;
		roadIndex = newi;

		double distLeft = env.simcar.p.z - env.road.left(env.simcar.fracIndex, lane).z;
		double distRight = env.simcar.p.z - env.road.right(env.simcar.fracIndex, lane).z;
		dist_to_nearest_lane = Utilities.absoluteMin(distLeft, distRight);
		diffDist = Math.abs(distLeft) - Math.abs(distRight); //positive -> should drive to the right
	}

	void update(Env env) {
		updateDynamics(env);

		nearPoint = env.road.nearPoint(this, lane);
		farPoint = env.road.farPoint(this, lane);
		carPoint = env.autocar.p;
	}

	void draw(Graphics g, Env env) {
		int dashHeight = Env.envHeight / 3; // default: 80
		g.setColor(Color.black);
		g.fillRect(0, Env.envHeight - dashHeight, Env.envWidth, dashHeight);

		int steerX = Env.envWidth / 4;
		int steerY = (int) (Env.envHeight * (2.6 / 3)); // Just under the middle of the dashboard
		int steerR = Env.envHeight / 6;
		g.setColor(Color.darkGray);
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform saved = g2d.getTransform();
		g2d.translate(steerX, steerY);
		g2d.rotate(steerAngle);
		g2d.setStroke(new BasicStroke(10));
		g2d.drawOval(-steerR, -steerR, 2 * steerR, 2 * steerR);
		g2d.fillOval(-steerR / 4, -steerR / 4, steerR / 2, steerR / 2);
		g2d.drawLine(-steerR, 0, +steerR, 0);
		g2d.setTransform(saved);

		// mh - speedometer
		double speedNum = speed;
		int speedoX = Env.envWidth / 4; // In-between the left border and the steering wheel
		int speedoY = steerY - steerR / 2; // Just under the middle of the dashboard
		String speed = Integer.toString((int) Utilities.mph2kph(Utilities.mps2mph(speedNum)));
		Font myFont = new Font("Helvetica", Font.BOLD, 24);
		g.setFont(myFont);
		g.setColor(Color.WHITE);

		FontMetrics fm = g.getFontMetrics();
		speedoX = speedoX - fm.stringWidth(speed) / 2 ;

		g.drawString(speed, speedoX, speedoY);

		// top - mirror
		int mirrorWidth = Env.envWidth / 10;
		int mirrorHeight = Env.envHeight / 20;
		int mirrorX = Env.envWidth / 2 - mirrorWidth / 2;
		int mirrorY = Env.envHeight / 20;

		g.setColor(Color.black);
		g.fillRoundRect(mirrorX, mirrorY, mirrorWidth, mirrorHeight, 30, 20);
		g.fillRect(Env.envWidth / 2 - 5, 0, 10, mirrorY);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRoundRect(mirrorX + 5, mirrorY + 5, mirrorWidth - 10, mirrorHeight - 10, 30, 20);
		// g.fillRoundRect(5, Env.envHeight - dashHeight, 45, 25, 30, 20); side-view

		mirrorWidth = Env.envWidth / 20;
		mirrorX = 5;
		mirrorY = Env.envHeight - dashHeight - mirrorHeight;
		// left-side mirror
		g.setColor(Color.black);
		g.fillRoundRect(mirrorX, mirrorY, mirrorWidth, mirrorHeight, 40, 20);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRoundRect(mirrorX + 5, mirrorY + 5, mirrorWidth - 10, mirrorHeight - 10, 40, 20);

		mirrorX = Env.envWidth - mirrorWidth - 5;
		// right-side mirror
		g.setColor(Color.black);
		g.fillRoundRect(mirrorX, mirrorY, mirrorWidth, mirrorHeight, 40, 20);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRoundRect(mirrorX + 5, mirrorY + 5, mirrorWidth - 10, mirrorHeight - 10, 40, 20);

		hud.draw(g, env);
	}

	double devscale = .0015;
	double devx = -.7;
	double devy = .5;

	double ifc2gl_x(double x) {
		return devx + (devscale * -(x - Driving.centerX));
	}

	double ifc2gl_y(double y) {
		return devy + (devscale * -(y - Driving.centerY));
	}

	double gl2ifc_x(double x) {
		return Driving.centerX - ((x - devx) / devscale);
	}

	double gl2ifc_y(double y) {
		return Driving.centerY - ((y - devy) / devscale);
	}
}
