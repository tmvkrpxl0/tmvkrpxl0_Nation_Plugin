package tmvkrpxl0.Kukga;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class TeamManager {
	private static Map<String, List<UUID>> team;
	protected static LinkedHashMap<UUID, String> invites;
	ConsoleCommandSender sender = KukgaMain.sender;
	protected TeamManager(){
		//여기서 팀 정보를 불러오기도 합니다
		team = ((tmvkrpxl0.Config.TeamConfig)KukgaMain.loadFile("팀.yml", tmvkrpxl0.Config.TeamConfig.class)).getTeams();
		invites = new LinkedHashMap<UUID, String>();
	}
	
	protected void save(){
		KukgaMain.saveFile("팀.yml", tmvkrpxl0.Config.TeamConfig.class, new tmvkrpxl0.Config.TeamConfig(team));
	}
	
	protected static String getNation(Player player) {
		if(!team.keySet().isEmpty())
		for(String s: team.keySet()) {
			for(UUID k : team.get(s)) {;
				if(player.getUniqueId().equals(k))return s;
			}
		}
		return null;
	}
	protected static void createTeam(String Teamname, Player player) {
		LinkedList<UUID> ls = new LinkedList<UUID>();
		ls.add(player.getUniqueId());
		team.put(Teamname, ls);
		TerritoryManager.addNation(Teamname);
	}
	
	protected static void deleteTeam(String nation){
		deleteTeam(nation, "국가가 멸망했습니다!");
	}
	
	protected static void deleteTeam(String nation, String message) {
		team.remove(nation);
		TerritoryManager.deleteNation(nation);
		BattleManager.remove(nation);
		KukgaMain.broadcast(nation + message);
	}
	protected static boolean invite(Player player, String nation) {
		if(invites.containsKey(player.getUniqueId()))return false;
		invites.put(player.getUniqueId(), nation);
		Bukkit.getPlayer(player.getUniqueId()).sendMessage(nation + "국가에서 초대가 왔습니다. 수락하시겠습니까?(제한시간 30초)");
		Bukkit.getPlayer(player.getUniqueId()).sendMessage("초대 수락:/국가 초대수락");
		Bukkit.getPlayer(player.getUniqueId()).sendMessage("초대 거절:/국가 초대거절");
		new org.bukkit.scheduler.BukkitRunnable() {
			public void run() {
				if(invites.containsKey(player.getUniqueId())) {
					invites.remove(player.getUniqueId());
					Bukkit.getPlayer(player.getUniqueId()).sendMessage("30초가 지나, 초대를 거절하도록 하겠습니다.");
				}
			}
		}.runTaskLater(KukgaMain.plugin, 20 * 30);
		return true;
	}
	
	protected static void joinTeam(Player player, String nation){
		List<UUID> t = team.get(nation);
		t.add(player.getUniqueId());
		team.put(nation, t);
	}
	
	protected static void leaveTeam(Player player, String nation){
		List<UUID> t = team.get(nation);
		t.remove(player.getUniqueId());
		team.put(nation, t);
	}
	
	protected static List<String> getTeam(String nation){
		LinkedList<String> temp = new LinkedList<>();
		for(UUID s : team.get(nation)) {
			if(Bukkit.getPlayer(s)==null)temp.add(Bukkit.getPlayer(s).getName());
		}
		return temp;
	}
	
	protected static Set<String> getTeamList(){
		return team.keySet();
	}
	
	
}
