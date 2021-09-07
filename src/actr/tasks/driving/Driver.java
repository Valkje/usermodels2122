package actr.tasks.driving;

import actr.model.Model;

/**
 * The class that defines the driver's particular behavioral parameters.
 * 
 * @author Dario Salvucci
 */
public class Driver {
	Model model;
	String name;
	int age;
	float steeringFactor;
	float stabilityFactor;
	Coordinate eyeLocation = new Coordinate(100, 100);

	public Driver(Model model, String nameArg, int ageArg, float steeringArg, float stabilityArg) {
		name = nameArg;
		age = ageArg;
		steeringFactor = steeringArg;
		stabilityFactor = stabilityArg;
	}

	public Coordinate getEyeLocation(Model model) {
		if (model == null)
			return null;
		eyeLocation.x = model.getVision().getEyeX();
		eyeLocation.y = model.getVision().getEyeY();
		return eyeLocation;
	}

	void update(Model model) {
		eyeLocation = getEyeLocation(model);
	}

	public String writeString() {
		return new String("\"" + name + "\"\t" + age + "\t" + steeringFactor + "\t" + stabilityFactor + "\n");
	}

	public String toString() {
		return name;
	}
}
