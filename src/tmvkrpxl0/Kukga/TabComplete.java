package tmvkrpxl0.Kukga;

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
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String text, String[] args) {
		List<String> r = new LinkedList<>();
		if(args.length == 1) {
			 StringUtil.copyPartialMatches(args[0], Arrays.asList(tmvkrpxl0.Kukga.Command.available), r);
			 if(!sender.hasPermission("minecraft.command.op")) {
				 r.remove("저장");
				 r.remove("설정");
			 }
			 if(!(sender instanceof Player)) {
				 r.remove("초대");
				 r.remove("찾기");
				 r.remove("추방");
				 r.remove("전쟁선포");
				 r.remove("전쟁방어");
				 r.remove("초대수락");
				 r.remove("초대거절");
				 r.remove("탈퇴");
				 r.remove("생성");
				 r.remove("삭제");
			 }else {
				 if(!PermissionManager.getPermission(((Player)sender).getUniqueId(), PermissionManager.secondary)){
					 r.remove("초대");
					 r.remove("추방");
					 r.remove("전쟁선포");
					 r.remove("전쟁방어");
				 }else r.remove("탈퇴");
				 if(!PermissionManager.getPermission(((Player)sender).getUniqueId(), PermissionManager.create)) {
					 r.remove("생성");
					 r.remove("삭제");
				 }else {
					 r.remove("초대수락");
					 r.remove("초대거절");
				 }
			 	if(TeamManager.getNation((Player)sender)==null){
					 r.remove("탈퇴");
					 r.remove("삭제");
					 r.remove("찾기");
					 r.remove("초대");
					 r.remove("추방");
					 r.remove("전쟁선포");
					 r.remove("전쟁방어");
				 }
				 else {
					 r.remove("생성");
					 r.remove("초대수락");
					 r.remove("초대거절");
				 }
			 }
			 Collections.sort(r); 
			 return r;
		}else if(args.length == 2) {
			switch(args[0]) {
			case "초대":
				if(TeamManager.getNation((Player)sender)!=null) {
					if(PermissionManager.getPermission(((Player)sender).getUniqueId(), PermissionManager.secondary)){
						LinkedList<String> players = new LinkedList<>();
						for(Player p : KukgaMain.getOnlinePlayers()) {
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
					}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				break;
			case "추방":
				if(TeamManager.getNation((Player)sender)!=null) {
					if(PermissionManager.getPermission(((Player)sender).getUniqueId(), PermissionManager.secondary)){
						r = TeamManager.getTeam(TeamManager.getNation((Player)sender));
						r.remove(sender.getName());
						Collections.sort(r);
						return r;
					}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				break;
			case "승급":
			case "강등":
				if(TeamManager.getNation((Player)sender)!=null) {
					if(PermissionManager.getPermission(((Player)sender).getUniqueId(), PermissionManager.secondary)){
						StringUtil.copyPartialMatches(args[1], TeamManager.getTeam(TeamManager.getNation((Player)sender)), r);
						r.remove(sender.getName());
						Collections.sort(r);
						return r;
					}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				break;
			case "전쟁선포":
				if(TeamManager.getNation((Player)sender)!=null) {
					if(PermissionManager.getPermission(((Player)sender).getUniqueId(), PermissionManager.secondary)){
						StringUtil.copyPartialMatches(args[1], TeamManager.getTeamList(), r);
						r.remove(TeamManager.getNation((Player)sender));
						Collections.sort(r);
						return r;
					}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
				break;
			case "설정":
				if(sender.hasPermission("minecraft.command.op")) {
					StringUtil.copyPartialMatches(args[1], Arrays.asList(tmvkrpxl0.Kukga.Command.settings), r);
					r.remove("패치");
					Collections.sort(r);
					return r;
				}else sender.sendMessage("이 명령어를 사용하시려면 관리자여야 합니다!");
				break;
			}
		}else if(args.length > 2 && args[0].equals("설정")) {
			if(args.length==3) {
				if(args[1].equals("권한설정")) {
					StringUtil.copyPartialMatches(args[2], Arrays.asList("시민", "왕", "개척자"), r);
					Collections.sort(r);
					return r;
				}
			}else if(args.length==4) {
				if(args[1].equals("권한설정")) {
					LinkedList<String> players = new LinkedList<>();
					for(Player p : KukgaMain.getOnlinePlayers()) {
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
