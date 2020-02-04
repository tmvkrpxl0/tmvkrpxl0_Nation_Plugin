package tmvkrpxl0;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import org.bukkit.scheduler.BukkitRunnable;
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
	protected static ItemStack declarepaper;
	protected static ItemStack defendpaper;
	protected static LinkedHashSet<CustomThread> threads;
	
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
		getCommand("국가").setTabCompleter(new TabComplete());
		injector = new PacketInjector();
		declarepaper = loadItem("전쟁선포권.json");
		defendpaper = loadItem("국가방어권.json");
		threads = new LinkedHashSet<CustomThread>();
		new BukkitRunnable() {
			public void run() {
				Iterator<CustomThread> itr = threads.iterator();
				while(itr.hasNext()) {
					if(!itr.next().isAlive())itr.remove();
				}
				
			}
		}.runTaskTimer(plugin, 0, 20);
		sender.sendMessage("주의! OP권한이 있으면 이 플러그인의 권한 시스템이 작동하지 않게 됩니다!");
		sender.sendMessage("주의! OP권한이 있으면 이 플러그인의 권한 시스템이 작동하지 않게 됩니다!");
		sender.sendMessage("만약 당신이 이 서버에서 플레이를 하게 된다면, 무조건 OP를 해제하세요!");
		sender.sendMessage("만약 당신이 이 서버에서 플레이를 하게 된다면, 무조건 OP를 해제하세요!");
		sender.sendMessage("타 권한 플러그인와 같이 사용해야만 합니다!");
		sender.sendMessage("타 권한 플러그인와 같이 사용해야만 합니다!");
		sender.sendMessage("2번 나오는거 중요해서 2번알려주는겁니다!");
	}
	
	public static void save() {
		for(CustomThread t : threads) {
			t.st();
		}
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
	protected static ItemStack loadItem(String filename) {
		sender.sendMessage("경고:모든 아이템 json 파일은 UTF-8 혹은 UTF-8(BOM)형식으로 인코딩 되어야 합니다.");
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
			Core.broadcast((String) meta.get("display-name"));
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
}

