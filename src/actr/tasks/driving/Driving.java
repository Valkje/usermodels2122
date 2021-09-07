package actr.tasks.driving;

import java.awt.BorderLayout;
// import java.io.BufferedWriter;
// import java.io.FileWriter;
// import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;

import actr.task.Result;
import actr.task.Task;

/**
 * The main Driving task class that sets up the simulation and starts periodic
 * updates.
 * 
 * @author Dario Salvucci
 */
public class Driving extends actr.task.Task {
	static Simulator simulator = null;

	Simulation simulation;
	JLabel nearLabel, farLabel, signLabel, carLabel, speedoLabel, leftmirrorLabel, rightmirrorLabel, leftLaneLabel,
			rightLaneLabel, construction;

	final double scale = 0.6; // .85 //.6
	final double steerFactor_dfa = (16 * scale);
	final double steerFactor_dna = (4 * scale);
	final double steerFactor_na = (3 * scale);
	final double steerFactor_fa = (0 * scale);
	final double accelFactor_thw = (1 * .40);
	final double accelFactor_dthw = (3 * .40);
	final double steerNaMax = .07;
	final double thwFollow = 1.0;
	final double thwMax = 5.0;

	double startTime = 0, endTime = 5000;
	double accelBrake = 0, speed = 0;

	static int minX = 174, maxX = (238 + 24), minY = 94, maxY = (262 + 32);
	static int centerX = (minX + maxX) / 2, centerY = (minY + maxY) / 2;

	String previousLimit = "0";
	int nback_count = 0;
	double signOnset = 0;
	double instructionsOnset = 0;
	double warningOnset = 0;
	static boolean instructionsSeen = false;
	static boolean warningSeen = false;
	static int speedI = 0;
	static String currentNBack = "";
	String[] nBack_list = { "2back", "3back", "0back", "1back", "4back", "0back", "3back", "4back", "1back", "2back" };
	double sign_count = 0;
	int rehearsal_count = 0;
	static String imaginedSpeedlimit = "";
	List<String> output = new ArrayList<String>();
	// int block;
	double othw = 0;
	double st = 0;
	double lastSafe = 0;
	boolean previousSafe = false;
	double switch_to_safe = 0;

	public Driving() {
		super();
		nearLabel = new JLabel(".");
		farLabel = new JLabel("X");
		carLabel = new JLabel("Car");
		signLabel = new JLabel("Sign");
		speedoLabel = new JLabel("Speed");
		leftmirrorLabel = new JLabel("Lmirror");
		rightmirrorLabel = new JLabel("Rmirror");
		leftLaneLabel = new JLabel("lLane");
		rightLaneLabel = new JLabel("rLane");
		construction = new JLabel("Cons");
	}

	public void start() {
		simulation = new Simulation(getModel());

		if (getModel().getRealTime()) {
			setLayout(new BorderLayout());
			if (simulator == null)
				simulator = new Simulator();
			add(simulator, BorderLayout.CENTER);
			simulator.useSimulation(simulation);
		} else {
			// road
			add(nearLabel);
			nearLabel.setSize(20, 20);
			nearLabel.setLocation(250, 250);
			add(farLabel);
			farLabel.setSize(20, 20);
			farLabel.setLocation(250, 250);
			add(leftLaneLabel);
			leftLaneLabel.setSize(20, 20);
			leftLaneLabel.setLocation(125, 250);
			add(rightLaneLabel);
			rightLaneLabel.setSize(20, 20);
			rightLaneLabel.setLocation(375, 250);
			add(signLabel);
			signLabel.setSize(20, 20);
			signLabel.setLocation(250, 250);
			// car
			add(speedoLabel);
			speedoLabel.setSize(20, 20);
			speedoLabel.setLocation(300, 350);
			add(leftmirrorLabel);
			leftmirrorLabel.setSize(20, 20);
			leftmirrorLabel.setLocation(30, Env.envHeight - 90);
			add(rightmirrorLabel);
			rightmirrorLabel.setSize(20, 20);
			rightmirrorLabel.setLocation(Env.envWidth - 30, Env.envHeight - 90);
			add(carLabel);
			carLabel.setSize(20, 20);
			carLabel.setLocation(250, 250);
		}

		getModel().runCommand("(set-visual-frequency near .1)");
		getModel().runCommand("(set-visual-frequency far .1)");
		getModel().runCommand("(set-visual-frequency car .1");

		// they don't reset otherwise if you run the model multiple times in a row
		accelBrake = 0;
		previousLimit = "60";

		getModel().getVision().addVisualnoStuffing("near", "near", "near", nearLabel.getX(), nearLabel.getY(), 1, 1,
				10);
		getModel().getVision().addVisualnoStuffing("far", "far", "far", farLabel.getX(), farLabel.getY(), 1, 1, 100);
		getModel().getVision().addVisualnoStuffing("speedometer", "speedometer", "speedometer", 260, 300, 10, 10, 1);
		getModel().getVision().addVisualnoStuffing("left-mirror", "left-mirror", "road-clear", 30, Env.envHeight - 90,
				10, 10, 1);
		getModel().getVision().addVisualnoStuffing("right-mirror", "right-mirror", "road-clear", Env.envWidth - 30,
				Env.envHeight - 90, 10, 10, 1);
		getModel().getVision().addVisualnoStuffing("left-lane", "left-lane", "clear", 20, 20, 20, 20, 10);
		getModel().getVision().addVisualnoStuffing("right-lane", "right-lane", "clear", 180, 20, 20, 20, 10);

		addPeriodicUpdate(Env.sampleTime);
	}

