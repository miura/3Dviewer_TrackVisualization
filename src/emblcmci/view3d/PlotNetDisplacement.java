package emblcmci.view3d;


import ij.IJ;
import ij3d.Content;
import ij3d.ContentCreator;
import ij3d.Image3DUniverse;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import org.apache.commons.math.geometry.euclidean.threed.Vector3D;

import util.opencsv.CSVWriter;

import customnode.CustomLineMesh;
import customnode.CustomMultiMesh;
import customnode.CustomTriangleMesh;
import customnode.Mesh_Maker;

import emblcmci.view3d.CalcAngularDisplacement.AngularDisp;
import emblcmci.view3d.CalcNetDisplacement.DispVec;

public class PlotNetDisplacement extends Plot4d{ 	

	//private Image3DUniverse univ;
	//private ArrayList<TrajectoryObj> trajlist;
	
	public PlotNetDisplacement(Image3DUniverse univ, ArrayList<TrajectoryObj> trajlist){
		super.univ = univ;
		super.trajlist = trajlist;
	}
	
	public PlotNetDisplacement(String path, Integer datatype){
		super(path, datatype);
	}
	public PlotNetDisplacement(Image3DUniverse univ){
		super(univ);
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
		CalcNetDisplacement cnd = new CalcNetDisplacement();
		if (ref.size() == 1) {
			srv = new Vector3D(ref.get(0).x - spoint.x, ref.get(0).y - spoint.y, ref.get(0).z - spoint.z); //startpoint to reference point vector
			dv = cnd.calcDisplacementVector(sev, srv);
		} else {
			rv = new Vector3D(ref.get(1).x - ref.get(0).x, ref.get(1).y - ref.get(0).y, ref.get(1).z - ref.get(0).z);
			pv = new Vector3D(spoint.x - ref.get(0).x, spoint.y - ref.get(0).y, spoint.z - ref.get(0).z); 
			qv = new Vector3D(spoint.x - ref.get(1).x, spoint.y - ref.get(1).y, spoint.z - ref.get(1).z); 
			dv = cnd.calcDisplacementVector(sev, rv, pv, qv);
		}
		return dv;
	}

	void lockCurrentContents(Image3DUniverse univ){
		Collection<Content> ccs = (Collection<Content>) univ.getContents();
		for (Content item : ccs)
			item.setLocked(true);
	}
//*********************************** Net Displacement Full Track length ***********************
	
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
	
//**************************** incremental *************************************

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

//******************************* start-end track vector *********************
	
	
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
	Color3f colorCodeDisplacements(double disp, double maxdisp){
		float cR, cG, cB;
		cR =0; cG = 0.6f; cB = 0;
		if (disp > 0)
			cR = (float) (disp/maxdisp);
		else
			cB = (float) (Math.abs(disp)/maxdisp);
		return new Color3f(cR, cG, cB);
	}	
//************************* reference plotter ***********
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
	
	/** Create 3D sphere contents to show starting points of tracks. 
	 * Plot only in the first frame.
	 * 
	 * @param timestart
	 * @param tList
	 * @return
	 */
//************************* start points ***********
	
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

	//**************************** incremental, Angular *************************************

