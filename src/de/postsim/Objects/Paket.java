package de.postsim.Objects;

/**
 * paket object, that represents Pakets in the simulation
 * @author Nils Stahlhut
 *
 */
public class Paket {

	private Coordinate position;				// position of the paket
	private Coordinate startingposition;		// position, where the paket started
	private Coordinate destination;				// destination of the paket
	private int paketnumber;					// id
	private int handovers = 0;					// amount of handovers between carriers for the paket
	private double coveredDistance = 0;			// distance covered, while being carried
	private User carrier = null;				// User-object that is the current carrier
	private boolean delivered; 					// is the paket delivered or not
	private long deliverytimestamp;				// timestamp in cycles since deliverystart

	/**
	 * Standard Constructor for a Paket. Gives it an id, a starting position and a destination
	 * @param startPosition
	 * @param destination
	 * @param id
	 */
	public Paket(Coordinate startPosition, Coordinate destination, int id) {
		setPosition(startPosition);
		setStartingposition(new Coordinate (startPosition.getX(), startPosition.getY()));
		setDestination(destination);
		setPaketnumber(id);
	}
	
	/**
	 * overrides java.lang.Object.toString
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String s = "Paket " + paketnumber + "\n" + "Position: " + position.toString() + "\n" + "Ziel: " + destination.toString() + "\n";
		sb.append(s);
		if (carrier != null){
			String s2 = "Carrier: User " + carrier.getUsernumber() + "\n";
			sb.append(s2);
		}
		sb.append("Handovers: " + handovers);
		sb.append("Distance: " + coveredDistance);
		return sb.toString();
	}
	
	// getters and setters

	public Coordinate getDestination() {
		return destination;
	}

	public void setDestination(Coordinate destination) {
		this.destination = destination;
	}

	public int getHandovers() {
		return handovers;
	}

	public void setHandovers(int handovers) {
		this.handovers = handovers;
	}

	public double getCoveredDistance() {
		return coveredDistance;
	}

	public void setCoveredDistance(double coveredDistance) {
		this.coveredDistance = coveredDistance;
	}

	public Coordinate getPosition() {
		return position;
	}

	public void setPosition(Coordinate position) {
		this.position = position;
	}

	public User getCarrier() {
		return carrier;
	}

	public void setCarrier(User carrier) {
		this.carrier = carrier;
	}

	public boolean isDelivered() {
		return delivered;
	}

	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}

	public int getPaketnumber() {
		return paketnumber;
	}

	public void setPaketnumber(int paketnumber) {
		this.paketnumber = paketnumber;
	}

	public long getDeliverytimestamp() {
		return deliverytimestamp;
	}

	public void setDeliverytimestamp(long deliverytimestamp) {
		this.deliverytimestamp = deliverytimestamp;
	}

	public Coordinate getStartingposition() {
		return startingposition;
	}

	public void setStartingposition(Coordinate startingposition) {
		this.startingposition = startingposition;
	}
}
