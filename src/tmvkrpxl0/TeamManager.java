package tmvkrpxl0;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class TeamManager {
	private static LinkedHashMap<String, ArrayList<String>> team = null;
	protected static LinkedHashMap<String, String> invites;
	ConsoleCommandSender sender = Core.sender;
	@SuppressWarnings("unchecked")
	protected TeamManager(){
		//여기서 팀 정보를 불러오기도 합니다
		team = (LinkedHashMap<String, ArrayList<String>>) Core.loadfile("plugins/Kukga/team.yml");
		if(team == null)team = new LinkedHashMap<String, ArrayList<String>>();
		invites = new LinkedHashMap<String, String>();
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
	
	protected static void deleteTeam(String nation) {
		team.remove(nation);
		TerritoryManager.deleteRegion(nation);
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
	
	protected static void joinTeam(String playername, String nation) {
		ArrayList<String> t = team.get(nation);
		t.add(playername);
		team.put(nation, t);
	}
	
	protected static void leaveTeam(String playername, String nation, boolean force) {
		ArrayList<String> t = team.get(nation);
		t.remove(playername);
		for(String p : t) {
			Bukkit.getPlayerExact(p).sendMessage(playername + "님께서 국가" + (force ? "에서 추방당하셨습니다!" : "에서 탈퇴하셨습니다!"));
		}
		team.put(nation, t);
	}
}
