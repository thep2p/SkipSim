package Simulator;

import DataBase.ChurnDBEntry;
import DataBase.SimulationDB;
import DataTypes.Constants;
import LandmarkPlacement.landmarkSimulation;
import SimulationSchema.SchemaManager;
import SkipGraph.Node;
import SkipGraph.SkipGraphOperations;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.*;

/**
 * Created by Dell on 10.08.2016.
 */


public class GUI extends Application
{

    public static boolean[] isReplica;
    public static String sim_name;
    public static String newsim_name;
    private static String simulationName;
    private static String edit_sim;
    GridPane grid = new GridPane();
    SkipGraphOperations sgo;
    private SimulationDB simDB;

    /**
     * TRUE: if the simulation type is blockchain, false otherwise
     */
    private boolean isBlockChain;
    public static void main(String[] args) {
        launch(args);
    }
    public static String infoBox(String title, String message)
    {
        // a jframe here isn't strictly necessary, but it makes the example a little more real
        JFrame frame = new JFrame(title);

        // prompt the user to enter their name
        String sim_name = JOptionPane.showInputDialog(frame, message);

        // get the user's input. note that if they press Cancel, 'name' will be null
        return sim_name;

    }

    @Override
    public void start(Stage window)
    {
    	
        new SchemaManager();
        simDB = new SimulationDB();
        //Todo ready to relinquish
        /*
         * get information of config.txt
         * EVERYTHING related to the system class should be written right after this
         */
        //FileInteractions.readConfigFile();
        FileInteractions.PrintSimulationParameters();

                /*
        Determines whether the simulation is blockchain or not based on the value of the simulationType in system class
         */
        isBlockChain = SkipSimParameters.getSimulationType().equalsIgnoreCase(Constants.SimulationType.BLOCKCHAIN);
        isReplica = new boolean[SkipSimParameters.getSystemCapacity()];

        Canvas canvas = new Canvas(660, 660);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Scene scene = new Scene(grid, 800, 800);
        Label lbl1 = new Label(0 + " of " + 0);
        lbl1.setFont(javafx.scene.text.Font.font("System Regular", FontWeight.BOLD, 12));
        lbl1.setDisable(false);
        /**
         * show some information of config.txt
         */
//        Label sizeLabel = new Label("Size: " + SkipSimParameters.getSystemCapacity());
//        Label mnrLabel = new Label("MNR: " + SkipSimParameters.getReplicationDegree());
//        Label repTypeLabel = new Label("Replication Type: " + SkipSimParameters.getReplicationType());
//        Label repAlgLabel = new Label("Replication Algorithm: " + SkipSimParameters.getReplicationAlg());
//        Label repTimeLabel = new Label("Replication Time: " + SkipSimParameters.getReplicationTime());
//        Label nameIDAlgLabel = new Label("NameIDAssignment:" + SkipSimParameters.getNameIDAssignment());
//        Label sessionKLabel = new Label("SessionK: " + SkipSimParameters.getSessionLengthShapeParameter());
//        Label sessionLambdaLabel = new Label("SessionLambda: " + SkipSimParameters.getSessionLengthScaleParameter());
//        Label downTimeLambdaLabel = new Label("DownLambda: " + SkipSimParameters.getDownLambda());


        /**
         * ProgressBar
         */
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(240);
        progressBar.setStyle("@android:style/Widget.ProgressBar.Small");
        progressBar.setDisable(false);
        progressBar.setStyle("-fx-accent: blue;");

        ProgressBar timeBar = new ProgressBar(0);
        timeBar.setPrefWidth(240);
        timeBar.setStyle("@android:style/Widget.ProgressBar.Small");
        timeBar.setDisable(false);
        timeBar.setStyle("-fx-accent: red;");


        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setHgap(10);
        grid.setVgap(10);

        Button newButton = new Button("New");
        Button openButton = new Button("Open");
        Button deleteButton = new Button("Delete");
        Button editButton = new Button("Edit");

        newButton.setStyle("-fx-text-fill: white ;" + "-fx-background-color: black ;");
        openButton.setStyle("-fx-text-fill: white ;" + "-fx-background-color: black ;");
        deleteButton.setStyle("-fx-text-fill: white ;" + "-fx-background-color: black ;");
        editButton.setStyle("-fx-text-fill: white ;" + "-fx-background-color: black ;");
        newButton.setOnMouseMoved(e -> newButton.setStyle("-fx-text-fill: black ;" + "-fx-background-color: white ;"));
        newButton.setOnMouseExited(e -> newButton.setStyle("-fx-text-fill: white ;" + "-fx-background-color: black ;"));
        openButton.setOnMouseMoved(e -> openButton.setStyle("-fx-text-fill: black ;" + "-fx-background-color: white ;"));
        openButton.setOnMouseExited(e -> openButton.setStyle("-fx-text-fill: white ;" + "-fx-background-color: black ;"));
        deleteButton.setOnMouseMoved(e -> deleteButton.setStyle("-fx-text-fill: black ;" + "-fx-background-color: white ;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-text-fill: white ;" + "-fx-background-color: black ;"));
        editButton.setOnMouseMoved(e -> editButton.setStyle("-fx-text-fill: black ;" + "-fx-background-color: white ;"));
        editButton.setOnMouseExited(e -> editButton.setStyle("-fx-text-fill: white ;" + "-fx-background-color: black ;"));
        newButton.setDisable(false);


        /**
         * set the position of labels, button, progressbar ,canvas
         */
        GridPane.setConstraints(newButton, 0, 0);
        GridPane.setConstraints(openButton, 1, 0);
        GridPane.setConstraints(deleteButton, 2, 0);
        GridPane.setConstraints(editButton, 3, 0);
        GridPane.setConstraints(lbl1, 6, 1);
//        GridPane.setConstraints(mnrLabel, 0, 2, 3, 2);
//        GridPane.setConstraints(sizeLabel, 0, 3, 3, 3);
//        GridPane.setConstraints(repTypeLabel, 0, 4, 3, 4);
//        GridPane.setConstraints(repAlgLabel, 0, 5, 3, 5);
//        GridPane.setConstraints(repTimeLabel, 0, 6, 3, 6);
//        GridPane.setConstraints(availabilityBoostLabel, 0, 7, 3, 7);
//        GridPane.setConstraints(nameIDAlgLabel, 0, 8, 3, 8);
//        GridPane.setConstraints(sessionKLabel, 0, 9, 3, 9);
//        GridPane.setConstraints(sessionLambdaLabel, 0, 10, 3, 10);
//        GridPane.setConstraints(downTimeLambdaLabel, 0, 11, 3, 11);
        GridPane.setConstraints(canvas, 5, 1, 6, 65);
        GridPane.setConstraints(progressBar, 5, 0, 3, 1);
        GridPane.setConstraints(timeBar, 5, 2, 3, 1);


        //   fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Folder", "*.*"));

        /**
         * Button New
         */
        newButton.setOnAction(e ->
        {

            //sim_name = infoBox("Simulation Name", "Save Simulation as:");
            sim_name = showAndEnterDialog("Create new simulation", "Save simulation as:");
            simDB.saveSimulationName(sim_name, SkipSimParameters.getSimulationType());

            new AnimationTimer()
            {
                @Override
                public void handle(long now)
                {
                    SkipSimParameters.incrementSimIndex();
                    /**
                     * clear skip graph
                     */
                    gc.clearRect(0, 0, 660, 660);

                    int top_id = 0;
                    try
                    {
                        /*
                        Initializing the replica indicator flags
                         */
                        Arrays.fill(isReplica, false);
                        top_id = simDB.saveTopologyName(SkipSimParameters.getCurrentTopologyIndex(), sim_name);

                        //TODO ready to detach
//                        /*
//                        A call to the system garbage collector
//                         */
//                        System.gc();

                        sgo = new SkipGraphOperations(isBlockChain);
                        if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.LANDMARK))
                        {
                            /*
                            landmark placement simulation
                             */
                            landmarkSimulation ls = new landmarkSimulation(sgo, Constants.Topology.GENERATE);
                        }
                        else if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.STATIC))
                        {
                            /*
                            Static simulation
                             */
                            staticSimulation ss = new staticSimulation(sgo, Constants.Topology.GENERATE);
                        }
                        else if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.DYNAMIC)
                                || SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN))
                        {
                            /*
                            dynamic simulation
                             */
                            for (int time = 0; time < SkipSimParameters.getLifeTime(); time++)
                            {
                                DynamicSimulation ds = new DynamicSimulation(sgo);
                                ArrayList<ChurnDBEntry> churnLog = new ArrayList<>();
                                sgo = ds.Simulate(Constants.Topology.GENERATE, time, churnLog);
                                simDB.saveChurnLogToDB(churnLog, top_id, time);
                                timeBar.setProgress((float) time / SkipSimParameters.getLifeTime());
                                //drawNodesAndLandmarks(sgo, gc);
                            }

                        }
