# Nbackmodel
ACT-R driving model that performs a five-level n-back task using speed signs. This model is based on the driving model by Dario Salvucci.

## Steering wheel and gamepad support
To enable steering wheel support, the JInput JAR file has to be added to your project as a library. 

To do this in IntelliJ IDEA, for example, navigate to the `lib` folder, right-click on `jinput-2.0.9.jar`, hit 'Add as Library...'. A dialog appears in which you should set the 'Level' field as 'Project Library'. Then hit 'OK'.

Additionally, JInput requires some platform-specific object code to properly communicate with your OS. The different files that contain that code are packaged in `lib/jinput-2.0.9-natives-all.jar`, but have already been directly extracted to `lib` for your convenience. The only thing that you need to do is to make sure Java can find those files by setting `java.library.path` to `/path/to/usermodels2122/lib`. 

In IntelliJ IDEA, this can be done by clicking 'Run' in the top menu and then selecting 'Edit Configurations...'. In the dialog box that pops up, select the configuration that you use to run your application. In the section entitled 'Build and run', under the field that specifies your JDK, there is a field that accepts CLI arguments for the program to run. In there, enter `-Djava.library.path=/path/to/usermodels2122/lib`. Make sure to check if these delimiters are still valid even if you work on Windows.
