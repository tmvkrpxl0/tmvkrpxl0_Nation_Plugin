package tmvkrpxl0.Config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TerritoryConfig{
	private Map<String, List<BeaconInfo>> locations;
	public Map<String, List<BeaconInfo>> getLocations(){
		return locations;
	}
	
	
	public void setLocations(Map<String, List<BeaconInfo>> m) {
		locations = m;
	}
	
	public TerritoryConfig(Map<String, List<BeaconInfo>> m) {
		locations = m;
	}
	
	public TerritoryConfig() {
		locations = new LinkedHashMap<String, List<BeaconInfo>>();
	}
}