	public void update(double time) {
		Env env = simulation.env;
		// if (time <= endTime) {
		if (env.road.block < simulation.scenario.blocks) {
			env.time = time - startTime;
			updateVisuals();
			simulation.update();
			env.simcar.driver.update(getModel());
			if (env.simcar.fracIndex > Env.scenario.block_length * env.road.block)
				env.road.block++;
		} else {
			String filename = "_behavior_";

			if (simulation.model.behaviorOut) {
				List<String> output = output(simulation.samples);
				simulation.model.print(output, filename, simulation.model.subj);
			}
			// simulation.model.print(simulation.samples, filename, simulation.model.subj);
			getModel().stop();
		}
	}

	// save to file
	List<String> output(Vector<Sample> samples) {
		for (int i = 1; i < samples.size(); i++) {
			Sample s = samples.elementAt(i);
			if (i == 1) {
				output.add(s.listVarsSep() + System.lineSeparator());
				output.add(s.toStringSep() + System.lineSeparator());
			} else
				output.add(s.toStringSep() + System.lineSeparator());
		}
		return output;
		// Model.print(output, "_driving_");
	}

	void updateVisuals() {

		Env env = simulation.env;
		// egocar
		if (env.simcar.nearPoint != null) {
			Coordinate cn = env.world2image(env.simcar.nearPoint);
			Coordinate cf = env.world2image(env.simcar.farPoint);
			if (cn == null || cf == null) {
				env.done = true;
			} else {
				nearLabel.setLocation(cn.x, cn.y);
				farLabel.setLocation(cf.x, cf.y);
				getModel().getVision().moveVisual("near", cn.x, cn.y);
				getModel().getVision().moveVisual("far", cf.x, cf.y);
			}
		}

		// lane-change
		if (env.simcar.nearPoint != null) {
			Coordinate cnr = null;
			Coordinate cnl = null;
			if (env.simcar.lane == 1) {
				cnr = env.world2image(env.road.lanePoint(env.simcar, 2));
			} else if (env.simcar.lane == 2) {
				cnl = env.world2image(env.road.lanePoint(env.simcar, 1));
				cnr = env.world2image(env.road.lanePoint(env.simcar, 3));
			} else if (env.simcar.lane == 3) {
				cnl = env.world2image(env.road.lanePoint(env.simcar, 2));
			}

			if (cnr != null) {
				getModel().getVision().moveVisual("right-lane", cnr.x, cnr.y);
				if ((env.autocar.distance < 20 || (env.autocar.distance > -20 && env.autocar.distance < 0))
						&& env.autocar.lane == env.simcar.lane + 1)
					getModel().getVision().changeValue("right-lane", "busy");
				else
					getModel().getVision().changeValue("right-lane", "clear");
			}

			if (cnl != null) {
				getModel().getVision().moveVisual("left-lane", cnl.x, cnl.y);
				if (Math.abs(env.autocar.distance) < 40 && env.autocar.lane == env.simcar.lane - 1)
					getModel().getVision().changeValue("left-lane", "busy");
				else
					getModel().getVision().changeValue("left-lane", "clear");
			}

			if (env.construction.construction_vis && env.simcar.lane < 3) {
				getModel().getVision().changeValue("left-lane", "busy");
			} else {
				getModel().getVision().changeValue("left-lane", "clear");
			}
		}

		// speed sign
		Coordinate cs = null;
		if (env.speedsign.signPos != null)
			cs = env.world2image(env.speedsign.signPos);
		if (getModel().getVision().visualObjects().contains("speedsign") == false && env.speedsign.newSign == true) {
			signLabel.setLocation(cs.x, cs.y);
			env.speedsign.visible = true;
			if (cs.d < 40) {
				getModel().getVision().addVisual("speedsign", "speedsign", env.speedsign.speedlimit, cs.x, cs.y, 1, 1,
						cs.d);
				env.speedsign.newSign = false;
			}
		} else if (cs != null) {
			signLabel.setLocation(cs.x, cs.y);
			getModel().getVision().moveVisual("speedsign", cs.x, cs.y);
		} else {
			getModel().getVision().removeVisual("speedsign");
			env.speedsign.visible = false;
		}

		// autocar
		if (env.autocar.p != null) {
			Coordinate cc = env.world2image(env.autocar.p);
			if (cc != null) {
				if (cc.x > 0 && cc.x < Env.envWidth && env.autocar.turn == false) {
					if (getModel().getVision().visualObjects().contains("car")) {
						carLabel.setLocation(cc.x, cc.y);
						getModel().getVision().moveVisual("car", cc.x, cc.y, env.autocar.distance);
						getModel().getVision().changeValue("car", String.valueOf(Math.ceil(env.autocar.lane)));
					} else if (cc.d > 10 && cc.d < 50 && env.autocar.lane % 1 == 0 && env.autocar.turn == false) {
						getModel().getVision().addVisual("car", "car", String.valueOf(Math.ceil(env.autocar.lane)),
								cc.x, cc.y, 1, 1, env.autocar.distance);
					}
				} else {
					getModel().getVision().removeVisual("car");
				}
			}
			if (env.autocar.distance > 80 && Math.abs(env.autocar.lane - 3) < 0.01 && env.autocar.carFlag == false
					&& env.simcar.lane > 2 && getModel().getVision().visualObjects().contains("car")) {
				// getModel().getVision().addVisualnoStuffing("car", "car",
				// String.valueOf(env.autocar.lane), cc.x, cc.y, 1, 1,
				// (int)env.autocar.distance);
				env.autocar.carFlag = true;
			}
			// left mirror
			if (env.autocar.p != null) {
				double d = env.autocar.distance;
				if (d < 0 && d > -15 && env.autocar.lane == env.simcar.lane - 1)
					getModel().getVision().changeValue("left-mirror", "busy");
				else
					getModel().getVision().changeValue("left-mirror", "clear");
			}

			// right mirror
			if (env.autocar.p != null) {
				double d = env.autocar.distance;
				if (d < 0 && d > -15 && env.autocar.lane == env.simcar.lane + 1)
					getModel().getVision().changeValue("right-mirror", "busy");
				else
					getModel().getVision().changeValue("right-mirror", "clear");
			}

			// speedometer
			String speed = Integer.toString((int) Utilities.mph2kph(Utilities.mps2mph(simulation.env.simcar.speed)));
			getModel().getVision().changeValue("speedometer", speed);
			env.done = true;

			// construction site
			if (env.construction.construction_vis) {
				if (getModel().getVision().visualObjects().contains("construction") == false) {
					Position pos = env.road.middle(env.construction.start_con, 1);
					Coordinate ccc = env.world2image(pos);
					getModel().getVision().addVisual("construction", "construction", "start", ccc.x, ccc.y, 20, 20, 1);
				}
			} else if (env.simcar.fracIndex < env.construction.stop_con + 10) {
				if (getModel().getVision().visualObjects().contains(" end ") == false) {
					getModel().getVision().removeVisual("construction");
					Position pos = env.road.middle(env.simcar.fracIndex + 10, 1);
					Coordinate ccc = env.world2image(pos);
					getModel().getVision().addVisual("construction", "construction", "end", ccc.x, ccc.y, 20, 20, 1);
				}
			} else {
				getModel().getVision().removeVisual("construction");

			}
		}
	}

