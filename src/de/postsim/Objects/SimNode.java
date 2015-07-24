package de.postsim.Objects;

import java.util.HashMap;

/**
 * a Node Object representing points on our Map
 * @author Nils Stahlhut
 *
 */
public class SimNode implements Comparable<SimNode>{
	
	private long id;								// id
	private Coordinate position;					// position of the node
	private HashMap<SimNode, Double> neighbours;	// HashMap of the neighbours of this node referencing the node and the distance to it
	private Double fvalue;							// used to sort Nodes for the A*-Algorithm
	private double clustervalue;					// used to denote clusters for SMOOTH algorithm
	private int timesvisited;						// the number of times this node has been visited by a user
	
	/**
	 * Constructor for SimNodes with id and a position
	 * @param id
	 * @param position
	 */
	public SimNode(long id, Coordinate position) {
		this.id = id;
		this.position = position;
		neighbours = new HashMap<SimNode, Double>();
		fvalue = (double) 0;
	}
	
	
	/**
	 * used to get a natural ordering on the basis of fvalue for nodes in the A*-algorithm. 
	 * overrides java.lang.Comparable<Objects.SimNode>.compareTo
	 */
	public int compareTo(SimNode node) {
		if(this.fvalue>node.getFvalue()) {
			return 1;
		}
	    else if(this.fvalue.equals(node.getFvalue())) {
	    	if(this.id>node.getId()) {
				return 1;
			}
		    else if(this.equals(node)) {
		    	return 0;
		    }
		    else {
		    	 return -1;
		    }
	    }
	    else {
	    	 return -1;
	    }
	}
	
	
	/**
	 * overrides java.lang.Object.toString
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		String s = "\nID: (" + id + ") Position: " + position.toString();
		sb.append(s);
		return sb.toString();
	}

	// getters and setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public Coordinate getPosition() {
		return position;
	}
	
	public void setPosition(Coordinate position) {
		this.position = position;
	}
	
	public void addNeighbour(SimNode node, double distance) {
		neighbours.put(node, distance);
	}
	
	public HashMap<SimNode, Double> getNeighbours(){
		return neighbours;
	}
	
	public Double getFvalue() {
		return fvalue;
	}

	public void setFvalue(Double fvalue) {
		this.fvalue = fvalue;
	}

	public int getTimesvisited() {
		return timesvisited;
	}

	public void increaseTimesvisited() {
		timesvisited++;
	}


	public double getClustervalue() {
		return clustervalue;
	}


	public void setClustervalue(double clustervalue) {
		this.clustervalue = clustervalue;
	}
}
