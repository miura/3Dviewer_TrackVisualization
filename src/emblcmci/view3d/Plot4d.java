package emblcmci.view3d;

import ij.IJ;
import ij3d.Content;
import ij3d.ContentCreator;
import ij3d.Image3DUniverse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import org.apache.commons.math.geometry.euclidean.threed.Vector3D;
import util.opencsv.CSVReader;
import util.opencsv.CSVWriter;
import customnode.CustomLineMesh;
import customnode.CustomMultiMesh;
import customnode.CustomTriangleMesh;
import customnode.Mesh_Maker;


public class Plot4d {
	
	private Image3DUniverse univ;
	private ArrayList<TrajectoryObj> trajlist; //trajectory coordinates
	private ArrayList<DotObj> coords;	//coordinates of segmented dots. "framewise" 

	public static final int DATATYPE_VOLOCITY = 0;
	
	public Plot4d(){
	}
	
	public Plot4d(Image3DUniverse univ){
		this.univ = univ;
	}
	/** No visualization, for access from scripts directly
	 * 
	 * @param datapath
	 */
	public Plot4d(String datapath, int datatype){
		if (datatype == 0){
			this.trajlist = loadFileVolocity(datapath);
		}
	}
			
	public ArrayList<TrajectoryObj> loadFileVolocity(String datapath){

		File testaccess = new File(datapath);
		if (!testaccess.exists()){
			IJ.log("The file does not exists");
			return null;
		}
		testaccess = null;
		
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(datapath), ',');
		} catch (FileNotFoundException e) {
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
		ArrayList<Integer> timepoints = new ArrayList<Integer>();
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
					timepoints = new ArrayList<Integer>();
				}
				// pixel positions
	 			//cvec.add(Point3f(Double.valueOf(cA[3]),Double.valueOf(cA[4]),Double.valueOf(cA[5])));
	 			// scaled positions
	 			atraj.add(new Point3f(Float.valueOf(cA[6]),Float.valueOf(cA[7]),Float.valueOf(cA[8]))); 
	 			timepoints.add((int) (Double.valueOf(cA[2]).intValue()));  
			}
			counter++;
		}
		this.trajlist = trajlist;
		IJ.log("file loaded successfully");
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
		this.coords = coords;
		return coords;
	}	
	/**
	 * check if a time point is included in the trajectory. 
	 * @param timepoints
	 */
	public boolean CheckTimePointExists(int thistimepoint, ArrayList<Integer> timepoints){
		boolean includesthistime = false;
		if (timepoints != null){
			if ((timepoints.get(0) <= thistimepoint) && (timepoints.get(timepoints.size()-1) >= thistimepoint)){
				includesthistime = true;
			}
		} else {
			IJ.log("timepoints array is null");
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
	 * @return 
	 * 
	 */
	public ArrayList<Content> PlotTimeColorCodedLine(int timestart, int timeend, ArrayList<TrajectoryObj> tList){
		int i, j, k;
		ArrayList<CustomMultiMesh> multiMeshA = new ArrayList<CustomMultiMesh>();
		for (i = timestart; i < timeend-1; i++)
				multiMeshA.add(new CustomMultiMesh());
		for (i = timestart; i < timeend-1; i++){
			//multiMeshA.add(new CustomMultiMesh());	
			ArrayList<List> tubes = new ArrayList<List>();
			float cR = ((float) (i))/((float)(timeend -1 - timestart));
			float cB = 1 - cR;
			for (TrajectoryObj curtraj : tList) {
				if (CheckTimePointExists(i, curtraj.timepoints) && CheckTimePointExists(i+1, curtraj.timepoints)){
					int ind = curtraj.timepoints.indexOf(i);										
					tubes.add(curtraj.dotList.subList(0, ind+1)); 
				}
			}	 
			//adding progressive tracks to custommultimesh
			for (j = i-timestart; j<timeend-timestart-1; j++ ){
				for (k = 0; k < tubes.size(); k++){
					CustomLineMesh clm = new CustomLineMesh(tubes.get(k), CustomLineMesh.CONTINUOUS, new Color3f(cR, 0.6f, cB), 0);
					multiMeshA.get(j).add(clm);
					//IJ.log("frame" + j + "added tube" + k);
				}
			}
		}
		ArrayList<Content> meshcontents = new ArrayList<Content>();
//		for (i = 1; i < 4; i++){
		for (i = 0; i < multiMeshA.size(); i++){
			Content ccs = ContentCreator.createContent(multiMeshA.get(i), "tubetime" + Integer.toString(i), i);
//			Content ccs = ContentCreator.createContent(multiMeshA.get(i), "tubetime" + Integer.toString(i), i+1);
			IJ.log("frame " + i + " be added to the universe");
			IJ.log("...number of lines" + multiMeshA.get(i).size());
			//univ.addContent(ccs);
			meshcontents.add(ccs);
			//univ.addContentLater(ccs);
		}
		univ.addContentLater(meshcontents);
		for (Content item : meshcontents)
			item.setLocked(true);
		return meshcontents;
	}

	/** Plots tracks in static way (track in a single frame) 
	 * ... uses lineMesh for speed. 
	 * time points are color-coded.
	 *  
	 * @return track contents.  
	 * 
	 */
	public Content PlotTimeColorCodedLineOnlyFinalFrame(int timestart, int timeend, ArrayList<TrajectoryObj> tList){
		int i, ind;
		Color3f colornow;
		float cR, cB;
		CustomMultiMesh LineMultiMesh = new CustomMultiMesh();
		//CustomMultiMesh clmmProLine = new CustomMultiMesh();
		for (i = timestart; i < timeend-1; i++){
			cR = ((float) (i))/((float)(timeend -1 - timestart));
			cB = 1.0f - cR;
			colornow = new Color3f(cR, 0.6f, cB);
			for (TrajectoryObj curtraj : tList) {
				if (CheckTimePointExists(i, curtraj.timepoints) && CheckTimePointExists(i+1, curtraj.timepoints)){
					ind = curtraj.timepoints.indexOf(i);
					CustomLineMesh clm = new CustomLineMesh(curtraj.dotList.subList(ind, ind+2), CustomLineMesh.CONTINUOUS, colornow, 0.4f);
					LineMultiMesh.add(clm);
				}
			}
			IJ.log("frame " + i + " plotted");
		}
		Content ccs = ContentCreator.createContent(LineMultiMesh, "color_coded_Tracks", (int) 0);
		univ.addContent(ccs);
		ccs.setLocked(true);
		return ccs;
		
	}
	/** creates line-track content from single track. 
	 * 
	 * @param tList
	 * @param trackid
	 * @return
	 */
	public Content containSingleTrack(ArrayList<TrajectoryObj> tList, int trackid, int timepoint){
		CustomMultiMesh LineMultiMesh =createSingleTrackMesh(tList, trackid);
		Content ccs = ContentCreator.createContent(LineMultiMesh, "color_coded_Tracks", timepoint);
		//univ.addContent(ccs);
		ccs.setLocked(true);
		return ccs;		
	}
	public CustomMultiMesh createSingleTrackMesh(ArrayList<TrajectoryObj> tList, int trackid){
		int i;
		Color3f colornow;
		CustomMultiMesh LineMultiMesh = new CustomMultiMesh();
		TrajectoryObj curtraj = tList.get(trackid);
		colornow = new Color3f(1.0f, 0.6f, 1.0f);
		for (i = 0; i < curtraj.dotList.size()-1; i++){
			CustomLineMesh clm = new CustomLineMesh(curtraj.dotList.subList(i, i+2), CustomLineMesh.CONTINUOUS, colornow, 0.4f);			
			LineMultiMesh.add(clm);
		}
		return LineMultiMesh;
	}
	public CustomMultiMesh createSingleTrackMeshShifted(ArrayList<TrajectoryObj> tList, int trackid, float dx, float dy, float dz){
		int i;
		Color3f colornow;
		CustomMultiMesh LineMultiMesh = new CustomMultiMesh();
		TrajectoryObj curtraj = tList.get(trackid);
		colornow = new Color3f(1.0f, 0.6f, 1.0f);
		for (i = 0; i < curtraj.dotList.size()-1; i++){
			List<Point3f> frag = curtraj.dotList.subList(i, i+2);
			for (Point3f item : frag){
				item.x -=dx;
				item.y -=dy;
				item.z -=dz;
			}
			CustomLineMesh clm = new CustomLineMesh(frag, CustomLineMesh.CONTINUOUS, colornow, 0.4f);			
			LineMultiMesh.add(clm);
		}
		return LineMultiMesh;
	}
	
	public Content HighlightSelectedSingleTrack(ArrayList<TrajectoryObj> tList, int index){
		TrajectoryObj curtraj = tList.get(index);
		CustomLineMesh clm = new CustomLineMesh(curtraj.dotList, CustomLineMesh.CONTINUOUS, new Color3f(1, 1, 1), 0.4f);
		clm.setLineWidth(3f);
		Content ccs = ContentCreator.createContent(clm, "highlightedTrack"+Integer.toString(index), (int) 0);
		univ.addContent(ccs);
		return ccs;
	}
	
	//spheres from trajectories 20111216
	public ArrayList<Content> plotTrajectorySpheres(int timestart, int timeend, ArrayList<TrajectoryObj> tList, boolean switch3d){
		int i, j;
		ArrayList<Content> contentsList = new ArrayList<Content>();
		int nodesnum;
		for (i = timestart; i < timeend; i++){
			nodesnum = 0;
			ArrayList<Point3f> spheres = new ArrayList<Point3f>();
			for (j = 0; j < tList.size(); j++) {
				TrajectoryObj curtraj = tList.get(j);
				if (CheckTimePointExists(i, curtraj.timepoints)){
					//IJ.log("...add "+ Double.toString(curtraj.id));
					int ind = curtraj.timepoints.indexOf(i);
					Point3f p3f = curtraj.dotList.get(ind);
					double curtime = curtraj.timepoints.get(ind);
					List<Point3f> sphere = Mesh_Maker.createSphere(p3f.x, p3f.y, p3f.z, 0.5, 24, 24);
					spheres.addAll(sphere);
					nodesnum++;
				}
			}
			CustomTriangleMesh csp = new CustomTriangleMesh(spheres, new Color3f(1.0f, 1.0f, 1.0f), 0.0f);
			Content ccs;
			if (switch3d)
				ccs = ContentCreator.createContent(csp, "TrajectoryNodes" + Integer.toString(i), 0);
			else
				ccs = ContentCreator.createContent(csp, "TrajectoryNodes" + Integer.toString(i), i-timestart);
			contentsList.add(ccs);
			IJ.log("timepoint "+ Integer.toString(i) + "..." + Integer.toString(nodesnum));
		}
		univ.addContentLater(contentsList);
		for (Content item : contentsList)
			item.setLocked(true);
		return contentsList;
	}

	//spheres from dotlists 20111216
	// direct plotting of segmented dots, without tracking
	public void plotSpheresFromDotLists(int timestart, int timeend, ArrayList<DotObj> pList){
		int i, j;
		for (i = timestart; i < timeend; i++){
			ArrayList<Point3f> spheres = new ArrayList<Point3f>();
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
		lockCurrentContents(univ);
	}
	
	
	/** plots net displacement towards a reference point or bar.
	 * net displacement is caliculated from full track (compare with "Incremental" methods). 
	 * if ref arraylist containes only one, then point. if two, a line (reference bar)
	 * eventually, this could also be a surface.  
	 * 
	 * @param timestart
	 * @param timeend
	 * @param tList
	 * @param ref List of doubles, in case of reference line [r0x, r0y, r0z, r1x, r1y, r1z]
	 * @return
	 */
	public ArrayList<Content> plotTrackNetDisplacements(int timestart, int timeend, 
			ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref){
		
		ArrayList<Content> packedcontents =containTrackNetDisplacements(timestart, timeend, tList, ref);
		for (Content item : packedcontents)
			univ.addContent(item);
		lockCurrentContents(univ);	
		return packedcontents;		
	}
	public ArrayList<Content> containTrackNetDisplacements(int timestart, int timeend, 
			ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref){

		NetDisplacementResults results = netDispTotal(tList, ref);
		ArrayList<ArrayList<Point3f>> dispvecs =  results.dispVecs;
		ArrayList<Double> dispA =  results.displacements;
		ArrayList<DispVec> vecs = results.vecs;
		CustomMultiMesh clmmDispLine = new CustomMultiMesh();
		for (int i = 0; i < dispvecs.size(); i++)	{
			CustomLineMesh clmdisp = 
				new CustomLineMesh(dispvecs.get(i), CustomLineMesh.CONTINUOUS, colorCodeAwayTowards(vecs.get(i).direc), 0);
			clmmDispLine.add(clmdisp);

		}

		Content netDV = ContentCreator.createContent(clmmDispLine, "NetDisplacementVecs", 0);	
		Content startpoint_spheres = createStartPointSphereContent(timestart, tList);
		Content refcont = createReferenceContent(timestart, ref);
		
		ArrayList<Content> packedcontents = new ArrayList<Content>(); //for packaging contents
		packedcontents.add(netDV);
		packedcontents.add(startpoint_spheres);
		packedcontents.add(refcont);
		return packedcontents;	
	}
	
	public NetDisplacementResults netDispTotal(ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref){
		ArrayList<DispVec> vecs = new ArrayList<DispVec>();
		ArrayList<ArrayList<Point3f>> dispvecs = new ArrayList<ArrayList<Point3f>>();
		ArrayList<Double> dispA = new ArrayList<Double>(); //displacements array
		ArrayList<Integer> timeA = new ArrayList<Integer>();
		double displacement;
		Integer timepoint;
		for (TrajectoryObj curtraj : tList)	{
			ArrayList<Point3f> dvec =  new ArrayList<Point3f>();
			Point3f spoint = curtraj.dotList.get(0);
			Point3f epoint = curtraj.dotList.get(curtraj.dotList.size()-1);
			
			DispVec dispV =calcNetDisp2Ref(spoint, epoint, ref);
			displacement = calcDisplacement(dispV);			
			timepoint = curtraj.timepoints.get(0);
			//displacement vector along reference axis
			dvec.add(spoint);
			dvec.add(new Point3f(
					((float) (spoint.x + dispV.dv.getX())), 
					((float) (spoint.y + dispV.dv.getY())), 
					((float) (spoint.z + dispV.dv.getZ())))
			);
			vecs.add(dispV);
			dispvecs.add(dvec);
			dispA.add(displacement);
			timeA.add(timepoint);
		}
		NetDisplacementResults results = new NetDisplacementResults(dispvecs, dispA, timeA, vecs);
		return results;
	}
	
	/** Plots incremental (every displacement per time point) net displacement 
	 * towards reference point or line
	 * 
	 * @param timestart
	 * @param timeend
	 * @param tList
	 * @param ref
	 * @return
	 */
	public ArrayList<Content> plotTrackNetDispIncremental(int timestart, int timeend, ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref){
		ArrayList<Content> packedcontents =containNetDispIncremental(timestart, timeend, tList, ref);
		for (Content item : packedcontents)
			univ.addContent(item);
		lockCurrentContents(univ);	
		return packedcontents;
	}
	/** prepares Contents in arraylist to be plotted in the universe. 
	 * Net displacement is calculated incremental (for every time points).
	 * reference could be a point or a line.   
	 * 
	 * @param timestart
	 * @param timeend
	 * @param tList
	 * @param ref
	 * @return
	 */
	public ArrayList<Content> containNetDispIncremental(int timestart, int timeend, ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref){
		NetDisplacementResults results = netDispIncrement(timestart, timeend, tList, ref);
		ArrayList<DispVec> vecs = results.vecs;
		ArrayList<ArrayList<Point3f>> dispvecs =  results.dispVecs;
		CustomMultiMesh clmmDispLine = new CustomMultiMesh();
		for (int i = 0; i < dispvecs.size(); i++){
			CustomLineMesh clmdisp = 
				new CustomLineMesh(dispvecs.get(i), CustomLineMesh.CONTINUOUS, colorCodeAwayTowards(vecs.get(i).direc), 0);
			clmmDispLine.add(clmdisp);
		}

		Content netDV = ContentCreator.createContent(clmmDispLine, "NetDisplacementVecs", 0);	
		Content startpoint_spheres = createStartPointSphereContent(timestart, tList);
		Content refcont = createReferenceContent(timestart, ref);
		
		ArrayList<Content> packedcontents = new ArrayList<Content>(); //for packaging contents
		packedcontents.add(netDV);
		packedcontents.add(startpoint_spheres);
		packedcontents.add(refcont);
		return packedcontents;
		
	}
	/** Incremental (every time point) calculation of net displacement towards a reference line or point.
	 * 
	 * @param timestart
	 * @param timeend
	 * @param tList
	 * @param ref 
	 * @return
	 */
	public NetDisplacementResults netDispIncrement(int timestart, int timeend, ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref){
		int i;
		ArrayList<DispVec> vecs = new ArrayList<DispVec>();
		ArrayList<ArrayList<Point3f>> dispvecs = new ArrayList<ArrayList<Point3f>>();
		ArrayList<Double> dispA = new ArrayList<Double>(); //displacements array
		ArrayList<Integer> timeA = new ArrayList<Integer>();
		Point3f spoint, epoint;
		Integer tpoint;
		double displacement;
		ArrayList<Point3f> dvec;
		for (TrajectoryObj curtraj : tList)	{	
			for (i = 0; i < curtraj.dotList.size()-2; i++){
				dvec =  new ArrayList<Point3f>();
				spoint = curtraj.dotList.get(i);
				epoint = curtraj.dotList.get(i+1);
				if (spoint.distance(epoint) > 0){
					tpoint = curtraj.timepoints.get(i);
					DispVec dispV = calcNetDisp2Ref(spoint, epoint, ref);
					displacement = calcDisplacement(dispV);
					vecs.add(dispV);
					dispA.add(displacement);
					timeA.add(tpoint);
					//displacement vector along reference axis
					dvec.add(spoint);
					dvec.add(new Point3f(
							((float) (spoint.x + dispV.dv.getX())), 
							((float) (spoint.y + dispV.dv.getY())), 
							((float) (spoint.z + dispV.dv.getZ())))
					);
					dispvecs.add(dvec);
				}
			}
		}
		NetDisplacementResults results = new NetDisplacementResults(dispvecs, dispA, timeA, vecs);
		return results;
	}
	/** A class for containing net displacement calculation results
	 * 
	 * @author Kota Miura
	 *
	 */
	class NetDisplacementResults {
		public final ArrayList<ArrayList<Point3f>> dispVecs;
		public final ArrayList<Double> displacements;
		public final ArrayList<Integer> timeA;
		public final ArrayList<DispVec> vecs;
		public NetDisplacementResults(ArrayList<ArrayList<Point3f>> dispVecs, ArrayList<Double> displacements, ArrayList<Integer> timeA, ArrayList<DispVec> vecs){
			this.dispVecs = dispVecs;
			this.displacements = displacements;
			this.timeA = timeA;
			this.vecs = vecs;
		}
		
	}
	
	/** plots a straight line for each track connecting starting point and end point. 
	 * as of 20120202 not appearing in the GUI interface since it seems to be not helpful to the analysis 
	 * 
	 * @param timestart
	 * @param timeend
	 * @param tList
	 * @param ref
	 * @return
	 */
	public ArrayList<Content> plotTrackNetTravel(int timestart, int timeend, ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref){
		int j = 0;
		ArrayList<Double> 				dispA = new ArrayList<Double>(); //displacements array
		ArrayList<ArrayList<Point3f>>	vecs = new ArrayList<ArrayList<Point3f>>();
		Point3f spoint, epoint;	// start and end point of a track	
		Vector3D sev;
		double displacement;
		for (TrajectoryObj curtraj : tList)	{
			ArrayList<Point3f> cvec =  new ArrayList<Point3f>();
			spoint = curtraj.dotList.get(0);
			epoint = curtraj.dotList.get(curtraj.dotList.size()-1);
			
			DispVec dispV = calcNetDisp2Ref(spoint, epoint, ref);
			//this should be replaced with srv or dv
			//sev = new Vector3D(epoint.x - spoint.x, epoint.y - spoint.y, epoint.z - spoint.z); 
			displacement = calcDisplacement(dispV);
			dispA.add(displacement);
			//if (j == 0) IJ.log("id\t" + "theta\t" + "CosTheta\t" + "displacement");
			//IJ.log("" +j + "\t" + theta + "\t" + Math.cos(theta) + "\t" + displacement);
			cvec.add(spoint);
			cvec.add(epoint);
			vecs.add(cvec);
			j++;
				
		}
		CustomMultiMesh clmmProLine = new CustomMultiMesh();
		double maxdisp = maxOfDisplacements(dispA);
		for (j = 0; j < vecs.size(); j++){			
			CustomLineMesh clm = new CustomLineMesh(vecs.get(j), CustomLineMesh.CONTINUOUS, colorCodeDisplacements(dispA.get(j), maxdisp), 0);
			clmmProLine.add(clm);
			clm.setLineWidth(2);
		}
		Content cc = ContentCreator.createContent(clmmProLine, "NetTravelss", 0);	
		Content startpoint_spheres = createStartPointSphereContent(timestart, tList);
		lockCurrentContents(univ);
		
		ArrayList<Content> packedcontents = new ArrayList<Content>(); //for packaging contents
		packedcontents.add(cc);
		packedcontents.add(startpoint_spheres);
		return packedcontents;
		
	}

	/** Calculates displacement vector towards a reference point.
	 * returned value is a DispVec class holding containing displacement vector and direction (1 or -1)  
	 * 
	 * @param sev track start to the end point
	 * @param srv trackstart point to the reference point
	 * @return DispVec instance, holding a Vector3D, the net displacement vector towards a point and its direction  
	 */
	public DispVec calcDisplacementVector(Vector3D sev, Vector3D srv){
		double theta;		//angle made beteen srv and sev
		Vector3D srvDispv;	// projection vector of sev to the srv axis
		theta = Vector3D.angle(srv, sev);
		srvDispv = srv.normalize().scalarMultiply(Math.cos(theta)* sev.getNorm());

		double direc = srv.dotProduct(srvDispv) / srv.getNorm()/srvDispv.getNorm();
		direc = Math.round(direc);
		DispVec dispvec = new DispVec(srvDispv, (int) direc);
		return dispvec;
	}
	/** Calculates displacement vector towards a reference bar.
	 * returned value is an DispVec class holding containing displacement vector and direction (1 or -1) 
	 * 
	 * @param sev track vector, between start and end
	 * @param rv reference vector
	 * @param pv track start point to reference start point
	 * @param qv track start point to reference end point
	 * @return DispVec instance, holding a Vector3D, the net displacement vector towards a point and its direction
	 * <br>
	 * <br>
	 * followings are calculated in this methods:<br>
	 * 		pvproj --- length of projection of pv onto rv<br>
	 * 		refdash --- above projected vector (scaled rv)<br>
	 * 		dv vector --- starting at track start point, perpendicular to rv. (ends at the tip of refdash)<br>
	 * 		dvdash --- sev projected onto dv. the displacement vector towards reference<br>
	 */
	public DispVec calcDisplacementVector(Vector3D sev, Vector3D rv, Vector3D pv, Vector3D qv){
		double pvproj;			// length of a projection vector of pv to rv. 
		Vector3D refdash;		// a projection vector of pv to rv.
		Vector3D dv;			// vector from track start point to the endpoint of refdash
		double sevproj;			// length of a projection vector of sev to dv
		Vector3D dvdash;		// a net displacement vector against reference point (the answer) 
		
		pvproj = rv.dotProduct(pv) / Math.pow(rv.getNorm(), 2);
		refdash = rv.scalarMultiply(pvproj);
		if (pvproj > 1)
			dv = qv;
		else if (pvproj < 0)
			dv = pv;
		else
			dv = refdash.subtract(pv);	
		
		sevproj = dv.dotProduct(sev) / Math.pow(dv.getNorm(), 2);
		dvdash = dv.scalarMultiply(sevproj);
//		IJ.log(Double.toString(dv.dotProduct(dvdash)) + "\t" 
//				+ Double.toString(dv.getNorm())+"\t"
//				+ Double.toString(dvdash.getNorm()));
		double direc = dv.dotProduct(dvdash) / dv.getNorm()/dvdash.getNorm();
		direc = Math.round(direc);	
		DispVec dispvec = new DispVec(dvdash, (int) direc);
		return dispvec;

	}
	public class DispVec {
		public final Vector3D dv;
		public final int direc;
		public DispVec(Vector3D dv, int direc){
			this.dv = dv;
			this.direc = direc;
		}
	}
	/** taking track net displacement vector and net displacement vector, returns displacement scalar values
	 * with signs depending on towards/away from the reference.  
	 * 
	 * @param dispvec DispVec instance holding displacement vector and direction (1 or -1)
	 * 
	 * @return displacement length, signed (+ if towards, - if away). 
	 */
	public double calcDisplacement(DispVec dispvec){	
		double displacement;
		displacement = dispvec.dv.getNorm();
		displacement *= dispvec.direc;
		return displacement;
	}
	/** method for receiving request and depending on the length of given reference, 
	 * diverges either to point reference calculation or line reference calculation. 
	 * 
	 * @param spoint
	 * @param epoint
	 * @param ref
	 * @return
	 */
	public DispVec calcNetDisp2Ref(Point3f spoint, Point3f epoint, ArrayList<Point3f> ref){
		//ArrayList para;
		Vector3D sev = new Vector3D(epoint.x - spoint.x, epoint.y - spoint.y, epoint.z - spoint.z); 
		Vector3D rv, srv, pv, qv;
		DispVec dv;
		if (ref.size() == 1) {
			srv = new Vector3D(ref.get(0).x - spoint.x, ref.get(0).y - spoint.y, ref.get(0).z - spoint.z); //startpoint to reference point vector
			dv = calcDisplacementVector(sev, srv);
		} else {
			rv = new Vector3D(ref.get(1).x - ref.get(0).x, ref.get(1).y - ref.get(0).y, ref.get(1).z - ref.get(0).z);
			pv = new Vector3D(spoint.x - ref.get(0).x, spoint.y - ref.get(0).y, spoint.z - ref.get(0).z); 
			qv = new Vector3D(spoint.x - ref.get(1).x, spoint.y - ref.get(1).y, spoint.z - ref.get(1).z); 

			dv = calcDisplacementVector(sev, rv, pv, qv);
		}
		return dv;
	}
	
	/** Create 3D sphere contents to show starting points of tracks. 
	 * Plot only in the first frame.
	 * 
	 * @param timestart
	 * @param tList
	 * @return
	 */
	public Content createStartPointSphereContent(int timestart, ArrayList<TrajectoryObj> tList){
		ArrayList<Point3f> spheres = new ArrayList<Point3f>();
		Point3f spoint;
		for (TrajectoryObj traj : tList) 	{
			spoint = traj.dotList.get(timestart);
			List<Point3f> sphere = Mesh_Maker.createSphere(
					spoint.x, 
					spoint.y, 
					spoint.z, 
					0.7, 12, 12);
			spheres.addAll(sphere);				
		}
		CustomTriangleMesh csp = new CustomTriangleMesh(spheres, new Color3f(1.0f,1.0f,1.0f), 0.0f);
		Content ccs = ContentCreator.createContent(csp, "startpoints", 0);
		return ccs; 
	}
	/** Creates 3D reference point content
	 * 
	 */
	public Content createPointReferenceContent(int timestart, ArrayList<Point3f> ref){
		List<Point3f> referencepoint = Mesh_Maker.createSphere(ref.get(0).x, ref.get(0).y, ref.get(0).z, 2, 12, 12);
		CustomTriangleMesh refmesh = new CustomTriangleMesh(referencepoint, new Color3f(1,0,0), 0.0f);
		Content refcont = ContentCreator.createContent(refmesh, "referencePoint", 0);
		return refcont;
	}
	/** Creates 3D reference bar content. 
	 * 
	 * @param timestart
	 * @param ref an ArrayList consisting of two Point3f objects, startpoint and end point of the reference bar. 
	 * @return
	 */
	public Content createLineReferenceContent(int timestart, ArrayList<Point3f> ref){
		List<Point3f> referenceline = Mesh_Maker.createTube(
				new double[]{ref.get(0).x, ref.get(1).x},
				new double[]{ref.get(0).y, ref.get(1).y},
				new double[]{ref.get(0).z, ref.get(1).z},
				new double[]{1, 1},
				12, false); //true makes cones
		CustomTriangleMesh refmesh = new CustomTriangleMesh(referenceline, new Color3f(1,0,0), 0.0f);
		Content refcont = ContentCreator.createContent(refmesh, "referenceLine", timestart);
		return refcont;
	}
	/** Creates Reference contnent depeending on the size of the ArrayList ref.
	 * This method was made to unify two different methods. 
	 * 
	 * @param timestart
	 * @param ref
	 * @return
	 */
	public Content createReferenceContent(int timestart, ArrayList<Point3f> ref){
		Content refcont;
		if (ref.size() == 1)
			// reference: single point
			refcont = createPointReferenceContent(timestart, ref);
		else
			//reference: a bar
			refcont = createLineReferenceContent(timestart, ref);
		return refcont;
	}
	/** Calculates maximum absolute value of dispA<Double>
	 *  
	 * @param dispA an ArrayList Containing displacement values (in Double)
	 * @return maximum displacement 
	 */
	Double maxOfDisplacements(ArrayList<Double> dispA){
		Object minvalobj = Collections.min(dispA);
		Object maxvalobj = Collections.max(dispA);
		IJ.log("Max displacement" + maxvalobj);
		IJ.log("Min displacement" + minvalobj);
		double minval =  (Double) minvalobj;
		double maxval = (Double) maxvalobj;		
		double maxdisp = maxval;
		if (Math.abs(maxval) < Math.abs(minval))  
			maxdisp = minval;
		else
			maxdisp = maxval;		
		return maxdisp;
	}
	Color3f colorCodeDisplacements(double disp, double maxdisp){
		float cR, cG, cB;
		cR =0; cG = 0.6f; cB = 0;
		if (disp > 0)
			cR = (float) (disp/maxdisp);
		else
			cB = (float) (Math.abs(disp)/maxdisp);
		return new Color3f(cR, cG, cB);
	}
	/** Color assignments for net displacement vectors (towards reference point or a line)
	 * 
	 * @param awaytowards
	 * @return
	 */
	Color3f colorCodeAwayTowards(int awaytowards){
		Color3f dispcol;
		if (awaytowards > 0)
			 dispcol = new Color3f(1,0,0);
		else			
			 dispcol = new Color3f(0,1,1);
		return dispcol;
	}
	void lockCurrentContents(Image3DUniverse univ){
		Collection<Content> ccs = (Collection<Content>) univ.getContents();
		for (Content item : ccs)
			item.setLocked(true);
	}
	/** 
	 * @TODO add also total track net displacement
	 * @param timestart
	 * @param timeend
	 * @param tList
	 * @param ref
	 * @return
	 */
	public ArrayList<String[]> calcNetDisplacementData(int timestart, int timeend, 
			ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref) {
		NetDisplacementResults results = netDispIncrement(timestart, timeend, tList, ref);
		Point3f sp, ep;
		double dd;
		Integer tt;
		ArrayList<String[]> data = new ArrayList<String[]>();

		for (int i = 0; i < results.dispVecs.size(); i++){
			String[] aline = new String[9];
			sp = results.dispVecs.get(i).get(0);
			ep = results.dispVecs.get(i).get(1);
			dd = results.displacements.get(i);
			tt = results.timeA.get(i);
			aline[0] = Integer.toString(i);
			aline[1] = Integer.toString(tt);
			aline[2] = Float.toString(sp.x);
			aline[3] = Float.toString(sp.y);
			aline[4] = Float.toString(sp.z);
			aline[5] = Float.toString(ep.x);
			aline[6] = Float.toString(ep.y);
			aline[7] = Float.toString(ep.z);
			aline[8] = Double.toString(dd);
			data.add(aline);
		}
		return data;
	}
    public ArrayList<Integer> getMinMaxFrame(ArrayList<TrajectoryObj> tList){

    	ArrayList<Integer> alltimepoints = new ArrayList<Integer>();
    	for (TrajectoryObj item : tList){
    		alltimepoints.addAll(item.timepoints);
		}
	    Object objmax = Collections.max(alltimepoints);
	    Object objmin = Collections.min(alltimepoints);
	    ArrayList<Integer> startendframeList = new ArrayList<Integer>();
	    startendframeList.add(Integer.valueOf(objmin.toString()));
	    startendframeList.add(Integer.valueOf(objmax.toString()));
	    IJ.log("min:" + objmin + " max:" + objmax);
		return startendframeList;
	}
	public void saveNetDisplacementData(ArrayList<Point3f> ref, String savepath) throws IOException {
		if (this.trajlist == null)
			return;
		ArrayList<Integer> startendframeList = getMinMaxFrame(this.trajlist);
		ArrayList<String[]> data = null;
		data = calcNetDisplacementData(
					startendframeList.get(0), 
					startendframeList.get(1), 
					this.trajlist, ref);
		//String fullpath = savepath + File.separator + "netdisp.csv";
		String fullpath = savepath + "netdisp.csv";
		IJ.log("datafile: " + fullpath);
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new FileWriter(fullpath), ',', CSVWriter.NO_QUOTE_CHARACTER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.writeAll(data);
		writer.close();
	}

	/**
	 * 
	 * @param atrack
	 * @return
	 */
	static public ArrayList<Float> getBoudingBox(ArrayList<Point3f> atrack){
		ArrayList<Float> bnd = new ArrayList<Float>(6);
		bnd.add(atrack.get(0).x);
		bnd.add(atrack.get(0).y);
		bnd.add(atrack.get(0).z);
		bnd.add(atrack.get(0).x);
		bnd.add(atrack.get(0).y);
		bnd.add(atrack.get(0).z);
		for (Point3f item : atrack){
			if (bnd.get(0) > item.x) bnd.set(0, item.x);
			if (bnd.get(1) > item.y) bnd.set(1, item.y);
			if (bnd.get(2) > item.z) bnd.set(2, item.z);
			if (bnd.get(3) < item.x) bnd.set(3, item.x);
			if (bnd.get(4) < item.y) bnd.set(4, item.y);
			if (bnd.get(5) < item.z) bnd.set(5, item.z);
		}
		return bnd;		
	}
	/**
	 * @return the trajlist
	 */
	public ArrayList<TrajectoryObj> getTrajlist() {
		return trajlist;
	}

	
	
}