	double minSigned(double x, double y) {
		return (x >= 0) ? Math.min(x, y) : Math.max(x, -y);
	}

	void doSteer(double na, double dna, double dfa, double dt) {
		Simcar simcar = simulation.env.simcar;
		if (simcar.speed >= 10.0) {
			double dsteer = (dna * steerFactor_dna) + (dfa * steerFactor_dfa)
					+ (minSigned(na, steerNaMax) * steerFactor_na * dt);
			dsteer *= simulation.driver.steeringFactor;
			simcar.steerAngle += dsteer;
		} else
			simcar.steerAngle = 0;
	}

	void keepLane(double na, double dna, double dfa, double dt) {
		Env env = simulation.env;
		double dist = env.simcar.dist_to_nearest_lane; // positive: smallest diff is to the left
		if (env.simcar.speed >= 10.0) {
			// double dsteer = 0.00001; // (dist * 0.005) + (minSigned(dist, steerNaMax) *
			// 0.005 * dt);
			double dsteer = (dna * steerFactor_dna) + (dfa * steerFactor_dfa)
					+ (minSigned(na, steerNaMax) * steerFactor_na * dt);
			// left.middle = -3.66, center.middle = 0, right.middle = 3.66
			// double distDiff = env.simcar.diffDist > 0 ? (Math.abs(env.simcar.diffDist/10)
			// + 1) * -1 : env.simcar.diffDist/10 + 1;
			double distDiff = Math.abs(env.simcar.diffDist) / 10 + 1;
			dsteer *= simulation.driver.steeringFactor; // positive -> steer more to the right
			if (dist < 1)
				dsteer = dsteer > 0 && dist > 0 ? dsteer : dsteer;
			dsteer *= distDiff;
			env.simcar.steerAngle += dsteer;
		} else
			env.simcar.steerAngle = 0;
	}

