package de.postsim.Objects;

/**
 * Coordinate object used to store the position of an object
 * @author Nils Stahlhut
 *
 */
public class Coordinate {
	
	private double lat;							// x value or latitude
	private double lon;							// y value or longitude

	/**
	 * Standard Constructor for a Coordinate. A point in 2-dimensional space with x (lat) and y-value (lon)
	 * @param lat the x-value
	 * @param lon the y-value
	 */
	public Coordinate(double lat, double lon){
		setLat(lat);
		setLon(lon);
	}
	


	/**
	 * Gives you the direct distance between two lat/lon Coordinates in meter (not tested for negative values)
	 * @param target
	 * @return
	 */
	public double getDistance(Coordinate target) {
		Double result;
		double radius = 6378388;
		result = radius * Math.acos(Math.sin(Math.toRadians(this.lat)) * Math.sin(Math.toRadians(target.getLat()))
						+ Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(target.getLat()))
						* Math.cos(Math.toRadians(this.lon) - Math.toRadians(target.getLon())));
		
		// if numbers get sufficiently close to each other or the coordinates are actually the same the result is NAN, when it should be 0 (or very close to it) 
		if (result.isNaN()) {
			return 0;
		}
		return result;
	}
	
	/**
	 * overrides java.lang.Object.toString
	 */
	public String toString() {
		String s = "(" + getLat() + " | " + getLon() + ")";
		return s;
	}
	
	// getters and setters
	
	public double getLat() {
		return lat;
	}
	
	public void setLat(double lat) {
		this.lat = lat;
	}
	
	public double getLon() {
		return lon;
	}
	
	public void setLon(double lon) {
		this.lon = lon;
	}


    /**
	 * Project this coordinate onto the given coordinate (into its direction) using the scalar to determine the length. I.e. scalar=.5 means "half the way".
     * FIXME: verify coordinate math
     * FIXME: handle wrap-around and negative coordinates
     * @param b
     * @param scalar
	 */
	public void project(Coordinate b, float scalar) {
		Coordinate a = this;
		a.lat = a.lat +  scalar*(b.lat -a.lat);
		a.lon = a.lon +  scalar*(b.lon -a.lon);

	}


}
