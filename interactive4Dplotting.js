// 4D plotter
// imports csv file (with 3D coordinates) and plots trajectories in various ways 
// in 3Dviewer. 
//
// Kota Miura


importClass(Packages.javax.vecmath.Point3f);
importClass(Packages.java.util.Vector);
importClass(Packages.java.util.ArrayList);
importPackage(Packages.util.opencsv);
importPackage(java.io);
importClass(Packages.javax.vecmath.Color3f);
importPackage(Packages.ij3d);
importClass(Packages.customnode.CustomTriangleMesh);
importClass(Packages.customnode.CustomMesh);
importClass(Packages.customnode.CustomMultiMesh);
importClass(Packages.customnode.CustomPointMesh);
importClass(Packages.customnode.CustomLineMesh);
importClass(Packages.org.apache.commons.math.geometry.euclidean.threed.Vector3D);

// laoding track data to a List (vector)
// file is direct out put of ImageJ results table.
// (volocity output converted by volocity importer javascript)
// row0 index
// row1 trajectoryID
// row2 
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
/*
Mosaic ParticleTracker 2D/3D ImageJ plugin results tatble
0: index
1: trackID (from 1)
2: FrameNumber (from 0)
3: y
4: x
5: z
*/
function loadMosaicFile(datapath){

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
				IJ.log(Double.toString(currentTrajID) + cA[1]);
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
 			var xyscale = 0.4131612;
 			var zscale = 0.7553099; 
 			atraj.add(Point3f(Double.valueOf(cA[4])*xyscale,Double.valueOf(cA[3])*xyscale,Double.valueOf(cA[5])*zscale)); 
 			timepoints.add(Double.valueOf(cA[2]));  
		}
		counter++;
	}
	return trajlist;
}

//volocity, original file before tracking. 
function loadPointsFile(datapath){

	var reader = new CSVReader(new FileReader(datapath), ",");
	var ls = reader.readAll();
	var it = ls.iterator();
	var counter = 0;
	var currentTrajID = 1.0;
	var coords = Vector();
	while (it.hasNext()){
		var cA = it.next();
		if (counter != 0){
			var pf = Double.valueOf(cA[2]);
			var pmeanint = Double.valueOf(cA[7]);
			var px = Double.valueOf(cA[10]);
			var py = Double.valueOf(cA[11]);
			var pz = Double.valueOf(cA[12]);
			var sx = Double.valueOf(cA[13]);
			var sy = Double.valueOf(cA[14]);
			var sz = Double.valueOf(cA[15]);											
			var dotObj = new DotObj(pf, px, py, pz, sx, sy, sz, pmeanint);
			coords.add(dotObj);
		}
		counter++;
	}
	return coords;
}



// trajectory as an object. 
function trajectoryObj(id, dotList, timepoints) {
	this.id = id;
	this.dotList = dotList;
	this.timepoints = timepoints; //a vector tith time points of the trajectory. 
}

//pointObject
function DotObj(frame, x, y, z,sx, sy, sz, meanint) {
	this.frame = frame;
	this.x = x;
	this.y = y;
	this.z = z;
	this.sx = sx;
	this.sy = sy;
	this.sz = sz;	
	this.meanint = meanint; 
}


/*
algorithm for dynamic plotting. 
for each time point, create gourp of mesh.
add the results to the time point (iterate this)
*/

//check if a time point is included in the trajectory. 
//(int, vector)
function CheckTimePointExists(thistimepoint, timepoints){
	var includesthistime = false;
	if ((timepoints.get(0) <= thistimepoint) && (timepoints.get(timepoints.size()-1) >= thistimepoint)){
		includesthistime = true;
	}
	return includesthistime;
}

function ReturnIndexFromTime(srctime, timepoints){
	var index = -1;
	for (var i = 0; i < timepoints.size(); i++){
		if (srctime == timepoints.get(i))
			index = i;
	}
	return index;
}

