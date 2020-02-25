package tmvkrpxl0.Config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamConfig{
	private Map<String, List<UUID>> teams;
	public TeamConfig(Map<String, List<UUID>> t) {
		teams = t;
	}
	
	public Map<String, List<UUID>>getTeams(){
		return teams;
	}
	
	public void setTeams(Map<String, List<UUID>> t) {
		teams = t;
	}
	
	public TeamConfig() {
		teams = new LinkedHashMap<>();
	}
}
