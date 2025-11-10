## Tutorial: Efficiency Experiment
For this quick-start tutorial, we will perform an efficiency experiment on Proof-of-Validation consensus protocol defined [here](https://www.researchgate.net/publication/332104732_LightChain_A_DHT-based_Blockchain_for_Resource_Constrained_Environments).
When a new transaction is generated, we aim to find a predefined number of honest nodes that can act as validators for the transaction.
Efficiency experiment measures the average number of honest nodes that are acquired throughout the simulation.

### Building

First, clone this repository on your local computer:

`$ git clone https://github.com/yhassanzadeh13/SkipSimWS`

This project can be built using the provided Makefile or IntelliJ IDEA.

**Using Makefile:**
```bash
make compile
```

**Using IntelliJ IDEA:**
Open the project in IntelliJ and build using Build > Build Project.

### Configuring

Open `SkipSimParameters.java` and set the following parameters:
```java
public static final boolean NumIdHashing = true;
private static int ValidatorThreshold = 12;
private static int SignatureThreshold = 1;
public static float MaliciousFraction = 0.16f;
...
//////////////////////////////////////Experiments/////////////////////////////////////////
...
public static boolean MaliciousSuccessExperiment = false;
public static boolean EfficiencyExperiment = true;
public static boolean AvailabilityExperiment = false;
public static boolean OnlineProbabilityExperiment = false;
public static boolean btsMaliciousSuccessExperiment = false;
public static boolean btsEfficiencyExperiment = false;
```
Then create a new class "Blockchain" under the SimulationSchema package:
```java
package SimulationSchema;

import DataTypes.Constants;
import Simulator.SkipSimParameters;

public class Blockchain extends SkipSimParameters {
    public Blockchain() {
        SimulationType = Constants.SimulationType.BLOCKCHAIN;
        ChurnType = Constants.Churn.Type.COOPERATIVE;
        SystemCapacity = 1024;
        LandmarksNum = 10;
        NameIDLength = 10;
        //LOG = true;
        TopologyNumbers = 1;
        LifeTime = 168;

        SessionLengthScaleParameter = Constants.Churn.Model.Debian.Fast.SessionLength.Scale;
        SessionLengthShapeParameter = Constants.Churn.Model.Debian.Fast.SessionLength.Shape;
        InterarrivalScaleParameter = Constants.Churn.Model.Debian.Fast.SessionInterarrival.Scale;
        InterarrivalShapeParameter = Constants.Churn.Model.Debian.Fast.SessionInterarrival.Shape;
    }
}
```
Now, we need to make sure that the SkipSim will use this schema while running the simulation. To do this, go to the SchemaManager
class and modify it as the following:
```java
package SimulationSchema;

public class SchemaManager {
    public SchemaManager() {
        new Blockchain();
    }
}
```
With these settings, we will measure the average number of honest nodes that are achievable in 12 (ValidatorThreshold) trials.

### Running

For this tutorial we will use an already generated simulation. Download it [here](#) and put it on the SkipSim directory. Rename it as `skipsim3db.db`.

Run SkipSim using the command-line interface:

**Using Makefile:**
```bash
# List available simulations
make run-list

# Load the simulation
make run-load NAME=100_1024_DEBIAN_1W
```

**Using direct Java command:**
```bash
# List available simulations
java -cp "libs/*:out/production" Simulator.Main list

# Load the simulation
java -cp "libs/*:out/production" Simulator.Main load 100_1024_DEBIAN_1W
```

The `100_1024_DEBIAN_1W` simulation uses FAST_DEBIAN as a churn model, and it has 1024 nodes. The simulation is done over one week (168 hours).

Watch as the simulation runs. The experiment results will be outputted on the standard output.