// for checking single track, assigned by trajectoryID. 
// plot over trajetory. 
function SingleTrackPointPlot(id, tList, univ){
	var curtraj = tList.get(0);
	var j = 0;
	while (curtraj.id != id){
		curtraj = tList.get(j); 
		j++;
	}
	IJ.log("selected ID: " + curtraj.id);
	var dt = curtraj.dotList;
	var pathextract = Vector();
	pathextract.addAll(curtraj.dotList);
			//pathextract.removeRange(i+1, pathextract.size()-1);
	var timeextract = Vector();
	timeextract.addAll(curtraj.timepoints);
	for (var i = 0; i < curtraj.timepoints.size(); i++){
		//var clmmProLine = CustomMultiMesh();
		var singlepoint = Vector();
		singlepoint.add(curtraj.dotList.get(i));
		var cmp = new CustomPointMesh(singlepoint, Color3f(1.0, 1.0, 1.0), 0);
		//clmmProLine.add(cmp);
		cmp.setPointSize(5);
		var framepoint = curtraj.timepoints.get(i);
//		cc = ContentCreator.createContent(clmmProLine, "ptime" + Integer.toString(framepoint), framepoint);
		var cp = ContentCreator.createContent(cmp, "ptime" + Integer.toString(framepoint), framepoint);

		univ.addContent(cp);
	}
}

//20111216 place a sphere at defined position
function putSphere(univ, x, y, z, r, merid, para, color, name){
	var msp = Mesh_Maker.createSphere(x, y, z, r, merid, para);
	univ.addTriangleMesh(msp, color, name);
}

//20111219 plot color coded track using tube-mesh
//20111220 updated, adding progressive tracks. 
//progressive version
function PlotTimeColorCodedTrack(timestart, timeend, tList, univ){
	multiMeshA = ArrayList();
	for (var i = timestart; i < timeend-1; i++){
		multiMeshA.add(CustomMultiMesh());	
	}
	for (var i = timestart; i < timeend-1; i++){
		var tubes = Vector();
		for (var j = 0; j < tList.size(); j++) {
			var curtraj = tList.get(j);
			if (CheckTimePointExists(i, curtraj.timepoints) && CheckTimePointExists(i+1, curtraj.timepoints)){
				var dt = curtraj.dotList;
				var pathextract = Vector();
				pathextract.addAll(curtraj.dotList); //required in javascript
				var timeextract = Vector();
				timeextract.addAll(curtraj.timepoints); //required in javascript as well
				var ind = timeextract.indexOf(i);
				var spoint = pathextract.get(ind);
				var epoint = pathextract.get(ind+1);			
				var xA = javaArray(spoint.x, epoint.x);
				var yA = javaArray(spoint.y, epoint.y);
				var zA = javaArray(spoint.z, epoint.z);
				var rA = javaArray(0.2, 0.2);					
				var tube = Mesh_Maker.createTube(xA, yA, zA, rA, 24, false); //true makes cone shape
				tubes.addAll(tube);
				IJ.log("index"+j + " frame" + i);
			}
		}
		var cR = i/(timeend -1 - timestart);
		var cB = 1 - cR; 
		//adding progressive tracks to custommultimesh
		for (var j = i-timestart; j<timeend-timestart-1; j++ ){
			var csp = CustomTriangleMesh(tubes, Color3f(cR, 0.6, cB), 0.0);
			multiMeshA.get(j).add(csp);
		}
	}
	//var ccs = ContentCreator.createContent(csp, "tubetime" + Integer.toString(i), i-timestart+1);
	//	var ccs = ContentCreator.createContent(csp, "tubetime" + Integer.toString(i), 0);
	for (var i = 0; i < multiMeshA.size(); i++){
		var ccs = ContentCreator.createContent(multiMeshA.get(i), "tubetime" + Integer.toString(i), i+1);
		univ.addContent(ccs);
	}		
}