	void doAccelerate(double fthw, double dthw, double dt) {
		Simcar simcar = simulation.env.simcar;
		if (simcar.speed >= 10.0) {
			double dacc = (dthw * accelFactor_dthw) + (dt * (fthw - thwFollow) * accelFactor_thw);
			accelBrake += dacc;
			accelBrake = minSigned(accelBrake, 1.0);
		} else {
			accelBrake = .65 * (simulation.env.time / 3.0);
			accelBrake = minSigned(accelBrake, .65);
		}
		simcar.accelerator = (accelBrake >= 0) ? accelBrake : 0;
		simcar.brake = (accelBrake < 0) ? -accelBrake : 0;
	}

	void keepLimit(double tlimit) {
		Env env = simulation.env;
		imaginedSpeedlimit = Integer.toString((int) tlimit); // for sampling
		Simcar simcar = simulation.env.simcar;
		double speed = simcar.speed;
		tlimit = Utilities.mph2mps(Utilities.kph2mph(tlimit));
		// double diff = (tlimit - speed);

		if (Math.abs(tlimit - speed) > 0) {
			// double dacc = (dthw * accelFactor_dthw 1.2)
			// + (dt * (fthw - thwFollow 1.0) * accelFactor_thw 0.4);
			// double dacc = diff/100;
			// accelBrake += dacc;
			double fp = env.simcar.p.x;
			double dt = env.time - st;
			double tr = tlimit * dt;
			double ftr = speed * dt;
			fp = fp - ftr + tr;
			double dist = fp - env.simcar.p.x;
			double thw = Math.min(dist / speed, 4.0);
			double dhw = thw - othw;
			double dacc = (dhw * accelFactor_dthw * 5) + (dt * (thw) * accelFactor_thw * 5);
			// double dacc = dhw + (Env.sampleTime* (-dhw));
			accelBrake += dacc;
			accelBrake = minSigned(accelBrake, 1.0);
			othw = thw;
			st = env.time;
		}
		simcar.accelerator = (accelBrake >= 0) ? accelBrake : 0;
		simcar.brake = (accelBrake < 0) ? -accelBrake : 0;
	}

	boolean isCarStable(double na, double nva, double fva) {
		double f = 2.5;
		return (Math.abs(na) < .025 * f) && (Math.abs(nva) < .0125 * f) && (Math.abs(fva) < .0125 * f);
	}

	boolean isCarSafe() {
		Env env = simulation.env;
		if (env.time > 5 && (switch_to_safe + 3 < env.time)) {

			double steerAngle = env.simcar.steerAngle;
			double safeDistance = 0.7;
			double carWidth = 2.0;
			double carPosition = env.simcar.p.z;
			double leftLane = env.road.left(env.simcar.fracIndex, env.simcar.lane).z;
			double rightLane = env.road.right(env.simcar.fracIndex, env.simcar.lane).z;
			boolean b = carPosition > leftLane + (safeDistance + carWidth / 2)
					&& carPosition < rightLane - (safeDistance + carWidth / 2)
					&& Math.abs(steerAngle) < 0.05;					;
			if (b == false){
				switch_to_safe = env.time;
			}
			previousSafe = b;
			b = b && previousSafe ? true: false;
			return b;
		} else {
			return false;
		}
	}

