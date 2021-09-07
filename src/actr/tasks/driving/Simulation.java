package actr.tasks.driving;

import java.awt.Graphics;
import java.util.Arrays;
import java.util.Vector;

import actr.model.Model;

/**
 * A class that defines the entire simulation include driver, scenario, and
 * samples.
 * 
 * @author Dario Salvucci
 */
public class Simulation {
	Model model;
	Driver driver;
	Scenario scenario;
	Env env = null;
	Vector<Sample> samples = new Vector<Sample>();
	Results results = null;
	double[] myData;

	Simulation(Model model) {
		this.model = model;
		driver = new Driver(model, "Driver", 25, 1.0f, 1.0f);
		scenario = new Scenario();
		env = new Env(driver, scenario);

		samples.add(recordSample(env));
	}

	synchronized void update() {
		env.update();

		samples.add(recordSample(env));
	}

	Results getResults() {
		results = analyze();
		return results;
	}

	int numSamples() {
		return samples.size();
	}

	Sample recordSample(Env env) {
		Sample s = new Sample();
		s.time = env.time;
		s.simcarPos = env.simcar.p.myclone();
		s.simcarHeading = env.simcar.h.myclone();
		s.simcarFracIndex = env.simcar.fracIndex;
		s.simcarSpeed = (int) Utilities.mph2kph(Utilities.mps2mph(env.simcar.speed));
		s.simcarRoadIndex = env.simcar.roadIndex;
		if (env.simcar.nearPoint != null) {
			s.nearPoint = env.simcar.nearPoint.myclone();
			s.farPoint = env.simcar.farPoint.myclone();
		}
		s.steerAngle = env.simcar.steerAngle;
		s.accelerator = env.simcar.accelerator;
		s.brake = env.simcar.brake;
		s.autocarPos = env.autocar.p.myclone();
		s.autocarFracIndex = env.autocar.fracIndex;
		s.autocarSpeed = env.autocar.speed;

		// mlh: don't need this for my analysis
		// s.autocarHeading = env.autocar.h.myclone();
		// s.eyeLocation = env.simcar.simDriver.model.vision.eyeLocation;

		//
		// s.autocarBraking = env.autocar.braking;
		//
		// if (s.eyeLocation != null) s.eyeLocation = s.eyeLocation.myclone();
		// s.handLocation = env.simcar.simDriver.model.motor.handLocation;
		// if (s.handLocation != null) s.handLocation = s.handLocation.myclone();
		// s.handMoving = (env.simcar.simDriver.model.motor.handLocationNext != null);
		// s.listening = ! env.simcar.simDriver.model.vision.auralFree();

		// if (env.simcar.simDriver.model.goals.size() > 0)
		// s.inDriveGoal = (env.simcar.simDriver.model.goals.elementAt(0).getClass() ==
		// DriveGoal.class);
		//
		// s.event = env.simcar.simDriver.model.event;
		s.lanepos = env.road.vehicleLanePosition(env.simcar);

		// mlh
		s.currentspeed = Integer.parseInt(env.speedsign.speedlimit);
		s.imaginedSpeedlimit = Driving.imaginedSpeedlimit;
		s.visAttention = env.construction.construction_bool ? "construction" : "highway";
		s.turning = env.simcar.turning;
		if (env.simcar.turning)
			env.simcar.turning = false;
		s.block = env.road.block;
		s.followedLane = env.simcar.lane;
		s.eyeLocation = env.simcar.driver.eyeLocation;
		s.signVis = env.speedsign.visible;
		return s;
	}

	synchronized void draw(Graphics g) {
		if (env != null)
			env.draw(g);
	}

	double rotAngle(double hx, double hz) {
		return (-180 * (Math.atan2(hz, hx)) / Math.PI);
	}

	double headingError(Sample s) {
		Road.Segment s2 = Road.getSegment((int) s.simcarRoadIndex);
		Road.Segment s1 = Road.getSegment((int) s.simcarRoadIndex - 1);
		Position rh = s2.middle.subtract(s1.middle);
		rh.normalize();
		return Math.abs(rotAngle(rh.x, rh.z) - rotAngle(s.simcarHeading.x, s.simcarHeading.z));
	}

