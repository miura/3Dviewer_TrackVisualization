/**
 * Plot 3D tracking results in 3Dviewer
 * Uses extended plotter, with net displacement studies. 
 * 
 * 20110112 first version
 * @author miura (miura@embl.de)
 */
package emblcmci.view3d;

import ij.IJ;
import ij.WindowManager;
import ij.io.DirectoryChooser;
import ij.io.OpenDialog;
import ij3d.Content;
import ij3d.Image3DUniverse;
import ij3d.ImageWindow3D;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.vecmath.Point3f;

//import org.hamcrest.core.IsInstanceOf;
import ij.io.OpenDialog;

/**
 * @author miura
 *
 */
public class DialogVisualizeTracks implements ActionListener, WindowListener {

	PlotNetDisplacement p4d;
	//private PlotNetDisplacement p4dnet;
	ImageWindow3D univwin;
	Image3DUniverse univ;
	ArrayList<TrajectoryObj> tList;
	
	//parameters
	String datapath = "not selected yet";
		//flags for plotting
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
	String imgfilepath = "---";
	
	JFrame mainFrame;
	JPanel panelTop;
	JPanel panelToprow2;
	private JPanel panelToprow4;
	
	JPanel panelCenter;
	JPanel panelCenterLeft;
	//JPanel panelBottomRight;

	private JPanel panelFrames;
	private JPanel panelBottom;
	private JPanel panelBottom1;
	private JPanel panelBottom2;

	
	JButton filechoosebutton = new JButton("Choose Track File...");
	JRadioButton resultsTableImportSwitch = new JRadioButton();
	JButton columnsetButton = new JButton("set column order...");
	private JPanel panelToprow3;
	private JButton imagefileButton = new JButton("set image stack path...");
	private JLabel imagepathtext = new JLabel(imgfilepath);
	JLabel filepathtext = new JLabel("---");
	
	// central panel
	JTextField fieldStartframe = new JTextField(Integer.toString(framestart), 4);
	JTextField fieldEndframe = new JTextField(Integer.toString(frameend), 4);	
	JCheckBox ColorCodedTracks = new JCheckBox("Tracks (3D only)");
	JRadioButton switchColorCodedTracks = new JRadioButton();
	JCheckBox TrackNodes = new JCheckBox("Nodes (3D only)");
	JRadioButton switchTrackNodes = new JRadioButton();
	JCheckBox ColorCodedDyamicTracks = new JCheckBox("Dynamic Tracks");
	JCheckBox DynamicTrackNodes = new JCheckBox("Dynamic Nodes");
	
	JRadioButton switchDispFullTrack = new JRadioButton();
	JRadioButton switchDispIncrement = new JRadioButton();
	private JButton exportNetDispbutton = new JButton("Export NetDisp Data");
	
	
	JScrollPane scrollPane;
	JTextArea textArea;
	
	//bottom
	String plotinfohead = "   Plot Info: ";
	JLabel plotinfo = new JLabel(plotinfohead);
	JButton doplotbutton = new JButton("Plot!");
	JButton doclosebutton = new JButton("Close");
	JButton doAddbutton = new JButton("Add");

	
	//examples (could be discarded)
	JLabel label;
	JButton button;
	
	//added later, to be organized
	private JPanel panelBottom3;
	Content listColorcofdedTracks = null;
	ArrayList<Content> listStaticNodes;
	ArrayList<Content> listDynamicTracks;
	ArrayList<Content> listDynamicNodes;

	static int clicknum = 0;

	private JPanel panelTrack3d;
	private JPanel panelNode3d;
	private JPanel panelCenterRight;
	private JList list;
	private JButton highlightOnTrackButton;
	private JButton highlightOffTrackButton;
	DefaultListModel trackList;
	private JButton extractTrackButton;
	private ArrayList<Content> highlightedList;
	private boolean flagNetDispFull;
	private JPanel panelExport;
	private boolean flagFullIncrem;
	private JPanel panelSphereCenter;
	
	Font font1verysmall = new Font("DefaultSmall", Font.PLAIN, 9);

