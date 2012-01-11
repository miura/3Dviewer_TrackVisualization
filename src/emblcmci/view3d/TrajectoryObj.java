package emblcmci.view3d;

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
import javax.vecmath.Point3f;

public class TrajectoryObj {
	double id;
	ArrayList<Point3f> dotList;
	ArrayList<Integer> timepoints;
	
	public TrajectoryObj(double id, ArrayList<Point3f> dotList, ArrayList<Integer> timepoints){
		this.id = id;
		this.dotList = dotList;
		this.timepoints = timepoints; //a vector with time points of the trajectory. 

	}
}
