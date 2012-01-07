package emblcmci.view3d;

import ij.IJ;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Point3f;

import util.opencsv.CSVReader;

public class Plot4d {

	//@SuppressWarnings("unchecked")
	public ArrayList<TrajectoryObj> loadFileVolocity(String datapath){

		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(datapath), ',');
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			IJ.log("file access failed");
			e.printStackTrace();
		}
		List<String[]> ls = null;
		try {
			ls = reader.readAll();
		} catch (IOException e) {
			IJ.log("file reading failed");
			e.printStackTrace();
		}
		Iterator<String[]> it = ls.iterator();
		int counter = 0;
		double currentTrajID = 1;
		ArrayList<Point3f> atraj = new ArrayList<Point3f>();
		ArrayList<Double> timepoints = new ArrayList<Double>();
		ArrayList<TrajectoryObj> trajlist = new ArrayList<TrajectoryObj>();
		while (it.hasNext()){
			String[] cA = it.next();
			if (counter != 0){
				if ((currentTrajID - Double.valueOf(cA[1]) != 0) && (atraj.size() > 0)){
					//IJ.log(Double.toString(currentTrajID) + cA[1]);
					TrajectoryObj atrajObj = new TrajectoryObj(currentTrajID, atraj, timepoints);
					trajlist.add(atrajObj);
					currentTrajID = Double.valueOf(cA[1]);
					//cvec.clear();
					atraj = new ArrayList<Point3f>();
					timepoints = new ArrayList<Double>();
				}
				// pixel positions
	 			//cvec.add(Point3f(Double.valueOf(cA[3]),Double.valueOf(cA[4]),Double.valueOf(cA[5])));
	 			// scaled positions
	 			atraj.add(new Point3f(Float.valueOf(cA[6]),Float.valueOf(cA[7]),Float.valueOf(cA[8]))); 
	 			timepoints.add(Double.valueOf(cA[2]));  
			}
			counter++;
		}
		return trajlist;
	}
	/* Loads coordinates of segmented particles.  
	 * 
	 * @param datapath full path to the csv file
	 * 
	 */
	public ArrayList<DotObj> loadPointsFile(String datapath){

		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(datapath), ',');
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			IJ.log("file access failed");
			e.printStackTrace();
		}
		List<String[]> ls = null;
		try {
			ls = reader.readAll();
		} catch (IOException e) {
			IJ.log("file reading failed");
			e.printStackTrace();
		}
		Iterator<String[]> it = ls.iterator();
		int counter = 0;
		double currentTrajID = 1.0;
		ArrayList<DotObj> coords = new ArrayList<DotObj>();
		while (it.hasNext()){
			String[] cA = it.next();
			if (counter != 0){
				double pf = Double.valueOf(cA[2]);
				double pmeanint = Double.valueOf(cA[7]);
				double px = Double.valueOf(cA[10]);
				double py = Double.valueOf(cA[11]);
				double pz = Double.valueOf(cA[12]);
				double sx = Double.valueOf(cA[13]);
				double sy = Double.valueOf(cA[14]);
				double sz = Double.valueOf(cA[15]);											
				DotObj dotObj = new DotObj(pf, px, py, pz, sx, sy, sz, pmeanint);
				coords.add(dotObj);
			}
			counter++;
		}
		return coords;
	}	
	//check if a time point is included in the trajectory. 
	//(int, vector)
	public boolean CheckTimePointExists(double thistimepoint, ArrayList<Double> timepoints){
		boolean includesthistime = false;
		if ((timepoints.get(0) <= thistimepoint) && (timepoints.get(timepoints.size()-1) >= thistimepoint)){
			includesthistime = true;
		}
		return includesthistime;
	}

	public int ReturnIndexFromTime(double srctime, ArrayList<Double> timepoints){
		int index = -1;
		for (int i = 0; i < timepoints.size(); i++){
			if (srctime == timepoints.get(i))
				index = i;
		}
		return index;
	}
	
}
