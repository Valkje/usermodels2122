package actr.tasks.driving;

import java.awt.Color;
import java.awt.Graphics;

/**
 * An automated car that drives itself down the road.
 * 
 * @author Dario Salvucci
 */
public class Autocar extends Vehicle {

	long roadIndex;
	boolean visible = false;
	double lane = 2.0;
	Double runningTime = 0.0;
	double distance;
	String perceivedDistance;
	boolean carFlag = false;
	boolean turn = false;
	double changeStart;
	double fl;
	double ot = 3.0; // time to change lanes

	public Autocar() {
		super();
		speed = 0;
	}

	void update(Env env) {
		// autocar follows 0back
		speed = Integer.parseInt(env.speedsign.speedlimit);
		speed = Utilities.mph2mps(Utilities.kph2mph(speed));
		fracIndex += speed * Env.sampleTime;
		fracIndex = Math.min(fracIndex, env.simcar.fracIndex + 100);
		runningTime += Env.sampleTime;

		// autocar switch lane when simcar is in front of the autocar
		if (env.simcar.fracIndex - fracIndex > 0) {
			if (env.simcar.lane != 1) {
				if (env.simcar.lane == 2 && env.construction.construction_vis == false)
					lane = 1;
				else if (env.simcar.lane == 2 && env.construction.construction_vis) {
					lane = 2;
					speed = env.simcar.speed;
					fracIndex = env.simcar.fracIndex - 20;
				} else if (env.simcar.lane == 3) {
					lane = Math.random() > 0.5 ? 2 : 1;
					lane = env.construction.construction_vis ? 2 : lane;

				} else if (env.simcar.lane % 1 != 0) {
					lane = 2;
					speed = env.simcar.speed;
					fracIndex = env.simcar.fracIndex - 20;
				}
			}
		} else {
			if (lane == (int) env.simcar.lane && env.construction.construction_vis
					&& fracIndex - env.simcar.fracIndex < 40) {
				changeStart = env.time;
				fl = Math.min(lane + 1, 3);
				turn = true;
			} else if (lane == 1 && env.construction.construction_vis) {
				changeStart = env.time;
				fl = Math.min(lane + 1, 3);
				turn = true;
			} else if (lane == 1 && env.simcar.lane < 2 && env.construction.construction_vis == false){
				changeStart = env.time;
				fl = Math.min(lane + 2, 3);
				turn = true;
			}
		}

		if (env.construction.construction_vis && env.simcar.lane != 3) {
			if (env.simcar.lane >= lane) {
				changeStart = env.time;
				fl = 3;
				turn = true;
			} else {
				speed = env.simcar.speed / 2;
				changeStart = env.time + 5;
				fl = 3;
				turn = true;
			}
		}

		h = env.road.heading(fracIndex);
		distance = fracIndex - env.simcar.fracIndex;
		perceivedDistance = Math.abs(distance) < 15 ? "close" : "far";

		// ensuring it stays relevant
		if ((distance > 80 && lane != 3) && turn == false) {
			changeStart = env.time;
			fl = lane + 1;
			turn = true;
		} else if (distance > 80 && lane == 3) {
			p = env.road.middle(fracIndex, lane);
			p.y = .65;
		} else {
			p = env.road.middle(fracIndex, lane);
			p.y = .65;
		}

		if (turn)
			switchLane(fl, env);
	}

	void switchLane(double fl, Env env) {

		Position fp = env.road.middle(fracIndex, fl);
		double lw = env.construction.construction_bool ? Env.scenario.lanewidth[1] : Env.scenario.lanewidth[0];
		double inc = ((Math.abs((fp.z - env.road.middle(fracIndex, (int) lane).z)) / (20 * ot))) / lw;
		inc = p.z > fp.z ? inc * (-1) : inc;

		if (0.1 < Math.abs(fl - lane)) {
			lane = lane + inc;
			p = env.road.middle(fracIndex, lane);
		} else {
			lane = fl;
			p = env.road.middle(fracIndex, lane);
			turn = false;
		}
	}

	void draw(Graphics g, Env env) {

		Position pos1 = env.road.middle(fracIndex, lane);
		pos1.z = pos1.z - 1.0;
		pos1.y = 0.0;
		Coordinate im1 = env.world2image(pos1);

		Position pos2 = env.road.middle(fracIndex, lane);
		pos2.z = pos2.z + 1.0;
		pos2.y = 1.0;
		Coordinate im2 = env.world2image(pos2);

		if (im1 != null && im2 != null) {
			g.setColor(Color.blue);
			g.fillRect(im1.x, im2.y, im2.x - im1.x, im1.y - im2.y);
		} else {

		}
	}
}
