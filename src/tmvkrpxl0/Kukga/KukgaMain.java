package tmvkrpxl0.Kukga;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.annotation.Nonnull;


public class KukgaMain extends JavaPlugin {
	protected static Plugin plugin;
	protected static ConsoleCommandSender sender;
	private static TeamManager teammanager;
	private static BattleManager battlemanager;
	protected static PluginDescriptionFile pdFile;
	private static TerritoryManager territorymanager;
	private static PermissionManager permissionmanager;
	protected static String prefix;
	protected static PacketInjectorInterface injector;
	protected static ItemStack declarepaper;
	protected static ItemStack defendpaper;
	protected static Plugin hologram = null;
	private static Objective obj;
	protected static FileConfiguration config;
	@Override
	@SuppressWarnings("deprecation")
	public void onEnable() {
		prefix = ChatColor.GOLD + "[" + ChatColor.WHITE + "국가" + ChatColor.GOLD + "]" + ChatColor.RESET;//[국가]
		sender = Bukkit.getConsoleSender();//콘솔 메세지 보내는거
		plugin = this;//플러그인 인스턴스
		pdFile = getDescription();//설명파일
		config = plugin.getConfig();//설정파일
		sender.sendMessage("####################################");
		sender.sendMessage(ChatColor.DARK_PURPLE + "[tmvkrpxl0]국가 전쟁 플러그인을 실행합니다..");
		sender.sendMessage("[" + pdFile.getFullName() + ": " + pdFile.getDescription() + "]");
		sender.sendMessage("####################################");
		if(Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			sender.sendMessage("Holographic Displays가 활성화 되어있으니, 홀로그램 기능을 킵니다..");
			sender.sendMessage("신호기 위에 신호기 이름이 표시될 것입니다");
			hologram = Bukkit.getPluginManager().getPlugin("HolographicDisplays");
		}else {
			sender.sendMessage("Holographic Displays가 비활성화 되어있으니, 홀로그램 기능을 끕니다..");
			sender.sendMessage("신호기 위에 이름이 표시되지 않습니다");
		}
		Objective temp;
		if((temp=Bukkit.getScoreboardManager().getMainScoreboard().getObjective("showHealth-Kukga"))!=null)temp.unregister();
		obj = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("showHealth-Kukga", "health");
		obj.setDisplayName(ChatColor.RED + "♥");
		getServer().getPluginManager().registerEvents(new KukgaListener(this), plugin);//리스너 등록
		File f;
		if(!(f = new File(plugin.getDataFolder() + File.separator + "config.yml")).exists()) {
			f.getParentFile().mkdirs();
			plugin.saveDefaultConfig();
			plugin.reloadConfig();
			config = plugin.getConfig();
		}
		getCommand("국가").setExecutor(new Command());
		getCommand("국가").setTabCompleter(new TabComplete());
		permissionmanager = new PermissionManager();
		teammanager = new TeamManager();//팀매니저
		battlemanager = new BattleManager();
		try {
			Class.forName("net.minecraft.util.io.netty.channel.Channel");
			injector = new PacketInjector();
		}catch(ClassNotFoundException e) {
			injector = new PacketInjectorNew();
		}
		declarepaper = loadItem("전쟁선포권.json");
		defendpaper = loadItem("국가방어권.json");
		for(Player p : getOnlinePlayers()) {
			injector.addPlayer(p);
			p.setHealth(p.getHealth());
		}
		territorymanager = new TerritoryManager();
		//밑에 3개말고 더 넣지 마셈
		KukgaListener.changeHardness(Material.IRON_DOOR, config.getInt("territory.ironDoorBreakTime"));
		KukgaListener.changeHardness(Material.IRON_DOOR_BLOCK, config.getInt("territory.ironDoorBreakTime"));
		KukgaListener.changeHardness(Material.BEACON, config.getInt("territory.beaconBreakTime"));
	}

	protected static void save() {
		System.gc();
		teammanager.save();
		battlemanager.save();
		territorymanager.save();
		permissionmanager.save();
		plugin.saveConfig();
		broadcast("국가 정보들이 저장되었습니다!");
	}
	
	
	
	@Override
	public void onDisable() {
		super.onDisable();
		for(Player p : getOnlinePlayers()) {
			injector.removePlayer(p);
		}
		obj.unregister();
		save();
		File f = new File(plugin.getDataFolder() + File.separator + "config.yml");
		if(!f.exists()) {
			plugin.saveDefaultConfig();
		}else {
			plugin.saveConfig();
		}
		KukgaListener.disable();
		sender.sendMessage("####################################");
		sender.sendMessage(ChatColor.DARK_PURPLE + "[com.tmvkrpxl0]국가 전쟁 플러그인을 종료합니다..");
		sender.sendMessage("[" + pdFile.getFullName() + ": " + pdFile.getDescription() + "]");
		sender.sendMessage("####################################");
	}

	protected static void broadcast(String message){
		Bukkit.broadcastMessage(prefix + message);
	}
	@SuppressWarnings("unchecked")
	protected static ItemStack loadItem(String filename) {
		try {
			JSONParser jparser = new JSONParser();
			BufferedReader br = new BufferedReader(new InputStreamReader(plugin.getResource(filename), StandardCharsets.UTF_8));
			String json = br.readLine();//"\uFEFF"
			if(json.startsWith("\uFEFF"))json = json.substring(1);
			JSONObject jobj= (JSONObject) jparser.parse(json);
			String type = (String) jobj.get("type");
			String cl = (String) jobj.get("==");
			Map<String, Object> meta = (Map<String, Object>) jobj.get("meta");
			//ItemStack stack = new ItemStack(Material.DIAMOND, 3);
			ItemStack stack = (ItemStack) Class.forName(cl).getConstructor(Material.class, int.class).newInstance(Material.valueOf(type), 1);
			ItemMeta imeta = stack.getItemMeta();
			imeta.setDisplayName((String) meta.get("display-name"));
			imeta.setLore((ArrayList<String>)meta.get("lore"));
			stack.setItemMeta(imeta);
			stack.setAmount(1);
			br.close();
			return stack;
			}catch(Exception e) {
				e.printStackTrace();
			}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	protected static Collection<? extends Player> getOnlinePlayers() {
		List<Player> r = new LinkedList<>();
		try {
			Method onlines = Bukkit.getServer().getClass().getMethod("getOnlinePlayers");
			onlines.setAccessible(true);
			Object obj = onlines.invoke(Bukkit.getServer());
			if(obj.getClass().isArray()) {
				r.addAll(Arrays.asList((Player[])obj));
			}else {
				return (Collection<? extends Player>) obj;
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return r;
	}
}

