and wh## Simulator
This package contains configuration and execution procedures for the SkipSim itself.
Mainly, SkipSimParameters class is used for configuring the simulator.
### SkipSimParameters
The simulation parameters can be directly configured from here. However, the correct way of handling configurations is to create a new simulation schema.
Please see the [SimulationSchema](https://github.com/yhassanzadeh13/SkipSimWS/tree/Utkan-LightChain/src/main/java/SimulationSchema)
package for documentation & examples.

There are many parameters and they are well commented. 
Please refer to the Java file itself to learn about the parameters. 
Here are a few parameters:

##### SystemCapacity
Denotes the number of Nodes in the system it should be non-negative and greater than zero
##### NumIDHashing
Determines whether the numerical ids of the nodes are computed by a hash
function using their indexes as input. Proof-of-Validation and randomized bootstrapping
requires this to be true.
##### ValidatorThreshold
ValidatorThreshold determines how many validators will be searched on the Nodes skip-graph 
for the validator acquisition for transactions (Proof-of-Validation). Also, this determines
how many view introducers will be searched on the Nodes skip-graph for randomized bootstrapping.
##### SignatureThreshold
SignatureThreshold determines how many validators are required to sign a transaction. If an honest node acquires this many honest validators, the valid transaction is successfully validated.
If a malicious node acquires this many malicious validators, an attack has been successfully
performed by introducing an invalid transaction to the skip-graph.
This parameter also determines the amount of honest view introducers required to form a view of
the network. This many malicious view introducers will provide an invalid view for the node.
##### MaliciousFraction
The fraction of malicious nodes to all the nodes. The nodes that have an index in [0, MaliciousFraction*SystemCapacity)
will be chosen as malicious.
##### SimulationType
SimulationType can be either static, dynamic or blockchain.
* Static simulation has no churn.
* Dynamic simulation is with churn.
* Blockchain has churn and an additional skip-graph layer for transaction.
##### LifeTime
Denotes the number of time-slots the simulation should be consisted.
##### ChurnType
Denotes the churn-type, which can be cooperative or adversarial. In cooperative churn, nodes inform the system before going offline. In adversarial churn, nodes do not inform the system.

### GUI
This class is the main entry point of the SkipSim.

### DynamicSimulation
This class contains the simulation procedure for dynamic simulations (i.e. simulations
with churn.) It is invoked by the GUI class.