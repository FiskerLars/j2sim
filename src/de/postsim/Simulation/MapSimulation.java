package de.postsim.Simulation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.postsim.IO.CSVLogging;
import de.postsim.IO.ContactGraph;
import de.postsim.Objects.Coordinate;
import de.postsim.Objects.Paket;
import de.postsim.Objects.SimMap;
import de.postsim.Objects.SimNode;
import de.postsim.Objects.User;

/**
 * Simulation class, this is where the magic happens. Stores Users and Pakets, the map and other simulation parameters
 * @author Nils Stahlhut
 *
 */
public class MapSimulation {

	private SimMap map;
	private ArrayList<User> users = new ArrayList<User>();							// list of users
	private ArrayList<Paket> pakets = new ArrayList<Paket>();						// list of non-delivered pakets
	private ArrayList<Paket> deliveredpakets = new ArrayList<Paket>();				// list of delivered pakets
	private double speed = 1.4; 													// the speed of a user in m/s
	private int steptime = 1000; 													// amount of time in ms that elapses per simulation "cycle"
	private double actualspeed;														// speed/steptime*1000
	private int bluetoothrange = 10;												// bluetoothrange in meters
	private int tradeDelay = 10;													// amount of cycles needed for trading pakets
	private long cycles = 0;														// simulation cycles elapsed
	private int movementalgorithm = RANDOM_WAYPOINT;								// movement algorithms
	public static final int RANDOM_WAYPOINT = 1;
	public static final int CLUSTER_WAYPOINT = 2;
	private ArrayList<SimNode> clusternodes = new ArrayList<SimNode>();				// nodes that are cluster in the cluster_waypoint algorithm					
	private CSVLogging logger;														// logging object
	private ContactGraph graph;
	
