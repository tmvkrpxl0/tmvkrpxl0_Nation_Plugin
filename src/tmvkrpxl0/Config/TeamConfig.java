package tmvkrpxl0.Config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TeamConfig{
	private Map<String, List<String>> teams;
	public TeamConfig(Map<String, List<String>> t) {
		teams = t;
	}
	
	public Map<String, List<String>>getTeams(){
		return teams;
	}
	
	public void setTeams(Map<String, List<String>> t) {
		teams = t;
	}
	
	public TeamConfig() {
		teams = new LinkedHashMap<>();
	}
}
