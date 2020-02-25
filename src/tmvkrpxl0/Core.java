package tmvkrpxl0;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

public class Core extends JavaPlugin {
	protected static Plugin plugin;
	protected static ConsoleCommandSender sender;
	private static TeamManager teammanager;
	private static BattleManager battlemanager;
	protected static PluginDescriptionFile pdFile;
	private static TerritoryManager territorymanager;
	private static PermissionManager permissionmanager;
	protected static String prefix;
	protected static Scoreboard sb;
	protected static Objective obj;
	protected static PacketInjectorInterface injector;
	protected static ItemStack declarepaper;
	protected static ItemStack defendpaper;
	protected static Plugin hologram = null;
	protected static tmvkrpxl0.Config.CustomConfig Config = null;
	protected static boolean patch = false;
	protected static Thread patcher = null;
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
		getServer().getPluginManager().registerEvents(new listener(this), plugin);//리스너 등록
		//getServer().getPluginManager().registerEvents(new HardnessChanger(this), plugin);
		//HardnessChanger.setStrength(Blocks.IRON_DOOR_BLOCK, 35F);
		File f;
		if(!(f = new File(plugin.getDataFolder() + File.separator + "config.yml")).exists()) {
			f.getParentFile().mkdirs();
			plugin.saveDefaultConfig();
		}
		try {
			org.yaml.snakeyaml.events.MappingStartEvent.class.getConstructor(String.class, String.class, boolean.class,
					org.yaml.snakeyaml.error.Mark.class, org.yaml.snakeyaml.error.Mark.class, org.yaml.snakeyaml.DumperOptions.FlowStyle.class);
		} catch (NoSuchMethodException | SecurityException e) {
			sender.sendMessage(ChatColor.DARK_RED + "경고!!!!!!!!!!!!!!!!!!!!!!!");
			sender.sendMessage(ChatColor.DARK_RED + "경고!!!!!!!!!!!!!!!!!!!!!!!");
			sender.sendMessage(ChatColor.DARK_RED + "서버의 SnakeYaml의 버전이 낮습니다!");
			sender.sendMessage(ChatColor.DARK_RED + "패치를 진행하시려면, [/국가 설정 패치] 명령어를 사용하세요");
			sender.sendMessage(ChatColor.DARK_RED + "주의: 위 명령어를 사용하면 서버가 종료됩니다. 데이터는 저장이 될것이지만, 국가 플러그인은 저장이 되지 않을 수 있습니다.");
			sender.sendMessage(ChatColor.DARK_RED + "최대한 빨리 위 명령어를 사용하시기를 바랍니다. 그렇지 않으면 모든 국가, 영토, 전쟁 정보가 저장 되지 않을 수 있습니다.");
			sender.sendMessage(ChatColor.DARK_RED + "경고!!!!!!!!!!!!!!!!!!!!!!!");
			sender.sendMessage(ChatColor.DARK_RED + "경고!!!!!!!!!!!!!!!!!!!!!!!");
			sender.sendMessage(ChatColor.DARK_RED + "서버의 SnakeYaml의 버전이 낮습니다!");
			sender.sendMessage(ChatColor.DARK_RED + "패치를 진행하시려면, [/국가 설정 패치] 명령어를 사용하세요");
			sender.sendMessage(ChatColor.DARK_RED + "주의: 위 명령어를 사용하면 서버가 종료됩니다. 데이터는 저장이 될것이지만, 국가 플러그인은 저장이 되지 않을 수 있습니다.");
			sender.sendMessage(ChatColor.DARK_RED + "최대한 빨리 위 명령어를 사용하시기를 바랍니다. 그렇지 않으면 모든 국가, 영토, 전쟁 정보가 저장 되지 않을 수 있습니다.");
			patch = true;
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
			}
		}
		getCommand("국가").setExecutor(new Command());
		getCommand("국가").setTabCompleter(new TabComplete());
		if(!patch) {
		Config = (tmvkrpxl0.Config.CustomConfig) loadFile("config.yml", tmvkrpxl0.Config.CustomConfig.class);
		permissionmanager = new PermissionManager();
		teammanager = new TeamManager();//팀매니저
		battlemanager = new BattleManager();
		sender.sendMessage("####################################");
		sender.sendMessage(ChatColor.DARK_PURPLE + "[tmvkrpxl0]국가 전쟁 플러그인을 실행합니다..");
		sender.sendMessage("[" + pdFile.getFullName() + ": " + pdFile.getDescription() + "]");
		sender.sendMessage("####################################");
		try {
			Class.forName("net.minecraft.util.io.netty.channel.Channel");
			injector = new PacketInjector();
		}catch(ClassNotFoundException e) {
			injector = new PacketInjectorNew();
		}
		declarepaper = loadItem("전쟁선포권.json");
		defendpaper = loadItem("국가방어권.json");
		if(Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			sender.sendMessage("Holographic Displays가 활성화 되어있으니, 홀로그램 기능을 킵니다..");
			sender.sendMessage("신호기 위에 신호기 이름이 표시될 것입니다");
			hologram = Bukkit.getPluginManager().getPlugin("HolographicDisplays");
		}else {
			sender.sendMessage("Holographic Displays가 비활성화 되어있으니, 홀로그램 기능을 끕니다..");
			sender.sendMessage("신호기 위에 이름이 표시되지 않습니다");
		}
		for(Player p : getOnlinePlayers()) {
			injector.addPlayer(p);
		}
		territorymanager = new TerritoryManager();
		//밑에 3개말고 더 넣지 마셈
		listener.changeHardness(Material.IRON_DOOR, Config.getTerritory().get("ironDoorBreakTime"));
		listener.changeHardness(Material.IRON_DOOR_BLOCK, Config.getTerritory().get("ironDoorBreakTime"));
		listener.changeHardness(Material.BEACON, Config.getTerritory().get("beaconBreakTime"));
		}
	}

	protected static void save() {
		System.gc();
		teammanager.save();
		battlemanager.save();
		territorymanager.save();
		permissionmanager.save();
		broadcast("국가 정보들이 저장되었습니다!");
	}
	
	
	
	@Override
	public void onDisable() {
		super.onDisable();
		for(Player p : getOnlinePlayers()) {
			injector.removePlayer(p);
		}
		if(patcher!=null)patcher.interrupt();
		if(!patch) {
		save();
		File f = new File(plugin.getDataFolder() + File.separator + "config.yml");
		if(!f.exists()) {
			plugin.saveDefaultConfig();
		}else {
			try {
				ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
				BufferedWriter r = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
				mapper.writeValue(r, Config);
				r.close();
			} catch (IOException e) {
				e.printStackTrace();
				sender.sendMessage("경고!!!!");
				sender.sendMessage("설정을 저장하는데에 실패했습니다!!!");
				sender.sendMessage(f.getAbsolutePath() + "를 저장하는데에 실패했습니다!!");
			}
		}
		listener.disable();
		sender.sendMessage("####################################");
		sender.sendMessage(ChatColor.DARK_PURPLE + "[tmvkrpxl0]국가 전쟁 플러그인을 종료합니다..");
		sender.sendMessage("[" + pdFile.getFullName() + ": " + pdFile.getDescription() + "]");
		sender.sendMessage("####################################");
		}
	}

	protected static void broadcast(String message){
		Bukkit.broadcastMessage(prefix + message);
	}
	
	protected static Object loadFile(String filename, Class<?> t) {
		File f = new File(Core.plugin.getDataFolder() + File.separator + filename);
		Object re;
		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
			if(!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
				return t.getConstructor().newInstance();
			}
			if(f.length()==0)return t.getConstructor().newInstance();
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			char [] buffer = new char[1];
			r.read(buffer);
			if(((int)buffer[0])!=65279) {
				r.close();
				r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			}
			re = mapper.readValue(r, t);
			r.close();
			return re;
		} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
			e.printStackTrace();
			sender.sendMessage(filename + "을(를) 불러오는데에 실패했습니다!");
			sender.sendMessage("데이터를 보존하기 위해 플러그인을 비활성화 합니다!");
			sender.sendMessage("만약 서버 실행중에 플러그인 파일을 대체하여 발생한 문제라면, 서버를 완전히 재시작해 보세요");
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
		}
		return null;
	}
	
	protected static void saveFile(String filename, Class<?> t, Object o) {
    	try {
    		File f = new File(Core.plugin.getDataFolder() + File.separator + filename);
    		ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
    		if(!f.exists()) {
    			f.getParentFile().mkdirs();
    			f.createNewFile();
    		}
    		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
    		mapper.writeValue(w, o);
    		w.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
	}
	
	@SuppressWarnings("unchecked")
	protected static ItemStack loadItem(String filename) {
		try {
			JSONParser jparser = new JSONParser();
			File f = new File("plugins/Kukga/" + filename);
			if(!f.exists()) {
				try(BufferedInputStream bi = new BufferedInputStream(new URL("https://raw.githubusercontent.com/tmvkrpxl0/tmvkrpxl0_Nation_Plugin/"
						+ "master/" + URLEncoder.encode(filename, "UTF-8")).openStream())){
					f.getParentFile().mkdirs();
					f.createNewFile();
					FileOutputStream out = new FileOutputStream(f);
					byte [] buffer = new byte[2048];
					int byteread;
					while((byteread = bi.read(buffer, 0, 2048)) != -1) {
						out.write(buffer, 0, byteread);
					}
					out.close();
					bi.close();
				} catch (IOException e) {
					sender.sendMessage(filename + "을(를) 다운받는 중 문제가 발생했습니다!");
					e.printStackTrace();
				}
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("plugins/Kukga/" + filename),"UTF-8"));
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
	protected static Collection<? extends Player> getOnlinePlayers() {
		try {
			Method onlines = Bukkit.getServer().getClass().getMethod("getOnlinePlayers");
			onlines.setAccessible(true);
			Object obj = onlines.invoke(Bukkit.getServer());
			if(obj.getClass().isArray()) {
				return Arrays.asList((Player[])obj);
			}else {
				return (Collection<? extends Player>) obj;
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}

