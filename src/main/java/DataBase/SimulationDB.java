package DataBase;

import Simulator.SkipSimParameters;
import SkipGraph.SkipGraphOperations;
import SkipGraph.Node;

import java.awt.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

public class SimulationDB extends SQLiteJDBC
{

    /**
     * name of the whole simulation in DB
     */
    private String sim_name;

    /**
     * @param sim_name the simulation name
     * @return insert the simulation in the database and returns the unique simulation id
     */
    public int saveSimulationName(String sim_name, String simulationType)
    {
        this.sim_name = sim_name;
        /*
        Stores the simulation name
         */
        if (sim_name != null && !sim_name.isEmpty())
        {
            // Check if simulation already exists
            int existing_sim_id = tryFetchSimIDFromDB(sim_name);
            if (existing_sim_id >= 0) {
                // Simulation already exists, return existing ID
                return existing_sim_id;
            }

            // Simulation doesn't exist, insert new one
            ArrayList<String> parameters = new ArrayList<>();
            parameters.add(sim_name);
            parameters.add(simulationType);
            queryWithoutResult("INSERT INTO " + SimulationsSchema.TABLE_NAME
                    + "("
                    + SimulationsSchema.Columns.NAME + ","
                    + SimulationsSchema.Columns.TYPE
                    + ") VALUES (?,?) ", parameters);
        }
        else
        {
            System.err.println("SimulationDB.java: wrong name selected for the simulation");
            System.exit(0);
        }

        return fetchSimIDFromDB(sim_name);

    }

