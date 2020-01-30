package tmvkrpxl0;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Core extends JavaPlugin {
	protected static Plugin plugin;
	protected static ConsoleCommandSender sender;
	private static TeamManager teammanager;
	private static BattleManager battlemanager;
	protected static PluginDescriptionFile pdFile;
	private static TerritoryManager territorymanager;
	protected static String prefix;
	protected static Scoreboard sb;
	protected static Objective obj;
	protected static PacketInjector injector;

	@Override
	public void onEnable() {
		prefix = ChatColor.GOLD + "[" + ChatColor.WHITE + "국가" + ChatColor.GOLD + "]" + ChatColor.RESET;//[국가]
		sender = Bukkit.getConsoleSender();//콘솔 메세지 보내는거
		plugin = this;//플러그인 인스턴스
		pdFile = getDescription();//설명파일
		sb = Bukkit.getScoreboardManager().getNewScoreboard();//스코어보드
		Objective obj = sb.registerNewObjective("showHealth", "health");
		obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
		obj.setDisplayName(ChatColor.RED + "♥");
		getServer().getPluginManager().registerEvents(new listener(), plugin);//리스너 등록
		teammanager = new TeamManager();//팀매니저
		territorymanager = new TerritoryManager();
		battlemanager = new BattleManager();
		sender.sendMessage("####################################");
		sender.sendMessage(ChatColor.DARK_PURPLE + "[tmvkrpxl0]국가 전쟁 플러그인을 실행합니다..");
		sender.sendMessage("[" + pdFile.getFullName() + ": " + pdFile.getDescription() + "]");
		sender.sendMessage("####################################");
		getCommand("국가").setExecutor(new Command());
		injector = new PacketInjector();
	}
	
	public static void save() {
		teammanager.save();
		plugin.saveConfig();
		battlemanager.save();
		territorymanager.save();
		broadcast("국가 정보들이 저장되었습니다!");
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		save();
		sender.sendMessage("####################################");
		sender.sendMessage(ChatColor.DARK_PURPLE + "[tmvkrpxl0]국가 전쟁 플러그인을 종료합니다..");
		sender.sendMessage("[" + pdFile.getFullName() + ": " + pdFile.getDescription() + "]");
		sender.sendMessage("####################################");
	}

	protected static void broadcast(String message){
		Bukkit.broadcastMessage(prefix + message);
	}
	
	@SuppressWarnings("unchecked")
	protected static ItemStack loadItem(String filepath) {
		try {
			JSONParser jparser = new JSONParser();
			JSONObject jobj= (JSONObject) jparser.parse(new BufferedReader(new InputStreamReader(new FileInputStream("plugins/Kukga/pap.json"),"UTF8")));
			String type = (String) jobj.get("type");
			String cl = (String) jobj.get("==");
			Map<String, Object> meta = (Map<String, Object>) jobj.get("meta");
			//ItemStack stack = new ItemStack(Material.DIAMOND, 3);
			ItemStack stack = (ItemStack) Class.forName(cl).getConstructor(Material.class, int.class).newInstance(Material.valueOf(type), 1);
			ItemMeta imeta = stack.getItemMeta();
			Core.broadcast((String) meta.get("display-name"));
			imeta.setDisplayName((String) meta.get("display-name"));
			imeta.setLore((ArrayList<String>)meta.get("lore"));
			stack.setItemMeta(imeta);
			return stack;
			}catch(Exception e) {
				e.printStackTrace();
			}
		return null;
	}
}

