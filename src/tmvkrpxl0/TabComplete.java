package tmvkrpxl0;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class TabComplete implements TabCompleter{
	private String [] available;
	protected TabComplete() {
		available = new String[tmvkrpxl0.Command.available.length];
		for(int i = 0;i< tmvkrpxl0.Command.available.length;i++) {
			available[i] = tmvkrpxl0.Command.available[i].substring(0, tmvkrpxl0.Command.available[i].indexOf(' '));
		}
	}
	@SuppressWarnings("deprecation")
	@Override
	public LinkedList<String> onTabComplete(CommandSender sender, Command cmd, String text, String[] args) {
		/*
		 * final LinkedList<String> complete = new LinkedList<String>();
		 * StringUtil.copyPartialMatches(args[0], Arrays.asList(available), complete);
		 * Collections.sort(complete); return complete;
		 */
		LinkedList<String> r = new LinkedList<String>();
		if(!(sender instanceof Player))return r;
		Player p = (Player)sender;
		if(args.length == 1) {
			 StringUtil.copyPartialMatches(args[0], Arrays.asList(available), r);
			 if(!p.hasPermission("minecraft.command.op")) {
				 r.remove("저장");
				 r.remove("설정");
			 }
			 if(!p.hasPermission("kukga.create")) {
				 r.remove("생성");
				 if(TeamManager.getNation(p.getName())==null)r.remove("삭제");
			 }
			 if(!p.hasPermission("kukga.secondary")) {
				 r.remove("초대");
				 r.remove("추방");
				 r.remove("승급");
				 r.remove("강등");
				 r.remove("전쟁선포");
				 r.remove("전쟁방어");
			 }
			 Collections.sort(r); 
			 return r;
		}else if(args.length == 2) {
			switch(args[1]) {
			case "초대":
				if(p.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(p.getName())!=null) {
						for(Player online : Bukkit.getOnlinePlayers()) {
							if(TeamManager.getNation(online.getName()) == null)r.add(online.getName());
						}
						r.remove(p.getName());
						Collections.sort(r);
						return r;
					}else p.sendMessage("국가에 소속되어 있어야 합니다!");
				}else p.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "추방":
				if(p.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(p.getName())!=null) {
						r = TeamManager.getTeam(TeamManager.getNation(p.getName()));
						r.remove(p.getName());
						Collections.sort(r);
						return r;
					}else p.sendMessage("국가에 소속되어 있어야 합니다!");
				}else p.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "승급":
				if(p.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(p.getName())!=null) {
						r = TeamManager.getTeam(TeamManager.getNation(p.getName()));
						r.remove(p.getName());
						Collections.sort(r);
						return r;
					}else p.sendMessage("국가에 소속되어 있어야 합니다!");
				}else p.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "강등":
				if(p.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(p.getName())!=null) {
						r = TeamManager.getTeam(TeamManager.getNation(p.getName()));
						r.remove(p.getName());
						Collections.sort(r);
						return r;
					}else p.sendMessage("국가에 소속되어 있어야 합니다!");
				}else p.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "전쟁선포":
				if(p.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(p.getName())!=null) {
						r.addAll(TeamManager.getTeamList());
						r.remove(TeamManager.getNation(p.getName()));
						Collections.sort(r);
						return r;
					}else p.sendMessage("국가에 소속되어 있어야 합니다!");
				}else p.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			}
		}
		return null;
	}
	
}