    /**
     * Tries to fetch simulation ID from database without throwing error if not found.
     * @param sim_name the simulation name
     * @return simulation ID if found, -1 if not found
     */
    public int tryFetchSimIDFromDB(String sim_name)
    {
        int sim_id = -1;
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(sim_name);
        ResultSet res = queryForResult("SELECT " + SimulationsSchema.Columns.SIM_ID + " FROM " + SimulationsSchema.TABLE_NAME + " WHERE " + SimulationsSchema.Columns.NAME + " = (?)", parameters);

        try
        {
            while (res.next())
            {
                sim_id = res.getInt(SimulationsSchema.Columns.SIM_ID);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return sim_id;
    }

    public int fetchSimIDFromDB(String sim_name)
    {
        int sim_id = tryFetchSimIDFromDB(sim_name);

        if (sim_id < 0)
        {
            System.err.println("SimulationDB.java: could not retrive the simulation id form DB");
            System.exit(0);
        }
        return sim_id;
    }

    //TODO
    public void deleteSimulationFromDB(String sim_name)
    {
        //queryWithoutResult("PRAGMA foreign_keys = ON", new ArrayList<>());
        String query = "DELETE FROM " + SimulationsSchema.TABLE_NAME + " WHERE " + SimulationsSchema.Columns.SIM_ID + " = (?)";
        int delete_sim_id = fetchSimIDFromDB(sim_name);
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(Integer.toString(delete_sim_id));
        queryWithoutResult(query, parameters);

    }

    public int saveTopologyName(int sim_index, String sim_name)
    {
        String top_name = sim_name + "_" + sim_index;
        int sim_id = fetchSimIDFromDB(sim_name);

        // Check if topology already exists
        int existing_top_id = tryFetchTopologyIDFromDB(sim_index, sim_name);
        if (existing_top_id >= 0) {
            // Topology already exists, return existing ID
            return existing_top_id;
        }

        // Topology doesn't exist, insert new one
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(top_name);
        parameters.add(Integer.toString(sim_id));
        queryWithoutResult("INSERT INTO " + TopologiesDBSchema.TABLE_NAME + "(" + TopologiesDBSchema.Columns.NAME + "," + TopologiesDBSchema.Columns.SIM_ID + ") VALUES (?,?) ", parameters);
        return fetchTopologyIDFromDB(sim_index, sim_name);
    }

    /**
     * Tries to fetch topology ID from database without throwing error if not found.
     * @param sim_index the simulation index
     * @param sim_name the simulation name
     * @return topology ID if found, -1 if not found
     */
    public int tryFetchTopologyIDFromDB(int sim_index, String sim_name)
    {
        ArrayList<String> parameters = new ArrayList<>();
        int top_id = -1;
        String top_name = sim_name + "_" + sim_index;
        parameters.add(top_name);
        parameters.add(Integer.toString(fetchSimIDFromDB(sim_name)));
        ResultSet res = queryForResult(" SELECT " + TopologiesDBSchema.Columns.TOPOLOGY_ID
                + " FROM " + TopologiesDBSchema.TABLE_NAME
                + " WHERE " + TopologiesDBSchema.Columns.NAME + " = (?) AND "
                + TopologiesDBSchema.Columns.SIM_ID + " = (?)", parameters);

        try
        {
            while (res.next())
            {
                top_id = res.getInt("topology_id");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return top_id;
    }

    public int fetchTopologyIDFromDB(int sim_index, String sim_name)
    {
        int top_id = tryFetchTopologyIDFromDB(sim_index, sim_name);

        if (top_id < 0)
        {
            System.err.println("SimulationDB.java: could not retrive the topology id form DB");
            System.exit(0);
        }
        return top_id;
    }

    public void saveSkipGraph(SkipGraphOperations sgo, int top_id)
    {

        /**
         * save Nodes to the database
         */

        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            ArrayList<String> parameters = new ArrayList<>();
            parameters.add(Integer.toString(top_id));
            parameters.add(Integer.toString(i));
            parameters.add(Double.toString(((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate().x));
            parameters.add(Double.toString(((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate().y));
            queryWithoutResult("INSERT INTO " + NodesDBSchema.TABLE_NAME +
                    "("
                    + NodesDBSchema.Columns.TOPOLOGY_ID
                    + "," + NodesDBSchema.Columns.NODE_INDEX
                    + "," + NodesDBSchema.Columns.X
                    + "," + NodesDBSchema.Columns.Y
                    + ") VALUES (?,?,?,?) ",parameters);
        }

        /**
         * saving landmarks to the database
         */
        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            ArrayList<String> parameters = new ArrayList<>();
            parameters.add(Integer.toString(top_id));
            parameters.add(Integer.toString(i));
            parameters.add(Integer.toString(sgo.getTG().mLandmarks.getLandmarkCoordination(i).x));
            parameters.add(Integer.toString(sgo.getTG().mLandmarks.getLandmarkCoordination(i).y));
            queryWithoutResult("INSERT INTO " + LandmarksDBSchema.TABLE_NAME
                    + "("
                    + LandmarksDBSchema.Columns.TOPOLOGY_ID + ","
                    + LandmarksDBSchema.Columns.LANDMARK_INDEX + ","
                    + LandmarksDBSchema.Columns.X + ","
                    + LandmarksDBSchema.Columns.Y
                    +")"
                    + " VALUES (?,?,?,?) ", parameters);
        }

        System.out.println("SimulationDB.java: Topology " + SkipSimParameters.getCurrentTopologyIndex() + " was saved to the database");

    }

    public void saveChurnLogToDB(ArrayList<ChurnDBEntry> churnLog, int top_id, int currentTime)
    {
        for (ChurnDBEntry entery : churnLog)
        {
            ArrayList<String> parameters = new ArrayList<>();
            parameters.add(Integer.toString(top_id));
            parameters.add(Integer.toString(entery.getNodeIndex()));
            parameters.add(Double.toString(entery.getArrivalTime()));
            parameters.add(Double.toString(entery.getSessionLength()));
            queryWithoutResult("INSERT INTO " + ChurnDBSchema.TABLE_NAME
                    + "("
                    + ChurnDBSchema.Columns.TOPOLOGY_ID + ","
                    + ChurnDBSchema.Columns.NODE_INDEX + ","
                    + ChurnDBSchema.Columns.ARRIVAL_TIME + ","
                    + ChurnDBSchema.Columns.SESSION_LENGTH
                    + ")"
                    + " VALUES (?,?,?,?) ", parameters);
        }

        //System.out.println("SimulationDB.java: ChurnLog for topology " + system.getCurrentTopologyIndex() + " at time " + currentTime + " was saved to the database");
    }


    /**
     * @param top_id      topology id of the current topology
     * @param currentTime current time of simulation
     * @return the churnLog of Nodes who arrive to the system between currentTime and currentTime + 1
     */
    public ArrayList<ChurnDBEntry> fetchChurnLogFromDB(int top_id, int currentTime)
    {
        ArrayList<ChurnDBEntry> churnLog = new ArrayList<>();

        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(Integer.toString(currentTime));
        parameters.add(Integer.toString(currentTime + 1));
        parameters.add(Integer.toString(top_id));
        ResultSet resultSet = queryForResult("SELECT * FROM "
                + ChurnDBSchema.TABLE_NAME
                + " WHERE "
                + ChurnDBSchema.Columns.ARRIVAL_TIME + " >= (?)  AND "
                + ChurnDBSchema.Columns.ARRIVAL_TIME + " < (?) AND "
                + ChurnDBSchema.Columns.TOPOLOGY_ID + " = (?) ", parameters);

        try
        {
            while (resultSet.next())
            {
                int nodeIndex = resultSet.getInt(ChurnDBSchema.Columns.NODE_INDEX);
                double arrivalTime = resultSet.getDouble(ChurnDBSchema.Columns.ARRIVAL_TIME);
                double sessionLength = resultSet.getDouble(ChurnDBSchema.Columns.SESSION_LENGTH);
                churnLog.add(new ChurnDBEntry(nodeIndex, arrivalTime, sessionLength));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return churnLog;
    }

    public Vector<String> fetchSimulationNamesFromDB(String simulationType)
    {
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(simulationType);
        ResultSet res = queryForResult("SELECT  "
                + SimulationsSchema.Columns.NAME + " FROM "
                + SimulationsSchema.TABLE_NAME + " WHERE "
                + SimulationsSchema.Columns.TYPE + " = (?)", parameters);

        Vector<String> simulationNames = new Vector<>();
        try
        {

            while (res.next())
            {
                simulationNames.add(res.getString(SimulationsSchema.Columns.NAME));
            }

        }
        catch (SQLException e2)
        {
            e2.printStackTrace();
        }

        return simulationNames;
    }

    /**
     *
     * @param simIndex the simulation index from system class
     * @param simulationName the selected simulation name
     * @param isBlockchain boolean denoting whether the simulation type is blockchain or not
     * @return a SkipGraphOperations object with coordination of Nodes and landmarks loaded
     */
    public SkipGraphOperations fetchSkipGraphFromDB(int simIndex, String simulationName, boolean isBlockchain)
    {
        int top_id = fetchTopologyIDFromDB(simIndex, simulationName);
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(Integer.toString(top_id));
        ResultSet resultSet = queryForResult("SELECT " +
                NodesDBSchema.Columns.X + "," + NodesDBSchema.Columns.Y + "," + NodesDBSchema.Columns.NODE_INDEX +
                " FROM " + NodesDBSchema.TABLE_NAME +
                " WHERE " + NodesDBSchema.Columns.TOPOLOGY_ID + " = (?)", parameters);
        Point[] nodesArray = new Point[SkipSimParameters.getSystemCapacity()];

            /*
            Fetching Nodes
             */
        try
        {
            while (resultSet.next())
            {
                nodesArray[resultSet.getInt(NodesDBSchema.Columns.NODE_INDEX)] = new Point(resultSet.getInt(NodesDBSchema.Columns.X), resultSet.getInt(NodesDBSchema.Columns.Y));
            }
        }
        catch (SQLException e1)
        {
            e1.printStackTrace();
        }

            /*
            Fetching Landmarks
             */
        //ArrayList<Point> landmark_list = new ArrayList<>();
        Point[] landmarksArray = new Point[SkipSimParameters.getLandmarksNum()];
        parameters = new ArrayList<>();
        parameters.add(Integer.toString(top_id));
        resultSet = queryForResult("SELECT * FROM " + LandmarksDBSchema.TABLE_NAME
                + " WHERE " + LandmarksDBSchema.Columns.TOPOLOGY_ID + " = (?)", parameters);
        try
        {
            while (resultSet.next())
            {
                //landmark_list.add(new Point(res2.getInt("x"), res2.getInt("y")));
                landmarksArray[resultSet.getInt(LandmarksDBSchema.Columns.LANDMARK_INDEX)] = new Point(resultSet.getInt(LandmarksDBSchema.Columns.X), resultSet.getInt(LandmarksDBSchema.Columns.Y));
            }
        }
        catch (SQLException e1)
        {
            e1.printStackTrace();
        }

        SkipGraphOperations sgo = new SkipGraphOperations(isBlockchain);
        sgo.getTG().loadCoordination(nodesArray, landmarksArray);
        return sgo;


//                            /**
//                             * loading Nodes and landmarks
//                             */
//                            for (int i = 0; i < system.getSystemCapacity(); i++)
//                            {
//                                //    sgo.getTG().mNodeSet.getNode(i).mCoordinate = (Point) ois.readObject();
//                                if (i < nodes_list.size())
//                                {
//                                    sgo.getTG().mNodeSet.getNode(i).mCoordinate = nodes_list.get(i);
//                                }
//                            }
//
//                            for (int i = 0; i < system.getLandmarksNum(); i++)
//                            {
//                                sgo.getTG().mLandmarks.setLandmarkCoordination(i, landmarksArray[i]);
//                            }

                            /*
                            Loads Nodes and landmarks
                             */

    }

}
