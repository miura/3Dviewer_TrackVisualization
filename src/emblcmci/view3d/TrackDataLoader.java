package emblcmci.view3d;

import ij.IJ;
import ij.WindowManager;
import ij.plugin.PlugIn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.vecmath.Point3f;

import emblcmci.view3d.DialogVisualizeTracks.DoPlot;

import util.opencsv.CSVReader;

public class TrackDataLoader implements ActionListener, PlugIn {
	
	private JFrame mainFrame;
	private JPanel panelTop;
	static private int p_trackid = 1;
	static private int p_frame = 2;
	static private int p_x = 6;
	static private int p_y = 7;
	static private int p_z = 8;

	JTextField fieldTrackid = new JTextField(Integer.toString(p_trackid), 4);	
	JTextField fieldFrame = new JTextField(Integer.toString(p_frame), 4);	
	JTextField fieldx = new JTextField(Integer.toString(p_x), 4);	
	JTextField fieldy = new JTextField(Integer.toString(p_y), 4);		
	JTextField fieldz = new JTextField(Integer.toString(p_z), 4);
	private JPanel paneltrackid;
	private JPanel panelframe;		
	private JPanel panelx;		
	private JPanel panely;		
	private JPanel panelz;
	private JPanel panelBottom;		
	JButton setbutton = new JButton("Set");
	JButton cancelbutton = new JButton("Cancel");	

	public void run(String arg) {
		// TODO Auto-generated method stub
		TrackDataLoader tld = new TrackDataLoader();
		tld.columnsetter();
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
				if ((currentTrajID - Double.valueOf(cA[p_trackid]) != 0) && (atraj.size() > 0)){
					//IJ.log(Double.toString(currentTrajID) + cA[1]);
					TrajectoryObj atrajObj = new TrajectoryObj(currentTrajID, atraj, timepoints);
					trajlist.add(atrajObj);
					currentTrajID = Double.valueOf(cA[p_trackid]);
					//cvec.clear();
					atraj = new ArrayList<Point3f>();
					timepoints = new ArrayList<Integer>();
				}
				// pixel positions
	 			//cvec.add(Point3f(Double.valueOf(cA[3]),Double.valueOf(cA[4]),Double.valueOf(cA[5])));
	 			// scaled positions
	 			atraj.add(new Point3f(Float.valueOf(cA[p_x]),Float.valueOf(cA[p_y]),Float.valueOf(cA[p_z]))); 
	 			timepoints.add((int) (Double.valueOf(cA[p_frame]).intValue()));  
			}
			counter++;
		}
//		this.trajlist = trajlist;
		IJ.log("file loaded successfully");
		return trajlist;
	}
		
	public void columnsetter(){
		
		Font font3 = new Font("Times New Roman", Font.ITALIC, 15);		
		JFrame mainFrame = new JFrame("Data Column Setter");
		this.mainFrame = mainFrame;
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//mainFrame.setSize(480, 640);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setFont(font3);
		Container contentPane = mainFrame.getContentPane();
		//FileChooserPanle
		panelTop = new JPanel();
		panelTop.setLayout(new GridLayout(6, 1));
		panelTop.setBorder(BorderFactory.createTitledBorder("Column Number"));
			paneltrackid = fieldgenearator("TrackID: ", fieldTrackid);
			panelframe = fieldgenearator("Frame: ", fieldFrame);
			panelx = fieldgenearator("x: ", fieldx);
			panely = fieldgenearator("y: ", fieldy);
			panelz = fieldgenearator("z: ", fieldz);
		panelTop.add(paneltrackid);
		panelTop.add(panelframe);
		panelTop.add(panelx);
		panelTop.add(panely);
		panelTop.add(panelz);
		
		contentPane.add(panelTop, BorderLayout.CENTER);
		
		panelBottom = new JPanel();	//button for "plot" and "close"
		panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.X_AXIS));
		panelBottom.add(setbutton);
		setbutton.addActionListener(this);
		panelBottom.add(cancelbutton);
		cancelbutton.addActionListener(this);
		contentPane.add(panelBottom, BorderLayout.SOUTH);
		mainFrame.pack();
		WindowManager.addWindow(mainFrame);
		mainFrame.setVisible(true);		
	}
	
	private JPanel fieldgenearator(String title, JTextField tf){
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(new JLabel(title));
		p.add(tf);
		return p;					
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == setbutton){
			this.p_trackid = Integer.valueOf(fieldTrackid.getText());
			this.p_frame = Integer.valueOf(fieldFrame.getText());			
			this.p_x = Integer.valueOf(fieldx.getText());			
			this.p_y = Integer.valueOf(fieldy.getText());			
			this.p_z = Integer.valueOf(fieldz.getText());			
			testprintColumns();
		}
		if (arg0.getSource() == cancelbutton){
			WindowEvent windowClosing = new WindowEvent(this.mainFrame, WindowEvent.WINDOW_CLOSING);
			mainFrame.dispatchEvent(windowClosing);
		}
	}
	
	public void testprintColumns(){
		IJ.log("trackid column: " + Integer.toString(p_trackid));
		IJ.log("frame column: " + Integer.toString(p_frame));
		IJ.log("x column: " + Integer.toString(p_x));
		IJ.log("y column: " + Integer.toString(p_y));
		IJ.log("z column: " + Integer.toString(p_z));
		
	}
	// for debugging, stand-alone
	public static void main(String[] args) {
        	TrackDataLoader tdl = new TrackDataLoader();
        	tdl.columnsetter();
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
		//this.coords = coords;
		return coords;
	}

}