//above function modified, so that uses line instead of tubes (memory saving)
//progressive version
function PlotTimeColorCodedLine(timestart, timeend, tList, univ){
	multiMeshA = ArrayList();
	var clmmProLine = CustomMultiMesh();
	for (var i = timestart; i < timeend-1; i++){
		multiMeshA.add(CustomMultiMesh());	
	}
	for (var i = timestart; i < timeend-1; i++){
		var tubes = Vector();
		var cR = i/(timeend -1 - timestart);
		var cB = 1 - cR;		
		for (var j = 0; j < tList.size(); j++) {
			var curtraj = tList.get(j);
			if (CheckTimePointExists(i, curtraj.timepoints) && CheckTimePointExists(i+1, curtraj.timepoints)){
				var dt = curtraj.dotList;
				var pathextract = Vector();
				pathextract.addAll(curtraj.dotList); //required in javascript
				var timeextract = Vector();
				timeextract.addAll(curtraj.timepoints); //required in javascript as well
				var ind = timeextract.indexOf(i);
									
				//var clm = CustomLineMesh(pathextract.subList(ind, ind+2), CustomLineMesh.CONTINUOUS, col, 0);			
				//var clm = CustomLineMesh(pathextract.subList(ind, ind+2), CustomLineMesh.CONTINUOUS, Color3f(cR, 0.6, cB), 0);			
				//tubes.add(clm);
				tubes.add(pathextract.subList(ind, ind+2));
				IJ.log("index"+j + " frame" + i);
			}
		}
 
		//adding progressive tracks to custommultimesh
		for (var j = i-timestart; j<timeend-timestart-1; j++ ){
			//var csp = CustomTriangleMesh(tubes, Color3f(cR, 0.6, cB), 0.0);
			for (var k = 0; k < tubes.size(); k++){
				var clm = CustomLineMesh(tubes.get(k), CustomLineMesh.CONTINUOUS, Color3f(cR, 0.6, cB), 0);
				multiMeshA.get(j).add(clm);
			}
		}
	}
	//var ccs = ContentCreator.createContent(csp, "tubetime" + Integer.toString(i), i-timestart+1);
	//	var ccs = ContentCreator.createContent(csp, "tubetime" + Integer.toString(i), 0);
	for (var i = 0; i < multiMeshA.size(); i++){
		var ccs = ContentCreator.createContent(multiMeshA.get(i), "tubetime" + Integer.toString(i), i+1);
		univ.addContent(ccs);
	}		
}

// plots only the last frame, showing all trajectories time-colorcoded
function PlotTimeColorCodedLineOnlyFinalFrame(timestart, timeend, tList, univ){
	var LineMultiMesh = CustomMultiMesh();
	var clmmProLine = CustomMultiMesh();
	for (var i = timestart; i < timeend-1; i++){
		var tubes = Vector();
		var cR = i/(timeend -1 - timestart);
		var cB = 1 - cR;				
		for (var j = 0; j < tList.size(); j++) {
			var curtraj = tList.get(j);
			if (CheckTimePointExists(i, curtraj.timepoints) && CheckTimePointExists(i+1, curtraj.timepoints)){
				var dt = curtraj.dotList;
				var pathextract = Vector();
				pathextract.addAll(curtraj.dotList); //required in javascript
				var timeextract = Vector();
				timeextract.addAll(curtraj.timepoints); //required in javascript as well
				var ind = timeextract.indexOf(i);
									
				//var clm = CustomLineMesh(pathextract.subList(ind, ind+2), CustomLineMesh.CONTINUOUS, col, 0);			
				//var clm = CustomLineMesh(pathextract.subList(ind, ind+2), CustomLineMesh.CONTINUOUS, Color3f(cR, 0.6, cB), 0);			
				//tubes.add(clm);
				tubes.add(pathextract.subList(ind, ind+2));
				IJ.log("index"+j + " frame" + i);
			}
		}
 
		//adding progressive tracks to custommultimesh
		//var csp = CustomTriangleMesh(tubes, Color3f(cR, 0.6, cB), 0.0);
		for (var k = 0; k < tubes.size(); k++){
			var clm = CustomLineMesh(tubes.get(k), CustomLineMesh.CONTINUOUS, Color3f(cR, 0.6, cB), 0.4);
			LineMultiMesh.add(clm);
		}

	}
	//var ccs = ContentCreator.createContent(csp, "tubetime" + Integer.toString(i), i-timestart+1);
	//	var ccs = ContentCreator.createContent(csp, "tubetime" + Integer.toString(i), 0);
	var ccs = ContentCreator.createContent(LineMultiMesh, "colorcodedTracks", 0);
	univ.addContent(ccs);		
}

