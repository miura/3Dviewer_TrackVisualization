// java version of the single track extractor
package emblcmci.view3d;

// migrated from jython script

import javax.vecmath.Point3f;
import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.vecmath.Color3f;
import ij3d.Content;
import ij3d.ContentCreator;
import ij3d.Image3DUniverse;
import ij3d.behaviors.ViewPlatformTransformer;
import customnode.CustomLineMesh;
import customnode.CustomMultiMesh;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import emblcmci.Extractfrom4D;
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.io.TiffDecoder;
import ij.plugin.FileInfoVirtualStack;
import ij.plugin.Concatenator;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;

import java.util.Properties;
import ij.measure.Calibration;

public class SingeleTackExtract{

  String srcpath;
  String imgpath;
  int trackid;

  public SingeleTackExtract(String srcpath, String imgpath, int trackid){
    this.srcpath = srcpath;
    this.imgpath = imgpath;
    this.trackid = trackid;
  }
  
  // returns a time point ImagePlus from hyperstack
  public ImagePlus oneTimePointFrom4D(ImagePlus imp){
    Extractfrom4D e4d = new Extractfrom4D();
    e4d.setGstarttimepoint(1);
    IJ.log("current time point" + Integer.toString(1));
    ImagePlus aframe = e4d.coreheadless(imp, 3);
    return aframe;
  }

  //return a substack, defined by the argument. 
  public ImagePlus getSubstack(String imgpath, int zmin, int zmax, int timepoint){

    //this line should be changed with path constrction
	File imgfile = new File(imgpath);
//    TiffDecoder td = new TiffDecoder(os.path.dirname(imgpath) , os.path.basename(imgpath));
    TiffDecoder td = new TiffDecoder(imgfile.getParent() , imgfile.getName());

    FileInfo[] fileinfoA = null;
	try {
		fileinfoA = td.getTiffInfo();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		IJ.log("Opening failed: " + imgfile.getPath());
	}
    FileInfo fo1 = (FileInfo) fileinfoA[0].clone();
    fo1.nImages = 1;
    FileOpener fo = new FileOpener(fo1);
    Properties props = fo.decodeDescriptionString(fileinfoA[0]);
     //print props.toString();
    IJ.log(props.toString());

    int frames = Integer.parseInt(props.getProperty("frames"));
    int slices = Integer.parseInt(props.getProperty("slices"));
    IJ.log("frames" + Integer.toString(frames) + 
        "slices" + Integer.toString(slices));
    ImagePlus imp = fo.open(false);;
    Calibration calib = imp.getCalibration();
    double xys = calib.pixelWidth;
    double zs = calib.pixelDepth;
    IJ.log("xy scales: " + Double.toString(xys) + "\nzscale: " + Double.toString(zs));
    int topslice = (int) Math.floor(zmin / zs);
    int bottomslice = (int) Math.ceil(zmax / zs);
    IJ.log("slice range:" + Integer.toString(topslice)+ "-" + Integer.toString(bottomslice));
    if (topslice <0) {
      topslice = 0;
      IJ.log("topslice reset to 0");
    }
    if (bottomslice > slices-1){
      bottomslice = slices-1;
      IJ.log("bottom slice set to " + Integer.toString(imp.getNSlices()-1));
    }
    FileInfoVirtualStack vstack = new FileInfoVirtualStack(fileinfoA[0], false);
    IJ.log("vstack size " + Integer.toString(vstack.getSize()));
    int frameoffset = slices * timepoint;
    ImageStack substack = new ImageStack(imp.getWidth(), imp.getHeight());
    ImageProcessor ip;
    for (int i = topslice; i < bottomslice+1; i++){
      ip = vstack.getProcessor(frameoffset + i + 1);
      substack.addSlice("z"+ Integer.toString(i)+ "t" + Integer.toString(timepoint), ip);
    }
    ImagePlus outimp = new ImagePlus("substack", substack);
    outimp.setCalibration(calib);
    return outimp;

  }
  private ImagePlus getSubstack(String imgpath2, Float float1, Float float2, Integer tt) {
	  ImagePlus imp = getSubstack(imgpath2, Math.round(float1), Math.round(float2), tt);
	  return imp;
  }  

