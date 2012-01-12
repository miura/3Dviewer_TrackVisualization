/**
 * Plot 3D tracking results in 3Dviewer
 * 20110112 first version
 * @author miura (miura@embl.de)
 */
package emblcmci.view3d;

import ij.IJ;
import ij.io.OpenDialog;
import ij3d.Image3DUniverse;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * @author miura
 *
 */
public class DialogVisualizeTracks implements ActionListener {

	//parameters
	String datapath = "not selected yet";
		//flags for plotting
	boolean flagColorCodedTracks = false;
	boolean flagTrackNodes = false;
	boolean flagDynamicColorCodedTracks = false;
	boolean flagDynamicTrackNodes = false;
	boolean flagNetDisplacement = false;
	Integer framestart = 0;
	Integer frameend = 23;
	Integer rx = 117;
	Integer ry = 95;
	Integer rz = 88;
	
	Image3DUniverse univ;
	ArrayList<TrajectoryObj> tList;
	
	JFrame mainFrame;
	JPanel panelTop;
	JPanel panelToprow2;
	private JPanel panelToprow3;
	
	JPanel panelCenter;
	JPanel panelCenterLeft;
	//JPanel panelBottomRight;

	private JPanel panelFrames;
	private JPanel panelBottom;
	private JPanel panelBottom1;
	private JPanel panelBottom2;
	private JPanel panelRefPoints;

	
	JButton filechoosebutton = new JButton("Choose Track File...");
	JRadioButton resultsTableImportSwitch = new JRadioButton();
	JLabel filepathtext = new JLabel("---");

	// central panel
	JTextField fieldStartframe = new JTextField(Integer.toString(framestart), 4);
	JTextField fieldEndframe = new JTextField(Integer.toString(frameend), 4);	
	JCheckBox ColorCodedTracks = new JCheckBox("Tracks (3D only)");
	JCheckBox TrackNodes = new JCheckBox("Nodes (3D only)");
	JCheckBox ColorCodedDyamicTracks = new JCheckBox("Dynamic Tracks");
	JCheckBox DyamicTrackNodes = new JCheckBox("Dynamic Nodes");
	JCheckBox NetDisplacements = new JCheckBox("Net Displacement");
	private JTextField fieldRX = new JTextField(Integer.toString(rx));
	private JTextField fieldRY = new JTextField(Integer.toString(ry));
	private JTextField fieldRZ = new JTextField(Integer.toString(rz));

	JScrollPane scrollPane;
	JTextArea textArea;
	
	//bottom
	String plotinfohead = "   Plot Info: ";
	JLabel plotinfo = new JLabel(plotinfohead);
	JButton doplotbutton = new JButton("Plot!");
	JButton doclosebutton = new JButton("Close");
	
	//examples (could be discarded)
	JLabel label;
	JButton button;
	private JPanel panelBottom3;
	static int clicknum = 0;
	

	
	

	public void showDialog(){
		Font font1 = new Font("Default", Font.PLAIN, 12);
		Font font1small = new Font("DefaultSmall", Font.PLAIN, 12);		
		Font font2 = new Font("Serif", Font.BOLD, 15);
		Font font3 = new Font("Times New Roman", Font.ITALIC, 15);
		Font font4 = new Font("Arial", Font.ITALIC|Font.BOLD, 12);
		
		JFrame mainFrame = new JFrame("Visualize Tracks");
		this.mainFrame = mainFrame;
		//mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(480, 640);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setFont(font3);
		Container contentPane = mainFrame.getContentPane();
		//FileChooserPanle
		panelTop = new JPanel();
		panelTop.setLayout(new GridLayout(3, 1));
		panelTop.add(filechoosebutton);
		filechoosebutton.setFont(font1small);
		filechoosebutton.addActionListener(this);
		

		panelToprow2 = new JPanel();
		panelToprow2.setLayout(new GridLayout(1, 2));
		panelToprow2.add(resultsTableImportSwitch);
		resultsTableImportSwitch.setText("Use ResultsTable");
		resultsTableImportSwitch.setFont(font1small);
		resultsTableImportSwitch.addActionListener(this);
		panelTop.add(panelToprow2);
		panelToprow3 = new JPanel();
		panelToprow3.add(filepathtext);
		filepathtext.setFont(font1small);
		panelTop.add(panelToprow3);
		
		
		// center, parameter choosing and track lists in the right
		panelCenter = new JPanel();
		panelCenter.setLayout(new GridLayout(1, 2));

			panelCenterLeft = new JPanel();
			panelCenterLeft.setLayout(new GridLayout(10, 1));
			//panelBottomLeft.add(new JLabel("Frame:"));
				panelFrames = new JPanel();
				panelFrames.setLayout(new BoxLayout(panelFrames, BoxLayout.X_AXIS));
				panelFrames.add(new JLabel("   Frames: Start "));
				panelFrames.add(fieldStartframe);
				panelFrames.add(new JLabel(" End "));
				panelFrames.add(fieldEndframe);
			panelCenterLeft.add(panelFrames);	
			panelCenterLeft.add(ColorCodedTracks);
			ColorCodedTracks.addActionListener(this);
			panelCenterLeft.add(TrackNodes);
			panelCenterLeft.add(ColorCodedDyamicTracks);
			ColorCodedDyamicTracks.addActionListener(this);
			panelCenterLeft.add(DyamicTrackNodes);
			panelCenterLeft.add(NetDisplacements);			
				panelRefPoints = new JPanel();
				panelRefPoints.setLayout(new BoxLayout(panelRefPoints, BoxLayout.X_AXIS));
				panelRefPoints.add(new JLabel("   Reference XYZ:"));
				panelRefPoints.add(fieldRX);
				panelRefPoints.add(fieldRY);
				panelRefPoints.add(fieldRZ);
			panelCenterLeft.add(panelRefPoints);
			panelRefPoints.setVisible(true);//toggle this depend on the selection of relative meovements

		panelCenter.add(panelCenterLeft);
		
		textArea = new JTextArea();
		scrollPane = new JScrollPane(textArea);
		panelCenter.add(scrollPane);
		
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
			panelBottom3 = new JPanel();
			panelBottom3.setLayout(new BoxLayout(panelBottom3, BoxLayout.X_AXIS));
			String testtext = "<html><p>   this is a test line to check if the " +
			"line breaking autoatically works or not. Might add more information " +
			"<br> about track details, stats and so on in this information panel. " +
			"for example concerning trajectory numbers, length, time, displacement towards reference " +
			"point and so on. this might be helpful in brah brah brah</p></html>";
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
		mainFrame.setVisible(true); 
	}
	
