package de.embl.cmci.view3d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.media.j3d.VirtualUniverse;
//import javax.vecmath.Point3d;
import org.scijava.vecmath.Point3f;

import org.junit.Test;

import de.embl.cmci.view3d.DotObj;
import de.embl.cmci.view3d.Plot4d;
import de.embl.cmci.view3d.PlotNetDisplacement;
import de.embl.cmci.view3d.TrackDataLoader;
import de.embl.cmci.view3d.TrajectoryObj;
//import static org.junit.Assert.*;
//import ij.IJ;
import ij.IJ;
import ij3d.Image3DUniverse;

@SuppressWarnings("restriction")
public class Plot4dTest {

	PlotNetDisplacement p4d;
	private ArrayList<TrajectoryObj> trajlist;
	private Image3DUniverse univ;
	private int timestart;
	private int timeend;
	private int rx;
	private int ry;
	private int rz;
	ArrayList<Point3f> refline;
	String path;
//	@Before
	public void setUp() throws Exception {
		Image3DUniverse univ = new Image3DUniverse();
		this.univ = univ;
		
		timestart = 0;
		timeend = 23;
		rx = 117;
		ry = 95;
		rz = 88;
		String path = "/Users/miura/Dropbox/Mette/Tracks.csv";
//		String path = "/Users/miura/Dropbox/Mette/23h_/23hdatacut0_1_6_6.csv";
//		String path = "C:\\dropbox\\My Dropbox\\Mette\\Tracks.csv";
		this.path = path;
		TrackDataLoader tld = new TrackDataLoader();
		ArrayList<TrajectoryObj> trajlist = tld.loadFileVolocity(path);
		PlotNetDisplacement p4d = new PlotNetDisplacement(univ, trajlist);

		
		this.p4d = p4d;
		this.trajlist = trajlist;
		this.refline = new ArrayList<Point3f>();
		refline.add(new Point3f());
		refline.add(new Point3f());		
		refline.get(0).x = 117;
		refline.get(0).y = 32;
		refline.get(0).z = 63;
		refline.get(1).x = 121;
		refline.get(1).y = 184;
		refline.get(1).z = 63;
		
	}
//	@Before
	public void setupBoryFile(){
		Image3DUniverse univ = new Image3DUniverse();
		this.univ = univ;
		
		timestart = 0;
		timeend = 80;
		rx = 117;
		ry = 95;
		rz = 88;
		String path = "/Users/miura/Desktop/bory3DmovieMaking/c0track.csv";
		this.path = path;
		TrackDataLoader tld = new TrackDataLoader();
		tld.setColumnOrder(2, 1, 4, 5, 6);
		ArrayList<TrajectoryObj> trajlist = tld.loadFileVolocity(path);
		PlotNetDisplacement p4d = new PlotNetDisplacement(univ, trajlist);
		this.p4d = p4d;
		this.trajlist = trajlist;
		
	}
//	@Test
	public void testLoadFileVolocity() {
		String path = "/Users/miura/Dropbox/Mette/Tracks.csv";
		TrackDataLoader tld = new TrackDataLoader();
		tld.setColumnOrder(1, 2, 6, 7, 8);
		ArrayList<TrajectoryObj> trajlist = tld.loadFileVolocity(path);
		int counter = 0;
		for (TrajectoryObj t : trajlist){
			IJ.log("trackid: " + Double.toString(t.id));
			counter = 0;
			for (Point3f pos : t.dotList ){
				IJ.log(Integer.toString(t.timepoints.get(counter)) + "\t" + 
						Float.toString(pos.x) + "," +
						Float.toString(pos.y) + "," +
						Float.toString(pos.z));
				counter++;
			}
		}
		//fail("Not yet implemented");
	}
//	@Test
	public void testLoadFileVolocityBory() {
//		String path = "/Users/miura/Dropbox/Mette/Tracks.csv";
		String path = "/Users/miura/Desktop/bory3DmovieMaking/c0track.csv";		
		TrackDataLoader tld = new TrackDataLoader();
		tld.setColumnOrder(2, 1, 4, 5, 6);
		ArrayList<TrajectoryObj> trajlist = tld.loadFileVolocity(path);
		int counter = 0;
		for (TrajectoryObj t : trajlist){
			IJ.log("trackid: " + Double.toString(t.id));
			counter = 0;
			for (Point3f pos : t.dotList ){
				IJ.log(Integer.toString(t.timepoints.get(counter)) + "\t" + 
						Float.toString(pos.x) + "," +
						Float.toString(pos.y) + "," +
						Float.toString(pos.z));
				counter++;
			}
		}
		//fail("Not yet implemented");
	}
	@Test
	public void testLoadFilePavel() {
//		String path = "/Users/miura/Dropbox/Mette/Tracks.csv";
//		String path = "/Users/miura/Desktop/bory3DmovieMaking/c0track.csv";	
		String path = "/Users/miura/Dropbox/_ToDo/Pavel/dataconverted.csv";
		TrackDataLoader tld = new TrackDataLoader();
		tld.setSwitchTrackColor(true);
		ArrayList<TrajectoryObj> trajlist = tld.loadFileVolocity(path);
		int counter = 0;
		for (TrajectoryObj t : trajlist){
			IJ.log("trackid: " + Double.toString(t.id));
			IJ.log("color" + t.color.x + t.color.y + t.color.z);
			counter = 0;
			for (Point3f pos : t.dotList ){
//				IJ.log(Integer.toString(t.timepoints.get(counter)) + "\t" + 
//						Float.toString(pos.x) + "," +
//						Float.toString(pos.y) + "," +
//						Float.toString(pos.z));
				counter++;
			}
		}
		//fail("Not yet implemented");
	}	
						
//	@Test
	public void loadPointsFile() {
		Plot4d p4d = new Plot4d();
		String path = "/Users/miura/Dropbox/Mette/20_23h/segmentation_z21-47t2-24_3CONVERTED.csv";
		TrackDataLoader tld = new TrackDataLoader();
		ArrayList<DotObj> trajlist = tld.loadPointsFile(path);
		//fail("Not yet implemented");
	}
	//@Test	
	public void PlotTimeColorCodedLineOnlyFinalFrame(){
		//univ.show();
		VirtualUniverse vu = new VirtualUniverse();
		Map<?, ?> vuMap = vu.getProperties();
		System.out.println("Java3D version: " + vuMap.get("j3d.version"));
		System.out.println("Java3D vender: " + vuMap.get("j3d.vender"));
		System.out.println("Java3D remderer: " + vuMap.get("j3d.renderer"));
		p4d.PlotTimeColorCodedLineOnlyFinalFrame(timestart, timeend, this.trajlist);
	}
	//@Test
	public void PlotTimeColorCodedLine(){
		//univ.show();
		p4d.PlotTimeColorCodedLine(timestart, timeend, this.trajlist);
	}
//	public void plotTrackDisplacements(){
//		p4d.plotTrackDisplacements(timestart, timeend, this.trajlist, rx, ry, rz);
//	}
	//@Test
	public void plotTrackNetDisplacementsPoint(){
		Point3f refp = new Point3f(rx, ry, rz);
		ArrayList<Point3f> ref = new ArrayList<Point3f>();
		ref.add(refp);
//		p4d.plotTrackNetDisplacements(timestart, timeend, this.trajlist, rx, ry, rz);
		p4d.plotTrackNetDisplacements(timestart, timeend, this.trajlist, ref);
	}
	//@Test
	public void plotTrackNetDispacementBar(){
		p4d.plotTrackNetDisplacements(timestart, timeend, this.trajlist, refline);
		//univ.show();
	}
	//@Test
	public void plotTrackNetTravelBarTest(){
		p4d.plotTrackNetTravel(timestart, timeend, this.trajlist, refline);
		//univ.show();
	}	
	//@Test
	public void startpointCreatertest(){
		p4d.createStartPointSphereContent(timestart, this.trajlist);
	}
	//@Test
	public void plotTrackNetDispIncremental(){
		p4d.plotTrackNetDispIncremental(timestart, timeend, this.trajlist, refline);
	}
	//@Test
	public void datawriteTest() throws IOException{
		String path = "/Users/miura/tmp";
		p4d.saveNetDisplacementData(refline, path);
	}

}