  // main part
  public void showSingleTrack(){
    Image3DUniverse univ = new Image3DUniverse();
	File datafile = new File(srcpath);
    String destpath = datafile.getParent() + File.separator;    
    //destpath = os.path.dirname(srcpath) + os.sep; jythonix  
    
    Plot4d p4d = new Plot4d(srcpath, Plot4d.DATATYPE_VOLOCITY);
    ArrayList<TrajectoryObj> tList = p4d.getTrajlist()	;
    ArrayList<Point3f> atrack = tList.get(trackid).getDotList();
    ArrayList<Integer> atimepoints = tList.get(trackid).getTimepoints();
    ArrayList<Float> bnd = Plot4d.getBoudingBox(atrack); // arraylist of Float;
    ArrayList<ImagePlus> timeseries = new ArrayList<ImagePlus>();
    for (Integer tt : atimepoints){
        ImagePlus subsubimp = getSubstack(imgpath, bnd.get(2), bnd.get(5), tt);
        timeseries.add(subsubimp);
    }
    //jaimp = array(timeseries, ImagePlus); this is specific to jython
    ImagePlus[] jaimp = new ImagePlus[timeseries.size()]; 
    jaimp = timeseries.toArray(jaimp);   
    Concatenator ccc = new Concatenator();
    ImagePlus subimp = ccc.concatenate(jaimp, true);
    Calibration calib = timeseries.get(0).getCalibration();    
    subimp.setCalibration(calib);
    int frames = atimepoints.size();
    int slices = subimp.getStackSize()/frames;
    subimp.setDimensions(1, slices, frames);
    
    calib = subimp.getCalibration();
    double xys = calib.pixelWidth;
    double zs = calib.pixelDepth;
    int offset = 0; //micrometers;

    int left = (int) ((Math.round(bnd.get(0))- offset)/xys) ;
    int top  = (int) ((Math.round(bnd.get(1))- offset)/xys) - offset;
    int ww = (int) ((Math.round(bnd.get(3) - bnd.get(0)) + 2 * offset) / xys);
    int hh = (int) ((Math.round(bnd.get(4) - bnd.get(1)) + 2 * offset) / xys);
    ImageStack xycropstk = new StackProcessor(subimp.getStack(), null).crop(left, top, ww, hh);
    ImagePlus xycropimp = new ImagePlus("vol", xycropstk);
    xycropimp.setCalibration(calib);
    xycropimp.setDimensions(1, slices, frames);
    xycropimp.setOpenAsHyperStack(true);
    //xycropimp.show();
    IJ.log("cropped from x-y-ww-hh:" + Integer.toString(left) +
        Integer.toString(top) +
        Integer.toString(ww) +
        Integer.toString(hh) );
    univ.show();
    Content obj = univ.addVoltex(xycropimp);
    //obj = univ.addSurfacePlot(xycropimp, Color3f(255.0, 0.0, 0.0), "surf", 150, jba , 2);
    //obj = univ.addSurfacePlot(xycropimp);
    obj.setThreshold(0);
    obj.setTransparency(0.1f);
    obj.setLocked(true) ;
    //setTransparency(float transparency) ;
    int dx = Math.round(bnd.get(0))- offset;
    int dy = Math.round(bnd.get(1))- offset;
    int dz = (int) (Math.floor(bnd.get(2) / zs) * zs);
    CustomMultiMesh trackmesh;
    Content cont;

    for (int i = 0; i <frames; i++){
      trackmesh = p4d.createSingleTrackMesh(tList, trackid); //Content;
      //trackmesh = p4d.createSingleTrackMeshShifted(tList, trackid, dx, dy, dz) #Content;
      cont =ContentCreator.createContent(trackmesh, "Track" + Integer.toString(i), i);
      Transform3D t3d = new Transform3D() ;
      Point3f trans = new Point3f(-1 * dx,-1 * dy, -1 * dz);
      t3d.setTranslation(new Vector3f(trans));
      cont.setTransform(t3d);
      univ.addContent(cont);
    }
    //univ.addContent(ccs);
    univ.centerSelected(obj);
    ViewPlatformTransformer vtf = new ViewPlatformTransformer(univ, univ);
    vtf.zoomTo(10)    ;
  }


}
