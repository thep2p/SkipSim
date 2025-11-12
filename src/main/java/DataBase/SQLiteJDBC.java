package DataBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by NOUR-N on 7/17/2017.
 */
public class SQLiteJDBC
{
    private static final Logger log = LoggerFactory.getLogger(SQLiteJDBC.class);

    //private Connection con;
    private static final String SkipSimDB = "jdbc:sqlite:skipsim3db.db";
    public SQLiteJDBC()
    {
        Statement stmt = null;
        try
        {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection(SkipSimDB);  //Create Database or connect if it exists
            log.info("Database connection opened [path={}, status=READY]", SkipSimDB);

            stmt = connection.createStatement();

            String sql = "CREATE TABLE  IF NOT EXISTS " + SimulationsSchema.TABLE_NAME +
                    "( " + SimulationsSchema.Columns.SIM_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT ," +
                    SimulationsSchema.Columns.NAME + " TEXT   NOT NULL UNIQUE," +
                    SimulationsSchema.Columns.TYPE + " TEXT NOT NULL) ";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS  " + TopologiesDBSchema.TABLE_NAME +
                    "(" + TopologiesDBSchema.Columns.SIM_ID + " INT     NOT NULL," +
                    TopologiesDBSchema.Columns.TOPOLOGY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," +
                    TopologiesDBSchema.Columns.NAME + " TEXT NOT NULL UNIQUE," +
                    "CONSTRAINT foreign_keys FOREIGN KEY (" + TopologiesDBSchema.Columns.SIM_ID + " ) REFERENCES  " + SimulationsSchema.TABLE_NAME + "( " + SimulationsSchema.Columns.SIM_ID +") on delete cascade )";

            stmt.executeUpdate(sql);

            /*
            Static Nodes table
             */
            sql = "CREATE TABLE  IF NOT EXISTS  " + NodesDBSchema.TABLE_NAME +
                    "(" + NodesDBSchema.Columns.TOPOLOGY_ID + " INT NOT NULL," +
                    NodesDBSchema.Columns.NODE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                    NodesDBSchema.Columns.NODE_INDEX + " INT NOT NULL," +
                    NodesDBSchema.Columns.X + " INT NOT NULL," +
                    NodesDBSchema.Columns.Y + " INT NOT NULL," +
                    "CONSTRAINT foreign_keys FOREIGN KEY ( " + NodesDBSchema.Columns.TOPOLOGY_ID + " ) REFERENCES " + TopologiesDBSchema.TABLE_NAME + "(" + TopologiesDBSchema.Columns.TOPOLOGY_ID + " ) on delete cascade)";
            stmt.executeUpdate(sql);


            /*
            Landmarks table
             */
            sql = "CREATE TABLE  IF NOT EXISTS  " + LandmarksDBSchema.TABLE_NAME +
                    "(" + LandmarksDBSchema.Columns.TOPOLOGY_ID + " INT NOT NULL," +
                    LandmarksDBSchema.Columns.LANDMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                    LandmarksDBSchema.Columns.LANDMARK_INDEX + " INT NOT NULL," +
                    LandmarksDBSchema.Columns.X + " INT NOT NULL," +
                    LandmarksDBSchema.Columns.Y + " INT NOT NULL," +
                    "CONSTRAINT foreign_keys FOREIGN KEY ( " + LandmarksDBSchema.Columns.TOPOLOGY_ID + " ) REFERENCES " + TopologiesDBSchema.TABLE_NAME + "("  + TopologiesDBSchema.Columns.TOPOLOGY_ID + " ) on delete cascade )";
            stmt.executeUpdate(sql);

            /*
            Churn table
             */
            sql = "CREATE TABLE  IF NOT EXISTS  " + ChurnDBSchema.TABLE_NAME +
                    "(" + ChurnDBSchema.Columns.RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                    + ChurnDBSchema.Columns.TOPOLOGY_ID + " INT NOT NULL," +
                    ChurnDBSchema.Columns.NODE_INDEX + " INT NOT NULL," +
                    ChurnDBSchema.Columns.ARRIVAL_TIME + " REAL NOT NULL," +
                    ChurnDBSchema.Columns.SESSION_LENGTH + " REAL NOT NULL," +
                    "CONSTRAINT foreign_keys FOREIGN KEY ( " + ChurnDBSchema.Columns.TOPOLOGY_ID + " ) REFERENCES " + TopologiesDBSchema.TABLE_NAME + "("  + TopologiesDBSchema.Columns.TOPOLOGY_ID + " ) on delete cascade )";
            stmt.executeUpdate(sql);


            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.close();
            connection.close();

        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        //System.out.println("Table created successfully");

    }

//    public void query(String queryType, String str)
//    {
//        ResultSet res = null;
//        PreparedStatement pstmt;
//        try
//        {
//            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
//            pstmt = conn.prepareStatement(queryType);
//            //  Array array = conn.createArrayOf("VARCHAR", values.toArray());
//            pstmt.setString(1, str);
//            pstmt.executeUpdate();
//            //     pstmt.close();
//
//        }
//        catch (Exception e)
//        {
//            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//
//        }
//    }

//    public void query(String queryType, int v1)
//    {
//        ResultSet res = null;
//        try
//        {
//            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
//            conn.createStatement().execute("PRAGMA foreign_keys = ON");
//            PreparedStatement pstmt = conn.prepareStatement(queryType);
//            //  Array array = conn.createArrayOf("VARCHAR", values.toArray());
//            pstmt.setInt(1, v1);
//            pstmt.executeUpdate();
//            //   pstmt.close();
//
//        }
//        catch (Exception e)
//        {
//            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//
//        }
//
//    }


//    public ResultSet queryResult(String queryType, String v1)
//    {
//        ResultSet res = null;
//        try
//        {
//            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
//            PreparedStatement pstmt = conn.prepareStatement(queryType);
//            //  Array array = conn.createArrayOf("VARCHAR", values.toArray());
//            pstmt.setString(1, v1.toUpperCase());
//            res = pstmt.executeQuery();
//            //  pstmt.close();
//
//        }
//        catch (Exception e)
//        {
//            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//
//        }
//        return res;
//    }

//    public ResultSet queryResult(String queryType, int v1)
//    {
//        ResultSet res = null;
//        try
//        {
//            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
//            PreparedStatement pstmt = conn.prepareStatement(queryType);
//            //  Array array = conn.createArrayOf("VARCHAR", values.toArray());
//            pstmt.setInt(1, v1);
//            res = pstmt.executeQuery();
//            //     pstmt.close();
//
//        }
//        catch (Exception e)
//        {
//            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//
//        }
//        return res;
//    }


//    public ResultSet queryResult(String queryType)
//    {
//        ResultSet res = null;
//        try
//        {
//            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
//            PreparedStatement pstmt = conn.prepareStatement(queryType);
//
//
//            res = pstmt.executeQuery();
//            //    pstmt.close();
//
//        }
//        catch (Exception e)
//        {
//            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//
//        }
//        return res;
//    }
//
//
//    public void query(String queryType, String v1, int v2)
//    {
//        ResultSet res = null;
//        try
//        {
//            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
//            PreparedStatement pstmt = conn.prepareStatement(queryType);
//            //  Array array = conn.createArrayOf("VARCHAR", values.toArray());
//            pstmt.setString(1, v1.toUpperCase());
//            pstmt.setInt(2, v2);
//            pstmt.executeUpdate();
//            //  pstmt.close();
//
//        }
//        catch (Exception e)
//        {
//            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//
//        }
//
//    }
//
//    public void query(String queryType, int v1, int v2, int v3)
//    {
//        ResultSet res = null;
//        try
//        {
//            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
//            PreparedStatement pstmt = conn.prepareStatement(queryType);
//            //  Array array = conn.createArrayOf("VARCHAR", values.toArray());
//
//            pstmt.setInt(1, v1);
//            pstmt.setInt(2, v2);
//            pstmt.setInt(3, v3);
//            pstmt.executeUpdate();
//            // pstmt.close();
//
//        }
//        catch (Exception e)
//        {
//            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//
//        }
//
//    }
//
//    public void query(String queryType, String v1, String v2)
//    {
//        ResultSet res = null;
//        try
//        {
//            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
//            PreparedStatement pstmt = conn.prepareStatement(queryType);
//            //  Array array = conn.createArrayOf("VARCHAR", values.toArray());
//
//            pstmt.setString(1, v1.toUpperCase());
//            pstmt.setString(2, v2.toUpperCase());
//            pstmt.executeUpdate();
//            // pstmt.close();
//
//        }
//        catch (Exception e)
//        {
//            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//
//        }
//
//    }

    public void queryWithoutResult(String query, ArrayList<String> parameters)
    {

        try
        {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
            PreparedStatement pr = conn.prepareStatement("PRAGMA foreign_keys = ON");
            pr.execute();
            pr = conn.prepareStatement(query);
            if(parameters != null)
                for(int i = 0; i < parameters.size() ; i++)
                {
                    pr.setString(i+1, parameters.get(i).toUpperCase());
                }
            pr.execute();
            pr.close();
            conn.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    public ResultSet queryForResult(String query, ArrayList<String> parameters)
    {

        try
        {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
            PreparedStatement pr = conn.prepareStatement("PRAGMA foreign_keys = ON");
            pr.execute();
            pr = conn.prepareStatement(query);
            if(parameters != null)
                for(int i = 0; i < parameters.size() ; i++)
                {
                    pr.setString(i+1, parameters.get(i).toUpperCase());
                }

            ResultSet rs = pr.executeQuery();
            //pr.close();
            return rs;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;

    }

//    public void setCascadeON()
//    {
//        try
//        {
//            Connection conn = DriverManager.getConnection("jdbc:sqlite:skipsim3db.db");
//            Statement stmt = conn.createStatement();
//            stmt.execute("PRAGMA foreign_keys = ON");
//        }
//        catch (SQLException e)
//        {
//            e.printStackTrace();
//        }
//
//    }

}














