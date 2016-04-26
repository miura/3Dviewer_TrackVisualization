package de.embl.cmci.view3d;

import static org.junit.Assert.*;
import ij.ImagePlus;

import org.junit.Before;
import org.junit.Test;

public class SingeleTackExtractTest {

	private SingleTackExtract sg;
	private String imgpath;

	@Before
	public void setUp() throws Exception {
//		String srcpath =  "/Users/miura/Desktop/demo/20_23hrfull_corrected_1_6_6.csv";
		//String srcpath =  "C:\\dropbox\\My Dropbox\\Mette\\20_23h\\20_23hrfull_corrected_1_6_6.csv";
		String srcpath =  "/Users/miura/Dropbox/people/Mette/20_23h/20_23hrfull_corrected_1_6_6.csv";
		String imgpath = "/Users/miura/Dropbox/temp/20h_shifted.tif";//Z:\\mette\\20_23h_firstSample\\20h_shifted.tif";
		this.imgpath = imgpath;
		int trackid = 11;
		SingleTackExtract sg = new SingleTackExtract(srcpath, imgpath, trackid);
		this.sg = sg;
		
	}

	@Test
	public void testGetSubstack() {
		ImagePlus imp = sg.getSubstack(imgpath, 1, 5, 5);
		imp.show();
	}

	@Test
	public void testShowSingleTrack() {
		sg.showSingleTrack();
	}

}
