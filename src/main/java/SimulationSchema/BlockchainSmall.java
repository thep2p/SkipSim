package SimulationSchema;

import DataTypes.Constants;
import Simulator.SkipSimParameters;

/**
 * Small blockchain simulation for quick testing.
 * Useful for verifying the system works without long wait times.
 */
public class BlockchainSmall extends SkipSimParameters
{
    public BlockchainSmall()
    {
        SimulationType = Constants.SimulationType.BLOCKCHAIN;
        Topologies = 2;          // Only 2 topologies instead of 100
        SystemCapacity = 64;      // Only 64 nodes instead of 1024
        LifeTime = 24;            // Only 24 hours instead of 168
        TXB_RATE = 1;
        BlockchainProtocol = Constants.Protocol.LIGHTCHAIN;

        // Setting the churn model to FAST_DEBIAN
        setChurnModel(Constants.Churn.Model.Debian.Fast.Name);

        ChurnType = Constants.Churn.Type.ADVERSARIAL;
        MaliciousFraction = 0.16f;
        LOG = true;
    }
}
