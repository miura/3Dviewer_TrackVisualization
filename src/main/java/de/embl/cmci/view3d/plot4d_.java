package de.embl.cmci.view3d;

import ij.plugin.PlugIn;

public class plot4d_ implements PlugIn {
	
		@Override
		public void run(String arg) {
			// TODO Auto-generated method stub
			DialogVisualizeTracks dg = new DialogVisualizeTracks();
			dg.showDialog();
		}

}