	public void showDialog(){
		Font font1 = new Font("Default", Font.PLAIN, 12);
		Font font1small = new Font("DefaultSmall", Font.PLAIN, 12);		
		Font font2 = new Font("Serif", Font.BOLD, 15);
		Font font3 = new Font("Times New Roman", Font.ITALIC, 15);
		Font font4 = new Font("Arial", Font.ITALIC|Font.BOLD, 12);
		
		JFrame mainFrame = new JFrame("Visualize Tracks");
		this.mainFrame = mainFrame;
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//mainFrame.setSize(480, 640);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setFont(font3);
		Container contentPane = mainFrame.getContentPane();
		//FileChooserPanle
		panelTop = new JPanel();
		panelTop.setLayout(new GridLayout(4, 1));
		panelTop.setBorder(BorderFactory.createTitledBorder("Data Source"));
		panelTop.add(filechoosebutton);
		filechoosebutton.addActionListener(this);
		
		panelToprow2 = new JPanel();
		panelToprow2.setLayout(new GridLayout(1, 2));
			resultsTableImportSwitch.setText("Use ResultsTable");
			resultsTableImportSwitch.setFont(font1small);
			resultsTableImportSwitch.addActionListener(this);
			panelToprow2.add(resultsTableImportSwitch);
			panelToprow2.add(columnsetButton);
			columnsetButton.setFont(font1small);
			columnsetButton.addActionListener(this);
		panelTop.add(panelToprow2);

		panelToprow3 = new JPanel();
		panelToprow3.setLayout(new GridLayout(1, 2));
			panelToprow3.add(imagepathtext);
			panelToprow3.add(imagefileButton);
			imagefileButton.setFont(font1small);
			imagefileButton.addActionListener(this);
		panelTop.add(panelToprow3);
		panelToprow4 = new JPanel();
		panelToprow4.add(filepathtext);
		filepathtext.setFont(font1small);
		panelTop.add(panelToprow4);
		
		
		// center, parameter choosing in the left and track lists in the right
		panelCenter = new JPanel();
		panelCenter.setLayout(new GridLayout(1, 2));
			
			//left side
			panelCenterLeft = new JPanel();
			panelCenterLeft.setLayout(new GridLayout(6, 1));
			panelCenterLeft.setBorder(BorderFactory.createTitledBorder("Parameters"));
			//panelBottomLeft.add(new JLabel("Frame:"));
				panelFrames = new JPanel();
				panelFrames.setLayout(new BoxLayout(panelFrames, BoxLayout.X_AXIS));
				panelFrames.add(new JLabel("   Frames: Start "));
				panelFrames.add(fieldStartframe);
				panelFrames.add(new JLabel(" End "));
				panelFrames.add(fieldEndframe);
			panelCenterLeft.add(panelFrames);
			panelTrack3d = new JPanel();
			panelTrack3d.setLayout(new BoxLayout(panelTrack3d, BoxLayout.X_AXIS));
				panelTrack3d.add(ColorCodedTracks);
				ColorCodedTracks.addActionListener(this);
				panelTrack3d.add(switchColorCodedTracks);
				switchColorCodedTracks.addActionListener(this);
				switchColorCodedTracks.setEnabled(false);
			panelCenterLeft.add(panelTrack3d);
			panelNode3d = new JPanel();	
			panelNode3d.setLayout(new BoxLayout(panelNode3d, BoxLayout.X_AXIS));
				panelNode3d.add(TrackNodes);
				TrackNodes.addActionListener(this);
				panelNode3d.add(switchTrackNodes);
				switchTrackNodes.addActionListener(this);
				switchTrackNodes.setEnabled(false);
			panelCenterLeft.add(panelNode3d);
			panelCenterLeft.add(ColorCodedDyamicTracks);
				ColorCodedDyamicTracks.addActionListener(this);
			panelCenterLeft.add(DynamicTrackNodes);
				DynamicTrackNodes.addActionListener(this);
		
			panelCenter.add(panelCenterLeft);
			
		

		panelCenterRight = new JPanel();
		trackList = constructTrackList(panelCenterRight);
		panelCenter.add(panelCenterRight);
		
		
		// bottom buttons and infos
		panelBottom = new JPanel();
		panelBottom.setLayout(new GridLayout(3, 1));
			panelBottom1 = new JPanel();	//information text field
			panelBottom1.setLayout(new BoxLayout(panelBottom1, BoxLayout.X_AXIS));
			panelBottom1.add(plotinfo);
			panelBottom2 = new JPanel();	//button for "plot" and "close"
			panelBottom2.setLayout(new BoxLayout(panelBottom2, BoxLayout.X_AXIS));
			panelBottom2.add(doplotbutton);
			doplotbutton.addActionListener(this);
			panelBottom2.add(doclosebutton);
			doclosebutton.addActionListener(this);
			panelBottom2.add(doAddbutton);
			doAddbutton.addActionListener(this);
			doAddbutton.setEnabled(false); //implement this in futre
			panelBottom3 = new JPanel();
			panelBottom3.setLayout(new BoxLayout(panelBottom3, BoxLayout.X_AXIS));
			String testtext = "<html><p>" +
					"Track 3D Visualization Plugin Ver 1.1beta, 2012<br>" +
					"Kota Miura (miura@embl.de)" +
					"CMCI, EMBL Heidelberg" +
					"</p></html>";
			panelBottom3.add(new JLabel(testtext));
		panelBottom.add(panelBottom1);
		panelBottom.add(panelBottom2);
		panelBottom.add(panelBottom3);
		panelBottom.setBorder(new EmptyBorder(10, 10, 10, 10) );
		
		//examples, couldbe ignored following two lines
		label = new JLabel("empty");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		contentPane.add(panelTop, BorderLayout.NORTH);
		contentPane.add(panelCenter, BorderLayout.CENTER);		
		contentPane.add(panelBottom, BorderLayout.SOUTH);			
		//button.addActionListener(this);
		//textArea = new JTextArea();
		//scrollPane = new JScrollPane(textArea);
		//textArea.setSize(320, 80);
		//contentPane.add(label, BorderLayout.CENTER);

		//contentPane.add(button, BorderLayout.NORTH);
		//contentPane.add(scrollPane, BorderLayout.SOUTH);
		mainFrame.pack();
		WindowManager.addWindow(mainFrame);
		mainFrame.setVisible(true);
		
		//for track highlights instances
		highlightedList = new ArrayList<Content>();
	}
	
