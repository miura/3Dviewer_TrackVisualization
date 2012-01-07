package emblcmci.view3d;

public class DotObj {
	private double x;
	private double y;
	private double z;
	private double sx;
	private double sy;
	private double sz;
	private double meanint;
	private double frame;

	DotObj(double frame, double x, double y, double z, double sx, double sy, double sz, double meanint) {
		this.frame = frame;
		this.x = x;
		this.y = y;
		this.z = z;
		this.sx = sx;
		this.sy = sy;
		this.sz = sz;	
		this.meanint = meanint; 
	}

}
