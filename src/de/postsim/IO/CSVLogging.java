package de.postsim.IO;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.opencsv.CSVWriter;

/**
 * Class used to write strings into a CSV-file
 * @author Nils Stahlhut
 *
 */
public class CSVLogging  {
	
	String csv;
	
	/**
	 * creates a CSVLogging object which can write into the specified filename
	 * @param filename
	 */
	public CSVLogging(String filename) {
		this.csv = filename;
		ArrayList<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { "Cycle", "Object1", "Object2", "Position", "Event" });
		data.add(new String[] { "0", "", "", "", "Simulation initialised" });
		write(data);
	}
	
	
	/**
	 * write the data into the CSV file
	 * @param data
	 */
	public void write(ArrayList<String[]> data){	
		try {
			FileWriter file;
			file = new FileWriter(csv, true);
			CSVWriter writer = new CSVWriter(file);
			writer.writeAll(data);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
