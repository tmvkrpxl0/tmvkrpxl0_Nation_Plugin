package tmvkrpxl0;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
	public List<String> onTabComplete(CommandSender sender, Command cmd, String text, String[] args) {
		/*
		 * final LinkedList<String> complete = new LinkedList<String>();
		 * StringUtil.copyPartialMatches(args[0], Arrays.asList(available), complete);
		 * Collections.sort(complete); return complete;
		 */
		List<String> r = new LinkedList<String>();
		if(args.length == 1) {
			 StringUtil.copyPartialMatches(args[0], Arrays.asList(available), r);
			 if(!sender.hasPermission("minecraft.command.op")) {
				 r.remove("저장");
				 r.remove("설정");
				 r.remove("패치");
				 r.remove("test");
			 }
			 if(!sender.hasPermission("kukga.create")) {
				 r.remove("생성");
				 if(TeamManager.getNation(sender.getName())==null)r.remove("삭제");
			 }
			 if(!sender.hasPermission("kukga.secondary") || !(sender instanceof Player)) {
				 r.remove("초대");
				 r.remove("추방");
				 r.remove("승급");
				 r.remove("강등");
				 r.remove("전쟁선포");
				 r.remove("전쟁방어");
			 }
			 if(!(sender instanceof Player)) {
				 r.remove("초대수락");
				 r.remove("초대거절");
				 r.remove("탈퇴");
				 r.remove("생성");
				 r.remove("삭제");
			 }
			 if(Core.patch)r.remove("패치");
			 Collections.sort(r); 
			 return r;
		}else if(args.length == 2) {
			LinkedList<String> players = new LinkedList<String>();
			
			try {
				Method onlines = Bukkit.getServer().getClass().getMethod("getOnlinePlayers");
				onlines.setAccessible(true);
				Object obj = onlines.invoke(Bukkit.getServer());
				if(obj.getClass().isArray()) {
					Player [] pa = (Player[]) obj;
					for(Player t : pa) {
						players.add(t.getName());
					}
				}else {
					@SuppressWarnings("unchecked")
					Collection<Player> pc = (Collection<Player>) obj;
					for(Player t : pc) {
						players.add(t.getName());
					}
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			switch(args[0]) {
			case "초대":
				if(sender.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(sender.getName())!=null) {
						for(Player online : Bukkit.getOnlinePlayers()) {
							if(TeamManager.getNation(online.getName()) == null)StringUtil.copyPartialMatches(args[1], players, r);
						}
						r.remove(sender.getName());
						Collections.sort(r);
						return r;
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "추방":
				if(sender.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(sender.getName())!=null) {
						r = TeamManager.getTeam(TeamManager.getNation(sender.getName()));
						r.remove(sender.getName());
						Collections.sort(r);
						return r;
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "승급":
				if(sender.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(sender.getName())!=null) {
						StringUtil.copyPartialMatches(args[1], TeamManager.getTeam(TeamManager.getNation(sender.getName())), r);
						r.remove(sender.getName());
						Collections.sort(r);
						return r;
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "강등":
				if(sender.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(sender.getName())!=null) {
						StringUtil.copyPartialMatches(args[1], TeamManager.getTeam(TeamManager.getNation(sender.getName())), r);
						r.remove(sender.getName());
						Collections.sort(r);
						return r;
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "전쟁선포":
				if(sender.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(sender.getName())!=null) {
						StringUtil.copyPartialMatches(args[1], TeamManager.getTeamList(), r);
						r.remove(TeamManager.getNation(sender.getName()));
						Collections.sort(r);
						return r;
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "설정":
				if(sender.hasPermission("minecraft.command.op")) {
					StringUtil.copyPartialMatches(args[1], Arrays.asList(tmvkrpxl0.Command.settings), r);
					Collections.sort(r);
					return r;
				}else sender.sendMessage("이 명령어를 사용하시려면 관리자여야 합니다!");
				break;
			}
		}else if(args.length==3) {
			if(args[0].equals("설정") && args[1].equals("패치") && Core.patch) {
				LinkedList<String> files = new LinkedList<>();
				for(String s : new java.io.File(".").list()) {
					if(s.endsWith(".jar")) {
						files.add(s.replaceAll("[/:?*<>|]", ""));
					}
				}
				StringUtil.copyPartialMatches(args[2], files, r);
				Collections.sort(r);
				return r;
			}
		}
		return null;
	}
	
}