	//trackListing in the center-right 
	DefaultListModel constructTrackList(JPanel trackListPanel){
		//textArea = new JTextArea();
		DefaultListModel listModel = new DefaultListModel();
		list = new JList(listModel);
		scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(list);
		trackListPanel.setLayout(new BorderLayout());
		trackListPanel.add(scrollPane, BorderLayout.CENTER);
		list.addListSelectionListener
        	(new ToDoListSelectionHandler());
		
		JPanel listSouth = new JPanel();
		highlightOnTrackButton = new JButton("Highlight");
		highlightOffTrackButton = new JButton("off");
		listSouth.add(highlightOffTrackButton, BoxLayout.X_AXIS);
		listSouth.add(highlightOnTrackButton, BoxLayout.X_AXIS);
		highlightOnTrackButton.addActionListener(this);
		highlightOffTrackButton.addActionListener(this);

		extractTrackButton = new JButton("Extract");
		listSouth.add(extractTrackButton, BoxLayout.X_AXIS);
		extractTrackButton.addActionListener(this);
		
		trackListPanel.add(listSouth, BorderLayout.SOUTH);
		return listModel;
	}
	void fillTrackList(DefaultListModel listModel, ArrayList<TrajectoryObj> tList){
		String trackname;
		for (TrajectoryObj atrack : tList){
			 trackname = "track " + Integer.toString( (int) atrack.id);
			 listModel.addElement(trackname);
		}
	}
	
