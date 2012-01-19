package emblcmci.view3d;

import static org.junit.Assert.*;
import ij.IJ;
import ij3d.Image3DUniverse;

import java.util.ArrayList;
import java.util.Map;

import javax.media.j3d.VirtualUniverse;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import org.junit.Before;
import org.junit.Test;

public class Plot4dTest {

	Plot4d p4d;
	private ArrayList<TrajectoryObj> trajlist;
	private Image3DUniverse univ;
	private int timestart;
	private int timeend;
	private int rx;
	private int ry;
	private int rz;
	ArrayList<Point3f> refline;
	@Before
	public void setUp() throws Exception {
		Image3DUniverse univ = new Image3DUniverse();
		this.univ = univ;
		Plot4d p4d = new Plot4d(univ);
		timestart = 0;
		timeend = 23;
		rx = 117;
		ry = 95;
		rz = 88;
//		String path = "/Users/miura/Dropbox/Mette/Tracks.csv";
		String path = "/Users/miura/Dropbox/Mette/23h_/23hdatacut0_1_6_6.csv";
		ArrayList<TrajectoryObj> trajlist = p4d.loadFileVolocity(path);
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

	@Test
	public void testLoadFileVolocity() {
		Plot4d p4d = new Plot4d();
		String path = "/Users/miura/Dropbox/Mette/Tracks.csv";
		ArrayList<TrajectoryObj> trajlist = p4d.loadFileVolocity(path); 
		//fail("Not yet implemented");
	}
	@Test
	public void loadPointsFile() {
		Plot4d p4d = new Plot4d();
		String path = "/Users/miura/Dropbox/Mette/segmentation_z21-47t2-24_3CONVERTED.csv";
		ArrayList<TrajectoryObj> trajlist = p4d.loadFileVolocity(path); 
		//fail("Not yet implemented");
	}
	//@Test	
	public void PlotTimeColorCodedLineOnlyFinalFrame(){
		//univ.show();
		VirtualUniverse vu = new VirtualUniverse();
		if (vu == null){
			System.out.println("failed getting VirtualUniverse");
		}
		Map vuMap = vu.getProperties();
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
	public void plotTrackDisplacements(){
		p4d.plotTrackDisplacements(timestart, timeend, this.trajlist, rx, ry, rz);
	}
	//@Test
	public void plotTrackNetDisplacements(){
		p4d.plotTrackNetDisplacements(timestart, timeend, this.trajlist, rx, ry, rz);
	}
	@Test
	public void plotNetDisp(){
		p4d.plotTrackNetDisplacements(timestart, timeend, this.trajlist, refline);
		//univ.show();
	}

}