//                        else if ()
//                        {
//                            //SignatureLookupTable slt = new SignatureLookupTable();
//                            for (int time = 0; time < system.getLifeTime(); time++)
//                            {
//                                BlockchainSimulation bs = new BlockchainSimulation(sgo);
//                                ArrayList<ChurnDBEntry> churnLog = new ArrayList<>();
//                                sgo = bs.Simulate(Constants.Topology.GENERATE, time, churnLog);
//                                simDB.saveChurnLogToDB(churnLog, top_id, time);
//                                timeBar.setProgress((float) time / system.getLifeTime());
//                                //drawNodesAndLandmarks(sgo, gc);
//                            }
//                        }
                        /*
                        Saving topology to DB
                         */
                        simDB.saveSkipGraph(sgo, top_id);

                        /*
                        Updating the progress bar
                         */
                        progressBar.setProgress((float) (SkipSimParameters.getCurrentTopologyIndex()) / SkipSimParameters.getTopologies());
                        lbl1.setText(SkipSimParameters.getCurrentTopologyIndex() + " of " + SkipSimParameters.getTopologies());

                        /*
                        Drawing Nodes and landmarks
                         */
                        drawNodesAndLandmarks(sgo, gc);


                        /**
                         * stop the animation when all the simulation run
                         */
                        if (SkipSimParameters.getCurrentTopologyIndex() >= SkipSimParameters.getTopologies())
                        {
                            stop();
                        }
