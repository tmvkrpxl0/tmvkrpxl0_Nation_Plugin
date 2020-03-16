package tmvkrpxl0.Config;

public class BeaconInfo {
	private String beaconname;
	private int [] beaconlocation;
	private int [][] regionedges;
	
	public String getBeaconName() {
		return beaconname;
	}
	public void setBeaconName(String b) {
		beaconname = b;
	}
	public int [] getBeaconLocation() {
		return beaconlocation;
	}
	public void setBeaconLocation(int [] i) {
		beaconlocation = i;
	}
	public int [][] getRegionEdges() {
		return regionedges;
	}
	public void setRegionEdges(int [][] e) {
		regionedges = e;
	}
}