// progressive tracks
function plotProgressiveLineTrack(timestart, timeend, tList, univ){
	for (var i = timestart; i < timeend; i++){
		var clmmProLine = CustomMultiMesh();
		for (var j = 0; j < tList.size(); j++) {
			var curtraj = tList.get(j);
			if (CheckTimePointExists(i, curtraj.timepoints)){
			//IJ.log(curtraj.id);
				var dt = curtraj.dotList;
				var pathextract = Vector();
				pathextract.addAll(curtraj.dotList);
			//pathextract.removeRange(i+1, pathextract.size()-1);
				var timeextract = Vector();
				timeextract.addAll(curtraj.timepoints);
				var ind = timeextract.indexOf(i);
			// limit the length of trajectoy
				var startind = ind -3;
				if (startind <= 0) 
					pathextract = pathextract.subList(0, ind+1);	//full track to current time point
				else
					pathextract = pathextract.subList(startind, ind+1);	//partial trajectory 			
				var clm = CustomLineMesh(pathextract, CustomLineMesh.CONTINUOUS, col, 0);
				clmmProLine.add(clm);
				clm.setLineWidth(2);
			}
		}
		cc = ContentCreator.createContent(clmmProLine, "time" + Integer.toString(i), i-timestart);
		univ.addContent(cc);
		IJ.log("timepoint:" + Integer.toString(i) + " ...plotted");	
	}
}

//geenrates a java array from two doubles
function javaArray(sp, ep){
	ja = new java.lang.reflect.Array.newInstance(java.lang.Double.TYPE, 2);
	ja[0] = sp;
	ja[1] = ep;
	return ja;
}

//spheres from trajectories 20111216
function plotTrajectorySpheres(timestart, timeend, tList, univ){
	for (var i = timestart; i < timeend; i++){
		var spheres = Vector();
		for (var j = 0; j < tList.size(); j++) {
			var curtraj = tList.get(j);
			if (CheckTimePointExists(i, curtraj.timepoints)){
				IJ.log(curtraj.id);
				var ind = curtraj.timepoints.indexOf(i);
				var p3f = curtraj.dotList.get(ind);
				var curtime = curtraj.timepoints.get(ind);
				var sphere = Mesh_Maker.createSphere(p3f.x, p3f.y, p3f.z, 2.0, 24.0, 24.0);
				spheres.addAll(sphere);
			}
		}
		var csp = CustomTriangleMesh(spheres, colw, 0.0);
		var ccs = ContentCreator.createContent(csp, "dotstime" + Integer.toString(i), i-timestart);
		univ.addContent(ccs);
	}
}

//spheres from dotlists 20111216
// direct plotting of segmented dots, without tracking
function plotSpheresFromDotLists(timestart, timeend, pList, univ){
  for (var i = timestart; i < timeend; i++){
    var spheres = Vector();
    for (var j = 0; j < pList.size(); j++) {
      var curdot = pList.get(j);
      if (curdot.frame -1  == i){
        var factor = Math.round(curdot.meanint/200 * 24);
        //var sphere = Mesh_Maker.createSphere(curdot.sx, curdot.sy, curdot.sz, 0.7, factor, factor);
        var sphere = Mesh_Maker.createSphere(curdot.sx, curdot.sy, curdot.sz, 0.7, 12, 12);

        spheres.addAll(sphere);
        IJ.log("index"+j + " frame" + i + " intfactor:"+factor);
      }
    }
    var csp = CustomTriangleMesh(spheres, colw, 0.0);
    var ccs = ContentCreator.createContent(csp, "dotstime" + Integer.toString(i), i-timestart);
    univ.addContent(ccs);
  }
}

