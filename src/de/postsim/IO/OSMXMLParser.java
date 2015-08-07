package de.postsim.IO;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;


import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import de.postsim.Objects.Coordinate;
import de.postsim.Objects.SimMap;
import de.postsim.Objects.SimNode;
import de.postsim.Objects.SimWay;

/**
 * Parses as XML exported OpenStreetMap Data through the osmosis library
 * @author Nils Stahlhut
 *
 */
public class OSMXMLParser {
	
	private ArrayList<SimNode> nodes = new ArrayList<SimNode>(); // list of all nodes in the file
	private ArrayList<SimWay> ways = new ArrayList<SimWay>(); 	// list of all highways in the file
	// the dimensions for this file
	private double topBound;
	private double bottomBound;
	private double rightBound;
	private double leftBound;

	File file;
	Sink sinkImplementation = new Sink() {
	    public void process(EntityContainer entityContainer) {
	    	// processes all entries in the file and handles them depending on their type
	        Entity entity = entityContainer.getEntity();
	        if (entity instanceof Bound) {
	        	topBound = ((Bound) entity).getTop();
	        	bottomBound = ((Bound) entity).getBottom();
	        	rightBound = ((Bound) entity).getRight();
	        	leftBound = ((Bound) entity).getLeft();
	        } else if (entity instanceof Node) {
	        	Coordinate position = new Coordinate(((Node) entity).getLatitude(), ((Node) entity).getLongitude());
	        	SimNode node = new SimNode(((Node) entity).getId(), position);
	        	nodes.add(node);
	        } else if (entity instanceof Way) {
	        	ArrayList<SimNode> waynodes = new ArrayList<SimNode>();
	        	// iterate through the way to get the corresponding nodeids and use them to get SimNode objects for the way
	        	for (int i = 0; i < ((Way) entity).getWayNodes().size(); i++) {
	        		SimNode result = null;
	        		for (int i2 = 0; i2 < nodes.size(); i2++) {
	        			SimNode node = nodes.get(i2);
	        			if (((Way) entity).getWayNodes().get(i).getNodeId() == node.getId()) {
	        				result = node;
	        			}
		        	}
	        		waynodes.add(result);
	        	}
	        	SimWay way = new SimWay(((Way) entity).getId(), waynodes);
	        	
	        	// only add way to the ways list if it is a highway
	        	boolean highway = false;
	        	Collection<Tag> tags = ((Way) entity).getTags();
	        	for (Iterator<Tag> iterator = tags.iterator(); iterator.hasNext();) {
	        		Tag tag = iterator.next();
	        		if(tag.getKey().equals("highway")){
	        			highway = true;
	        		}
	        	}
	        	
	        	if (highway == true) {
	        		ways.add(way);
	        	}
	        }
	    }
	    public void release() { }
	    public void complete() { }
		public void initialize(java.util.Map<String, Object> arg0) { }
	};
	
	
	public OSMXMLParser(File file) {
		this.file = file;
	}

	/**
	 * this method is used to parse an .osm file with osmosis and create a custom SimMap object out of it
	 * @return a SimMap that was parsed out of the given file
	 */
	public SimMap parseNewMap(Random rgen) {
		CompressionMethod compression = CompressionMethod.None;

		if (file.getName().endsWith(".gz")) {
			compression = CompressionMethod.GZip;
		} else if (file.getName().endsWith(".bz2")) {
		    compression = CompressionMethod.BZip2;
		} 

		RunnableSource reader;
		reader = new XmlReader(file, false, compression);
		reader.setSink(sinkImplementation);

		Thread readerThread = new Thread(reader);
		readerThread.start();

		while (readerThread.isAlive()) {
		    try {
		        readerThread.join();
		    } catch (InterruptedException e) {
		        // do nothing
		    }
		}
		// create SimMap and return it
		SimMap map = new SimMap(rgen, ways, topBound, bottomBound, rightBound, leftBound);
		System.out.println("Map extracted");
		System.out.println("Dimensions:");
		System.out.println("Heigth: " + new Coordinate(topBound,rightBound).getDistance(new Coordinate(bottomBound,rightBound)) + "m");
		System.out.println("Width: " + new Coordinate(topBound,leftBound).getDistance(new Coordinate(topBound,rightBound)) + "m");
		return map;
	}
}
