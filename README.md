# Adaptive Automation System for Driving Simulations
This project concerned itself with the adaptive automation of a simulated driving task. Relying on an existing driving model implemented in ACT-R, we researched and developed a real-time adaptive automation system that determines when the model should take over partial or full control of the driving task and when control should be handed back to the human driver.

For our wiki, see [this](docs/index.md) page.
For a more "paper-like" write-up of our methods, see [this](docs/methods.md) page.

## Driving model
The ACT-R driving model was based on the driving model by Dario Salvucci, see [here](https://github.com/heldmoritz/usermodels2122).

## Eye-tracking
An EyeLink Portable Duo (SRresearch) eye-tracking system was utilized to measure changes in the pupil size at 500 HZ. The interaction with the eye-tracker was based largely on previous code by Gilles Lijnzaad, which can be found [here](https://github.com/gilleslijnzaad/eye-tracking).

## Steering wheel and gamepad support
To enable steering wheel support, the JInput JAR file has to be added to your project as a library. 

To do this in IntelliJ IDEA, for example, navigate to the `lib` folder, right-click on `jinput-2.0.9.jar`, hit 'Add as Library...'. A dialog appears in which you should set the 'Level' field as 'Project Library'. Then hit 'OK'.

Additionally, JInput requires some platform-specific object code to properly communicate with your OS. The different files that contain that code are packaged in `lib/jinput-2.0.9-natives-all.jar`, but have already been directly extracted to `lib` for your convenience. The only thing that you need to do is to make sure Java can find those files by setting `java.library.path` to `/path/to/usermodels2122/lib`. 

In IntelliJ IDEA, this can be done by clicking 'Run' in the top menu and then selecting 'Edit Configurations...'. In the dialog box that pops up, select the configuration that you use to run your application. In the section entitled 'Build and run', under the field that specifies your JDK, there is a field that accepts CLI arguments for the program to run. In there, enter `-Djava.library.path=/path/to/usermodels2122/lib`. Make sure to check if these delimiters are still valid even if you work on Windows.