//TODO ready to detach
//                        /*
//                        A call to the java garbage collector
//                         */
//                        System.gc();


                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                        stop();
                    }
                }
            }.start();
        });


        openButton.setOnAction(e ->
        {

            /*
            Fetching simulations from database
             */

            // TODO : FETCHING SIMULATION FOR BLOCKCHAIN TYPE
            String simulationName;

            if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN))
            {
                simulationName = showAndCollectDialog("Select the simulation to execute", simDB.fetchSimulationNamesFromDB(Constants.SimulationType.DYNAMIC));
            }
            else
            {
                simulationName = showAndCollectDialog("Select the simulation to execute", simDB.fetchSimulationNamesFromDB(SkipSimParameters.getSimulationType()));
            }

            if (simulationName == null)
            {
                return;
            }
            progressBar.setDisable(false);
            lbl1.setDisable(false);
            new AnimationTimer()
            {
                @Override
                public void handle(long now)
                {
                    SkipSimParameters.incrementSimIndex();
                    progressBar.setProgress((float) (SkipSimParameters.getCurrentTopologyIndex()) / SkipSimParameters.getTopologies());
                    SkipGraphOperations sgo = simDB.fetchSkipGraphFromDB(SkipSimParameters.getCurrentTopologyIndex(), simulationName, isBlockChain);




                            /*
                            Running the simulation
                             */
                    Arrays.fill(isReplica, false);
                    //sgo = new SkipGraphOperations();
                    //bgo = new BlockGraphOperations(sgo);
                    if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.LANDMARK))
                    {
                        landmarkSimulation ls = new landmarkSimulation(sgo, Constants.Topology.LOAD);
                    }
                    else if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.STATIC))
                    {
                        staticSimulation ss = new staticSimulation(sgo, Constants.Topology.LOAD);
                    }
                    else if (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.DYNAMIC)
                            || (SkipSimParameters.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN)))
                    {
                            /*
                            dynamic simulation
                             */
                        int top_id = simDB.fetchTopologyIDFromDB(SkipSimParameters.getCurrentTopologyIndex(), simulationName);
                        for (int time = 0; time < SkipSimParameters.getLifeTime(); time++)
                        {
                            ArrayList<ChurnDBEntry> churnLog = simDB.fetchChurnLogFromDB(top_id, time);
                            DynamicSimulation ds = new DynamicSimulation(sgo);
                            sgo = ds.Simulate(Constants.Topology.LOAD, time, churnLog);
                            timeBar.setProgress((float) time / SkipSimParameters.getLifeTime());
                        }
                    }
