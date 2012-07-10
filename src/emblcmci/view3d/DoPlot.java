package emblcmci.view3d;

import ij.IJ;
import ij3d.Content;
import ij3d.Image3DUniverse;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

// class for asynchronous processing
//@TODO for being really thread safe, returned values should be
//using returned List of values and captured using get() method inside done(). 
//in this case, type should be specified as List<Object> or so. 
// see http://itpro.nikkeibp.co.jp/article/COLUMN/20070413/268205/
class DoPlot extends SwingWorker<ArrayList<Object>, Object> {
	private Image3DUniverse univ;
	private DialogVisualizeTracks gui;
	private JFrame frame;
	private String datapath;
//    public DoPlot() {
//   	frame = DialogVisualizeTracks.this.mainFrame;
//    }
    
    public DoPlot(DialogVisualizeTracks gui, String datapath) {
    	gui.mainFrame = frame;
//    	this.univ = parentuniv;
    	this.datapath = datapath;
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
		PlotNetDisplacement Lp4d = new PlotNetDisplacement(univ, LtList); // a class extending Plot4d
		IJ.log("File loaded...");
		UnivContents.set(0, univ);
		UnivContents.set(1, Lp4d);
		UnivContents.set(2, LtList);
		Integer framestart = gui.framestart;
		Integer frameend = gui.frameend;		
		if ((framestart != null) && (frameend != null)){
			if (gui.flagColorCodedTracks) {
				Content LlistColorcofdedTracks = Lp4d.PlotTimeColorCodedLineOnlyFinalFrame(framestart, frameend, LtList);
				IJ.log("3D track plotted");
				UnivContents.set(3, LlistColorcofdedTracks);
			}
			if (gui.flagTrackNodes){
				ArrayList<Content> LlistStaticNodes = Lp4d.plotTrajectorySpheres(framestart, frameend, LtList, true);
				IJ.log("Dynamic nodes plotted");
				UnivContents.set(4, LlistStaticNodes);
			}
			if (gui.flagDynamicColorCodedTracks) {
				ArrayList<Content> LlistDynamicTracks = Lp4d.PlotTimeColorCodedLine(framestart, frameend, LtList);
				IJ.log("3D dynamic track plotting done");
				UnivContents.set(5, LlistDynamicTracks);
			}
			if (gui.flagDynamicTrackNodes){
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
//			IJ.log("failed processing");
			 showErrorDialog("failed processing");
			e.printStackTrace();
		}
		//if (univcontents.get(0) != null)
//			gui.univ = (Image3DUniverse) univcontents.get(0);
		//if (univcontents.get(1) != null)
//			gui.p4d = (PlotNetDisplacement) univcontents.get(1);
		//if (univcontents.get(2) != null)
//			gui.tList = (ArrayList<TrajectoryObj>) univcontents.get(2);
		if (univcontents.get(3) instanceof ij3d.Content)
			gui.listColorcofdedTracks = (Content) univcontents.get(3);
		if (univcontents.get(4) instanceof ArrayList<?>)
			gui.listStaticNodes = (ArrayList<Content>) univcontents.get(4);
		if (univcontents.get(5)  instanceof ArrayList<?>)
			gui.listDynamicTracks = (ArrayList<Content>) univcontents.get(5);
		if (univcontents.get(6)  instanceof ArrayList<?>)
			gui.listDynamicNodes = (ArrayList<Content>) univcontents.get(6);
		
   		gui.univ.show();
		gui.univwin = gui.univ.getWindow();	
		gui.univwin.addWindowListener(gui);
		gui.fillTrackList( gui.trackList, gui.tList);
    }
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(frame, message, "failed...", JOptionPane.ERROR_MESSAGE);
    }
                                 
      
    
}