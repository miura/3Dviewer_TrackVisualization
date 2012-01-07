package emblcmci.view3d;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

public class Plot4dTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testLoadFileVolocity() {
		Plot4d p4d = new Plot4d();
		String path = "/Users/miura/Dropbox/Mette/Tracks.csv";
		ArrayList<TrajectoryObj> trajlist = p4d.loadFileVolocity(path); 
		//fail("Not yet implemented");
	}
	@Test
	public void loadPointsFile() {
		Plot4d p4d = new Plot4d();
		String path = "/Users/miura/Dropbox/Mette/segmentation_z21-47t2-24_3CONVERTED.csv";
		ArrayList<TrajectoryObj> trajlist = p4d.loadFileVolocity(path); 
		//fail("Not yet implemented");
	}	

}
