package de.postsim.Objects;

/**
 * Coordinate object used to store the position of an object
 * @author Nils Stahlhut
 *
 */
public class Coordinate {
	
	private double x;							// x value or latitude
	private double y;							// y value or longitude
	private final double radius = 6378388;		// radius of the earth
	
	/**
	 * Standard Constructor for a Coordinate. A point in 2-dimensional space with x (lat) and y-value (lon)
	 * @param x the x-value
	 * @param y the y-value
	 */
	public Coordinate(double x, double y){
		setX(x);
		setY(y);
	}
	
	
	/**
	 * Gives you the direct distance between two lat/lon Coordinates in meter (not tested for negative values)
	 * @param target
	 * @return
	 */
	public double getDistance(Coordinate target) {
		Double result;
		result = radius * Math.acos(Math.sin(Math.toRadians(this.x)) * Math.sin(Math.toRadians(target.getX())) 
						+ Math.cos(Math.toRadians(this.x)) * Math.cos(Math.toRadians(target.getX())) 
						* Math.cos(Math.toRadians(this.y) - Math.toRadians(target.getY())));
		
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
		String s = "(" + getX() + " | " + getY() + ")";
		return s;
	}
	
	// getters and setters
	
	public double getX() {
		return x;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public double getY() {
		return y;
	}
	
	public void setY(double y) {
		this.y = y;
	}
}
