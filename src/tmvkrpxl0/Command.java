package tmvkrpxl0;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Command implements CommandExecutor{
	private static final String[] available = {"생성 - 국가를 생성합니다", 
			"초대 - 다른 사람을 국가에 초대합니다", 
			"초대수락 - 초대를 수락합니다", 
			"초대거절 - 초대를 거절합니다", 
			"추방 - 다른 사람을 국가에서 내쫓습니다", 
			"승급 - 다른 사람을 국가에서 승급시켜줍니다", 
			"강등 - 다른 사람을 국가에서 강등시켜줍니다", 
			"전쟁선포 - " + ChatColor.RED +" 다른 국가와의 전쟁을 선포합니다", 
			"전쟁항복 - 다른 국가와의 전쟁에서 항복합니다(준비시간에만 가능)", 
			"저장 - 국가 구성원 정보를 저장합니다."};
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		boolean right = false;
		if(args.length > 0) {
			for(String s : available) {
				if(args[0].equals(s.substring(0, s.indexOf(' ')))) {
					right=true;
					break;
				}
			}
			if(!right)printUsage((Player)sender);
			switch(args[0]) {
			case "생성":
				if(sender.hasPermission("kukga.create")) {
					if(Core.teammanager.isInNation(sender.getName()))
						sender.sendMessage(ChatColor.RED + "이미 국가에 소속되어 있습니다!");else {
						if(args.length < 2)sender.sendMessage("사용법: /국가 생성 <국가 이름>");
						else Core.teammanager.createTeam(args[1], sender.getName());
					}
				}else {
					sender.sendMessage("오직 개척자만이 사용 가능합니다!");
				}
				break;
			}
		}else {
			printUsage((Player) sender);
		}
		return right;
	}
	
	private void printUsage(Player sender) {
			for(String s : available) {
				sender.sendMessage(ChatColor.GOLD + "" +  ChatColor.BOLD + "/국가 " + s);
			}
	}
}
