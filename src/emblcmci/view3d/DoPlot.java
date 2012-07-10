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
	private VisTrack vt;
	private JFrame frame;
	private String datapath;
    
    public DoPlot(VisTrack vt, String datapath) {
    	this.vt = vt;
    	this.datapath = datapath;
    }
    //asynchronous processing
    @Override
    public ArrayList<Object> doInBackground() {
		Image3DUniverse univ = null;
		univ = new Image3DUniverse();
		vt.univ = univ;
		TrackDataLoader tld = new TrackDataLoader();
		vt.tList = tld.loadFileVolocity(datapath);		
		vt.p4d = new PlotNetDisplacement(vt.univ, vt.tList); // a class extending Plot4d		
		IJ.log("File loaded...");
		if ((vt.framestart != null) && (vt.frameend != null)){
			if (vt.flagColorCodedTracks) {
				vt.listColorcofdedTracks = vt.p4d.PlotTimeColorCodedLineOnlyFinalFrame(vt.framestart, vt.frameend, vt.tList);				
				IJ.log("3D track plotted");
			}
			if (vt.flagTrackNodes){
				vt.listStaticNodes = vt.p4d.plotTrajectorySpheres(vt.framestart, vt.frameend, vt.tList, true);				
				IJ.log("Dynamic nodes plotted");
			}
			if (vt.flagDynamicColorCodedTracks) {
				vt.listDynamicTracks = vt.p4d.PlotTimeColorCodedLine(vt.framestart, vt.frameend, vt.tList);				
				IJ.log("3D dynamic track plotting done");
			}
			if (vt.flagDynamicTrackNodes){
				vt.listDynamicNodes = vt.p4d.plotTrajectorySpheres(vt.framestart, vt.frameend, vt.tList, false);
				IJ.log("Dynamic nodes plotted");
			}
		}
        return null;
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
   		vt.univ.show();
		vt.univwin = vt.univ.getWindow();	
		vt.univwin.addWindowListener(vt.gui);
		vt.gui.fillTrackList(vt.gui.trackList, vt.tList);
    }
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(frame, message, "failed...", JOptionPane.ERROR_MESSAGE);
    }
                                 
      
    
}