//                    else if (system.getSimulationType().equals(Constants.SimulationType.BLOCKCHAIN))
//                    {
//                            /*
//                            dynamic simulation
//                             */
//                        int top_id = simDB.fetchTopologyIDFromDB(system.getCurrentTopologyIndex(), simulationName);
//                        //SignatureLookupTable slt = new SignatureLookupTable();
//                        for (int time = 0; time < system.getLifeTime(); time++)
//                        {
//                            ArrayList<ChurnDBEntry> churnLog = simDB.fetchChurnLogFromDB(top_id, time);
//                            BlockchainSimulation bs = new BlockchainSimulation(sgo);
//                            sgo = bs.Simulate(Constants.Topology.LOAD, time, churnLog);
//                            timeBar.setProgress((float) time / system.getLifeTime());
//                        }
//                    }

                    gc.clearRect(0, 0, 660, 660);
                    drawNodesAndLandmarks(sgo, gc);
                    lbl1.setText(SkipSimParameters.getCurrentTopologyIndex() + " of " + SkipSimParameters.getTopologies());


                    if (SkipSimParameters.getCurrentTopologyIndex() >= SkipSimParameters.getTopologies())
                    {
                        stop();
                    }


                }
            }.start();

        });


        //TODO check for correctness
        deleteButton.setOnAction(e ->
        {

            Vector<String> simulationNames = simDB.fetchSimulationNamesFromDB(SkipSimParameters.getSimulationType());

            simulationName = showAndCollectDialog("Select simulation to delete", simulationNames);


            if (simulationName != null)
            {
                simDB.deleteSimulationFromDB(simulationName);
            }

        });

//        //TODO check for correctness
//        editButton.setOnAction(e ->
//        {
//            SQLiteJDBC sqLiteJDBC = new SQLiteJDBC();
//            ResultSet res = sqLiteJDBC.queryResult("SELECT name from simulations ");
//            Vector<String> temp = new Vector<String>();
//
//            try
//            {
//
//                while (res.next())
//                {
//                    temp.add(res.getString("name"));
//                }
//
//            }
//            catch (SQLException e2)
//            {
//                e2.printStackTrace();
//            }
//
//            JList list = new JList(temp);
//            ListDialog dialog = new ListDialog("Select Simulation: ", list);
//
//            dialog.setOnOk(
//                    e3 ->
//                    {
//                        edit_sim = dialog.getSelectedItem().toString();
//                        newsim_name = infoBox("Simulation Name", "Save Simulation as:");
//                    });
//
//            dialog.show();
//            sqLiteJDBC.query("UPDATE simulations SET name=? WHERE name=?", newsim_name, edit_sim);
//
//
//        });


        grid.getChildren().addAll(canvas, newButton, openButton, deleteButton, editButton, progressBar, lbl1);//, sizeLabel, mnrLabel, repTypeLabel, repAlgLabel, repTimeLabel,