	// for debugging, stand-alone
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			 
            @Override
            public void run() {
        		DialogVisualizeTracks dv = new DialogVisualizeTracks(); //original line
        		dv.showDialog();//original line
            }
        });
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource() == filechoosebutton){
			ArrayList<Integer> minmax;
			this.datapath = fileChooseDialog();
			filepathtext.setText(datapath);
			minmax = TrackDataLoader.getMinMaxFrame(datapath);
			if (minmax.get(1) != null){
				fieldStartframe.setText(minmax.get(0).toString());
				fieldEndframe.setText(minmax.get(1).toString());
			}
		}		
		if(arg0.getSource() == resultsTableImportSwitch){
			if (resultsTableImportSwitch.isSelected()){
				filechoosebutton.setEnabled(false);
				filepathtext.setText("Results table data will be used.");
				// there should be flag activated to import data from results table
			} else {
				filechoosebutton.setEnabled(true);
				filepathtext.setText(datapath);
			}
		}
		if (arg0.getSource() == columnsetButton){
			TrackDataLoader tdl = new TrackDataLoader();
			tdl.columnsetter();
		}
		if (arg0.getSource() == imagefileButton){
			OpenDialog od = new OpenDialog("Choose a hyperstack", "");		
			if (od.getFileName() == "")
				return;
			String fullpath = od.getDirectory() + od.getFileName();
			File ff = new File(fullpath);
			if (ff.canRead() == false){
				IJ.error("cannot read that file!");
				return;
			}
			this.imgfilepath = fullpath;
			imagepathtext.setText(this.imgfilepath);
			imagepathtext.setFont(font1verysmall);
			//imagepathtext.update(imagepathtext.getGraphics());
		}
		
		if (arg0.getSource() == ColorCodedTracks){
			if (ColorCodedTracks.isSelected()){
				ColorCodedDyamicTracks.setSelected(false);
				DynamicTrackNodes.setSelected(false);
				switchColorCodedTracks.setEnabled(true);
			} else {
				switchColorCodedTracks.setEnabled(false);
			}
		}
		if (arg0.getSource() == switchColorCodedTracks){
			if (this.listColorcofdedTracks instanceof Content){
				if (listColorcofdedTracks.isLive())
					listColorcofdedTracks.setVisible(switchColorCodedTracks.isSelected());
			}
		}
		if (arg0.getSource() == TrackNodes){
			if (TrackNodes.isSelected()){
				ColorCodedDyamicTracks.setSelected(false);
				DynamicTrackNodes.setSelected(false);
				switchTrackNodes.setEnabled(true);
			} else
				switchTrackNodes.setEnabled(false);
		}		
		if (arg0.getSource() == ColorCodedDyamicTracks){
			if (ColorCodedDyamicTracks.isSelected()){
				ColorCodedTracks.setSelected(false);
				TrackNodes.setSelected(false);
			}
		}
		if (arg0.getSource() == DynamicTrackNodes){
			if (DynamicTrackNodes.isSelected()){
				ColorCodedTracks.setSelected(false);
				TrackNodes.setSelected(false);
			}
		}		

		if (arg0.getSource() == switchDispFullTrack){
			switchDispIncrement.setSelected(!switchDispFullTrack.isSelected());
			exportNetDispbutton.setEnabled(false);
		}
		if (arg0.getSource() == switchDispIncrement){
			switchDispFullTrack.setSelected(!switchDispIncrement.isSelected());
			exportNetDispbutton.setEnabled(true);
		}

		if (arg0.getSource() == exportNetDispbutton){
			if (this.datapath == null){
				IJ.log("no data path provided for calculation");
				return;
			}
			retrieveParameters();
			if (this.p4d == null){
				this.p4d = new PlotNetDisplacement(this.datapath, Plot4d.DATATYPE_VOLOCITY);
			}
			ArrayList<Point3f> ref = new ArrayList<Point3f>();
			SaveNetDispData exporter = new SaveNetDispData(this.p4d, ref);
			exporter.execute();
		}
		
		//*********** from here, bottom of the panel
		if (arg0.getSource() == doplotbutton){
			if ((fieldStartframe.getText() != null) && (fieldEndframe.getText() != null)){
				doplotbutton.setEnabled(false);
				retrieveParameters();
				plotinfo.setText(plotinfohead + this.datapath);
				//doPlotting();
				if (this.univ != null)
					this.univ.close();
				DoPlot dp = new DoPlot(this.univ);
				dp.execute();
				
			} else {
				plotinfo.setText(plotinfohead + " need to set the frame range");
			}
		}
		if (arg0.getSource() == doclosebutton){
			WindowEvent windowClosing = new WindowEvent(this.mainFrame, WindowEvent.WINDOW_CLOSING);
			mainFrame.dispatchEvent(windowClosing);			
		}
		if (arg0.getSource() == doAddbutton){
				retrieveParameters();			
				plotinfo.setText(plotinfohead + this.datapath);
				//addPlotting(); 
				//this button is pending
				
			} else {
				plotinfo.setText(plotinfohead + " need to set the frame range");
		}		
		if (arg0.getSource() == doAddbutton){
//			if ((this.univ != null) && (!univwin.isShowing()))
//					univ.show();
//			univ.addContent(listColorcofdedTracks);
			//AddPlot ap = new AddPlot();
			//ap.execute();
			if (listColorcofdedTracks.isVisible())
				listColorcofdedTracks.setVisible(false);
			else
				listColorcofdedTracks.setVisible(true);
		}
		if (arg0.getSource() == highlightOnTrackButton){
			if (!list.isSelectionEmpty()) {
				int index = list.getSelectedIndex();
				Content httrack = p4d.HighlightSelectedSingleTrack(tList, index);
				highlightedList.add(httrack);
				plotinfo.setText(trackinfotext(tList, index, plotinfohead));
			} else {
				plotinfo.setText(plotinfohead + " ...track not selected");
			}
		}
		if (arg0.getSource() == highlightOffTrackButton){
			for(Content trackcontent:highlightedList)
				univ.removeContent(trackcontent.getName());
		}
		if (arg0.getSource() == extractTrackButton){
			if (!list.isSelectionEmpty()) {
				int index = list.getSelectedIndex();
				TrajectoryObj currenttraj = p4d.trajlist.get(index);
				int trackid = (int) Math.round(currenttraj.id);
				PlotSIngleTrack pse = new PlotSIngleTrack(trackid);
				pse.execute();
			} else {
				plotinfo.setText(plotinfohead + " ...track not selected");
			}
		}
	}
	
	private void retrieveParameters(){
		framestart = Integer.valueOf(fieldStartframe.getText());
		frameend = Integer.valueOf(fieldEndframe.getText());
		flagColorCodedTracks = ColorCodedTracks.isSelected();
		flagTrackNodes = TrackNodes.isSelected();
		flagDynamicColorCodedTracks = ColorCodedDyamicTracks.isSelected();
		flagDynamicTrackNodes = DynamicTrackNodes.isSelected();
		
	}
	private class ToDoListSelectionHandler 
    implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			if (list.getSelectedIndices().length!=1){
                                return;
            }
			int index = DialogVisualizeTracks.this.list.getSelectedIndex();
            plotinfo.setText(trackinfotext(tList, index, plotinfohead));
		}
		
	}
	
	public static String trackinfotext(ArrayList<TrajectoryObj> tList, int index, String plotinfohead){
		TrajectoryObj atrack = tList.get(
				index);
		String infotext = plotinfohead +
		"Track"+ atrack.id + 
		"...length:" + atrack.dotList.size() +
		"...start:" + 
		Math.round(atrack.dotList.get(0).x) + ", " +
		Math.round(atrack.dotList.get(0).y) + ", " + 
		Math.round(atrack.dotList.get(0).z);
		return infotext;
	}
	
	String fileChooseDialog(){
		String datapath = "";
		OpenDialog od = new OpenDialog("Choose a track data file...", "");
		String directory = od.getDirectory();
		String name = od.getFileName();
		datapath = directory + name;
		IJ.log(datapath);
		return datapath;
	}