// Distance color coding 20111221
//above function modified, so that uses line instead of tubes (memory saving)
//
//single frame 3D
function PlotDistancefromRefPointColorCodedLine(timestart, timeend, tList, univ, rx, ry, rz){
	multiMeshA = ArrayList();
	var clmmProLine = CustomMultiMesh();
	for (var i = timestart; i < timeend-1; i++){
		multiMeshA.add(CustomMultiMesh());	
	}
  	var maxmin = getMaxDist(tList, rx, ry, rz);
  	var maxdist = maxmin[0];
  	var mindist = maxmin[1];
  	IJ.log("min distance = " + mindist);  	
  	IJ.log("max distance = " + maxdist);
	for (var i = timestart; i < timeend-1; i++){
		var tubes = ArrayList();
		var distA = []; //js array
		var cR = i/(timeend -1 - timestart);
		var cB = 1 - cR;		
		for (var j = 0; j < tList.size(); j++) {
			var curtraj = tList.get(j);
			if (CheckTimePointExists(i, curtraj.timepoints) && CheckTimePointExists(i+1, curtraj.timepoints)){
				var dt = curtraj.dotList;
				var pathextract = Vector();
				pathextract.addAll(curtraj.dotList); //required in javascript
				var timeextract = Vector();
				timeextract.addAll(curtraj.timepoints); //required in javascript as well
				var ind = timeextract.indexOf(i);
									
				//var clm = CustomLineMesh(pathextract.subList(ind, ind+2), CustomLineMesh.CONTINUOUS, col, 0);			
				//var clm = CustomLineMesh(pathextract.subList(ind, ind+2), CustomLineMesh.CONTINUOUS, Color3f(cR, 0.6, cB), 0);			
				//tubes.add(clm);
				tubes.add(pathextract.subList(ind, ind+2));
				distA.push(calcDist(pathextract.get(ind), pathextract.get(ind+1), rx, ry, rz));
				IJ.log("index"+j + " frame" + i + " distance=" + distA[distA.length-1]);
			}
		}
 		IJ.log("stored line segments:" + tubes.size());		
 		IJ.log("stored distances:" + distA.length);
		//adding progressive tracks to custommultimesh
		for (var j = i-timestart; j<timeend-timestart-1; j++ ){
			//var csp = CustomTriangleMesh(tubes, Color3f(cR, 0.6, cB), 0.0);
			for (var k = 0; k < tubes.size(); k++){
				cB = (distA[k]-mindist)/(maxdist-mindist);
				cR = 1-cB;
				var clm = CustomLineMesh(tubes.get(k), CustomLineMesh.CONTINUOUS, Color3f(cR, 0.6, cB), 0);
				multiMeshA.get(j).add(clm);
			}
		}
	}
	//case showing progressive tracks
//	for (var i = 0; i < multiMeshA.size(); i++){
//		var ccs = ContentCreator.createContent(multiMeshA.get(i), "tubetime" + Integer.toString(i), i+1);
//		univ.addContent(ccs);
//	}
	//case with only one last frame to show tracks
	var ccs = ContentCreator.createContent(multiMeshA.get(multiMeshA.size()-1), "tracks", 0);
	univ.addContent(ccs);	
	
        var referencepoint = Mesh_Maker.createSphere(rx, ry, rz, 2, 12, 12);
	var refmesh = CustomTriangleMesh(referencepoint, Color3f(1,0,0), 0.0);
	var refcont = ContentCreator.createContent(refmesh, "referencePoint", 0);
	univ.addContent(refcont);			
}

// calculates distance between mid point of the first two arguments and a reference point given
// as rx, ry, rz
function calcDist(p3df1, p3df2, rx, ry, rz){
	var cx = (p3df1.x + p3df2.x)/2;
	var cy = (p3df1.y + p3df2.y)/2;
	var cz = (p3df1.z + p3df2.z)/2;		
	var sqdist = Math.pow(cx -rx, 2) + Math.pow(cy - ry, 2) + Math.pow(cz - rz, 2);
	return Math.pow(sqdist, 0.5);
}
//calculate distance between two points
function calcDist2(p3df, rx, ry, rz){
	var sqdist = Math.pow(p3df.x -rx, 2) + Math.pow(p3df.y - ry, 2) + Math.pow(p3df.z - rz, 2);
	return Math.pow(sqdist, 0.5);
}

function getMaxDist(tList, rx, ry, rz){
	var max = 0;
	var min = 1000000;
	for (var j = 0; j < tList.size(); j++) {
		var curtraj = tList.get(j);
		var pathextract = ArrayList();
		pathextract.addAll(curtraj.dotList); //required in javascript
        	for (var i = 0; i < pathextract.size(); i++){
          		var dd = calcDist2(pathextract.get(i), rx, ry, rz);
          		if (dd > max) max = dd;
          		if (dd < min) min = dd;
        	}
	}
	maxmin = [max, min];
	return maxmin;
}

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


