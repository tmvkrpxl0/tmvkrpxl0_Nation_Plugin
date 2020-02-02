package tmvkrpxl0;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class TeamManager {
	private static LinkedHashMap<String, LinkedList<String>> team;;
	protected static LinkedHashMap<String, String> invites;
	private FileConfiguration data;
	ConsoleCommandSender sender = Core.sender;
	@SuppressWarnings("unchecked")
	protected TeamManager(){
		//여기서 팀 정보를 불러오기도 합니다
		data = YamlConfiguration.loadConfiguration(new File(Core.plugin.getDataFolder(), "팀.yml"));
		team = new LinkedHashMap<String, LinkedList<String>>();
		if(data.getConfigurationSection("팀")!=null) {
			Map<String, Object> t = data.getConfigurationSection("팀").getValues(false);
			for(String s : t.keySet()) {
				LinkedList<String> ls = new LinkedList<String>();
				for(String as : (ArrayList<String>)t.get(s)) {
					ls.add(as);
				}
				team.put(s, ls);
			}
		}
		invites = new LinkedHashMap<String, String>();
	}
	
	protected void save(){
		data.createSection("팀", team);
		try {
			data.save(new File(Core.plugin.getDataFolder(), "팀.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		LinkedList<String> ls = new LinkedList<String>();
		ls.add(Playername);
		team.put(Teamname, ls);
		TerritoryManager.addNation(Teamname);
	}
	
	protected static void deleteTeam(String nation){
		team.remove(nation);
		TerritoryManager.deleteNation(nation);
	}
	
	protected static boolean invite(String playername, String nation) {
		if(invites.get(playername)!=null)return false;
		invites.put(playername, nation);
		Bukkit.getPlayerExact(playername).sendMessage(nation + "국가에서 초대가 왔습니다. 수락하시겠습니까?(제한시간 30초)");
		Bukkit.getPlayerExact(playername).sendMessage("초대 수락:/국가 초대수락");
		Bukkit.getPlayerExact(playername).sendMessage("초대 거절:/국가 초대거절");
		new org.bukkit.scheduler.BukkitRunnable() {
			public void run() {
				if(invites.get(playername)!=null) {
					invites.remove(playername);
					Bukkit.getPlayerExact(playername).sendMessage("30초가 지나, 초대를 거절하도록 하겠습니다.");
				}
			}
		}.runTaskLater(Core.plugin, 20 * 30);
		return true;
	}
	
	protected static void joinTeam(String playername, String nation){
		LinkedList<String> t = team.get(nation);
		t.add(playername);
		team.put(nation, t);
	}
	
	protected static void leaveTeam(String playername, String nation){
		LinkedList<String> t = team.get(nation);
		t.remove(playername);
		team.put(nation, t);
	}
	
	protected static LinkedList<String> getTeam(String nation){
		return team.get(nation);
	}
	
	protected static Set<String> getTeamList(){
		return team.keySet();
	}
}
