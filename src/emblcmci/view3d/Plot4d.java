package emblcmci.view3d;

import ij.IJ;
import ij3d.Content;
import ij3d.ContentCreator;
import ij3d.Image3DUniverse;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import org.apache.commons.math.geometry.euclidean.threed.Vector3D;

import customnode.CustomLineMesh;
import customnode.CustomMultiMesh;
import customnode.CustomTriangleMesh;
import customnode.Mesh_Maker;

import util.opencsv.CSVReader;

public class Plot4d {
	
	private Image3DUniverse univ;
	private ArrayList<TrajectoryObj> trajlist;

	public Plot4d(){
	}
	
	public Plot4d(Image3DUniverse univ){
		this.univ = univ;
	}

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
		this.trajlist = trajlist;
		return trajlist;
	}
	/** Loads coordinates of segmented particles.  
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
	/**
	 * check if a time point is included in the trajectory. 
	 * @param timepoints
	 */
	public boolean CheckTimePointExists(double thistimepoint, ArrayList<Double> timepoints){
		boolean includesthistime = false;
		if ((timepoints.get(0) <= thistimepoint) && (timepoints.get(timepoints.size()-1) >= thistimepoint)){
			includesthistime = true;
		}
		return includesthistime;
	}

	/** a method for retrieving index within trajectory object list. 
	 * 
	 * @param srctime
	 * @param timepoints
	 * @return index of specified srctime within the Arraylist timepoints. 
	 */
	public int ReturnIndexFromTime(double srctime, ArrayList<Double> timepoints){
		int index = -1;
		for (int i = 0; i < timepoints.size(); i++){
			if (srctime == timepoints.get(i))
				index = i;
		}
		return index;
	}

	/** progressive track plotting using tubes
	 * time points are color-coded
	 * 
	 * 20111219 plot color coded track using tube-mesh
	 * 20111220 updated, adding progressive tracks.
	 * 20120108 converted to java 
	 * progressive version
	 */