	boolean lookingAhead(Sample s) {
		return true;
		// return (s != null && s.eyeLocation != null && s.eyeLocation.x < 350);
	}

	Results analyze()

	{
		// double startTime = 0;
		double stopTime = 1000; // def -1000

		// int numTasks = 0;
		int numTaskSamples = 0;
		// double sumTime = 0;
		double sumLatDev = 0;
		double sumLatVel = 0;
		double sumSpeedDev = 0;
		double numTaskDetects = 0, numTaskDetectsCount = 0;
		double sumBrakeRTs = 0, numBrakeRTs = 0, lastBrakeTime = 0;
		boolean brakeEvent = false;
		double[] headingErrors = new double[samples.size()];
		int laneViolations = 0;
		boolean lvDetected = false;

		for (int i = 1; i < samples.size(); i++) {
			Sample s = samples.elementAt(i);
			Sample sprev = samples.elementAt(i - 1);
			if ((s.event > 0) || (s.time < stopTime + 5.0)) {
				numTaskSamples++;
				double latdev = 3.66 * (s.lanepos - 2.5);
				sumLatDev += (latdev * latdev);
				sumLatVel += Math.abs((3.66 * (s.lanepos - sprev.lanepos)) / Env.sampleTime);
				sumSpeedDev += (s.simcarSpeed - s.autocarSpeed) * (s.simcarSpeed - s.autocarSpeed);
				if ((s.event > 0) || (s.time < stopTime)) {
					numTaskDetectsCount++;
					if (lookingAhead(s)) {
						numTaskDetects += 1;
						// if (s.listening) numTaskDetects -= .1;
					}
					// if (s.inDriveGoal) numTaskDetects ++;
				}

				// if ((s.event > 0) || (s.time < stopTime))
				// {
				if (((s.event > 0) || (s.time < stopTime)) && !brakeEvent
						&& (s.autocarBraking && !sprev.autocarBraking)) {
					brakeEvent = true;
					lastBrakeTime = s.time;
				}
				if (brakeEvent && !s.autocarBraking) {
					brakeEvent = false;
				}
				if (brakeEvent && (s.brake > 0)) {
					// System.out.println ("BrakeRT: " + (s.time - lastBrakeTime));
					sumBrakeRTs += (s.time - lastBrakeTime);
					numBrakeRTs++;
					brakeEvent = false;
				}
				// }

				headingErrors[numTaskSamples - 1] = headingError(s);
				if (!lvDetected && (Math.abs(latdev) > (1.83 - 1.0))) {
					laneViolations++;
					lvDetected = true;
				}

			}
			if ((s.event == 1) && (sprev.event == 0)) {
				// startTime = s.time;
				lvDetected = false;
				brakeEvent = false;
			} else if ((s.event == 0) && (sprev.event == 1)) {
				// numTasks ++;
				stopTime = s.time;
				// sumTime += (stopTime - startTime);
			}
		}

		Results r = new Results();
		// r.ifc = ifc;
		// r.task = task;
		r.driver = driver;
		// if (r.task.numActions() > 0) r.taskTime = sumTime / numTasks;
		// else r.taskTime = 0;
		r.taskLatDev = Math.sqrt(sumLatDev / numTaskSamples);
		r.taskLatVel = sumLatVel / numTaskSamples;
		r.taskSpeedDev = Math.sqrt(sumSpeedDev / numTaskSamples);

		r.detectionError = (numTaskSamples == 0) ? 0 : (1.0 - (1.0 * numTaskDetects / numTaskDetectsCount));

		r.brakeRT = (numBrakeRTs == 0) ? 0 : (sumBrakeRTs / numBrakeRTs);

		Arrays.sort(headingErrors, 0, numTaskSamples - 1);
		int heIndex = (int) (0.9 * numTaskSamples);
		r.headingError = headingErrors[heIndex];

		r.laneViolations = laneViolations;

		return r;

	}

	Results myAnalyze() {

		return null;
	}
}
