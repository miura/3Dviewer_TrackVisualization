/**
 * 
 */
package emblcmci.view3d;

import ij.IJ;
import ij.io.OpenDialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

/**
 * @author miura
 *
 */
public class DialogVisualizeTracks implements ActionListener {

	JPanel panelTop;
	JPanel panelCenter;
	JPanel panelCenterLeft;
	JPanel panelBottomRight;
	JPanel panelToprow2;
	JButton filechoosebutton = new JButton("Choose Track File...");
	JRadioButton resultsTableImportSwitch = new JRadioButton();
	JLabel filepathtext = new JLabel("---");

	JTextField fieldStartframe = new JTextField(5);
	JTextField fieldEndframe = new JTextField(5);	
	JCheckBox ColorCodedTracks = new JCheckBox("Tracks (3D only)");
	JCheckBox TrackNodes = new JCheckBox("Nodes (3D only)");
	JCheckBox ColorCodedDyamicTracks = new JCheckBox("Dynamic Tracks");
	JCheckBox DyamicTrackNodes = new JCheckBox("Dynamic Nodes");
	JCheckBox NetDisplacements = new JCheckBox("Net Displacement");
	private JTextField fieldRX = new JTextField();
	private JTextField fieldRY = new JTextField();
	private JTextField fieldRZ = new JTextField();
	JButton doplotbutton = new JButton("Plot!");
	JButton doclosebutton = new JButton("Close");
	JLabel label;
	JButton button;
	JScrollPane scrollPane;
	JTextArea textArea;
	static int clicknum = 0;
	String datapath = "not selected yet";
	private JPanel panelFrames;
	private JPanel panelBottom;
	private JPanel panelBottom1;
	private JPanel panelBottom2;
	private JPanel panelRefPoints;

	public void showDialog(){
		JFrame mainFrame = new JFrame("Visualize Tracks");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(640, 480);
		mainFrame.setLocationRelativeTo(null);		
		Container contentPane = mainFrame.getContentPane();
		//FileChooserPanle
		panelTop = new JPanel();
		panelTop.setLayout(new GridLayout(3, 1));
		panelTop.add(filechoosebutton);
		filechoosebutton.addActionListener(this);
		

		panelToprow2 = new JPanel();
		panelToprow2.setLayout(new GridLayout(1, 2));
		panelToprow2.add(resultsTableImportSwitch);
		resultsTableImportSwitch.setText("Use ResultsTable");
		resultsTableImportSwitch.addActionListener(this);
		panelToprow2.add(filepathtext);
		panelTop.add(panelToprow2);
		
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
			panelCenterLeft.add(TrackNodes);
			panelCenterLeft.add(ColorCodedDyamicTracks);
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
		panelBottom.setLayout(new GridLayout(2, 1));
			panelBottom1 = new JPanel();	//information text field
			panelBottom1.setLayout(new BoxLayout(panelBottom1, BoxLayout.X_AXIS));
			panelBottom1.add(new JLabel("    Plot Info:"));
			panelBottom2 = new JPanel();	//button for "plot" and "close"
			panelBottom2.setLayout(new BoxLayout(panelBottom2, BoxLayout.X_AXIS));
			panelBottom2.add(doplotbutton);
			panelBottom2.add(doclosebutton);
		panelBottom.add(panelBottom1);
		panelBottom.add(panelBottom2);
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
}
