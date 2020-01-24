package tmvkrpxl0;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.yaml.snakeyaml.Yaml;

import net.md_5.bungee.api.ChatColor;

public class TeamManager {
	private static LinkedHashMap<String, ArrayList<String>> team = null;
	ConsoleCommandSender sender = Core.sender;
	private static Yaml yaml; 
	protected void failmessage(String Message, boolean disable) {
		sender.sendMessage("[국가]" + ChatColor.DARK_RED + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		sender.sendMessage("[국가]" + ChatColor.DARK_RED + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		sender.sendMessage("[국가]" + ChatColor.DARK_RED + "!!!!!!!! " + Message +" !!!!!!!!!");
		if(disable) {
		sender.sendMessage("[국가]" + ChatColor.DARK_RED + "!!!!!!!!!!플러그인을 비활성화 하도록 하겠습니다!!!!!!!!!!");
		Bukkit.getPluginManager().disablePlugin(Core.plugin);
		}
		sender.sendMessage("[국가]" + ChatColor.DARK_RED + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		sender.sendMessage("[국가]" + ChatColor.DARK_RED + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}
	@SuppressWarnings("unchecked")
	protected TeamManager(){
		File teamfile = new File("plugins/Kukga/team.yml");
		if(!teamfile.exists()) {
			teamfile.getParentFile().mkdirs();
			try {
				teamfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				failmessage("팀 정보를 생성하는데에 실패했습니다", true);
				e.printStackTrace();
			}
		}
		if(yaml==null)yaml = new Yaml();
		try {
			InputStream inputstream = new FileInputStream(teamfile);
			team = (LinkedHashMap<String, ArrayList<String>>) yaml.load(inputstream);
			if(team == null)team = new LinkedHashMap<String, ArrayList<String>>();
			sender.sendMessage("" + (team != null));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			failmessage("팀 정보를 불러오는데에 실패했습니다", true);
			e.printStackTrace();
		}
	}
	
	protected void save() {
		try {
			FileWriter writer = new FileWriter("plugins/Kukga/team.yml");
			yaml.dump(team, writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			failmessage("팀 정보를 저장하는데에 실패했습니다", false);
			e.printStackTrace();
		}
	}
	
	protected boolean isInNation(String name) {
		if(!team.keySet().isEmpty())
		for(String s: team.keySet()) {
			for(String k : team.get(s)) {;
				if(name.equals(k))return true;
			}
		}else return false;
		return false;
	}
	protected void createTeam(String Teamname, String Playername) {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(Playername);
		team.put(Teamname, temp);
	}
}
