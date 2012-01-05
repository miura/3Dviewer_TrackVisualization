// displacement of each trajectory is plotted
// direction against reference point (rx, ry, rz) is calculated.

importClass(Packages.org.apache.commons.math.geometry.euclidean.threed.Vector3D);
importClass(Packages.javax.vecmath.Point3f);
importClass(Packages.java.util.Vector);
importPackage(Packages.util.opencsv);
importPackage(java.io);
importPackage(Packages.ij3d);
importClass(Packages.customnode.CustomLineMesh);
importClass(Packages.customnode.CustomMesh);
importClass(Packages.customnode.CustomMultiMesh);
importClass(Packages.javax.vecmath.Color3f);
importClass(Packages.customnode.CustomTriangleMesh);

filepath = 'C:\\dropbox\\My Dropbox\\Mette\\Tracks.csv';
filepath = '/Users/miura/Dropbox/Mette/Tracks.csv'; //sample
filepath = '/Users/miura/Dropbox/Mette/27h/data20111213cut500_1_6_4cc.csv'; //intensity biased
timestart = 0; timeend = 23;
rx = 117;
ry = 95;
rz = 88;

//27h
filepath = '/Users/miura/Dropbox/Mette/27h/data27_cut0_1_6_6cc.csv'
timestart = 0; timeend = 24;
rx = 123;
ry = 109;
rz = 45;

tList = loadFile(filepath);


univ = new Image3DUniverse();
univ.show();
plotTrackDisplacements(timestart, timeend, tList, univ, rx, ry, rz);

// plots net displacement vector towards a reference point 
function plotTrackDisplacements(timestart, timeend, tList, univ, rx, ry, rz){
	dispA = []; //displacements array
	vecs = Vector();
	dispvecs = Vector();
	startPoints = Vector();
	awaytowardsA = [];
	for (var j = 0; j < tList.size(); j++) {
		var cvec = Vector();
		var dvec = Vector();
		var curtraj = tList.get(j);
		var dt = curtraj.dotList;
		var pathextract = Vector();
		pathextract.addAll(curtraj.dotList);
		//var timeextract = Vector();
		//timeextract.addAll(curtraj.timepoints);

		var spoint = pathextract.get(0);
		var epoint = pathextract.get(pathextract.size()-1);
		var srv = Vector3D(rx - spoint.x, ry - spoint.y, rz - spoint.z); //startpoint to reference point vector
		var sev = Vector3D(epoint.x - spoint.x, epoint.y - spoint.y, epoint.z - spoint.z); //startpoint to reference point vector
		var theta = Vector3D.angle(srv, sev);
		var srvDispv = srv.normalize().scalarMultiply(Math.cos(theta)* sev.getNorm());
		var displacement = srvDispv.getNorm();
		if (Math.cos(theta) < 0) {
			displacement *= -1;
			awaytowardsA.push(-1); //away 
		} else
			awaytowardsA.push(1); //towards 		
		dispA.push(displacement);
		if (j == 0) IJ.log("id\t" + "theta\t" + "CosTheta\t" + "displacement");
		IJ.log("" +j + "\t" + theta + "\t" + Math.cos(theta) + "\t" + displacement);
		cvec.add(spoint);
		cvec.add(epoint);
		vecs.add(cvec);
		startPoints.add(spoint);

		//displacement vector along reference axis
		dvec.add(spoint);
		dvec.add(Point3f(spoint.x + srvDispv.getX(), spoint.y + srvDispv.getY(), spoint.z + srvDispv.getZ()));
		dispvecs.add(dvec);
		
	}
	var minval = Math.min.apply(Math, dispA);
	maxval = Math.max.apply(Math, dispA);
	IJ.log("Max displacement" + maxval);
	IJ.log("Min displacement" + minval);
	var maxdisp = maxval;
	if (Math.abs(maxval) < Math.abs(minval))  
		maxdisp = minval;
	else
		maxdisp = maxval;
	var cR = 0;
	var cG = 0;
	var cB = 0;
	var clmmProLine = CustomMultiMesh();
	var clmmDispLine = CustomMultiMesh();
	var spheres = Vector();
	for (var j = 0; j < vecs.size(); j++){	
		cR =0; cG = 0.6; cB = 0;
		if (dispA[j] > 0)
			cR = dispA[j]/maxdisp;
		else
			cB = Math.abs(dispA[j])/maxdisp;
			
		var clm = CustomLineMesh(vecs.get(j), CustomLineMesh.CONTINUOUS, Color3f(cR, cG, cB), 0);
		clmmProLine.add(clm);
		clm.setLineWidth(2);

		if (awaytowardsA[j]>0)
			dispcol = Color3f(1,0,0);
		else			
			dispcol = Color3f(0,0,1);
		var clmdisp = CustomLineMesh(dispvecs.get(j), CustomLineMesh.CONTINUOUS, dispcol, 0);
		clmmDispLine.add(clmdisp);
		
		var sphere = Mesh_Maker.createSphere(startPoints.get(j).x, startPoints.get(j).y, startPoints.get(j).z, 0.7, 12.0, 12.0);
		spheres.addAll(sphere);		
	}
	//cc = ContentCreator.createContent(clmmProLine, "displacements" + Integer.toString(i), i-timestart);
	var cc = ContentCreator.createContent(clmmProLine, "displacements", 0);	
	univ.addContent(cc);

	var cc2 = ContentCreator.createContent(clmmDispLine, "displacementsAxis", 0);	
	univ.addContent(cc2);
		
	var csp = CustomTriangleMesh(spheres, Color3f(1,1,1), 0.0);
	var ccs = ContentCreator.createContent(csp, "startpoints", 0);
	univ.addContent(ccs);
	
	var referencepoint = Mesh_Maker.createSphere(rx, ry, rz, 2, 12, 12);
	var refmesh = CustomTriangleMesh(referencepoint, Color3f(1,0,0), 0.0);
	var refcont = ContentCreator.createContent(refmesh, "referencePoint", 0);
	univ.addContent(refcont);
	
}
function loadFile(datapath){

	var reader = new CSVReader(new FileReader(datapath), ",");
	var ls = reader.readAll();
	var it = ls.iterator();
	var counter = 0;
	var currentTrajID = 1.0;
	var atraj = Vector();
	var timepoints = Vector();
	var trajlist = Vector();
	while (it.hasNext()){
		var cA = it.next();
		if (counter != 0){
			if ((currentTrajID - Double.valueOf(cA[1]) != 0) && (atraj.size() > 0)){
				//IJ.log(Double.toString(currentTrajID) + cA[1]);
				var atrajObj = new trajectoryObj(currentTrajID, atraj, timepoints);
				trajlist.add(atrajObj);
				currentTrajID = Double.valueOf(cA[1]);
				//cvec.clear();
				atraj = Vector();
				timepoints = Vector();
			}
			// pixel positions
 			//cvec.add(Point3f(Double.valueOf(cA[3]),Double.valueOf(cA[4]),Double.valueOf(cA[5])));
 			// scaled positions
 			atraj.add(Point3f(Double.valueOf(cA[6]),Double.valueOf(cA[7]),Double.valueOf(cA[8]))); 
 			timepoints.add(Double.valueOf(cA[2]));  
		}
		counter++;
	}
	return trajlist;
}

// trajectory as an object. 
function trajectoryObj(id, dotList, timepoints) {
	this.id = id;
	this.dotList = dotList;
	this.timepoints = timepoints; //a vector tith time points of the trajectory. 
}