	boolean isLaneChangeNeeded(Double fd, Double fthw, Double dthw) {
		Env env = simulation.env;
		boolean change;
		if (env.simcar.lane == env.autocar.lane) {
			change = ((fthw < 3.0 && dthw < -0.125) || fthw < 2.0) ? true : false;
		} else {
			change = ((fthw < 4.5 && dthw < -0.1) || fthw < 3.0 && dthw < 0) ? true : false;
		}
		return change;
	}

	String angle2pos(double sl, double fd, double fx) {
		// angle to position of the autocar relative to simcar (left/right/in front)
		Env env = simulation.env;
		Position l = Road.location(env.simcar.fracIndex + fd, sl); // left line of the lane
		Position r = Road.location(env.simcar.fracIndex + fd, sl + 1); // right line of the lane
		String dir = null;
		if (env.world2image(env.autocar.p) != null) {
			if (env.world2image(l) != null && fx < env.world2image(l).x) {
				dir = "left";
			} else if (env.world2image(r) != null && fx > env.world2image(r).x) {
				dir = "right";
			} else
				dir = "ahead";
		} else if (env.simcar.p.z < env.autocar.p.z)
			dir = "left";
		else if (env.simcar.p.z > env.autocar.p.z)
			dir = "right";
		else
			dir = "ahead";
		return dir;
	}

	double image2angle(double x, double d) {
		Env env = simulation.env;
		double px = env.simcar.p.x + (env.simcar.h.x * d);
		double pz = env.simcar.p.z + (env.simcar.h.z * d);
		Coordinate im = env.world2image(new Position(px, pz));
		try {
			return Math.atan2(.5 * (x - im.x), 450);
		} catch (Exception e) {
			return 0;
		}
	}

	void changeLane(String dir) {
		Env env = simulation.env;
		env.simcar.turning = true;
		env.simcar.lane += dir == "left" ? -1 : 1;
	}

	public void eval(Iterator<String> it) {
		it.next();
		String cmd = it.next();
		if (cmd.equals("do-steer")) {
			double na = Double.valueOf(it.next());
			double dna = Double.valueOf(it.next());
			double dfa = Double.valueOf(it.next());
			double dt = Double.valueOf(it.next());
			doSteer(na, dna, dfa, dt);
		} else if (cmd.equals("do-accelerate")) {
			double fthw = Double.valueOf(it.next());
			double dthw = Double.valueOf(it.next());
			double dt = Double.valueOf(it.next());
			doAccelerate(fthw, dthw, dt);
		} else if (cmd.equals("keep-limit")) {
			double tlimit = Double.valueOf(it.next());
			keepLimit(tlimit);
		} else if (cmd.equals("keep-lane")) {
			double na = Double.valueOf(it.next());
			double dna = Double.valueOf(it.next());
			double dfa = Double.valueOf(it.next());
			double dt = Double.valueOf(it.next());
			keepLane(na, dna, dfa, dt);
		} else if (cmd.equals("change-lane-left")) {
			changeLane("left");
		} else if (cmd.equals("change-lane-right")) {
			changeLane("right");
		} else if (cmd.equals("placeholder")) {
			//
		}
	}

