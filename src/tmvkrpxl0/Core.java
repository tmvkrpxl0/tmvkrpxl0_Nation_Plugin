package tmvkrpxl0;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Core extends JavaPlugin{
	protected static Plugin plugin;
	protected static ConsoleCommandSender sender;
	protected static TeamManager teammanager; 
	protected static PluginDescriptionFile pdFile;
	@Override
	public void onEnable() {
		sender = Bukkit.getConsoleSender();
		plugin = this;
		pdFile = getDescription();
		getServer().getPluginManager().registerEvents(new listener(), plugin);
		teammanager = new TeamManager();
		sender.sendMessage("####################################");
		sender.sendMessage(ChatColor.DARK_PURPLE + "[tmvkrpxl0]국가 전쟁 플러그인을 실행합니다..");
		sender.sendMessage("[" + pdFile.getFullName() + ": " +pdFile.getDescription() +"]");
		sender.sendMessage("####################################");
		getCommand("국가").setExecutor(new Command());
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		teammanager.save();
		sender.sendMessage("####################################");
		sender.sendMessage(ChatColor.DARK_PURPLE + "[tmvkrpxl0]국가 전쟁 플러그인을 종료합니다..");
		sender.sendMessage("[" + pdFile.getFullName() + ": " +pdFile.getDescription() +"]");
		sender.sendMessage("####################################");
	}
	
	
}

