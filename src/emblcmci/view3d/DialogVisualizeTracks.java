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


	VisTrack vt;
	
	//parameters
	String datapath = "not selected yet";
	String imgfilepath = "---";
	
	JFrame mainFrame;
	JPanel panelTop;
	JPanel panelToprow2;
	protected JPanel panelToprow4;
	
	JPanel panelCenter;
	JPanel panelCenterLeft;

	protected JPanel panelFrames;
	protected JPanel panelBottom;
	protected JPanel panelBottom1;
	protected JPanel panelBottom2;

	
	JButton filechoosebutton;
	JRadioButton resultsTableImportSwitch;
	JButton columnsetButton;
	JPanel panelToprow3;
	JButton imagefileButton;
	JLabel imagepathtext;
	JLabel filepathtext;
	
	// central panel
	JTextField fieldStartframe;
	JTextField fieldEndframe;	
	JCheckBox ColorCodedTracks;
	JRadioButton switchColorCodedTracks ;
	JCheckBox TrackNodes ;
	JRadioButton switchTrackNodes;
	JCheckBox ColorCodedDyamicTracks;
	JCheckBox DynamicTrackNodes;
	
	JRadioButton switchDispFullTrack;
	JRadioButton switchDispIncrement;
	JButton exportNetDispbutton;
	
	
	JScrollPane scrollPane;
	JTextArea textArea;
	
	//bottom
	String plotinfohead = "   Plot Info: ";
	JLabel plotinfo;
	JButton doplotbutton;
	JButton doclosebutton;
	JButton doAddbutton;

	
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
	protected JButton highlightOnTrackButton;
	protected JButton highlightOffTrackButton;
	DefaultListModel trackList;
	protected JButton extractTrackButton;
	protected ArrayList<Content> highlightedList;
	private boolean flagNetDispFull;
	private JPanel panelExport;
	private boolean flagFullIncrem;
	private JPanel panelSphereCenter;
	
	Font font1verysmall = new Font("DefaultSmall", Font.PLAIN, 9);

	
	public DialogVisualizeTracks() {
		super();
		VisTrack vt = new VisTrack();
		this.vt = vt;
		vt.gui = this;
		initializeComponents();
	}

	public void initializeComponents() {
		filechoosebutton = new JButton("Choose Track File...");
		resultsTableImportSwitch = new JRadioButton();
		columnsetButton = new JButton("set column order...");

		imagefileButton = new JButton("set image stack path...");
		imagepathtext = new JLabel(imgfilepath);
		filepathtext = new JLabel("---");

		// central panel
		fieldStartframe = new JTextField(Integer.toString(vt.framestart), 4);
		fieldEndframe = new JTextField(Integer.toString(vt.frameend), 4);
		ColorCodedTracks = new JCheckBox("Tracks (3D only)");
		switchColorCodedTracks = new JRadioButton();
		TrackNodes = new JCheckBox("Nodes (3D only)");
		switchTrackNodes = new JRadioButton();
		ColorCodedDyamicTracks = new JCheckBox("Dynamic Tracks");
		DynamicTrackNodes = new JCheckBox("Dynamic Nodes");

		switchDispFullTrack = new JRadioButton();
		switchDispIncrement = new JRadioButton();
		exportNetDispbutton = new JButton("Export NetDisp Data");
		// bottom panel
		plotinfo = new JLabel(plotinfohead);
		doplotbutton = new JButton("Plot!");
		doclosebutton = new JButton("Close");
		doAddbutton = new JButton("Add");
	}

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
	public void fillTrackList(DefaultListModel listModel, ArrayList<TrajectoryObj> tList){
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
			if (vt.p4d == null){
				vt.p4d = new PlotNetDisplacement(this.datapath, Plot4d.DATATYPE_VOLOCITY);
			}
			ArrayList<Point3f> ref = new ArrayList<Point3f>();
			SaveNetDispData exporter = new SaveNetDispData(vt.p4d, ref);
			exporter.execute();
		}
		
		//*********** from here, bottom of the panel
		if (arg0.getSource() == doplotbutton){
			if ((fieldStartframe.getText() != null) && (fieldEndframe.getText() != null)){
				doplotbutton.setEnabled(false);
				retrieveParameters();
				plotinfo.setText(plotinfohead + this.datapath);
				//doPlotting();
				if (vt.univ != null)
					vt.univ.close();
				DoPlot dp = new DoPlot(vt, datapath);
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
				Content httrack = vt.p4d.HighlightSelectedSingleTrack(vt.tList, index);
				highlightedList.add(httrack);
				plotinfo.setText(trackinfotext(vt.tList, index, plotinfohead));
			} else {
				plotinfo.setText(plotinfohead + " ...track not selected");
			}
		}
		if (arg0.getSource() == highlightOffTrackButton){
			for(Content trackcontent:highlightedList)
				vt.univ.removeContent(trackcontent.getName());
		}
		if (arg0.getSource() == extractTrackButton){
			if (!list.isSelectionEmpty()) {
				int index = list.getSelectedIndex();
				TrajectoryObj currenttraj = vt.p4d.trajlist.get(index);
				int trackid = (int) Math.round(currenttraj.id);
				PlotSIngleTrack pse = new PlotSIngleTrack(trackid);
				pse.execute();
			} else {
				plotinfo.setText(plotinfohead + " ...track not selected");
			}
		}
	}
	
	private void retrieveParameters(){
		vt.framestart = Integer.valueOf(fieldStartframe.getText());
		vt.frameend = Integer.valueOf(fieldEndframe.getText());
		vt.flagColorCodedTracks = ColorCodedTracks.isSelected();
		vt.flagTrackNodes = TrackNodes.isSelected();
		vt.flagDynamicColorCodedTracks = ColorCodedDyamicTracks.isSelected();
		vt.flagDynamicTrackNodes = DynamicTrackNodes.isSelected();
		
	}
	private class ToDoListSelectionHandler 
    implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			if (list.getSelectedIndices().length!=1){
                                return;
            }
			int index = DialogVisualizeTracks.this.list.getSelectedIndex();
            plotinfo.setText(trackinfotext(vt.tList, index, plotinfohead));
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
		if (arg0.getSource() == vt.univwin){
			vt.univ = null;
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
