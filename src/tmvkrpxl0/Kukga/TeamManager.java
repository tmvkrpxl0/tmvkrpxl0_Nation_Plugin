package tmvkrpxl0.Kukga;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class TeamManager {
	protected static LinkedHashMap<UUID, String> invites;
	private static FileConfiguration config;
	protected TeamManager(){
		File f = new File(KukgaMain.plugin.getDataFolder() + File.separator + "팀.yml");
		try{
			if(!f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			config = YamlConfiguration.loadConfiguration(f);
			invites = new LinkedHashMap<>();
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	protected void save(){
		File f = new File(KukgaMain.plugin.getDataFolder() + File.separator + "팀.yml");
		try {
			if(!f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected static String getNation(Player player) {
		List<String> temp;
		String uuid = player.getUniqueId().toString();
		String nation = null;
		keygetter:for(String s : config.getKeys(false)){
			temp = config.getStringList(s);
			for(String t : temp){
				if(t.equals(uuid)){
					nation = s;
					break keygetter;
				}
			}
		}
		return nation;
	}
	protected static void createTeam(String nation, Player player) {
		List<String> ls = new LinkedList<>();
		ls.add(player.getUniqueId().toString());
		config.set(nation, ls);
	}

	protected static void deleteTeam(String nation, String message) {
		BattleManager.remove(nation);
		KukgaMain.broadcast(nation + message);
		config.set(nation, null);
		TerritoryManager.deleteTeam(nation);
	}
	protected static boolean invite(final Player player, String nation) {
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
		config.set(nation, config.getStringList(nation).add(player.getUniqueId().toString()));
	}

	protected static void leaveTeam(Player player, String nation){
		config.set(nation, config.getStringList(nation).remove(player.getUniqueId().toString()));
	}

	protected static List<String> getTeam(String nation){
		LinkedList<String> temp = new LinkedList<>();
		for(String s : config.getStringList(nation)) {
			if(Bukkit.getPlayerExact(s)!=null)temp.add(Bukkit.getPlayerExact(s).getName());
		}
		return temp;
	}

	protected static Set<String> getTeamList(){
		return config.getKeys(false);
	}
}
