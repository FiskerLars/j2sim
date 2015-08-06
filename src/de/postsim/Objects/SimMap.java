package de.postsim.Objects;

import java.util.*;
import java.util.Map.Entry;

/**
 * Map on which we simulate our Users and Pakets. Stores Nodes and Ways.
 * @author Nils Stahlhut
 *
 */
public class SimMap {
	
	private ArrayList<SimNode> nodes = new ArrayList<SimNode>();				// list of nodes on the map
	private ArrayList<SimNode> randomnodes = new ArrayList<SimNode>();			// list of nodes that are used, when a random node is needed
	private ArrayList<SimWay> ways = new ArrayList<SimWay>();					// list of ways on the map
	private double topBound;													// coordinates for borders of the map
	private double bottomBound;
	private double rightBound;
	private double leftBound;
	private Random rgen = new Random(4); // static random seed for reproducability


	/**
	 * construct our map out of the Ways we extracted from a OpenStreetMapFile
	 * @param ways
	 * @param topBound
	 * @param bottomBound
	 * @param rightBound
	 * @param leftBound
	 */
	public SimMap(ArrayList<SimWay> ways, double topBound, double bottomBound, double rightBound, double leftBound) {
		this.rgen = rgen;
		// get all the nodes out of the ways
		ArrayList<SimNode> tempnodes = new ArrayList<SimNode>();
		for (int i = 0; i < ways.size(); i++) {
			SimWay w = ways.get(i);
			tempnodes.addAll(w.getNodes());
		}
		
		// eliminate duplicate nodes in our list by putting them into a hashset (no duplicates allowed in this type of collection)
		HashSet<SimNode> hs = new HashSet<SimNode>();		
		hs.addAll(tempnodes);
		// clear our old list and add our nodes without duplicates from the Hashset
		tempnodes.clear();
		tempnodes.addAll(hs);
		this.setNodes(tempnodes);
		
		// iterate through all ways to add their neighbours to every node 
		for (int i = 0; i < ways.size(); i++) {
			SimWay w = ways.get(i);
			ArrayList<SimNode> waynodes = new ArrayList<SimNode>();
			waynodes = w.getNodes();
			for (int i2 = 0; i2 < (waynodes.size() - 1); i2++) {
				SimNode node1 = waynodes.get(i2);
				SimNode node2 = waynodes.get(i2 + 1);
				double distance = node1.getPosition().getDistance(node2.getPosition());
				node1.addNeighbour(node2, distance);
				node2.addNeighbour(node1, distance);
			}
		}
		
		// iterate through our nodes and remove every node without a neighbour as a safety measure
		for (int i = 0; i < this.getNodes().size(); i++) {
			SimNode node = this.getNodes().get(i);
			if (node.getNeighbours().size() < 1) {
				this.getNodes().remove(node);
			}
		}	
		
		// set our other constructor variables
		this.setWays(ways);
		this.setTopBound(topBound);
		this.setBottomBound(bottomBound);
		this.setRightBound(rightBound);
		this.setLeftBound(leftBound);
		
		// builds our list from which we extract random nodes as targets and positions for users and pakets
		buildRandomNodesGraph();
	}
	
	
	/**
	 * this function creates a list of nodes. This list contains the biggest possible subgraph in our map within its bounds.
	 * we set this list as "randomnodes" so Users and Pakets don't spawn or get targets out of bounds or in little places on the map 
	 * that have no connection to the rest of the map
	 */
	public void buildRandomNodesGraph () {
		ArrayList<SimNode> tempnodes = new ArrayList<SimNode>();
		// eliminate every node that is not within bounds of the map
		for (int i = 0; i < nodes.size(); i++) {
			SimNode node = nodes.get(i);
			if (node.getPosition().getLon() > leftBound &&
				node.getPosition().getLon() < rightBound &&
				node.getPosition().getLat() > bottomBound &&
				node.getPosition().getLat() < topBound) {
				tempnodes.add(node);
			}
		}
		
		// graphlist is a list of all the subgraphs we build
		ArrayList<HashSet<SimNode>> graphlist = new ArrayList<HashSet<SimNode>>();
		int biggestgraph = 0;
		/* all nodes that are added to one of our subgraphs are deleted from our tempnodes list (all nodes excluding out of bounds)
		 * so if our biggest subgraph is bigger than the tempnodes list with the remaining nodes, we can't find a bigger graph and 
		 * so have found our biggest possible subgraph
		 */
		while (tempnodes.size() > biggestgraph) {
			SimNode start = tempnodes.get(0);
			ArrayList<SimNode> tempnodes2 = new ArrayList<SimNode>();
			HashSet<SimNode> subgraph = new HashSet<SimNode>();
			subgraph.add(start);
			tempnodes2.addAll(tempnodes);
			// iterate through all nodes from a random starting point and add every node on every path we find
			// if our iteration is finished we know every node reachable from our random starting point
			// usually we won't need more than one to three iterations, which is faster than using Dijkstra
			while (!tempnodes2.isEmpty()) {
				int x = (int) RandomNumber(0, tempnodes2.size());
				ArrayList<SimNode> path = new ArrayList<SimNode>();
				path = getPath(start, tempnodes2.get(x));
				if (path != null) {
					subgraph.addAll(path);
					tempnodes2.remove(x);
					tempnodes2.removeAll(path);
				}
				else {
					tempnodes2.remove(x);
				}
			}
			graphlist.add(subgraph);
			for (int i = 0; i < graphlist.size(); i++) {
				HashSet<SimNode> g = new HashSet<SimNode>();
				g = graphlist.get(i);
				if (g.size() > biggestgraph) {
					biggestgraph = g.size();
				}
			}
			tempnodes.removeAll(subgraph);
		}
		
		HashSet<SimNode> result = new HashSet<SimNode>();
		for (int i = 0; i < graphlist.size(); i++) {
			HashSet<SimNode> subgraph = new HashSet<SimNode>();
			subgraph = graphlist.get(i);
			if (subgraph.size() > result.size()) {
				result.clear();
				result.addAll(subgraph);
			}
		}
		ArrayList<SimNode> result2 = new ArrayList<SimNode>();
		result2.addAll(result);
		this.setRandomnodes(result2);
	}
	
	
	/**
	 * get a path to another node on the map using the A*-algorithm
	 * @param startingnode
	 * @param endnode
	 * @return
	 */
	public ArrayList<SimNode> getPath(SimNode startingnode, SimNode endnode) {
		TreeMap<SimNode, SimNode> openlist = new TreeMap<SimNode, SimNode>();
		HashMap<SimNode, SimNode> closedlist = new HashMap<SimNode, SimNode>();
		startingnode.setFvalue(startingnode.getPosition().getDistance(endnode.getPosition()));
		openlist.put(startingnode, null);
		
		while (openlist.size() != 0) {
			SimNode current = openlist.firstKey();
			
			if (current.equals(endnode)) {
				ArrayList<SimNode> path = new ArrayList<SimNode>();
				path.add(0, current);
				SimNode prenode = openlist.get(endnode);
				while (prenode != null) {
					path.add(0, prenode);
					prenode = closedlist.get(prenode);
				}
				path.remove(startingnode);
				return path;
			}
			
			closedlist.put(current, openlist.get(openlist.firstKey()));
			openlist.remove(openlist.firstKey());
			
			
			for(Entry<SimNode, Double> neighbour : current.getNeighbours().entrySet()){
	            SimNode neighbournode = neighbour.getKey();
	            if (closedlist.containsKey(neighbournode)) {
	            	continue;
	            }
	            
	            double fvalue = neighbour.getValue().doubleValue();
	            SimNode currentnode = current;
            	SimNode previousnode = closedlist.get(current);
				while (previousnode != null) {
					double distance = previousnode.getNeighbours().get(currentnode);
					fvalue += distance;
					currentnode = previousnode;
					previousnode = closedlist.get(previousnode);
				}
				fvalue += neighbournode.getPosition().getDistance(endnode.getPosition());
	            
	            if (openlist.containsKey(neighbournode)) {
	            	if (fvalue < neighbournode.getFvalue().doubleValue()) {
	            		openlist.remove(neighbournode);
	            		neighbournode.setFvalue(new Double(fvalue));
	            		openlist.put(neighbournode, current);
	            	}
	            }
	            else {
	            	neighbournode.setFvalue(new Double(fvalue));
	            	openlist.put(neighbournode, current);
	            }
	        }
		}		
		return null;
	}
	
	
	/**
	 * fetches a random node from the map (excluding out of bounds or unreachable nodes)
	 * @return
	 */
	public SimNode getRandomNode() {
		int x = (int) RandomNumber(0, randomnodes.size());
		return randomnodes.get(x);
	}
	
	
	/**
	 * get the corresponding node for a Coordinate on the map
	 * @param nodeposition
	 * @return
	 */
	public SimNode getNode(Coordinate nodeposition) {
		SimNode result = null;
		for (int i = 0; i < nodes.size(); i++) {
			SimNode node = nodes.get(i);
			if (node.getPosition().getLat() == nodeposition.getLat() &&
				node.getPosition().getLon() == nodeposition.getLon()) {
				result = node;
			}
		}
		return result;
	}
	
	
	/**
	 * get a node with its id
	 * @param nodeid
	 * @return
	 */
	public SimNode getNodeById(long nodeid) {
		SimNode result = null;
		for (int i = 0; i < nodes.size(); i++) {
			SimNode node = nodes.get(i);
			if (nodeid == node.getId()) {
				result = node;
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * @return the amount of times the node with the least visits was visited
	 */
	public int getMinTimesVisited () {
		int result = Integer.MAX_VALUE;
		for (int i = 0; i < nodes.size(); i++) {
			SimNode node = nodes.get(i);
			if (node.getTimesvisited() < result) {
				result = node.getTimesvisited();
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * @return the amount of times the node with the most visits was visited
	 */
	public int getMaxTimesVisited () {
		int result = 0;
		for (int i = 0; i < nodes.size(); i++) {
			SimNode node = nodes.get(i);
			if (node.getTimesvisited() > result) {
				result = node.getTimesvisited();
			}
		}
		return result;
	}
	
	
	/**
	 * Returns a random number, but doesn't include upperLimit!
	 * 
	 * @param lowerLimit
	 * @param upperLimit
	 * @return
	 */
	public double RandomNumber(double lowerLimit, double upperLimit) {
		return rgen.nextDouble() * (upperLimit - lowerLimit) + lowerLimit;
		//Math.random() * (upperLimit - lowerLimit) + lowerLimit;
	}
	
	
	/**
	 * Ratio of the map of height/width
	 * @return
	 */
	public double getMapRatio() {
		return new Coordinate(topBound,rightBound).getDistance(new Coordinate(bottomBound,rightBound))/
		new Coordinate(topBound,leftBound).getDistance(new Coordinate(topBound,rightBound));
	}
	
	
	/**
	 * overrides java.lang.Object.toString
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		String s = "Boundaries: (" + topBound + "|" + bottomBound + ") (" + rightBound + "|" + leftBound + ") \n";
		sb.append(s);
		sb.append(nodes.toString());
		sb.append(ways.toString());
		return sb.toString();
	}

	// getters and setters
	public ArrayList<SimNode> getNodes() {
		return nodes;
	}

	private void setNodes(ArrayList<SimNode> nodes) {
		this.nodes = nodes;
	}

	public ArrayList<SimWay> getWays() {
		return ways;
	}

	private void setWays(ArrayList<SimWay> ways) {
		this.ways = ways;
	}

	public double getTopBound() {
		return topBound;
	}

	public void setTopBound(double topBound) {
		this.topBound = topBound;
	}

	public double getBottomBound() {
		return bottomBound;
	}

	public void setBottomBound(double bottomBound) {
		this.bottomBound = bottomBound;
	}

	public double getRightBound() {
		return rightBound;
	}

	public void setRightBound(double rightBound) {
		this.rightBound = rightBound;
	}

	public double getLeftBound() {
		return leftBound;
	}

	public void setLeftBound(double leftBound) {
		this.leftBound = leftBound;
	}

	public ArrayList<SimNode> getRandomnodes() {
		return randomnodes;
	}

	public void setRandomnodes(ArrayList<SimNode> randomnodes) {
		this.randomnodes = randomnodes;
	}

	public void setRandomGenerator(Random randomGenerator) {
		this.rgen = randomGenerator;
	}
}
