package de.postsim.GUI;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JApplet;

import de.postsim.Objects.Coordinate;
import de.postsim.Objects.Paket;
import de.postsim.Objects.SimMap;
import de.postsim.Objects.SimNode;
import de.postsim.Objects.SimWay;
import de.postsim.Objects.User;
import de.postsim.Simulation.MapSimulation;

/**
 * This class inherits JApplet and draws our Simulation in an Applet so we can view it. It is also used to draw a heatmap.
 * @author Nils Stahlhut
 *
 */
public class MapGUI extends JApplet {

	private SimMap map;					// map
	private MapSimulation sim;			// simulation
	private double width;				// width of the window
	private double height;				// height of the window
	private boolean heatmap = false;	// do we want to paint a heatmap?
	private boolean cluster = false;		// are we using the smooth algorithm

	private static final long serialVersionUID = 1L;

	
	/**
	 * constructs an applet to visualize the simulation on a map
	 * @param map the map
	 * @param sim the simulation
	 * @param width	the initial width of the window
	 * @param height the initial height of the window
	 */
	public MapGUI(SimMap map, MapSimulation sim, double width, double height) {
		this.map = map;
		this.sim = sim;
		this.width = width;
		this.height = height;
	}

	
	/**
	 * overrides java.applet.Applet.init
	 */
	public void init() {
		setBackground(Color.white);
		setForeground(Color.white);
	}


	/**
	 * overrides java.awt.Container.paint
	 */
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// clearing the window
		Dimension d = getSize();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, d.width, d.height);

		// checking if we want to draw a heatmap or a normal map with users and pakets
		if(heatmap == true)
			paintHeatmap(g2);
		else
			paintMap(g2);
	}

	private void paintHeatmap(Graphics2D g2) {
		// drawing the ways
		ArrayList<SimWay> ways = new ArrayList<SimWay>();
		ways = map.getWays();
		int minvisited = map.getMinTimesVisited();
		int maxvisited = map.getMaxTimesVisited();
		// iterating through all ways
		for (int i = 0; i < ways.size(); i++) {
			SimWay w = ways.get(i);
			ArrayList<SimNode> nodes = new ArrayList<SimNode>();
			nodes = w.getNodes();
			// iterate through the nodes of a way one by one and paint the line between the current node and its successor with color depending on the number of times visited
			for (int i2 = 0; i2 < (nodes.size() - 1); i2++) {
				SimNode node1 = nodes.get(i2);
				SimNode node2 = nodes.get(i2 + 1);
				Coordinate a = getGUIcoordinate(node1.getPosition());
				Coordinate b = getGUIcoordinate(node2.getPosition());

				// create appropriate color for ways
				float meanvisited = ((node1.getTimesvisited() + node2.getTimesvisited())/2);
				int red = (int) ((meanvisited-minvisited)/(maxvisited-minvisited)*350); // 350 for faster transition into red spectrum
				if (red > 255) {
					red = 255;
				}
				Color color = new Color(red, 0, 255-red);
				g2.setPaint(color);

				// increase thickness of ways with more use
				float strokethickness = (float) (((meanvisited-minvisited)/(maxvisited-minvisited)*2) + 0.2);
				g2.setStroke(new BasicStroke(strokethickness));

				g2.draw(new Line2D.Double(a.getLat(), a.getLon(), b.getLat(), b.getLon()));
			}
		}

	}

	private void paintWays(Graphics2D g2) {
		g2.setPaint(Color.gray);
		ArrayList<SimWay> ways = new ArrayList<SimWay>();
		ways = map.getWays();
		// iterating through all ways
		for (int i = 0; i < ways.size(); i++) {
			SimWay w = ways.get(i);
			ArrayList<SimNode> nodes = new ArrayList<SimNode>();
			nodes = w.getNodes();
			// iterate through the nodes of a way one by one and paint the line between the current node and its successor
			for (int i2 = 0; i2 < (nodes.size() - 1); i2++) {
				SimNode node1 = nodes.get(i2);
				SimNode node2 = nodes.get(i2 + 1);
				Coordinate a = getGUIcoordinate(node1.getPosition());
				Coordinate b = getGUIcoordinate(node2.getPosition());
				g2.draw(new Line2D.Double(a.getLat(), a.getLon(), b.getLat(), b.getLon()));
			}
		}
	}

	private void paintUsers(Graphics2D g2) {
		g2.setPaint(Color.red);
		ArrayList<User> users = new ArrayList<User>();
		users = sim.getUsers();
		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			Coordinate a = getGUIcoordinate(user.getPosition());
			g2.fill(new Ellipse2D.Double(a.getLat() - 4, a.getLon() - 4, 8, 8));
		}
	}

	private void paintPackets(Graphics2D g2) {
		// drawing the pakets
		g2.setPaint(Color.black);
		ArrayList<Paket> pakets = new ArrayList<Paket>();
		pakets = sim.getUnDeliveredpakets();
		for (int i = 0; i < pakets.size(); i++) {
			Paket paket = pakets.get(i);
			Coordinate a = getGUIcoordinate(paket.getPosition());
			g2.fill(new Rectangle2D.Double(a.getLat() - 2, a.getLon() - 2, 4, 4));
		}
	}
	private void paintMap(Graphics2D g2){
		paintWays(g2);
		paintUsers(g2);
		paintPackets(g2);

		// Todo: Document
		if (cluster == true) {
			g2.setPaint(Color.green);
			ArrayList<SimNode> smoothnodes = new ArrayList<SimNode>();
			smoothnodes = sim.getClusternodes();
			for (int i = 0; i < smoothnodes.size(); i++) {
				SimNode node = smoothnodes.get(i);
				Coordinate a = getGUIcoordinate(node.getPosition());
				g2.draw(new Ellipse2D.Double(a.getLat() - 5, a.getLon() - 5, 10, 10));
			}
		}
	}



	
	/**
	 * converts the geo-coordinate of a map to the correct projection coordinate for the GUI
	 * @param coord
	 * @return
	 */
	public Coordinate getGUIcoordinate(Coordinate coord) {
		double modifiedwidth = width;
		double modifiedheight = height;
		if (map.getMapRatio() > 1) {
			modifiedwidth = width/map.getMapRatio();
		}
		else {
			modifiedheight = height * map.getMapRatio();
		}
		// TODO fix projection for non quadratic maps
		double x = (coord.getLon() - map.getLeftBound()) * (modifiedwidth / (map.getRightBound() - map.getLeftBound()));
		double y = (coord.getLat() - map.getBottomBound()) * (modifiedheight / (map.getTopBound() - map.getBottomBound()));
		double z = y - (modifiedheight / 2);
		y = (modifiedheight / 2) + (z * (-1));
		Coordinate result = new Coordinate(x, y);
		return result;
	}

	
	/**
	 * decides if we want to draw a heatmap
	 * @param heatmap
	 */
	public void setHeatmap(boolean heatmap) {
		this.heatmap = heatmap;
	}
	
	/**
	 * decides if we are using clusters
	 * @param cluster
	 */
	public void setCluster(boolean cluster) {
		this.cluster = cluster;
	}
}
