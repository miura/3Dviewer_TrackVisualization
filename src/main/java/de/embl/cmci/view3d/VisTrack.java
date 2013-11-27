package de.embl.cmci.view3d;

import ij3d.Content;
import ij3d.Image3DUniverse;
import ij3d.ImageWindow3D;

import java.util.ArrayList;

import javax.swing.JPanel;

/**
 * Stores all the contents, including parameters set in the dialog. 
 * added afterwards for separating GUI and contents.  
 * @author miura
 *
 */
public class VisTrack {

	PlotNetDisplacement p4d;
	ImageWindow3D univwin;
	Image3DUniverse univ;
	ArrayList<TrajectoryObj> tList;
	DialogVisualizeTracks gui;
	
	boolean flagColorCodedTracks = false;
	boolean flagTrackNodes = false;
	boolean flagDynamicColorCodedTracks = false;
	boolean flagDynamicTrackNodes = false;
	Integer framestart = 0;
	Integer frameend = 23;
	Integer rx = 117;
	Integer ry = 95;
	Integer rz = 88;
	Integer r0x = 117;
	Integer r0y = 32;
	Integer r0z = 20;	
	Integer r1x = 121;
	Integer r1y = 184;
	Integer r1z = 20;	
	Integer srx = 117;
	Integer sry = 95;
	Integer srz = 88;
	Content listColorcofdedTracks;
	ArrayList<Content> listStaticNodes;
	ArrayList<Content> listDynamicTracks;
	ArrayList<Content> listDynamicNodes;

	// extended version
	boolean flagNetDisplacement = false;
	boolean flagNetDisplacementLineref = false;
	boolean flagAngularDisplacement = false;
	boolean flagNetDispFull;
	boolean flagFullIncrem;
	boolean useTrackColor;
	public ArrayList<Content> listNetDisplacements;
	public ArrayList<Content> listNetDisplacementsLineRef;

}
