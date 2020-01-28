package tmvkrpxl0;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.md_5.bungee.api.ChatColor;

public class Command implements CommandExecutor{
	private static final String[] available = {"생성 - 국가를 생성합니다", 
			"삭제 - 국가를 삭제합니다",
			"초대 - 다른 사람을 국가에 초대합니다", 
			"초대수락 - 초대를 수락합니다", 
			"초대거절 - 초대를 거절합니다", 
			"추방 - 다른 사람을 국가에서 내쫓습니다", 
			"탈퇴 - 국가에서 나갑니다",
			"승급 - 다른 사람을 국가에서 승급시켜줍니다", 
			"강등 - 다른 사람을 국가에서 강등시켜줍니다", 
			"전쟁선포 - " + ChatColor.RED +" 다른 국가와의 전쟁을 선포합니다", 
			"항복 - 다른 국가와의 전쟁에서 항복합니다(준비시간에만 가능)", 
			"저장 - 국가 구성원 정보를 저장합니다.",
			"test "};
			//중요: 명령어를 추가할 때, 설명은 적지 않더라도 공백은 뒤에 쓸것 예시: "xxxx " 처럼 뒤에 공백
	@SuppressWarnings("unchecked")
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
			if(!right)printUsage(sender);
			switch(args[0]) {
			case "생성":
				if(sender.hasPermission("kukga.create")) {
					if(TeamManager.getNation(sender.getName())!=null)
						sender.sendMessage(ChatColor.RED + "이미 국가에 소속되어 있습니다!");else {
						if(args.length < 2)sender.sendMessage("사용법: /국가 생성 <국가 이름>");
						else {
							TeamManager.createTeam(args[1], sender.getName());
							Core.broadcast(sender.getName() + "님께서 " + args[1] + "국가를 만드셨습니다!");
						}
					}
				}else {
					sender.sendMessage("오직 개척자만이 사용 가능합니다!");
				}
				break;
			case "삭제":
				if(sender.hasPermission("kukga.create")) {
					if(TeamManager.getNation(sender.getName())==null)sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
					else {
						Core.broadcast("1111111111111111");
						String name = TeamManager.getNation(sender.getName());
						if(BattleManager.warWithWho(name)!=null) {
							sender.sendMessage("전쟁중에는 사용할 수 없습니다!");
							Core.broadcast("2222222222222222222222");
							return true;
						}
						Core.broadcast("3333333333333333333");
						if(args.length > 1) {
							if(args[1] == TeamManager.getNation(sender.getName())) {
								TeamManager.deleteTeam(TeamManager.getNation(sender.getName()));
								Core.broadcast(TeamManager.getNation(sender.getName()) + "국가가 삭제되었습니다..");
								return true;
							}
						}
						sender.sendMessage(ChatColor.DARK_RED + "진짜 국가를 삭제하시려면, /국가 삭제 <국가 이름> 을 적으세요");
					}
				}else sender.sendMessage("오직 왕만이 사용 가능합니다!");
				break;
			case "초대":
				if(sender.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(sender.getName())==null) {
						sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
						return true;
					}
					if(args.length > 1) {
						if(Bukkit.getPlayerExact(args[1]) != null) {
							if(TeamManager.getNation(args[1])!=null) {
								sender.sendMessage(args[1] + "님은 이미 국가에 소속되어 있습니다!");
								return true;
							}
							if(Bukkit.getPlayerExact(args[1]).hasPermission("kukga.create")) {
								sender.sendMessage(args[1] + "님은 개척자 입니다!");
								return true;
							}
							if(!TeamManager.invite(args[1], TeamManager.getNation(sender.getName())))sender.sendMessage(args[1] + "님은 이미 초대를 받았습니다! 나중에 다시 시도하세요");
							else sender.sendMessage(args[1] + "님께 초대를 보냈습니다. 모든 초대는 30초 뒤에 제거됩니다");
						}else sender.sendMessage(args[1] + "님을 찾을 수 없습니다!");
					}else sender.sendMessage("사용법:/국가 초대 <플레이어 이름>");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "초대수락":
				if(TeamManager.getNation(sender.getName())==null) {
					if(sender.hasPermission("kukga.create")) {
						sender.sendMessage("개척자는 초대를 수락할 수 없습니다!");
					}
					if(TeamManager.invites.get(sender.getName())!=null) {
						TeamManager.joinTeam(sender.getName(), TeamManager.invites.get(sender.getName()));
						sender.sendMessage(TeamManager.invites.get(sender.getName()) + "국가에 참여하셨습니다!");
					}else sender.sendMessage("당신은 초대받지 못했습니다!");
				}else sender.sendMessage("당신은 이미 국가에 소속되어 있습니다!");
				break;
			case "초대거절":
				if(TeamManager.getNation(sender.getName())==null) {
					if(TeamManager.invites.get(sender.getName())!=null) {
						TeamManager.invites.remove(sender.getName());
						sender.sendMessage(TeamManager.invites.get(sender.getName()) + "국가에서 온 초대를 거절하셨습니다!");
					}else sender.sendMessage("당신은 초대받지 못했습니다!");
				}else sender.sendMessage("당신은 이미 국가에 소속되어 있습니다!");
				break;
			case "추방":
				if(sender.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(sender.getName())!=null) {
						if(args.length > 1) {
							if(TeamManager.getNation(sender.getName()) == TeamManager.getNation(args[1])) {
								if(Bukkit.getPlayerExact(args[1]).hasPermission("kukga.secondary")) {
									sender.sendMessage("왕은 추방될 수 없습니다!");
									return true;
								}
								Bukkit.getPlayerExact(args[1]).sendMessage(ChatColor.RED + "당신은 " + TeamManager.getNation(args[1]) + "국가에서 추방당하셨습니다!");
								TeamManager.leaveTeam(args[1], TeamManager.getNation(sender.getName()), true);
							}else sender.sendMessage(args[1] + "님을 당신의 국가에서 찾을 수 없습니다!");
						}else sender.sendMessage("사용법: /국가 추방 <플레이어 이름>");
					}else sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "탈퇴":
				if(!sender.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(sender.getName())!=null) {
						TeamManager.leaveTeam(sender.getName(), TeamManager.getNation(sender.getName()), false);
					}else sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("왕은 탈퇴할 수 없습니다!");
				break;
			case "승급":
				sender.sendMessage("개발 안된 명령어입니다.");
				break;
			case "강등":
				sender.sendMessage("개발 안된 명령어 입니다.");
				break;
			case "전쟁선포":
				sender.sendMessage("개발 안된 명령어 입니다. 전쟁 선포서 이름이랑 설명 까먹었습니다. 죄송합니다.");
				break;
			case "항복":
				sender.sendMessage("개발 안된 명령어 입니다. 전쟁 선포서 이름이랑 설명 까먹었습니다. 죄송합니다.");
				break;
			case "저장":
				TerritoryManager.save();
				TeamManager.save();
				BattleManager.save();
				Core.broadcast("국가 정보들이 저장되었습니다!");
				break;
			case "test":
				try {
				JSONParser jparser = new JSONParser();
				JSONObject jobj= (JSONObject) jparser.parse(new BufferedReader(new InputStreamReader(new FileInputStream("plugins/Kukga/pap.json"),"UTF8")));
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
				((Player)sender).getWorld().dropItem(((Player)sender).getLocation(), stack);
				}catch(Exception e) {
					e.printStackTrace();
				}
				break;
				
			}
		}else {
			printUsage(sender);
		}
		return right;
	}
	
	private void printUsage(CommandSender sender) {
			for(String s : available) {
				sender.sendMessage(Core.prefix +  ChatColor.BOLD + "/국가 " + s);
			}
	}
}