//---------- example hemisphere
//imagepath = '/Volumes/cmci/mette/f1_3.tif';	//test mosaic
//timestart = 0; timeend = 3;
imagepath = '/Users/miura/Desktop/s1_gb1new.tif';
timestart = 0; timeend = 23;

filepath = "/Users/miura/Dropbox/Mette/Tracks.csv";
filepathm = '/Volumes/cmci/mette/PTtrack8_0001_1.csv';	//mosaic output test
filepathm = '/Volumes/cmci/mette/PT_8_0001_1_10.csv' // mosaic output
rx = 117;
ry = 95;
rz = 88;

// -------------------- 27h
/*
imagepath = '/Users/miura/Desktop/27h.tif';
filepath = '/Volumes/cmci/mette/27h_/data27_cut0_1_6_6cc.csv'; // 27h sequence
originalpath = '/Volumes/cmci/mette/27h_/data27_cut0.csv';
*/
filepath = '/Volumes/cmci/mette/data20111213cut500_1_5_10.csv';
filepath = '/Volumes/cmci/mette/data20111213cut500_1_6_4cc.csv'; //intensity biased
filepath = '/Users/miura/Dropbox/Mette/27h/data20111213cut500_1_6_4cc.csv'; //intensity biased
originalpath = '/Users/miura/Dropbox/Mette/27h/data20111213cut500.csv';
filepath = '/Users/miura/Dropbox/Mette/27h/data27_cut0_1_6_6cc.csv'
timestart = 0; timeend = 24;
rx = 123;
ry = 109;
rz = 45;


//imp = IJ.openImage(imagepath); //temp out 20111216

tList = loadFile(filepath);		//volocity file (should be converted)
//tList = loadMosaicFile(filepathm);	//particle tracker file
IJ.log("tracks:" + tList.size());

pList = loadPointsFile(originalpath);

univ = new Image3DUniverse();
univ.show();

col = Color3f(0, 1.0, 0.5);
col2 = Color3f(1.0, 0, 0);
colw = Color3f(1.0, 1.0, 1.0);

channelswitch = java.lang.reflect.Array.newInstance(java.lang.Boolean.TYPE, 3);
channelswitch[0] = true;
channelswitch[1] = true;
channelswitch[2] = true;


/* //add volume*/
//c = univ.addVoltex(imp, Color3f(1.0, 1.0, 1.0), "vol", 40, channelswitch, 2);

/* //add surface */
//c = univ.addMesh(imp, col2, "surface", 130, channelswitch, 2);

//c.setTransparency(0.3);
//tl = univ.getTimeline();


//clmmProLine = CustomMultiMesh();

/* a point plotting, for checking trajectories*/
//SingleTrackPointPlot(1, tList, univ);

/* plots trajectory spots dynamically*/
// plotTrajectorySpheres(timestart, timeend, tList, univ);

/* plots progressive track, no color coding of time points. */
// plotProgressiveLineTrack(timestart, timeend, tList, univ);

/* plot progressive trajectory using mesh-tube objects, memory intensive */
//PlotTimeColorCodedTrack(timestart, timeend, tList, univ);

/* plot progressive trajectories using line objects. less memory */
//PlotTimeColorCodedLine(timestart, timeend, tList, univ); 

// plots only the last frame, showing all trajectories time-colorcoded
PlotTimeColorCodedLineOnlyFinalFrame(timestart, timeend, tList, univ)

/* plot all spheres that was segmented, using the particle list before linking*/
//plotSpheresFromDotLists(timestart, timeend, pList, univ)

/* plot trajectories, color coded distance from the reference point*/
//PlotDistancefromRefPointColorCodedLine(timestart, timeend, tList, univ, rx, ry, rz)

/* plots single 3D frame with net displacement indicated */
plotTrackDisplacements(timestart, timeend, tList, univ, rx, ry, rz)

IJ.log("plotting done");
//imp.flush();
IJ.freeMemory();

