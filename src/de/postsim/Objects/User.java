package de.postsim.Objects;

import java.util.ArrayList;

/**
 * user object, that represents Users in the simulation
 * @author Nils Stahlhut
 *
 */
public class User {
	private Coordinate position;										// position of the user
	private ArrayList<SimNode> path = new ArrayList<SimNode>();			// path to the next node the user wants to visit
	private ArrayList<Paket> pakets = new ArrayList<Paket>();			// list of pakets the user is carrying
	private double coveredDistance = 0;									// amount of distance covered
	private double coveredDistanceasCarrier = 0;						// amount of distance covered while carrying pakets
	private int usernumber;												// id
	private int tradestopcounter = 0;									// counter to control stopping for a trade
	private int waitcounter = 0;										// counter to control waiting at a smooth location
	private ArrayList<SimNode> knownclusters = new ArrayList<SimNode>();// known clusters to the user in the Cluster algorithm
	
	/**
	 * Standard Constructor for a User. Gives it an id, a starting position and a destination
	 * @param startPosition
	 * @param target
	 * @param id
	 */
	public User(Coordinate startPosition, ArrayList<SimNode> path, int id) {
		position = startPosition;
		setPath(path);
		setUsernumber(id);
	}	
	
	/**
	 * get angle to the target in relation to your target
	 * @param target
	 * @return
	 */
	public double getAngle(Coordinate target) {
		double vectorx = path.get(path.size()-1).getPosition().getX() - position.getX();
		double vectory = path.get(path.size()-1).getPosition().getY() - position.getY();
		double vectorx2 = target.getX() - position.getX();
		double vectory2 = target.getY() - position.getY();
		double angle = Math.acos(((vectorx*vectorx2)+(vectory*vectory2))/(position.getDistance(path.get(path.size()-1).getPosition()))*position.getDistance(target));
		return angle;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public SimNode getRandomKnowncluster() {
		double totalnodevalue = 0;
		ArrayList<SimNode> clusters = new ArrayList<SimNode>();
		for (int i = 0; i < knownclusters.size(); i++) {
			SimNode n = knownclusters.get(i);
			if (n.getPosition().getX() == getPosition().getX() && n.getPosition().getY() == getPosition().getY()) {
				
			}
			else{
				clusters.add(n);
			}
		}
		
		for (int i = 0; i < clusters.size(); i++) {
			SimNode n = clusters.get(i);
			totalnodevalue += n.getClustervalue();
		}
		int x = (int) SimMap.RandomNumber(0, totalnodevalue);
		
		double counter = 0;
		SimNode result = null;
		for (int i = 0; counter <= x; i++) {
			SimNode n = clusters.get(i);
			counter += n.getClustervalue();
			result = n;
		}
		return result;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getKnownClusterValue() {
		double totalnodevalue = 0;
		for (int i = 0; i < knownclusters.size(); i++) {
			SimNode n = knownclusters.get(i);
			totalnodevalue += n.getClustervalue();
		}
		return totalnodevalue;
	}
	
	/**
	 * overrides java.lang.Object.toString
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String s = "\n User " + usernumber + "\n" + "Position: " + position.toString() + "\n";
		sb.append(s);
		if (path != null) {
			 String s4 = "Path: " + path.size() + "; Ziel: " + path.get(path.size() - 1).getPosition().toString() + "\n";
			 sb.append(s4);
		}
		else {
			String s4 = "no path found" + "\n";
			sb.append(s4);
		}
		String s2 = "Distance " + coveredDistance + " ; " + coveredDistanceasCarrier + "\n" + "Pakete: ";
		sb.append(s2);		
		
		if(pakets.size() > 0) {
			for(int i = 0; i < pakets.size(); i++){
				Paket p = pakets.get(i);
				String s3 = "Paket " + p.getPaketnumber() + " ";
				sb.append(s3);
			}
			sb.append("\n");
		}
		else {
			sb.append("- \n");
		}
		return sb.toString();
	}
	
	public void increaseTradestopcounter(int increase) {
		this.tradestopcounter += increase;
	}
	
	public void decreaseTradestopcounter() {
		tradestopcounter--;
	}
	
	public void increaseWaitcounter(int increase) {
		this.waitcounter += increase;
	}
	
	public void decreaseWaitcounter() {
		waitcounter--;
	}
	
	public void addKnownclusters(SimNode knowncluster) {
		this.knownclusters.add(knowncluster);
	}
	
	// getters and setters
	
	public int getTradestopcounter() {
		return tradestopcounter;
	}
	
	public int getWaitcounter() {
		return waitcounter;
	}
	
	public Coordinate getPosition() {
		return position;
	}

	public void setPosition(Coordinate position) {
		this.position = position;
	}

	public ArrayList<Paket> getPakets() {
		return pakets;
	}

	public void setPakets(ArrayList<Paket> pakets) {
		this.pakets = pakets;
	}
	
	public void addPaket(Paket paket) {
		this.pakets.add(paket);
	}
	
	public void removePaket(Paket paket) {
		this.pakets.remove(paket);
	}

	public double getCoveredDistance() {
		return coveredDistance;
	}

	public void setCoveredDistance(double coveredDistance) {
		this.coveredDistance = coveredDistance;
	}

	public int getUsernumber() {
		return usernumber;
	}

	public void setUsernumber(int usernumber) {
		this.usernumber = usernumber;
	}

	public double getCoveredDistanceasCarrier() {
		return coveredDistanceasCarrier;
	}

	public void setCoveredDistanceasCarrier(double coveredDistanceasCarrier) {
		this.coveredDistanceasCarrier = coveredDistanceasCarrier;
	}
	
	public ArrayList<SimNode> getPath() {
		return path;
	}

	public void setPath(ArrayList<SimNode> path) {
		this.path = path;
	}

	public ArrayList<SimNode> getKnownclusters() {
		return knownclusters;
	}

	public void setKnownclusters(ArrayList<SimNode> knownclusters) {
		this.knownclusters = knownclusters;
	}
}