	/**
	 * empty constructor (except for a map) for testing purposes. Uses standard settings with 8 users and 25 pakets
	 */
	public MapSimulation(SimMap map, String csvfile {
		this(map, 8, 25, csvfile);
	}

	/**
	 * standard settings constructor with a variable amount of pakets and users
	 * @param numberofusers
	 * @param numberofpakets
	 */
	public MapSimulation(SimMap map, int numberofusers, int numberofpakets, String csvfile) {
		this(map, numberofusers, numberofpakets, this.bluetoothrange, this.movementalgorithm, csvfile);
	}
	
	
	/**
	 * a constructor for customising simulation parameters
	 * @param map
	 * @param numberofusers
	 * @param numberofpakets
	 * @param bluetoothrange in m
	 * @param movementalgorithm 1 = RandomWayPoint; 2 = ClusterWayPoint
	 * @param tradeDelay tradeDelay for Users in SimulationCycles
	 */
	public MapSimulation(SimMap map, int numberofusers, int numberofpakets, int bluetoothrange, int movementalgorithm, int tradeDelay, String filename) {
		setMap(map);
		switch(movementalgorithm) {
			case RANDOM_WAYPOINT:
				initRandomWaypointSim(numberofusers, numberofpakets);
				break;
			case CLUSTER_WAYPOINT:
				initClusterWaypointSim(numberofusers, numberofpakets);
				break;
			default:
				System.out.println("error no movement algorithm");
		}
		
		setBluetoothrange(bluetoothrange);
		setMovementalgorithm(movementalgorithm);
		setActualspeed((speed / 1000) * steptime);
		setTradeDelay(tradeDelay);
		
		// building the name of our logfile
		Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 'at' HH.mm.ss");
		logger = new CSVLogging("D:\\" + filename + " " + sdf.format(cal.getTime()) + " U" + numberofusers + "_P" + numberofpakets + ".csv");
	}




	private void initRandomWaypointSim(int numberofusers, int numberofpackets) {
		for (int i = 0; i < numberofusers; i++) {
			// create user with random starting location and random starting target
			SimNode random = map.getRandomNode();
			Coordinate start = new Coordinate(random.getPosition().getX(),random.getPosition().getY());
			ArrayList<SimNode> path = new ArrayList<SimNode>();
			path = map.getPath(map.getNode(start), map.getRandomNode());
			if (path == null || path.size() == 0) {
				path = new ArrayList<SimNode>();
				path.add(map.getNode(start));
			}
			User u = new User(start, path,(i+1));
			users.add(u);
		}
		for (int i = 0; i < numberofpackets; i++) {
			// create paket with random starting location and random destinatio
			SimNode random = map.getRandomNode();
			SimNode random2 = map.getRandomNode();
			Paket p = new Paket(new Coordinate(random.getPosition().getX(),random.getPosition().getY()),
					new Coordinate(random2.getPosition().getX(),random2.getPosition().getY()),(i+1));
			pakets.add(p);
		}

	}

	private void initClusterWaypointSim(int numberofusers, int numberofpakets) {
		// initialize Clusters for the algorithm
		initializeClusters();

		for (int i = 0; i < numberofusers; i++) {
			// create user with starting location on a random cluster weighed by the importance of the cluster
			SimNode randomclusternode = getRandomClusterNode();
			Coordinate start = new Coordinate(randomclusternode.getPosition().getX(),randomclusternode.getPosition().getY());
			ArrayList<SimNode> path = new ArrayList<SimNode>();
			path = map.getPath(map.getNode(start), getRandomClusterNode());
			while (path == null || path.size() == 0) {
				path = map.getPath(map.getNode(start), getRandomClusterNode());
			}
			User u = new User(start, path,(i+1));
			u.addKnownclusters(randomclusternode);
			u.addKnownclusters(u.getPath().get(u.getPath().size() - 1));
			users.add(u);
		}
		for (int i = 0; i < numberofpakets; i++) {
			// create pakets with starting location and destination on a random cluster weighed by the importance of the cluster
			SimNode random = getRandomClusterNode();
			SimNode random2 = getRandomClusterNode();
			while (random2 == random) {
				random2 = getRandomClusterNode();
			}
			Paket p = new Paket(new Coordinate(random.getPosition().getX(),random.getPosition().getY()),
					new Coordinate(random2.getPosition().getX(),random2.getPosition().getY()),(i+1));
			pakets.add(p);
		}
	}
	
	/**
	 * builds small medium and big clusters on a map
	 */
	private void initializeClusters() {
		int numberofclusters = map.getRandomnodes().size()/50;
		int bigclusters = (int) (numberofclusters * 0.15);
		int mediumclusters = (int) (numberofclusters * 0.25);
		int smallclusters = (int) (numberofclusters * 0.6);
		
		while ((bigclusters+mediumclusters+smallclusters) < numberofclusters) {
			smallclusters++;
		}
				
		while (getClusternodes().size() < smallclusters) {
			SimNode n = map.getRandomNode();
			if (!getClusternodes().contains(n)) {
				n.setClustervalue(1);
				addClusternode(n);
			}
		}
		while (getClusternodes().size() < (smallclusters+mediumclusters)) {
			SimNode n = map.getRandomNode();
			if (!getClusternodes().contains(n)) {
				n.setClustervalue(2);
				addClusternode(n);
			}
		}
		while (getClusternodes().size() < (numberofclusters)) {
			SimNode n = map.getRandomNode();
			if (!getClusternodes().contains(n)) {
				n.setClustervalue(4);
				addClusternode(n);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private SimNode getRandomClusterNode() {
		double totalnodevalue = 0;
		for (int i = 0; i < getClusternodes().size(); i++) {
			SimNode n = getClusternodes().get(i);
			totalnodevalue += n.getClustervalue();
		}
		int x = (int) SimMap.RandomNumber(0, totalnodevalue);
		
		double counter = 0;
		SimNode result = null;
		for (int i = 0; counter <= x; i++) {
			SimNode n = getClusternodes().get(i);
			counter += n.getClustervalue();
			result = n;
		}
		return result;
	}
	


	/**
	 * main method, simulates one cycle
	 * before users are moved 3 checks are made. 
	 * For Pakets without a Carrier in range of the user,
	 * For Pakets with a destination in range of the user and
	 * For Users in range to make a negotiation
	 */
	public void simulate() {
		checkForCarrierlessPakets();
		checkForDeliverablePakets();
		checkForNegotiations();
		// move all Users
		for (int i = 0; i < users.size(); i++) {
			User u = users.get(i);
			if (u.getTradestopcounter() <= 0) {
				if (u.getWaitcounter() <= 0) {
					moveUser(u, actualspeed);
				}
				else {
					u.decreaseWaitcounter();
				}
			} else {
				u.decreaseTradestopcounter();
			}
		}
		cycles++;
	}
	
	
	/**
	 * checks if there are pakets without carriers to pick up in range of a user
	 */
	public void checkForCarrierlessPakets() {
		// looking for pakets without carriers in range of the users
		for (int i = 0; i < pakets.size(); i++) {
			Paket p = pakets.get(i);
			if (p.getCarrier() == null) {
				for (int i2 = 0; i2 < users.size(); i2++) {
					User u = users.get(i2);
					if (u.getPosition().getDistance(p.getPosition()) < bluetoothrange) {
						if (u.getPosition().getX() == p.getPosition().getX() && u.getPosition().getY() == p.getPosition().getY() && p.getCarrier() == null) {
							// if you are at the pakets position, pick it up
							u.addPaket(p);
							p.setCarrier(u);

							// logging
							ArrayList<String[]> paketadd = new ArrayList<String[]>();
							String s1 = "User " + u.getUsernumber();
							String s2 = "Paket " + p.getPaketnumber();
							paketadd.add(new String[] { String.valueOf(cycles), s1, s2, u.getPosition().toString(), "Free Paket added" });
							logger.write(paketadd);
						} else {
							// if you are not at the pakets position, move towards it
							ArrayList<SimNode> path = new ArrayList<SimNode>();
							SimNode temp = u.getPath().get(0);
							path = map.getPath(u.getPath().get(0), map.getNode(p.getPosition()));
							if (path == null || path.size() == 0) {
								path = new ArrayList<SimNode>();
								path.add(temp);
							}
							// if you already want to go to the pakets position don't change your path
							if (path.get(path.size()-1) == u.getPath().get(u.getPath().size()-1)) {
								// do nothing
							}
							else {
								u.setPath(path);
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * checks if there are pakets that can be delivered in range of the user
	 */
	public void checkForDeliverablePakets() {
		// checking if pakets can be delivered
		for (int i = 0; i < users.size(); i++) {
			User u = users.get(i);
			for (int i2 = 0; i2 < u.getPakets().size(); i2++) {
				Paket p = u.getPakets().get(i2);
				if (u.getPosition().getDistance(p.getDestination()) < bluetoothrange) {
					if (u.getPosition().getX() == p.getDestination().getX() && u.getPosition().getY() == p.getDestination().getY()) {
						// if you are at the pakets destinations position, deliver it
						u.removePaket(p);
						p.setCarrier(null);
						p.setDelivered(true);
						p.setDeliverytimestamp(cycles);
						deliveredpakets.add(p);
						pakets.remove(p);

						// logging
						ArrayList<String[]> paketdelivered = new ArrayList<String[]>();
						String s1 = "User " + u.getUsernumber();
						String s2 = "Paket " + p.getPaketnumber();
						paketdelivered.add(new String[] { String.valueOf(cycles), s1, s2, u.getPosition().toString(), "Paket delivered" });
						logger.write(paketdelivered);
					} else {
						// if you are not at the pakets destinations position, move towards it
						ArrayList<SimNode> path = new ArrayList<SimNode>();
						SimNode temp = u.getPath().get(0);
						path = map.getPath(u.getPath().get(0), map.getNode(p.getDestination()));
						if (path == null || path.size() == 0) {
							path = new ArrayList<SimNode>();
							path.add(temp);
						}
						// if you already want to go to the pakets destination don't change your path
						if (path.get(path.size()-1) == u.getPath().get(u.getPath().size()-1)) {
							// do nothing
						}
						else {
							u.setPath(path);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * checks through all Users for possible negotiations about pakets
	 */
	public void checkForNegotiations() {
		// going through all users and for every user again through every other user to check all possible pairs
		for (int i = 0; i < users.size(); i++) {
			User u = users.get(i);
			for (int i2 = 0; i2 < users.size(); i2++) {
				User u2 = users.get(i2);
				if (i != i2) {
					// check if both users aren't currently blocked from trading
					if (u.getPosition().getDistance(u2.getPosition()) < bluetoothrange && u.getTradestopcounter() < 1 && u2.getTradestopcounter() < 1) {
						for (int i3 = 0; i3 < u.getPakets().size(); i3++) {
							Paket p = u.getPakets().get(i3);
							double anglecarrier = u.getAngle(p.getDestination());
							double anglecarriee = u2.getAngle(p.getDestination());
							if (anglecarriee < anglecarrier) {
								u.increaseTradestopcounter(tradeDelay);
								u2.increaseTradestopcounter(tradeDelay);
								u.removePaket(p);
								u2.addPaket(p);
								p.setCarrier(u2);
								p.setPosition(new Coordinate(u2.getPosition().getX(), u2.getPosition().getY()));
								p.setHandovers(p.getHandovers() + 1);

								// logging
								ArrayList<String[]> paketnegotiate = new ArrayList<String[]>();
								String s1 = "User " + u.getUsernumber();
								String s2 = "User " + u2.getUsernumber();
								String s3 = "Paket " + p.getPaketnumber();
								paketnegotiate.add(new String[] {String.valueOf(cycles), s1, s3, u.getPosition().toString(), "Paket negotiation succesful" });
								paketnegotiate.add(new String[] {String.valueOf(cycles), s2, s3, u2.getPosition().toString(), "Paket negotiation succesful" });
								logger.write(paketnegotiate);
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * moves the user a certain distance towards the target
	 * @param u the user that needs to be moved
	 * @param movedistance the amount of distance it needs to be moved
	 */
	public void moveUser(User u, double movedistance) {
		double distance = u.getPosition().getDistance(u.getPath().get(0).getPosition());
		// check if our target is nearer than the full movedistance
		if (distance <= movedistance) {
			// if we are in move distance for this cycle set our position to our target
			u.getPosition().setX(u.getPath().get(0).getPosition().getX());
			u.getPosition().setY(u.getPath().get(0).getPosition().getY());
			
			// if you are already at the nodes position, don't increase the counter for the times the node has been visited
			if (distance != 0) {
				u.getPath().get(0).increaseTimesvisited();
			}
			
			u.getPath().remove(0);
			
			// increase coveredDistance for our user and all their pakets
			u.setCoveredDistance(u.getCoveredDistance() + distance);
			if(u.getPakets().size() > 0) {
				u.setCoveredDistanceasCarrier(u.getCoveredDistanceasCarrier() + distance);
			}
			for (int i = 0; i < u.getPakets().size(); i++) {
				Paket p = u.getPakets().get(i);
				p.setPosition(new Coordinate(u.getPosition().getX(), u.getPosition().getY()));
				p.setCoveredDistance(p.getCoveredDistance() + distance);
			}
			
			// get new destination if we are at the end of our current one, depending on our movement algorithm
			if (u.getPath().size() == 0) {
				if (movementalgorithm == RANDOM_WAYPOINT) {
					ArrayList<SimNode> path = new ArrayList<SimNode>();
					path = map.getPath(map.getNode(u.getPosition()), map.getRandomNode());
					if (path == null || path.size() == 0) {
						path = new ArrayList<SimNode>();
						path.add(map.getNode(u.getPosition()));
					}
					u.setPath(path);
				}
				else if (movementalgorithm == CLUSTER_WAYPOINT){
					double x = SimMap.RandomNumber(0, 2);
					if (x > (u.getKnownClusterValue()/10)) {
						ArrayList<SimNode> path = new ArrayList<SimNode>();
						path = map.getPath(map.getNode(u.getPosition()), getRandomClusterNode());
						while (path == null || path.size() == 0) {
							path = map.getPath(map.getNode(u.getPosition()), getRandomClusterNode());
						}
						if (!u.getKnownclusters().contains(path.get(path.size() - 1))) {
							u.addKnownclusters(path.get(path.size() - 1));
						}
						u.setPath(path);
						double x2 = SimMap.RandomNumber(0, 1);
						if (x2 < 0.65) {
							int x3 = (int) SimMap.RandomNumber(300, 600);
							u.increaseWaitcounter(x3);
						}
						else if (x2 < 0.9) {
							int x3 = (int) SimMap.RandomNumber(1800, 7200);
							u.increaseWaitcounter(x3);
						}
						else {
							int x3 = (int) SimMap.RandomNumber(14400, 288800);
							u.increaseWaitcounter(x3);
						}
						
					}
					else {
						ArrayList<SimNode> path = new ArrayList<SimNode>();
						path = map.getPath(map.getNode(u.getPosition()), u.getRandomKnowncluster());
						while (path == null || path.size() == 0) {
							path = map.getPath(map.getNode(u.getPosition()), u.getRandomKnowncluster());
						}
						u.addKnownclusters(path.get(path.size() - 1));
						u.setPath(path);
						double x2 = SimMap.RandomNumber(0, 1);
						if (x2 < 0.5) {
							int x3 = (int) SimMap.RandomNumber(300, 600);
							u.increaseWaitcounter(x3);
						}
						else if (x2 < 0.8) {
							int x3 = (int) SimMap.RandomNumber(1800, 7200);
							u.increaseWaitcounter(x3);
						}
						else {
							int x3 = (int) SimMap.RandomNumber(14400, 288800);
							u.increaseWaitcounter(x3);
						}
					}
					
				}
				else {
					System.out.println("help no movement algorithm");
				}
			}	
		} else {
			// move towards our target
			double vectorx = u.getPath().get(0).getPosition().getX() - u.getPosition().getX();
			double vectory = u.getPath().get(0).getPosition().getY() - u.getPosition().getY();
			double normalisedx = vectorx/distance;
			double normalisedy = vectory/distance;
			u.getPosition().setX(u.getPosition().getX() + (normalisedx * movedistance));
			u.getPosition().setY(u.getPosition().getY() + (normalisedy * movedistance));
			
			// increase coveredDistance for our user and all their pakets
			u.setCoveredDistance(u.getCoveredDistance() + movedistance);
			if(u.getPakets().size() > 0) {
				u.setCoveredDistanceasCarrier(u.getCoveredDistanceasCarrier() + movedistance);
			}
			for (int i = 0; i < u.getPakets().size(); i++) {
				Paket p = u.getPakets().get(i);
				p.setPosition(new Coordinate(u.getPosition().getX(), u.getPosition().getY()));
				p.setCoveredDistance(p.getCoveredDistance() + movedistance);
			}
		}
	}
	
	
	/**
	 * writes some closing statistics into our logging file
	 */
	public void writeFinalStats() {
		// write all nodes into the textfile
		ArrayList<String[]> nodes = new ArrayList<String[]>();
		nodes.add(new String[] {""});
		nodes.add(new String[] {"Nodes:"});
		nodes.add(new String[] {"ID", "Times visited"});
		for (int i = 0; i < map.getNodes().size(); i++) {
			SimNode n = map.getNodes().get(i);
			nodes.add(new String[] {String.valueOf(n.getId()), String.valueOf(n.getTimesvisited())});
		}
		logger.write(nodes);
		
		// write all users into the textfile
		ArrayList<String[]> users = new ArrayList<String[]>();
		users.add(new String[] {""});
		users.add(new String[] {"Users:"});
		users.add(new String[] {"Usernumber", "Distance Covered", "Distance Covered as Carrier"});
		for (int i = 0; i < getUsers().size(); i++) {
			User u = getUsers().get(i);
			users.add(new String[] {"User " + String.valueOf(u.getUsernumber()), String.valueOf(u.getCoveredDistance()), String.valueOf(u.getCoveredDistanceasCarrier())});
		}
		logger.write(users);
		
		// write all delivered pakets into the textfile
		ArrayList<String[]> pakets = new ArrayList<String[]>();
		pakets.add(new String[] {""});
		pakets.add(new String[] {"Delivered Pakets:"});
		pakets.add(new String[] {"Paketnumber", "Handovers", "Delivery Time", "Distance Covered", "Shortest Distance to Destination"});
		for (int i = 0; i < getDeliveredpakets().size(); i++) {
			Paket p = getDeliveredpakets().get(i);
			double shortdistance = 0;
			ArrayList<SimNode> path = new ArrayList<SimNode>();
			path = map.getPath(map.getNode(p.getStartingposition()), map.getNode(p.getDestination()));
			if (path == null || path.size() == 0) {
				shortdistance = 0;
			}
			else {
				shortdistance += p.getStartingposition().getDistance(path.get(0).getPosition());
				for (int i2 = 1; i2 < path.size(); i2++) {
					SimNode n = path.get(i2);
					shortdistance += path.get(i2-1).getPosition().getDistance(n.getPosition());
				}
			}
			pakets.add(new String[] {"Paket " + String.valueOf(p.getPaketnumber()), String.valueOf(p.getHandovers()), String.valueOf(p.getDeliverytimestamp()), String.valueOf(p.getCoveredDistance()), String.valueOf(shortdistance)});
		}
		logger.write(pakets);
		
		// calculate some final statistics
		int averagehandovers = 0;
        long averagedeliverytime = 0;
        double averagedistancecovered = 0;
        for (int i = 0; i < getDeliveredpakets().size(); i++) {
        	Paket p = getDeliveredpakets().get(i);
        	averagehandovers += p.getHandovers();
        	averagedeliverytime += p.getDeliverytimestamp();
        	averagedistancecovered += p.getCoveredDistance();
        }
		
        // add to CSV-logging
        ArrayList<String[]> finalStats = new ArrayList<String[]>();
		finalStats.add(new String[] {""});
		finalStats.add(new String[] {"Final Statistics:"});
		finalStats.add(new String[] {"Delivered Pakets: ", String.valueOf(getDeliveredpakets().size())});
		finalStats.add(new String[] {"Undelivered Pakets: ", String.valueOf(getUnDeliveredpakets().size())});
		finalStats.add(new String[] {"Average Handovers: ", String.valueOf((averagehandovers / getDeliveredpakets().size()))});
		finalStats.add(new String[] {"Average Delivery Time: ", String.valueOf((averagedeliverytime / getDeliveredpakets().size()))});
		finalStats.add(new String[] {"Average Distance Covered: ", String.valueOf((averagedistancecovered / getDeliveredpakets().size()))});
		logger.write(finalStats);
		
		// console-version
        System.out.println("Paket stats:");
        System.out.println("Undelivered Pakets: " + getUnDeliveredpakets().size());
        System.out.println("Average Handovers: " + (averagehandovers / getDeliveredpakets().size()));
        System.out.println("Average Delivery Time: " + (averagedeliverytime / getDeliveredpakets().size()));
        System.out.println("Average delivered Paket Distance Covered: " + (averagedistancecovered / getDeliveredpakets().size()) + "m");
	}

	
	// getters and setters
	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public int getSteptime() {
		return steptime;
	}

	public void setSteptime(int steptime) {
		this.steptime = steptime;
	}

	public int getBluetoothrange() {
		return bluetoothrange;
	}
	
	public void setBluetoothrange(int bluetoothrange) {
		this.bluetoothrange = bluetoothrange;
	}
	
	public long getCycles() {
		return cycles;
	}

	public int getMovementalgorithm() {
		return movementalgorithm;
	}

	public void setMovementalgorithm(int movementalgorithm) {
		this.movementalgorithm = movementalgorithm;
	}

	public int getTradeDelay() {
		return tradeDelay;
	}

	public void setTradeDelay(int tradeDelay) {
		this.tradeDelay = tradeDelay;
	}
	
	public ArrayList<User> getUsers() {
		return users;
	}
	
	public ArrayList<Paket> getUnDeliveredpakets() {
		return pakets;
	}
	
	public ArrayList<Paket> getDeliveredpakets() {
		return deliveredpakets;
	}

	public double getActualspeed() {
		return actualspeed;
	}

	public void setActualspeed(double actualspeed) {
		this.actualspeed = actualspeed;
	}

	public SimMap getMap() {
		return map;
	}

	public void setMap(SimMap map) {
		this.map = map;
	}


	public ArrayList<SimNode> getClusternodes() {
		return clusternodes;
	}
	
	public void addClusternode(SimNode n) {
		clusternodes.add(n);
	}
}
