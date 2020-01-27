package tmvkrpxl0;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.bukkit.command.ConsoleCommandSender;

public class TeamManager {
	private static LinkedHashMap<String, ArrayList<String>> team = null;
	ConsoleCommandSender sender = Core.sender;
	@SuppressWarnings("unchecked")
	protected TeamManager(){
		//여기서 팀 정보를 불러오기도 합니다
		team = (LinkedHashMap<String, ArrayList<String>>) Core.loadfile("plugins/Kukga/team.yml");
		if(team == null)team = new LinkedHashMap<String, ArrayList<String>>();
	}
	
	protected static void save(){
		Core.savefile("plugins/Kukga/team.yml", team);
	}
	
	protected static String getNation(String name) {
		if(!team.keySet().isEmpty())
		for(String s: team.keySet()) {
			for(String k : team.get(s)) {;
				if(name.equals(k))return s;
			}
		}
		return null;
	}
	protected static void createTeam(String Teamname, String Playername) {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(Playername);
		team.put(Teamname, temp);
	}
}
