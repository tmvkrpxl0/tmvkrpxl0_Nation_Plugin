package tmvkrpxl0;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import net.minecraft.server.v1_7_R4.Block;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class Core extends JavaPlugin {
	protected static Plugin plugin;
	protected static ConsoleCommandSender sender;
	protected static TeamManager teammanager;
	protected static PluginDescriptionFile pdFile;
	protected static TerritoryManager territorymanager;
	protected static String prefix;

	@Override
	public void onEnable() {
		prefix = ChatColor.GOLD + "[" + ChatColor.WHITE + "국가" + ChatColor.GOLD + "]" + ChatColor.RESET;
		sender = Bukkit.getConsoleSender();
		plugin = this;
		pdFile = getDescription();
		getServer().getPluginManager().registerEvents(new listener(), plugin);
		teammanager = new TeamManager();
		territorymanager = new TerritoryManager();
		sender.sendMessage("####################################");
		sender.sendMessage(ChatColor.DARK_PURPLE + "[tmvkrpxl0]국가 전쟁 플러그인을 실행합니다..");
		sender.sendMessage("[" + pdFile.getFullName() + ": " + pdFile.getDescription() + "]");
		sender.sendMessage("####################################");
		getCommand("국가").setExecutor(new Command());
		try {
			Field f = net.minecraft.server.v1_7_R4.Block.class.getDeclaredField("strength");
			f.setAccessible(true);
			f.setFloat(org.bukkit.craftbukkit.v1_7_R4.util.CraftMagicNumbers.getBlock(Material.BEACON), 50F);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onDisable() {
		super.onDisable();
		TeamManager.save();
		sender.sendMessage("####################################");
		sender.sendMessage(ChatColor.DARK_PURPLE + "[tmvkrpxl0]국가 전쟁 플러그인을 종료합니다..");
		sender.sendMessage("[" + pdFile.getFullName() + ": " + pdFile.getDescription() + "]");
		sender.sendMessage("####################################");
	}

	protected static void failmessage(String Message, boolean disable) {
		sender.sendMessage(prefix + ChatColor.DARK_RED + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		sender.sendMessage(prefix + ChatColor.DARK_RED + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		sender.sendMessage(prefix + ChatColor.DARK_RED + "!!!!!!!! " + Message + " !!!!!!!!!");
		if (disable) {
			sender.sendMessage(prefix + ChatColor.DARK_RED + "!!!!!!!!!!플러그인을 비활성화 하도록 하겠습니다!!!!!!!!!!");
			Bukkit.getPluginManager().disablePlugin(Core.plugin);
		}
		sender.sendMessage(prefix + ChatColor.DARK_RED + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		sender.sendMessage(prefix + ChatColor.DARK_RED + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

	protected static Object loadfile(String filepath) {
		File f = new File(filepath);
		if (!f.exists()) {
			f.getParentFile().mkdirs();
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				failmessage("필수파일을 생성하는 데에 실패했습니다!", true);
				e.printStackTrace();
			}
		}
		try {
			return new Yaml().load(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			failmessage("파일을 불러오는 데에 실패했습니다!", true);
			e.printStackTrace();
		}
		return null; // 여기까지 오면 실패했단겁니다. 플러그인은 비활성화 됩니다.
	}

	protected static void savefile(String filepath, Object obj){
		try {
			new Yaml().dump(obj, new FileWriter(filepath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			failmessage("파일을 저장하는 데에 실패했습니다", false); //뭐... 저장한번 실패했다고 끄긴 좀..
			e.printStackTrace();
		}
	}

	protected static void broadcast(String message){
		Bukkit.broadcastMessage(prefix + message);
	}
}

