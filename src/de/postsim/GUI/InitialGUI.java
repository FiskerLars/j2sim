package de.postsim.GUI;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import de.postsim.IO.OSMXMLParser;
import de.postsim.Objects.SimMap;
import de.postsim.Simulation.MapSimulation;

/**
 * Our Main-Class used to iniate the simulation by choosing a OSM-File to use as a map.
 * @author Nils Stahlhut, Lars Fischer
 *
 */
public class InitialGUI
{
    private static boolean withGUI = false;


    public static void main(String[] args)
    {
        MapGUI applet = null;
        Random rgen = new Random(4); // Four is a perfectly random number.

        File mapfile = chooseMapfile();
        if(null==mapfile) {
            System.err.println("No Map given!");
            System.exit(0);
        }
        String mapname = mapfile.getName().split("\\.")[0];

        // parsing the .osm file and building our own map with it
        OSMXMLParser parser = new OSMXMLParser(mapfile);
        SimMap map = parser.parseNewMap(rgen);


        // constructing a simulation with our chosen parameters
        MapSimulation sim = new MapSimulation(map, rgen, 100, 250, 10,
                MapSimulation.CLUSTER_WAYPOINT, 10, mapname);

        if(withGUI) {
            // building our MapGUI Applet
            JFrame f = new JFrame("Map");
            f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            applet = new MapGUI(map, sim, 1000, 1000);
            f.getContentPane().add("Center", applet);
            applet.init();
            f.pack();
            f.setSize(new Dimension(1000, 1000));
            f.setVisible(true);

            // smooth
            applet.setCluster(true);
        }

        //main simulation Loop
        // start the simulation and update the GUI until we have not delivered all of our pakets
        while (sim.getUnDeliveredpakets().size() > 0 && sim.getCycles() < 604){//800) {
            sim.simulation_step();
            // uncomment this for live visuals
            /*  applet.repaint();
            	try {
					Thread.sleep(120);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} */
        }
        // write the final stats into our logging file
        sim.writeFinalStats();
        try {
            sim.writeContactGraph();
        } catch (IOException e) {
            // tell that writing failed
            e.printStackTrace();
        }
        // display a heatmap in the applet
        if(withGUI && null != applet) {
            applet.setHeatmap(true);
            applet.repaint();
        }
    }

    static File chooseMapfile() {// build a file chooser, that lets you choose a .osm file to use as a map
        JFileChooser chooser = new JFileChooser();
        int returnvalue = chooser.showOpenDialog(null);

        // if the user selected a file
        if (returnvalue == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }
}