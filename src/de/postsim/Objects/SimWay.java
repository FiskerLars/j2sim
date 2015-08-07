package de.postsim.Objects;

import java.util.ArrayList;

/**
 * A Way between nodes on our map
 * @author Nils Stahlhut
 *
 */
public class SimWay {
	
	private long id;												// id
	private ArrayList<SimNode> nodes = new ArrayList<SimNode>();	// nodes in a way
	
	/**
	 * Constructor for SimWay with id and a position
	 * @param id
	 * @param nodes
	 */
	public SimWay(long id, ArrayList<SimNode> nodes) {
		this.setNodes(nodes);
		this.setId(id);
	}
	
	/**
	 * overrides java.lang.Object.toString
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		String s = "\nID: (" + id + ") NodeIDs: " + nodes.toString();
		sb.append(s);
		return sb.toString();
	}

	// getters and setters
	public ArrayList<SimNode> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<SimNode> nodes) {
		this.nodes = nodes;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