	public static void main(String[] args) {
		DialogVisualizeTracks dv = new DialogVisualizeTracks();
		dv.showDialog();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getSource() == button){
			//textArea.append(textField.getText() + "\n");
			clicknum++;
			textArea.append("Clicked! (" + Integer.toString(clicknum) + ")\n");
			label.setText("clicked");
		}
		if(arg0.getSource() == filechoosebutton){
			this.datapath = fileChooseDialog();
			filepathtext.setText(datapath);
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
		if (arg0.getSource() == ColorCodedTracks){
			if (ColorCodedTracks.isSelected()){
				ColorCodedDyamicTracks.setSelected(false);
				DyamicTrackNodes.setSelected(false);
			}
		}
		if (arg0.getSource() == ColorCodedDyamicTracks){
			if (ColorCodedDyamicTracks.isSelected()){
				ColorCodedTracks.setSelected(false);
				TrackNodes.setSelected(false);
			}
		}
		if (arg0.getSource() == doplotbutton){
			if ((fieldStartframe.getText() != null) && (fieldEndframe.getText() != null)){
				framestart = Integer.valueOf(fieldStartframe.getText());
				frameend = Integer.valueOf(fieldEndframe.getText());
				flagColorCodedTracks = ColorCodedTracks.isSelected();
				flagTrackNodes = TrackNodes.isSelected();
				flagDynamicColorCodedTracks = ColorCodedDyamicTracks.isSelected();
				flagDynamicTrackNodes = DyamicTrackNodes.isSelected();
				flagNetDisplacement = NetDisplacements.isSelected();
				rx  = Integer.valueOf(fieldRX.getText());
				ry  = Integer.valueOf(fieldRY.getText());
				rz  = Integer.valueOf(fieldRZ.getText());
				plotinfo.setText(plotinfohead + this.datapath);
				doPlotting();
			} else {
				plotinfo.setText(plotinfohead + " need to set the frame range");
			}
		}
		if (arg0.getSource() == doclosebutton){
			WindowEvent windowClosing = new WindowEvent(this.mainFrame, WindowEvent.WINDOW_CLOSING);
			mainFrame.dispatchEvent(windowClosing);			
		}
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
	
	public void doPlotting(){
		if (univ == null){
			Image3DUniverse univ = new Image3DUniverse();
			this.univ = univ; 
		} 
		//univ.show();
		Plot4d p4d = new Plot4d(univ);
		//if (FileExists)
		tList = p4d.loadFileVolocity(datapath);
		if ((framestart != null) && (frameend != null)){
			if (flagColorCodedTracks) IJ.log("3D track selected"); //test
			if (flagColorCodedTracks) 
				p4d.PlotTimeColorCodedLineOnlyFinalFrame(framestart, frameend, tList);
			if (flagDynamicColorCodedTracks)
				p4d.PlotTimeColorCodedLine(framestart, frameend, tList);
			if (flagNetDisplacement)
				p4d.plotTrackNetDisplacements(framestart, frameend, tList, rx, ry, rz);			
		} else {
			plotinfo.setText(plotinfohead + " need to set the frame range");
		}
		univ.show();		

			

	}
}
