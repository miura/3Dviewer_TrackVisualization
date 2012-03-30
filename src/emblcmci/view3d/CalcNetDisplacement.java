package emblcmci.view3d;

import org.apache.commons.math.geometry.euclidean.threed.Vector3D;

public class CalcNetDisplacement {

	/** Calculates displacement vector towards a reference point.
	 * returned value is a DispVec class holding containing displacement vector and direction (1 or -1)  
	 * 
	 * @param sev track start to the end point
	 * @param srv trackstart point to the reference point
	 * @return DispVec instance, holding a Vector3D, the net displacement vector towards a point and its direction  
	 */
	public DispVec calcDisplacementVector(Vector3D sev, Vector3D srv){
		double theta;		//angle made beteen srv and sev
		Vector3D srvDispv;	// projection vector of sev to the srv axis
		theta = Vector3D.angle(srv, sev);
		srvDispv = srv.normalize().scalarMultiply(Math.cos(theta)* sev.getNorm());

		double direc = srv.dotProduct(srvDispv) / srv.getNorm()/srvDispv.getNorm();
		direc = Math.round(direc);
		CalcNetDisplacement cnd = new CalcNetDisplacement();
		DispVec dispvec = cnd. new DispVec(srvDispv, (int) direc);
		return dispvec;
	}
	
	/** Calculates displacement vector towards a reference bar.
	 * returned value is an DispVec class holding containing displacement vector and direction (1 or -1) 
	 * 
	 * @param sev track vector, between start and end
	 * @param rv reference vector
	 * @param pv track start point to reference start point
	 * @param qv track start point to reference end point
	 * @return DispVec instance, holding a Vector3D, the net displacement vector towards a point and its direction
	 * <br>
	 * <br>
	 * followings are calculated in this methods:<br>
	 * 		pvproj --- length of projection of pv onto rv<br>
	 * 		refdash --- above projected vector (scaled rv)<br>
	 * 		dv vector --- starting at track start point, perpendicular to rv. (ends at the tip of refdash)<br>
	 * 		dvdash --- sev projected onto dv. the displacement vector towards reference<br>
	 */
	public DispVec calcDisplacementVector(Vector3D sev, Vector3D rv, Vector3D pv, Vector3D qv){
		double pvproj;			// length of a projection vector of pv to rv. 
		Vector3D refdash;		// a projection vector of pv to rv.
		Vector3D dv;			// vector from track start point to the endpoint of refdash
		double sevproj;			// length of a projection vector of sev to dv
		Vector3D dvdash;		// a net displacement vector against reference point (the answer) 
		
		pvproj = rv.dotProduct(pv) / Math.pow(rv.getNorm(), 2);
		refdash = rv.scalarMultiply(pvproj);
		if (pvproj > 1)
			dv = qv;
		else if (pvproj < 0)
			dv = pv;
		else
			dv = refdash.subtract(pv);	
		
		sevproj = dv.dotProduct(sev) / Math.pow(dv.getNorm(), 2);
		dvdash = dv.scalarMultiply(sevproj);
//		IJ.log(Double.toString(dv.dotProduct(dvdash)) + "\t" 
//				+ Double.toString(dv.getNorm())+"\t"
//				+ Double.toString(dvdash.getNorm()));
		double direc = dv.dotProduct(dvdash) / dv.getNorm()/dvdash.getNorm();
		direc = Math.round(direc);	
		DispVec dispvec = new DispVec(dvdash, (int) direc);
		return dispvec;

	}
	public class DispVec {
		public final Vector3D dv;
		public final int direc;
		public DispVec(Vector3D dv, int direc){
			this.dv = dv;
			this.direc = direc;
		}
	}
}
