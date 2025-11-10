package SimulationSchema;

public class SchemaManager
{
    public SchemaManager()
    {
        // Use BlockchainSmall for quick testing (2 topologies, 64 nodes, 24 hours)
        new BlockchainSmall();

        // Use Blockchain for full experiments (100 topologies, 1024 nodes, 168 hours)
        // new Blockchain();

        //new MultiObjectiveReplication();
        //new Replication();
    }
}