/*	
	public void doPlotting(){

		Image3DUniverse univ = null;
		if (this.univ != null)
			this.univ.close();
		univ = new Image3DUniverse();
		this.univ = univ;		
		p4d = new Plot4d(univ);
		tList = p4d.loadFileVolocity(datapath);
		IJ.log("File loaded...");

		if ((framestart != null) && (frameend != null)){
			if (flagColorCodedTracks) {
					listColorcofdedTracks = p4d.PlotTimeColorCodedLineOnlyFinalFrame(framestart, frameend, tList);
					IJ.log("3D track plotted");
			}
			if (flagTrackNodes){
				listStaticNodes = p4d.plotTrajectorySpheres(framestart, frameend, tList, true);
				IJ.log("Dynamic nodes plotted");			
			}
			if (flagDynamicColorCodedTracks) {
				listDynamicTracks = p4d.PlotTimeColorCodedLine(framestart, frameend, tList);
				IJ.log("3D dynamic track plotting done");
			}
			if (flagDynamicTrackNodes){
				listDynamicNodes = p4d.plotTrajectorySpheres(framestart, frameend, tList, false);
				IJ.log("Dynamic nodes plotted");
			}
			if (flagNetDisplacement){
				listNetDisplacements = p4d.plotTrackNetDisplacements(framestart, frameend, tList, rx, ry, rz);			
				IJ.log("Net Displaement vectors plotted");
			}
			if (flagNetDisplacementLineref){
				ArrayList<Point3f> refline = new ArrayList<Point3f>();
				refline.add(new Point3f(r0x, r0y, r0z));
				refline.add(new Point3f(r1x, r1y, r1z));
				listNetDisplacements = p4d.plotTrackNetDisplacements(framestart, frameend, tList, refline);			
				IJ.log("Net Displaement vectors plotted");
			}
		} else {
			plotinfo.setText(plotinfohead + " need to set the frame range");
		}
		univ.show();
		univwin = univ.getWindow();	
		univwin.addWindowListener(this);
	}

	public void addPlotting(){
		int i;
		Image3DUniverse univ = null;
		if (this.univ == null)
			return;
		if (p4d == null)
			return;
		if (tList == null)
			return;		
		univ = this.univ;
		if ((framestart != null) && (frameend != null)){

			if ((flagColorCodedTracks) && (!listColorcofdedTracks.isVisible())) {
					listColorcofdedTracks = p4d.PlotTimeColorCodedLineOnlyFinalFrame(framestart, frameend, tList);
					IJ.log("3D track added");
			}
			if (flagTrackNodes){
				for (i = 0; i < frameend-framestart +1; i++)
					if (listStaticNodes.get(i).isVisibleAt(i))
						return;
				listStaticNodes = p4d.plotTrajectorySpheres(framestart, frameend, tList, true);
				IJ.log("Dynamic nodes addded");			
			}
			if (flagDynamicColorCodedTracks) {
				for (i = 0; i < frameend-framestart +1; i++)
					if (listDynamicTracks.get(i).isVisibleAt(i))
						return;
				listDynamicTracks = p4d.PlotTimeColorCodedLine(framestart, frameend, tList);
				IJ.log("3D dynamic track plotting added");
			}
			if (flagDynamicTrackNodes){
				for (i = 0; i < frameend-framestart +1; i++)
					if (listDynamicNodes.get(i).isVisibleAt(i))
						return;
				listDynamicNodes = p4d.plotTrajectorySpheres(framestart, frameend, tList, false);
				IJ.log("Dynamic nodes added");
			}
			if (flagNetDisplacement){
				listNetDisplacements = p4d.plotTrackNetDisplacements(framestart, frameend, tList, rx, ry, rz);			
				IJ.log("Net Displacement vectors plotted");
			}
			if (flagNetDisplacementLineref){
				ArrayList<Point3f> refline = new ArrayList<Point3f>();
				refline.add(new Point3f(r0x, r0y, r0z));
				refline.add(new Point3f(r1x, r1y, r1z));
				//listNetDisplacements = p4d.plotTrackNetDisplacements(framestart, frameend, tList, refline);			
				p4d.plotTrackNetDisplacements(framestart, frameend, tList, refline);			
				IJ.log("Net Displacement vectors (LineRef) plotted");
			}	
		} else {
			plotinfo.setText(plotinfohead + " need to set the frame range");
		}		
		
	}
	*/
	
    // class for asynchronous processing
	//@TODO for being really thread safe, returned values should be
	//using returned List of values and captured using get() method inside done(). 
	//in this case, type should be specified as List<Object> or so. 
	// see http://itpro.nikkeibp.co.jp/article/COLUMN/20070413/268205/
    class DoPlot extends SwingWorker<ArrayList<Object>, Object> {
		private Image3DUniverse univ;
		private JFrame frame;
        public DoPlot() {
        	frame = DialogVisualizeTracks.this.mainFrame;
        }
        
        public DoPlot(Image3DUniverse parentuniv) {
        	frame = DialogVisualizeTracks.this.mainFrame;
        	this.univ = parentuniv;
        }
        
         
        //asynchronous processing
        @Override
        public ArrayList<Object> doInBackground() {
            // processing that takes long time
            //try {
            //    TimeUnit.SECONDS.sleep(10L);
            //} catch (InterruptedException ex) {}
    		ArrayList<Object> UnivContents = new ArrayList<Object>();
    		for (int i = 0; i < 10 ; i++) UnivContents.add(0);
    		Image3DUniverse univ = null;
    		univ = new Image3DUniverse();
    		this.univ = univ;		
    		TrackDataLoader tld = new TrackDataLoader();
    		ArrayList<TrajectoryObj> LtList = tld.loadFileVolocity(datapath);
    		PlotNetDisplacement Lp4d = new PlotNetDisplacement(univ, LtList);
    		IJ.log("File loaded...");
    		UnivContents.set(0, univ);
    		UnivContents.set(1, Lp4d);
    		UnivContents.set(2, LtList);
    		
    		if ((framestart != null) && (frameend != null)){
    			if (flagColorCodedTracks) {
    				Content LlistColorcofdedTracks = Lp4d.PlotTimeColorCodedLineOnlyFinalFrame(framestart, frameend, LtList);
    				IJ.log("3D track plotted");
    				UnivContents.set(3, LlistColorcofdedTracks);
    			}
    			if (flagTrackNodes){
    				ArrayList<Content> LlistStaticNodes = Lp4d.plotTrajectorySpheres(framestart, frameend, LtList, true);
    				IJ.log("Dynamic nodes plotted");
    				UnivContents.set(4, LlistStaticNodes);
    			}
    			if (flagDynamicColorCodedTracks) {
    				ArrayList<Content> LlistDynamicTracks = Lp4d.PlotTimeColorCodedLine(framestart, frameend, LtList);
    				IJ.log("3D dynamic track plotting done");
    				UnivContents.set(5, LlistDynamicTracks);
    			}
    			if (flagDynamicTrackNodes){
    				ArrayList<Content> LlistDynamicNodes = Lp4d.plotTrajectorySpheres(framestart, frameend, LtList, false);
    				IJ.log("Dynamic nodes plotted");
    				UnivContents.set(6, LlistDynamicNodes);
    			}

    		}
 
            return UnivContents;
        }
         
        // processing to be done after the above process
        //@SuppressWarnings("unchecked")
		@Override
        protected void done() {
            //plotbut.setText("execute");
            //plotbut.setEnabled(true);

    		ArrayList<Object> univcontents = null;
    		try {
				univcontents = get();
			} catch (InterruptedException e) {
				//IJ.log("timeout");
				 showErrorDialog("timeout");
				e.printStackTrace();
			} catch (ExecutionException e) {
//				IJ.log("failed processing");
				 showErrorDialog("failed processing");
				e.printStackTrace();
			}
			//if (univcontents.get(0) != null)
				DialogVisualizeTracks.this.univ = (Image3DUniverse) univcontents.get(0);
			//if (univcontents.get(1) != null)
				DialogVisualizeTracks.this.p4d = (PlotNetDisplacement) univcontents.get(1);
			//if (univcontents.get(2) != null)
				DialogVisualizeTracks.this.tList = (ArrayList<TrajectoryObj>) univcontents.get(2);
			if (univcontents.get(3) instanceof ij3d.Content)
				DialogVisualizeTracks.this.listColorcofdedTracks = (Content) univcontents.get(3);
			if (univcontents.get(4) instanceof ArrayList<?>)
				DialogVisualizeTracks.this.listStaticNodes = (ArrayList<Content>) univcontents.get(4);
			if (univcontents.get(5)  instanceof ArrayList<?>)
				DialogVisualizeTracks.this.listDynamicTracks = (ArrayList<Content>) univcontents.get(5);
			if (univcontents.get(6)  instanceof ArrayList<?>)
				DialogVisualizeTracks.this.listDynamicNodes = (ArrayList<Content>) univcontents.get(6);
			
       		DialogVisualizeTracks.this.univ.show();
    		univwin = DialogVisualizeTracks.this.univ.getWindow();	
    		univwin.addWindowListener(DialogVisualizeTracks.this);
    		DialogVisualizeTracks.this.fillTrackList( DialogVisualizeTracks.this.trackList, DialogVisualizeTracks.this.tList);
        }
        private void showErrorDialog(String message) {
            JOptionPane.showMessageDialog(frame, message, "failed...", JOptionPane.ERROR_MESSAGE);
        }
                                     
          
        
    }
    /** to add more plot to current Image3DUniverse.
     * not finished yet. 
     * a class for asynchronous processing, 
     * @author miura
     *
     */
 /*   
    class AddPlot extends SwingWorker<Object, Object> {
    	Image3DUniverse univ = null;
    	AddPlot(){
    		if (DialogVisualizeTracks.this.univ == null)
    			return;
    		if (DialogVisualizeTracks.this.p4d == null)
    			return;
    		if (DialogVisualizeTracks.this.tList == null)
    			return;		
    		univ = DialogVisualizeTracks.this.univ;
    	}

        //asynchronous processing
        @Override
        public Object doInBackground() {
        	int i;
        	if ((framestart != null) && (frameend != null)){

        		if ((flagColorCodedTracks) && (!listColorcofdedTracks.isVisible())) {
        			listColorcofdedTracks = p4d.PlotTimeColorCodedLineOnlyFinalFrame(framestart, frameend, tList);
        			IJ.log("3D track added");
        		}
        		if (flagTrackNodes){
        			for (i = 0; i < frameend-framestart +1; i++)
        				if (listStaticNodes.get(i).isVisibleAt(i))
        					return null;
        			listStaticNodes = p4d.plotTrajectorySpheres(framestart, frameend, tList, true);
        			IJ.log("Dynamic nodes addded");			
        		}
        		if (flagDynamicColorCodedTracks) {
        			for (i = 0; i < frameend-framestart +1; i++)
        				if (listDynamicTracks.get(i).isVisibleAt(i))
        					return null;
        			listDynamicTracks = p4d.PlotTimeColorCodedLine(framestart, frameend, tList);
        			IJ.log("3D dynamic track plotting added");
        		}
        		if (flagDynamicTrackNodes){
        			for (i = 0; i < frameend-framestart +1; i++)
        				if (listDynamicNodes.get(i).isVisibleAt(i))
        					return null;
        			listDynamicNodes = p4d.plotTrajectorySpheres(framestart, frameend, tList, false);
        			IJ.log("Dynamic nodes added");
        		}
        		if (flagNetDisplacement){
        			ArrayList<Point3f> refpoint = new ArrayList<Point3f>();
    				refpoint.add(new Point3f(rx, ry, rz));
    				ArrayList<Content> LlistNetDisplacements = p4d.plotTrackNetDisplacements(framestart, frameend, tList, refpoint);			
        			IJ.log("Net Displaement vectors plotted");
        		}
        	}
        	return null; 
        }	
    }
    
*/
    class PlotSIngleTrack extends SwingWorker<ArrayList<Object>, Object> {
    	int trackid;
    	
		/**
		 * @param trackid
		 */
		public PlotSIngleTrack(int trackid) {
			super();
			this.trackid = trackid;
		}

		@Override
		protected ArrayList<Object> doInBackground() throws Exception {
			
			SingleTackExtract se = new SingleTackExtract(datapath, imgfilepath, trackid);
			se.showSingleTrack();
			
			return null;
		}
    	
    }
    class SaveNetDispData extends SwingWorker<ArrayList<Object>, Object> {
    	PlotNetDisplacement pt4d;
    	String savepath;
		private ArrayList<Point3f> ref;
    	public SaveNetDispData(PlotNetDisplacement pt4d, ArrayList<Point3f> ref){
    		this.pt4d = pt4d;
    		this.ref = ref;
    	} 
		@Override
		protected ArrayList<Object> doInBackground() throws Exception {
			DirectoryChooser dc = new DirectoryChooser("choose save detination");
			savepath = dc.getDirectory();
			IJ.log("target folder: " + savepath);
			this.pt4d.saveNetDisplacementData(ref, savepath);
			return null;
		}
//		@Override
//        protected void done() {
//			// get()
//		}
    
    }

	
	@Override
	public void windowActivated(WindowEvent arg0) {
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		if (arg0.getSource() == univwin){
			this.univ = null;
			doplotbutton.setEnabled(true);
		}
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowIconified(WindowEvent arg0) {
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		
	}
}
