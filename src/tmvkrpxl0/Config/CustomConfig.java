package tmvkrpxl0.Config;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomConfig{
	private Map<String, Integer> war;
	private Map<String, Integer> Territory;
	
	public Map<String, Integer> getWar() {
		return war;
	}
	
	public void setWar(Map<String, Integer> map) {
		war = map;
	}
	
	public Map<String, Integer> getTerritory() {
		return Territory;
	}
	
	public void setTerritory(Map<String, Integer> map) {
		Territory = map;
	}
	
	public CustomConfig() {
		war = new LinkedHashMap<>();
		Territory = new LinkedHashMap<>();
	}
	
	public CustomConfig(Map<String, Integer> w, Map<String, Integer> t) {
		war = w;
		Territory = t;
	}
}