//	public void PlotTimeColorCodedTrack(int timestart, int timeend, ArrayList<TrajectoryObj> tList, Image3DUniverse univ){
	public void PlotTimeColorCodedTrack(int timestart, int timeend, ArrayList<TrajectoryObj> tList){
		int i, j;
		ArrayList<CustomMultiMesh> multiMeshA = new ArrayList<CustomMultiMesh>();
		for (i = timestart; i < timeend-1; i++){
			multiMeshA.add(new CustomMultiMesh());	
		}
		for (i = timestart; i < timeend-1; i++){
			ArrayList<Point3f> tubes = new ArrayList<Point3f>();
			for (j = 0; j < tList.size(); j++) {
				TrajectoryObj curtraj = tList.get(j);
				if (CheckTimePointExists(i, curtraj.timepoints) && CheckTimePointExists(i+1, curtraj.timepoints)){
					//var dt = curtraj.dotList;
					//ArrayList pathextract = new ArrayList();
					//pathextract.addAll(curtraj.dotList); //required in javascript
					//ArrayList timeextract = new ArrayList();
					//timeextract.addAll(curtraj.timepoints); //required in javascript as well
					int ind = curtraj.timepoints.indexOf(i);
					Point3f spoint = curtraj.dotList.get(ind);
					Point3f epoint = curtraj.dotList.get(ind+1);			
					double[] xA = {spoint.x, epoint.x};
					double[] yA = {spoint.y, epoint.y};
					double[] zA = {spoint.z, epoint.z};
					double[] rA = {0.2, 0.2};					
					List<Point3f> tube = Mesh_Maker.createTube(xA, yA, zA, rA, 24, false); //true makes cone shape
					tubes.addAll(tube); 
					IJ.log("index"+j + " frame" + i);
				}
			}
			float cR = i/(timeend -1 - timestart);
			float cB = 1 - cR; 
			//adding progressive tracks to custommultimesh
			for (j = i-timestart; j<timeend-timestart-1; j++ ){
				CustomTriangleMesh csp = new CustomTriangleMesh(tubes, new Color3f(cR, (float) 0.6, cB), (float) 0.0);
				multiMeshA.get(j).add(csp);
			}
		}
		//var ccs = ContentCreator.createContent(csp, "tubetime" + Integer.toString(i), i-timestart+1);
		//var ccs = ContentCreator.createContent(csp, "tubetime" + Integer.toString(i), 0);
		for (i = 0; i < multiMeshA.size(); i++){
			Content ccs = ContentCreator.createContent(multiMeshA.get(i), "tubetime" + Integer.toString(i), i+1);
			univ.addContent(ccs);
		}		
	}

	//above function modified, so that uses line instead of tubes (memory saving)
	//progressive version
	
	/** progressive track plotting using lines
	 * time points are color-coded
	 * less-memory usage than tube version (above)
	 * 
	 */
	public void PlotTimeColorCodedLine(int timestart, int timeend, ArrayList<TrajectoryObj> tList){
		int i, j, k;
		ArrayList<CustomMultiMesh> multiMeshA = new ArrayList<CustomMultiMesh>();
		for (i = timestart; i < timeend-1; i++){
			multiMeshA.add(new CustomMultiMesh());	
			ArrayList<List> tubes = new ArrayList<List>();
			float cR = i/(timeend -1 - timestart);
			float cB = 1 - cR;
			for (j = 0; j < tList.size(); j++) {
				TrajectoryObj curtraj = tList.get(j);
				if (CheckTimePointExists(i, curtraj.timepoints) && CheckTimePointExists(i+1, curtraj.timepoints)){
					int ind = curtraj.timepoints.indexOf(i);										
					tubes.add(curtraj.dotList.subList(ind, ind+2)); //add to addAll in java
					IJ.log("index"+j + " frame" + i);
				}
			}
	 
			//adding progressive tracks to custommultimesh
			for (j = i-timestart; j<timeend-timestart-1; j++ ){
				for (k = 0; k < tubes.size(); k++){
					CustomLineMesh clm = new CustomLineMesh(tubes.get(k), CustomLineMesh.CONTINUOUS, new Color3f(cR, (float) 0.6, cB), 0);
					multiMeshA.get(j).add(clm);
				}
			}
		}
		for (i = 0; i < multiMeshA.size(); i++){
			Content ccs = ContentCreator.createContent(multiMeshA.get(i), "tubetime" + Integer.toString(i), i+1);
			univ.addContent(ccs);
		}		
	}

	/** track plotting using lines, only the last frame. 
	 * time points are color-coded
	 * no timeseries. 
	 * 
	 */
	public void PlotTimeColorCodedLineOnlyFinalFrame(int timestart, int timeend, ArrayList<TrajectoryObj> tList){
		int i, j, k;
		CustomMultiMesh LineMultiMesh = new CustomMultiMesh();
		//CustomMultiMesh clmmProLine = new CustomMultiMesh();
		for (i = timestart; i < timeend-1; i++){
			ArrayList<List> tubes = new ArrayList<List>();
			float cR = i/(timeend -1 - timestart);
			float cB = 1 - cR;				
			for (j = 0; j < tList.size(); j++) {
				TrajectoryObj curtraj = tList.get(j);
				if (CheckTimePointExists(i, curtraj.timepoints) && CheckTimePointExists(i+1, curtraj.timepoints)){
					int ind = curtraj.timepoints.indexOf(i);
					tubes.add(curtraj.dotList.subList(ind, ind+2));
					IJ.log("index"+j + " frame" + i);
				}
			}
			//adding progressive tracks to custommultimesh
			for (k = 0; k < tubes.size(); k++){
				CustomLineMesh clm = new CustomLineMesh(tubes.get(k), CustomLineMesh.CONTINUOUS, new Color3f(cR, (float) 0.6, cB), (float) 0.4);
				LineMultiMesh.add(clm);
			}

		}
		Content ccs = ContentCreator.createContent(LineMultiMesh, "colorcodedTracks", 0);
		univ.addContent(ccs);		
	}
	
	//spheres from trajectories 20111216
	public void plotTrajectorySpheres(int timestart, int timeend, ArrayList<TrajectoryObj> tList){
		int i, j;
		for (i = timestart; i < timeend; i++){
			ArrayList spheres = new ArrayList();
			for (j = 0; j < tList.size(); j++) {
				TrajectoryObj curtraj = tList.get(j);
				if (CheckTimePointExists(i, curtraj.timepoints)){
					IJ.log(Double.toString(curtraj.id));
					int ind = curtraj.timepoints.indexOf(i);
					Point3f p3f = curtraj.dotList.get(ind);
					double curtime = curtraj.timepoints.get(ind);
					List<Point3f> sphere = Mesh_Maker.createSphere(p3f.x, p3f.y, p3f.z, 2.0, 24, 24);
					spheres.addAll(sphere);
				}
			}
			CustomTriangleMesh csp = new CustomTriangleMesh(spheres, new Color3f(1.0f, 1.0f, 1.0f), 0.0f);
			Content ccs = ContentCreator.createContent(csp, "dotstime" + Integer.toString(i), i-timestart);
			univ.addContent(ccs);
		}
	}

	//spheres from dotlists 20111216
	// direct plotting of segmented dots, without tracking
	public void plotSpheresFromDotLists(int timestart, int timeend, ArrayList<DotObj> pList){
		int i, j;
		for (i = timestart; i < timeend; i++){
	    ArrayList spheres = new ArrayList();
	    for (j = 0; j < pList.size(); j++) {
	    	DotObj curdot = pList.get(j);
	      if (curdot.getFrame() -1  == i){
	        double factor = Math.round(curdot.getMeanint()/200 * 24);
	        //var sphere = Mesh_Maker.createSphere(curdot.sx, curdot.sy, curdot.sz, 0.7, factor, factor);
	        List<Point3f>  sphere = Mesh_Maker.createSphere(curdot.sx, curdot.sy, curdot.sz, 0.7, 12, 12);

	        spheres.addAll(sphere);
	        IJ.log("index"+j + " frame" + i + " intfactor:"+factor);
	      }
	    }
	    CustomTriangleMesh csp = new CustomTriangleMesh(spheres, new Color3f(1.0f, 1.0f, 1.0f), 0.0f);
	    Content ccs = ContentCreator.createContent(csp, "dotstime" + Integer.toString(i), i-timestart);
	    univ.addContent(ccs);
	  }
	}

	// plots net displacement vector towards a reference point 
	public void plotTrackDisplacements(int timestart, int timeend, ArrayList<TrajectoryObj> tList, double rx, double ry, double rz){
		int i, j;
		ArrayList<Double> dispA = new ArrayList<Double>(); //displacements array
		ArrayList vecs = new ArrayList();
		ArrayList dispvecs = new ArrayList();
		ArrayList<Point3f> startPoints = new ArrayList<Point3f>();
		ArrayList<Integer> awaytowardsA = new ArrayList<Integer>();
		for (j = 0; j < tList.size(); j++) {
			ArrayList cvec =  new ArrayList();
			ArrayList dvec =  new ArrayList();
			TrajectoryObj curtraj = tList.get(j);

			Point3f spoint = curtraj.dotList.get(0);
			Point3f epoint = curtraj.dotList.get(curtraj.dotList.size()-1);
			Vector3D srv = new Vector3D(rx - spoint.x, ry - spoint.y, rz - spoint.z); //startpoint to reference point vector
			Vector3D sev = new Vector3D(epoint.x - spoint.x, epoint.y - spoint.y, epoint.z - spoint.z); //startpoint to reference point vector
			double theta = Vector3D.angle(srv, sev);
			Vector3D srvDispv = srv.normalize().scalarMultiply(Math.cos(theta)* sev.getNorm());
			double displacement = srvDispv.getNorm();
			if (Math.cos(theta) < 0) {
				displacement *= -1;
				awaytowardsA.add(-1); //away 
			} else
				awaytowardsA.add(1); //towards 		
			dispA.add(displacement);
			if (j == 0) IJ.log("id\t" + "theta\t" + "CosTheta\t" + "displacement");
			IJ.log("" +j + "\t" + theta + "\t" + Math.cos(theta) + "\t" + displacement);
			cvec.add(spoint);
			cvec.add(epoint);
			vecs.add(cvec);
			startPoints.add(spoint);

			//displacement vector along reference axis
			dvec.add(spoint);
			dvec.add(new Point3f(
					((float) (spoint.x + srvDispv.getX())), 
					((float) (spoint.y + srvDispv.getY())), 
					((float) (spoint.z + srvDispv.getZ())))
			);
			dispvecs.add(dvec);
			
		}
//		var minval = Math.min.apply(Math, dispA);
		Object minvalobj = Collections.min(dispA);
//		maxval = Math.max.apply(Math, dispA);
		Object maxvalobj = Collections.max(dispA);
		IJ.log("Max displacement" + maxvalobj);
		IJ.log("Min displacement" + minvalobj);
		double minval = Double.valueOf((String) minvalobj);
		double maxval = Double.valueOf((String) maxvalobj);		
		double maxdisp = maxval;
		if (Math.abs(maxval) < Math.abs(minval))  
			maxdisp = minval;
		else
			maxdisp = maxval;
		float cR = 0;
		float cG = 0;
		float cB = 0;
		CustomMultiMesh clmmProLine = new CustomMultiMesh();
		CustomMultiMesh clmmDispLine = new CustomMultiMesh();
		ArrayList spheres = new ArrayList();
		for (j = 0; j < vecs.size(); j++){	
			cR =0; cG = 0.6f; cB = 0;
			if (dispA.get(j) > 0)
				cR = (float) (dispA.get(j)/maxdisp);
			else
				cB = (float) (Math.abs(dispA.get(j))/maxdisp);
				
			CustomLineMesh clm = new CustomLineMesh((List<Point3f>) vecs.get(j), CustomLineMesh.CONTINUOUS, new Color3f(cR, cG, cB), 0);
			clmmProLine.add(clm);
			clm.setLineWidth(2);
			
			Color3f dispcol;
			if (awaytowardsA.get(j) > 0.0f)
				dispcol = new Color3f(1f,0f,0f);
			else			
				dispcol = new Color3f(0f,0f,1f);
			CustomLineMesh clmdisp = new CustomLineMesh((List<Point3f>) dispvecs.get(j), CustomLineMesh.CONTINUOUS, dispcol, 0);
			clmmDispLine.add(clmdisp);
			
			List<Point3f> sphere = Mesh_Maker.createSphere(
					(double) startPoints.get(j).x, 
					(double) startPoints.get(j).y, 
					(double) startPoints.get(j).z, 
					0.7, 12, 12);
			spheres.addAll(sphere);		
		}
		//cc = ContentCreator.createContent(clmmProLine, "displacements" + Integer.toString(i), i-timestart);
		Content cc = ContentCreator.createContent(clmmProLine, "displacements", 0);	
		univ.addContent(cc);

		Content cc2 = ContentCreator.createContent(clmmDispLine, "displacementsAxis", 0);	
		univ.addContent(cc2);
			
		CustomTriangleMesh csp = new CustomTriangleMesh(spheres, new Color3f(1,1,1), 0.0f);
		Content ccs = ContentCreator.createContent(csp, "startpoints", 0);
		univ.addContent(ccs);
		
		//create reference point
		List<Point3f>  referencepoint = Mesh_Maker.createSphere(rx, ry, rz, 2, 12, 12);
		CustomTriangleMesh refmesh = new CustomTriangleMesh(referencepoint, new Color3f(1,0,0), 0.0f);
		Content refcont = ContentCreator.createContent(refmesh, "referencePoint", 0);
		univ.addContent(refcont);
		
	}	
	
	
	
}
