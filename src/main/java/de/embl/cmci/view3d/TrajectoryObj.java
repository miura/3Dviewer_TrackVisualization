package de.embl.cmci.view3d;

/** An object for single trajectory. 
 *  text data of trajectories are converted to an ArrayList of TrajectoryObj.
 *  
 *  id: trajectory id. 
 *  dotList: an ArrayList with series of coordinates, representing a track
 *  timepoints: an ArrayList of frame number containing corresponding frame numbers. 
 *  20120106
 *  @author miura
 */
import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

public class TrajectoryObj {
	double id;
	ArrayList<Point3f> dotList;
	ArrayList<Integer> timepoints;
	
	//to define track colors using data (MTrackJ output)
	Color3f color;
	boolean useDefinedColor = false;
	
	public TrajectoryObj(double id, ArrayList<Point3f> dotList, ArrayList<Integer> timepoints){
		this.id = id;
		this.dotList = dotList;
		this.timepoints = timepoints; //a vector with time points of the trajectory. 
		this.color = null;
	}

	public ArrayList<Point3f> getDotList() {
		return dotList;
	}

	public ArrayList<Integer> getTimepoints() {
		return timepoints;
	}
	
	public void setColor(Color3f color){
		this.color = color;
	}
}