//                availabilityBoostLabel,
//                nameIDAlgLabel, sessionKLabel, sessionLambdaLabel, downTimeLambdaLabel);
        window.setScene(scene);
        window.show();


    }

    private void drawNodesAndLandmarks(SkipGraphOperations sgo, GraphicsContext gc)
    {
        for (int i = 0; i < SkipSimParameters.getSystemCapacity(); i++)
        {
            if(((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate() == null)
            {
                System.out.println("Null coordinate : " + i);
                continue;
            }
//            if(system.getSimulationType().equals(Constants.SimulationType.DYNAMIC) && sgo.getTG().mNodeSet.getNode(i).isOffline())
//                continue;
            double nodeX = ((double) ((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate().x / SkipSimParameters.getDomainSize()) * 500;
            double nodeY = ((double) ((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate().y / SkipSimParameters.getDomainSize()) * 500;
                    /*
                    A replica Node is filled with choccolate
                     */
            if (isReplica[i])
            {
                gc.setFill(javafx.scene.paint.Color.BLACK);
                gc.fillArc((nodeX), nodeY, 21, 21, 0, 360, ArcType.CHORD);
            }
            else
            {
                            /*
                            A data requester Node is a black
                             */
                if ((SkipSimParameters.getReplicationType().contains("private") || SkipSimParameters.getReplicationType().contains("delaybased")) && i < SkipSimParameters.getDataRequesterNumber())
                {
                    gc.setFill(Color.ORANGE);
                    gc.fillArc((nodeX), nodeY, 7, 7, 0, 360, ArcType.CHORD);
                }

                else
                {
                    gc.setStroke(javafx.scene.paint.Color.AQUA);
                    gc.strokeArc(((double) ((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate().x / SkipSimParameters.getDomainSize()) * 500, ((double) ((Node) sgo.getTG().mNodeSet.getNode(i)).getCoordinate().y / SkipSimParameters.getDomainSize()) * 500, 7, 7, 0, 360, ArcType.CHORD);

                }
            }
            // System.out.println(((double)sgo.getTG().mNodeSet.getNode(i).mCoordinate.x/system.getDomainSize()) * 600+ " " + ((double)sgo.getTG().mNodeSet.getNode(i).mCoordinate.y/system.getDomainSize()) * 660);
        }

        for (int i = 0; i < SkipSimParameters.getLandmarksNum(); i++)
        {
            gc.setFill(javafx.scene.paint.Color.RED);
            gc.fillArc((int) (((double) sgo.getTG().mLandmarks.getLandmarkCoordination(i).x / SkipSimParameters.getDomainSize()) * 500), (int) (((double) sgo.getTG().mLandmarks.getLandmarkCoordination(i).y / SkipSimParameters.getDomainSize()) * 500), 21, 21, 0, 360, ArcType.CHORD);
            gc.fillText(String.valueOf(i), (int) (((double) sgo.getTG().mLandmarks.getLandmarkCoordination(i).x / SkipSimParameters.getDomainSize()) * 500) + 30, (int) (((double) sgo.getTG().mLandmarks.getLandmarkCoordination(i).y / SkipSimParameters.getDomainSize()) * 500) + 30);
            gc.setFont(new javafx.scene.text.Font(gc.getFont().toString(), 20));

        }
    }

    //TODO: java doc required, add
    private String showAndCollectDialog(String message, Vector<String> options)
    {
        try
        {
            ChoiceDialog<String> choice = new ChoiceDialog(message, options);
            Optional<String> results = choice.showAndWait();
            if (results == null || results.get().toString().equals(message))
            {
                return null;
            }
            return results.get().toString();
        }
        catch (NoSuchElementException ex)
        {
            return null;
        }

    }

    //TODO: java doc required, add
    private String showAndEnterDialog(String header, String message)
    {
        TextInputDialog choice = new TextInputDialog();
        choice.setHeaderText(header);
        choice.setContentText(message);
        Optional<String> results = choice.showAndWait();
        if (results == null || results.get().toString().equals(message))
        {
            return null;
        }
        return results.get().toString();
    }


}