	public boolean evalCondition(Iterator<String> it) {
		it.next();
		String cmd = it.next();
		if (cmd.equals("is-car-stable") || cmd.equals("is-car-not-stable")) {
			double na = Double.valueOf(it.next());
			double nva = Double.valueOf(it.next());
			double fva = Double.valueOf(it.next());
			boolean b = isCarStable(na, nva, fva);
			return cmd.equals("is-car-stable") ? b : !b;
		} else if (cmd.equals("safe-zone") || cmd.equals("not-safe-zone")) {
			boolean b = isCarSafe();
			return cmd.equals("safe-zone") ? b : !b;
		} else if (cmd.equals("same-lane") || cmd.equals("not-same-lane")) {
			double threshold = 0.001;
			double sl = Double.valueOf(it.next());
			double cl = Double.valueOf(it.next());
			boolean b = Math.abs(sl - cl) < threshold ? true : false;
			return cmd.equals("same-lane") ? b : !b;
		} else if (cmd.equals("autocar-left") || cmd.equals("autocar-not-left")) {
			double sl = Double.valueOf(it.next());
			double fd = Double.valueOf(it.next());
			double fx = Double.valueOf(it.next());
			String s1 = "left";
			boolean b = s1.equals(angle2pos(sl, fd, fx));
			return cmd.equals("autocar-left") ? b : !b;
		} else if (cmd.equals("autocar-right") || cmd.equals("autocar-not-right")) {
			double sl = Double.valueOf(it.next());
			double fd = Double.valueOf(it.next());
			double fx = Double.valueOf(it.next());
			String s1 = "right";
			boolean b = s1.equals(angle2pos(sl, fd, fx));
			return cmd.equals("autocar-right") ? b : !b;
		} else if (cmd.equals("autocar-ahead") || cmd.equals("autocar-not-ahead")) {
			double sl = Double.valueOf(it.next());
			double fd = Double.valueOf(it.next());
			double fx = Double.valueOf(it.next());
			String s1 = "ahead";
			boolean b = s1.equals(angle2pos(sl, fd, fx));
			return cmd.equals("autocar-ahead") ? b : !b;
		} else if (cmd.equals("car-too-close") || cmd.equals("car-not-too-close")) {
			double fd = Double.valueOf(it.next());
			double fthw = Double.valueOf(it.next());
			double dthw = Double.valueOf(it.next());
			boolean b = isLaneChangeNeeded(fd, fthw, dthw);
			return cmd.equals("car-too-close") ? b : !b; // c : !c
		} else if (cmd.equals("overtaking-safe") || cmd.equals("overtaking-not-safe")) {
			String value = String.valueOf(it.next());
			boolean b = value.equals("clear");
			return cmd.equals("overtaking-safe") ? b : !b;
		} else if (cmd.equals("merging") || cmd.equals("not-merging")) {
			Env env = simulation.env;
			boolean b = (env.construction.construction_vis && env.simcar.lane == 1) ? true : false;
			return cmd.equals("merging") ? b : !b;
		} else if (cmd.equals("do-reset") || cmd.equals("do-not-reset")) {
			boolean safe = isCarSafe();
			boolean b = (safe != true) && (simulation.env.time - lastSafe - Env.sampleTime*2 < 0.05) ? true : false;
			return cmd.equals("do-reset") ? b : !b;
		} else if (cmd.equals("tailgate") || cmd.equals("dont-tailgate")) {
			Env env = simulation.env;
			boolean b = env.construction.construction_vis == true && Math.abs(env.simcar.lane - env.autocar.lane) != 1
					&& env.simcar.lane == 2;
			return cmd.equals("tailgate") ? b : !b;
		} else
			return false;
	}

	public double bind(Iterator<String> it) {
		try {
			it.next(); // (
			String cmd = it.next();
			if (cmd.equals("image->angle")) {
				double x = Double.valueOf(it.next());
				double d = Double.valueOf(it.next());
				return image2angle(x, d);
			} else if (cmd.equals("mp-time"))
				return simulation.env.time;
			else if (cmd.equals("get-thw")) {
				double fd = Double.valueOf(it.next());
				double v = Double.valueOf(it.next());
				double thw = (v == 0) ? 4.0 : fd / v;
				return Math.min(thw, 7.0);
			} else if (cmd.equals("get-velocity"))
				return simulation.env.simcar.speed;
			else if (cmd.equals("get-chunk-id")) {
				// time as unique id for chunks
				double cid = (int) Math.round(simulation.env.time / 10.0) * 10;
				return cid;
			} else if (cmd.equals("get-num-sign")) {
				// number of signs passed
				sign_count += 1;
				return sign_count;
			} else if (cmd.equals("get-num-rehearsal")) {
				// number of rehearals per iteration
				rehearsal_count += 1;
				return rehearsal_count;
			} else if (cmd.equals("reset-rehearsal")) {
				// number of rehearsals in a loop
				rehearsal_count = 0;
				return rehearsal_count;
			} else if (cmd.equals("autocar-lane")) {
				// what lane is the other car driving on?
				return simulation.env.autocar.lane;
			} else if (cmd.equals("simcar-lane")) {
				// what lane am I driving on?
				return simulation.env.simcar.lane;
			} else if (cmd.equals("placeholder")) {
				return 0;
			} else
				return 0;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			return 0;
		}
	}

	void incNum(int rehearsal_count, boolean startover) {
		rehearsal_count = startover ? 0 : rehearsal_count + 1;
	}

	public int numberOfSimulations() {
		return 1;
	}

	public Result analyze(Task[] tasks, boolean output) {
		return null;
	}

}