	/** Plots incremental (every displacement per time point) angular displacement 
	 * towards reference point or line
	 * 
	 * @param timestart
	 * @param timeend
	 * @param tList
	 * @param ref
	 * @return
	 */
	public ArrayList<Content> plotTrackAngularDispIncremental(int timestart, int timeend, ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref){
		ArrayList<Content> packedcontents =containAngularDispIncremental(timestart, timeend, tList, ref);
		for (Content item : packedcontents)
			univ.addContent(item);
		lockCurrentContents(univ);	
		return packedcontents;
	}
	/** prepares Contents in arraylist to be plotted in the universe. 
	 * Angular displacement is calculated incremental (for every time points).
	 * reference could be a point or a line.   
	 * 
	 * @param timestart
	 * @param timeend
	 * @param tList
	 * @param ref
	 * @return
	 */
	public ArrayList<Content> containAngularDispIncremental(int timestart, int timeend, ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref){
		AngularDisplacementResults results = angularDispIncrement(timestart, timeend, tList, ref);
		ArrayList<AngularDisp> vecs = results.vecs;
		ArrayList<ArrayList<Point3f>> dispvecs =  results.dispVecs;
		CustomMultiMesh clmmDispLine = new CustomMultiMesh();
		for (int i = 0; i < dispvecs.size(); i++){
			CustomLineMesh clmdisp = 
				new CustomLineMesh(dispvecs.get(i), CustomLineMesh.CONTINUOUS, colorCodeAwayTowards(vecs.get(i).direc), 0);
			clmmDispLine.add(clmdisp);
		}

		Content netDV = ContentCreator.createContent(clmmDispLine, "AngularDisplacementVecs", 0);	
		Content startpoint_spheres = createStartPointSphereContent(timestart, tList);
		Content refcont = createReferenceContent(timestart, ref);
		
		ArrayList<Content> packedcontents = new ArrayList<Content>(); //for packaging contents
		packedcontents.add(netDV);
		packedcontents.add(startpoint_spheres);
		packedcontents.add(refcont);
		return packedcontents;
		
	} 

	
	public AngularDisplacementResults angularDispIncrement(int timestart, int timeend, ArrayList<TrajectoryObj> tList, ArrayList<Point3f> ref){
		int i;
		ArrayList<AngularDisp> vecs = new ArrayList<AngularDisp>();
		ArrayList<ArrayList<Point3f>> dispvecs = new ArrayList<ArrayList<Point3f>>();
		ArrayList<Double> dispA = new ArrayList<Double>(); //displacements array
		ArrayList<Integer> timeA = new ArrayList<Integer>();
		Point3f spoint, epoint, cpoint;
		Vector3D osv, oev, ocv;
		Integer tpoint;
		ArrayList<Point3f> dvec;
		for (TrajectoryObj curtraj : tList)	{	
			for (i = 0; i < curtraj.dotList.size()-2; i++){
				dvec =  new ArrayList<Point3f>();
				spoint = curtraj.dotList.get(i);
				epoint = curtraj.dotList.get(i+1);
				cpoint = ref.get(0);
				
				osv = new Vector3D((double) spoint.x, (double) spoint.y, (double) spoint.z);
				oev = new Vector3D((double) epoint.x, (double) epoint.y, (double) epoint.z);
				ocv = new Vector3D((double) cpoint.x, (double) cpoint.y, (double) cpoint.z);
				
				if (spoint.distance(epoint) > 0){
					tpoint = curtraj.timepoints.get(i);
					CalcAngularDisplacement cad = new CalcAngularDisplacement();
					//2 lines must be modified
					//DispVec dispV = calcNetDisp2Ref(spoint, epoint, ref);
					//displacement = calcDisplacement(dispV);
					AngularDisp dispV = cad.calcAngularVector(osv, oev, ocv);
					
					vecs.add(dispV);
					dispA.add(dispV.displacement);
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
		AngularDisplacementResults results = new AngularDisplacementResults(dispvecs, dispA, timeA, vecs);
		return results;
	}
	/** A class for containing Angular displacement calculation results
	 * 
	 * @author Kota Miura
	 *
	 */
	class AngularDisplacementResults {
		public final ArrayList<ArrayList<Point3f>> dispVecs;
		public final ArrayList<Double> displacements;
		public final ArrayList<Integer> timeA;
		public final ArrayList<AngularDisp> vecs;
		public AngularDisplacementResults(ArrayList<ArrayList<Point3f>> dispVecs, ArrayList<Double> displacements, ArrayList<Integer> timeA, ArrayList<AngularDisp> vecs){
			this.dispVecs = dispVecs;
			this.displacements = displacements;
			this.timeA = timeA;
			this.vecs = vecs;
		}
		
	}
}
