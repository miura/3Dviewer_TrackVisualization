package de.embl.cmci.view3d;

public class DotObj {
	protected double x;
	protected double y;
	protected double z;
	protected double sx;
	protected double sy;
	protected double sz;
	private double meanint;
	private double frame;

	DotObj(double frame, double x, double y, double z, double sx, double sy, double sz, double meanint) {
		this.setFrame(frame);
		this.x = x;
		this.y = y;
		this.z = z;
		this.sx = sx;
		this.sy = sy;
		this.sz = sz;	
		this.setMeanint(meanint); 
	}

	/**
	 * @param frame the frame to set
	 */
	public void setFrame(double frame) {
		this.frame = frame;
	}

	/**
	 * @return the frame
	 */
	public double getFrame() {
		return frame;
	}

	/**
	 * @param meanint the meanint to set
	 */
	public void setMeanint(double meanint) {
		this.meanint = meanint;
	}

	/**
	 * @return the meanint
	 */
	public double getMeanint() {
		return meanint;
	}

}
