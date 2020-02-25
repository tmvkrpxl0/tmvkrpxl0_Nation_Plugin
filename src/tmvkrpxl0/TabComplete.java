package tmvkrpxl0;

import java.util.Arrays;
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
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String text, String[] args) {
		List<String> r = new LinkedList<String>();
		if(args.length == 1) {
			 StringUtil.copyPartialMatches(args[0], Arrays.asList(available), r);
			 if(!sender.hasPermission("minecraft.command.op")) {
				 r.remove("저장");
				 r.remove("설정");
				 r.remove("패치");
				 r.remove("test");
			 }
			 if(!PermissionManager.getPermission(sender, PermissionManager.create)) {
				 r.remove("생성");
				 r.remove("삭제");
			 }
			 if(!PermissionManager.getPermission(sender, PermissionManager.secondary) || !(sender instanceof Player)) {
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
			 Collections.sort(r); 
			 return r;
		}else if(args.length == 2) {
			switch(args[0]) {
			case "초대":
				if(PermissionManager.getPermission(sender, PermissionManager.secondary)) {
					if(TeamManager.getNation((Player)sender)!=null) {
						LinkedList<String> players = new LinkedList<String>();
						for(Player p : Core.getOnlinePlayers()) {
							players.add(p.getName());
						}
						LinkedList<String> available = new LinkedList<>(players);
						java.util.Iterator<String> itr = available.iterator();
						while(itr.hasNext()) {
							String temp = itr.next();
							if(TeamManager.getNation(Bukkit.getPlayerExact(temp))!=null)itr.remove();
						}
						StringUtil.copyPartialMatches(args[1], available, r);
						r.remove(sender.getName());
						Collections.sort(r);
						return r;
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "추방":
				if(PermissionManager.getPermission(sender, PermissionManager.secondary)) {
					if(TeamManager.getNation((Player)sender)!=null) {
						r = TeamManager.getTeam(TeamManager.getNation((Player)sender));
						r.remove(sender.getName());
						Collections.sort(r);
						return r;
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "승급":
				if(PermissionManager.getPermission(sender, PermissionManager.secondary)) {
					if(TeamManager.getNation((Player)sender)!=null) {
						StringUtil.copyPartialMatches(args[1], TeamManager.getTeam(TeamManager.getNation((Player)sender)), r);
						r.remove(sender.getName());
						Collections.sort(r);
						return r;
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "강등":
				if(PermissionManager.getPermission(sender, PermissionManager.secondary)) {
					if(TeamManager.getNation((Player)sender)!=null) {
						StringUtil.copyPartialMatches(args[1], TeamManager.getTeam(TeamManager.getNation((Player)sender)), r);
						r.remove(sender.getName());
						Collections.sort(r);
						return r;
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "전쟁선포":
				if(PermissionManager.getPermission(sender, PermissionManager.secondary)) {
					if(TeamManager.getNation((Player)sender)!=null) {
						StringUtil.copyPartialMatches(args[1], TeamManager.getTeamList(), r);
						r.remove(TeamManager.getNation((Player)sender));
						Collections.sort(r);
						return r;
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "설정":
				if(sender.hasPermission("minecraft.command.op")) {
					StringUtil.copyPartialMatches(args[1], Arrays.asList(tmvkrpxl0.Command.settings), r);
					r.remove("패치");
					Collections.sort(r);
					return r;
				}else sender.sendMessage("이 명령어를 사용하시려면 관리자여야 합니다!");
				break;
			}
		}else if(args.length > 2 && args[0].equals("설정")) {
			if(args.length==3) {
				if(args[1].equals("패치") && Core.patch) {
					LinkedList<String> files = new LinkedList<>();
					for(String s : new java.io.File(".").list()) {
						if(s.endsWith(".jar")) {
							files.add(s.replaceAll("[/:?*<>|]", ""));
						}
					}//국가 설정 권한설정 시민 t
					StringUtil.copyPartialMatches(args[2], files, r);
					Collections.sort(r);
					return r;
				}else if(args[1].equals("권한설정")) {
					StringUtil.copyPartialMatches(args[2], Arrays.asList(new String[] {"시민", "왕", "개척자"}), r);
					Collections.sort(r);
					return r;
				}
			}else if(args.length==4) {
				if(args[1].equals("권한설정")) {
					LinkedList<String> players = new LinkedList<String>();
					for(Player p : Core.getOnlinePlayers()) {
						players.add(p.getName());
					}
					StringUtil.copyPartialMatches(args[3], players, r);
					Collections.sort(r);
					return r;
				}
			}
		}
		return null;
	}
	
}
