package tmvkrpxl0.Kukga;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tmvkrpxl0.Config.BattleInfo;
import tmvkrpxl0.Config.TerritoryInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.UUID;

public class Command implements CommandExecutor{
	protected static final String[] available = {
			"생성",
			"삭제",
			"찾기",
			"초대",
			"초대수락",
			"초대거절",
			"추방",
			"탈퇴",
			"전쟁선포",
			"전쟁방어",
			"저장",
			"설정"
	};
	protected static final String[] lores = {
			"국가를 생성합니다",
			"국가를 삭제합니다",
			"자신의 국가 위치를 찾습니다",
			"다른 사람을 국가에 초대합니다",
			"초대를 수락합니다",
			"초대를 거절합니다",
			"다른 사람을 국가에서 내쫓습니다",
			ChatColor.RED + "국가에서 나갑니다",
			ChatColor.RED +"다른 국가와의 전쟁을 선포합니다",
			"다른 국가가 선포한 전쟁을 취소시킵니다",
			"국가 구성원 정보를 저장합니다.",
			"국가 플러그인을 설정합니다"
	};
	protected static final String [] settings = {
			"권한설정",
			"다른세계에영토건설허용",
			"전쟁준비시간",
			"전쟁시간",
			"재시작이후대기시간",
			"재시작이후준비시간",
			"최소거리",
			"최소아군거리",
			"신호기파괴시간",
			"철문파괴시간",
			"초기화"};
	@Override
	@SuppressWarnings("ConstantConditions")
	public boolean onCommand(final CommandSender sender, org.bukkit.command.Command cmd, String label, final String[] args) {
		Player p = null;
		if(!(sender instanceof Player) && (args.length == 0 || !(args[0].equals("설정") || args[0].equals("저장")))) {
			sender.sendMessage(KukgaMain.prefix + "오직 플레이어만 사용할 수 있습니다!");
			return true;
		}else if(sender instanceof Player)p = (Player) sender;
		boolean right = false;
		if(args.length > 0) {
			for(String s : available) {
				if(args[0].equals(s) || args[0].equals("test")){
					right = true;
					break;
				}
			}
			if(!right) {
				sender.sendMessage(ChatColor.RED + args[0] + "은(는) 알 수 없는 명령어 입니다!");
				sender.sendMessage("사용 가능한 명령어 목록:");
				printUsage(sender);
			}
			switch(args[0]) {
				case "test":
					if(sender instanceof Player && sender.hasPermission("minecraft.command.op")){
						p.getWorld().dropItemNaturally(p.getLocation(), KukgaMain.declarepaper);
						p.getWorld().dropItemNaturally(p.getLocation(), KukgaMain.defendpaper);
					}
					break;
				case "생성":
					if(PermissionManager.getPermission(p.getUniqueId(), PermissionManager.create)) {
						if(TeamManager.getNation(p)==null) {
							if(args.length >= 2) {
								if(!TeamManager.getTeamList().contains(args[1])) {
									TeamManager.createTeam(args[1], p);
									PermissionManager.enablePermission(p.getUniqueId(), PermissionManager.secondary);
									KukgaMain.broadcast(sender.getName() + "님께서" + args[1] + "국가를 만드셨습니다");
								}else sender.sendMessage("이미 같은 이름의 국가가 존재합니다!");
							}else sender.sendMessage("사용법: /국가 생성 <국가 이름>");
						}else sender.sendMessage(ChatColor.RED + "이미 국가에 소속되어 있습니다!");
					}else sender.sendMessage("오직 개척자만이 사용 가능합니다!");
					break;
				case "삭제":
					if(PermissionManager.getPermission(p.getUniqueId(), PermissionManager.create)) {
						String from = TeamManager.getNation(p);
						if(from!=null) {
							if(BattleManager.warWithWho(from)==null) {
								if(args.length >= 2 && args[1].equals(from)) {
									TeamManager.deleteTeam(from, "국가가 삭제됬습니다!");
								}else sender.sendMessage(ChatColor.DARK_RED + "진짜 국가를 삭제하시려면, /국가 삭제 <국가 이름> 을 적으세요");
							}else sender.sendMessage("전쟁중에는 사용할 수 없습니다!");
						}else sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
					}else sender.sendMessage("오직 나라를 만든 사람만이 사용 가능합니다!");
					break;
				case "찾기":
					if(TeamManager.getNation(p)!=null){
						sender.sendMessage(KukgaMain.prefix + "당신이 속한 국가 영지들의 좌표는 다음과 같습니다:");
						for(TerritoryInfo info : TerritoryManager.getTerritories(TeamManager.getNation(p))){
							sender.sendMessage("영지 이름: " + ChatColor.BLUE + info.getBeaconName() + ChatColor.RESET + " 신호기 위치:" + ChatColor.GREEN + Arrays.toString(info.getBeaconLocation()) +
									(KukgaMain.config.getBoolean("territoies.allowbuildterritoryinotherdim")?ChatColor.RESET + "차원 번호: " + ChatColor.GOLD + info.getDim():""));
						}
					}else sender.sendMessage("국가에 소속되어 있어야 합니다!");
					break;
				case "초대":
					if(PermissionManager.getPermission(p.getUniqueId(), PermissionManager.secondary)) {
						if(TeamManager.getNation(p)!=null) {
							if(args.length>1) {
								if(Bukkit.getPlayerExact(args[1])!=null) {
									if(!args[1].equals(sender.getName())) {
										if(TeamManager.getNation(Bukkit.getPlayerExact(args[1]))==null) {
											if(!PermissionManager.getPermission(Bukkit.getPlayerExact(args[1]).getUniqueId(), PermissionManager.create)) {
												if(TeamManager.invite(Bukkit.getPlayerExact(args[1]), TeamManager.getNation(p))) {
													sender.sendMessage(KukgaMain.prefix + args[1] + "님에게 성공적으로 초대를 보냈습니다!");
												}else sender.sendMessage(args[1] + "님은 이미 초대를 받았습니다! 나중에 다시 시도하세요");
											}else sender.sendMessage(args[1] + "님은 개척자 입니다!");
										}else sender.sendMessage(args[1] + "님은 이미 국가에 소속되어 있습니다!");
									}else sender.sendMessage("자기 자신을 초대할 수 없습니다!");
								}else sender.sendMessage(args[1] + "라는 플레이어를 찾을 수 없습니다!");
							}else sender.sendMessage("사용법:/국가 초대 <플레이어 이름>");
						}else sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
					}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야 합니다!");
					break;
				case "초대수락":
					if(TeamManager.getNation(p)==null) {
						if(!PermissionManager.getPermission(p.getUniqueId(), PermissionManager.create)) {
							if(TeamManager.invites.get(p.getUniqueId())!=null) {
								TeamManager.joinTeam(p, TeamManager.invites.get(p.getUniqueId()));
								sender.sendMessage(TeamManager.invites.get(p.getUniqueId()) + "국가에 참여하셨습니다!");
								TeamManager.invites.remove(p.getUniqueId());
							}else sender.sendMessage("당신은 초대받지 못했습니다!");
						}else sender.sendMessage("개척자는 초대를 수락할 수 없습니다!");
					}else sender.sendMessage("당신은 이미 국가에 소속되어 있습니다!");
					break;
				case "초대거절":
					if(TeamManager.getNation(p)==null) {
						if(TeamManager.invites.get(p.getUniqueId())!=null) {
							sender.sendMessage(TeamManager.invites.get(p.getUniqueId()) + "국가에서 온 초대를 거절하셨습니다!");
							TeamManager.invites.remove(p.getUniqueId());
						}else sender.sendMessage("당신은 초대받지 못했습니다!");
					}else sender.sendMessage("당신은 이미 국가에 소속되어 있습니다!");
					break;
				case "추방":
					if(TeamManager.getNation(p)!=null) {
						if(PermissionManager.getPermission(p.getUniqueId(), PermissionManager.secondary)) {
							if(args.length > 1) {
								if(TeamManager.getNation(p).equals(TeamManager.getNation(Bukkit.getPlayerExact(args[1])))) {
									if(!args[1].equals(sender.getName())) {
										if(!PermissionManager.getPermission(Bukkit.getPlayerExact(args[1]).getUniqueId(), PermissionManager.secondary)) {
											Bukkit.getPlayerExact(args[1]).sendMessage(ChatColor.RED + "당신은 " + p + "국가에서 추방당하셨습니다!");
											TeamManager.leaveTeam(Bukkit.getPlayerExact(args[1]), TeamManager.getNation(p));
										}else sender.sendMessage("왕은 추방될 수 없습니다!");
									}else {
										sender.sendMessage("당신을 추방할 수 없습니다!");
										sender.sendMessage("[/국가 탈퇴] 를 사용하여 나갈 수 있습니다");
									}
								}else sender.sendMessage(args[1] + "님을 당신의 국가에서 찾을 수 없습니다!");
							}else sender.sendMessage("사용법: /국가 추방 <플레이어 이름>");
						}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
					}else sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
					break;
				case "탈퇴":
					if(TeamManager.getNation(p)!=null) {
						if(!PermissionManager.getPermission(p.getUniqueId(), PermissionManager.secondary)) {
							sender.sendMessage(KukgaMain.prefix + ChatColor.RED + "당신은 국가에서 탈퇴하셨습니다!");
							for(String s : TeamManager.getTeam(TeamManager.getNation(p))) {
								if(s.equals(p.getName()))continue;
								Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + sender.getName() + "님께서 국가에서 탈퇴하셨습니다!");
							}
							TeamManager.leaveTeam(p, TeamManager.getNation(p));
						}else sender.sendMessage("왕은 탈퇴할 수 없습니다!");
					}else sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
					break;
				case "전쟁선포":
					if(TeamManager.getNation(p)!=null) {
						if(PermissionManager.getPermission(p.getUniqueId(), PermissionManager.secondary)) {
							if(p.getInventory().containsAtLeast(KukgaMain.declarepaper, 1)) {
								if(TerritoryManager.getRegionNumber(TeamManager.getNation(p))>0) {
									if(args.length==1) {
										sender.sendMessage(KukgaMain.prefix + "전쟁선포를 할 국가를 입력하세요");
										sender.sendMessage(KukgaMain.prefix + "주의하세요. 전쟁 선포는 건쪽에서 취소할 수 없습니다!");
										KukgaListener.choose.put(p, true);
									}else {
										if(!TeamManager.getNation(p).equals(args[1])) {
											int result = BattleManager.declare(TeamManager.getNation(p), args[1]);
											switch(result) {
												case 1:
													sender.sendMessage("전쟁중에 다른 국가에 전쟁을 선포할 수 없습니다!");
													break;
												case 2:
													sender.sendMessage(args[1] + " 국가는 이미 전쟁중입니다!");
													break;
												case 3:
													sender.sendMessage(args[1] + " 라는 국가를 찾을 수 없습니다!");
													break;
												case 4:
													sender.sendMessage(args[1] + "국은 아직 영토가 없습니다!");
													break;
											}
										}else sender.sendMessage("당신의 국가에 전쟁을 선포할 수 없습니다!");
									}
								}else sender.sendMessage("당신의 국가엔 아직 영토가 없습니다!");
							}else sender.sendMessage("전쟁선포권이 없습니다!");
						}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
					}else sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
					break;
				case "전쟁방어":
					if(TeamManager.getNation(p)!=null) {
						if(PermissionManager.getPermission(p.getUniqueId(), PermissionManager.secondary)) {
							if(p.getInventory().containsAtLeast(KukgaMain.defendpaper, 1)) {
								String from = TeamManager.getNation(p);
								BattleInfo info = BattleManager.warWithWho(from);
								if(info!=null) {
									if(info.getReadyTime()>0) {
										if(info.getStarter().equals(from)) {
											String op = BattleManager.getOpponent(from);
											for(String pt : TeamManager.getTeam(from)) {
												Bukkit.getPlayerExact(pt).sendMessage(KukgaMain.prefix + "전쟁 방어권을 사용하여, " +
														op + "국과의 전쟁을 무효화 시켰습니다!");
											}
											for(String pt : TeamManager.getTeam(BattleManager.getOpponent(from))) {
												Bukkit.getPlayerExact(pt).sendMessage(KukgaMain.prefix + from + "국이 전쟁 방어권을 사용해 전쟁을 취소시켰습니다!");
											}
											for(org.bukkit.inventory.ItemStack item : p.getInventory()) {
												if(item.isSimilar(KukgaMain.defendpaper)) {
													if(item.getAmount()==1) {
														p.getInventory().remove(item);
														p.updateInventory();
														break;
													}
													item.setAmount(item.getAmount()-1);
													p.updateInventory();
													break;
												}
											}
											BattleManager.remove(TeamManager.getNation(p));
										}else sender.sendMessage("전쟁을 선포한 국가는 전쟁을 취소할 수 없습니다!");
									}else sender.sendMessage("전쟁방어는 준비시간에만 가능합니다!");
								}else sender.sendMessage("다른 국가와 전쟁하고 있지 않습니다!");
							}else sender.sendMessage("전쟁 방어권이 없습니다!");
						}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
					}else sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
					break;
				case "저장":
					if(sender.hasPermission("minecraft.command.op")) {
						KukgaMain.save();
					}else sender.sendMessage("오직 관리자만 사용가능합니다!");
					break;
				case "설정":
					if(sender.hasPermission("minecraft.command.op")) {
						if(args.length == 2) {
							switch(args[1]) {
								case "권한설정":
									sender.sendMessage(KukgaMain.prefix + "왕과 개척자 권한을 지정합니다");
									sender.sendMessage("사용법:[/국가 설정 권한설정 <시민|왕|개척자> <플레이어이름>]");
									break;
								case "다른세계에영토건설허용":
									sender.sendMessage(KukgaMain.prefix + "다른 세계에 영토를 만드는것을 허용합니다");
									sender.sendMessage("사용법:[/국가 설정 다른세계에영토건설허용 <활성화|비활성화>]");
									break;
								case "전쟁준비시간":
									sender.sendMessage(KukgaMain.prefix + "현제 전쟁 준비시간은 " + BattleManager.secondsToString(KukgaMain.config.getInt("war.preparing time")) +
											"입니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "전쟁시간":
									sender.sendMessage(KukgaMain.prefix + "현제 전쟁 시간은 " + BattleManager.secondsToString(KukgaMain.config.getInt("war.battle time")) +
											"입니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "재시작이후대기시간":
									sender.sendMessage(KukgaMain.prefix + "현제 재시작 이후 대기시간은 " + BattleManager.secondsToString(
											KukgaMain.config.getInt("war.delay message time after server restart")) + "입니다");
									sender.sendMessage(KukgaMain.prefix + "이 설정은, 서버가 재시작 했을때 전쟁중이던 국가가 있다면 타이머가 다시 시작된다는 메세지를 몇초뒤에 알릴지 조절합니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "재시작이후준비시간":
									sender.sendMessage(KukgaMain.prefix + "현제 재시작 이후 준비시간은 " + BattleManager.secondsToString(
											KukgaMain.config.getInt("re-preparing time after server restart")) +"입니다");
									sender.sendMessage(KukgaMain.prefix + "이 설정은, 서버가 재시작 했을때 전쟁중이던 국가가 있다면 몇초만큼의 준비시간을 추가로 줄지 결정합니다");
									sender.sendMessage(KukgaMain.prefix + "만약 남아있는 준비시간이 설정값보다 많다면, 무시됩니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "최소거리":
									sender.sendMessage(KukgaMain.prefix + "현제 최소 거리는 " + KukgaMain.config.getInt("territory.minDistance") +
											"M 입니다");
									sender.sendMessage(KukgaMain.prefix + "이 설정은, 영지끼리의 최소 거리를 결정합니다. 이 최소거리안은 다른 영지의 영토로 같이 취급되며, 그 영토 안에서는 영지를 소유하고 있는 국가가 아니라면, "
											+ "지형 변경, 영지 건설 등이 불가능합니다");
									sender.sendMessage("변경시에는 M로 적어주세요");
									break;
								case "최소아군거리":
									sender.sendMessage(KukgaMain.prefix + "현제 최소 아군거리는 " + KukgaMain.config.getInt("territory.minDistanceFriendly") + "M 입니다.");
									sender.sendMessage(KukgaMain.prefix + "이 설정은, 아군 영지끼리 너무 가깝게 놓지 않도록 방지합니다. 이도 최소거리랑 동일하게, 모서리와 신호기로부터 얼마나 떨어져 있는가로 측정합니다");
									sender.sendMessage("변경시에는 M로 적어주세요");
									break;
								case "신호기파괴시간":
									sender.sendMessage(KukgaMain.prefix + "현제 신호기 파괴 시간은 " + BattleManager.secondsToString(KukgaMain.config.getInt("territory.beaconBreakTime"))
											+ "입니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "철문파괴시간":
									sender.sendMessage(KukgaMain.prefix + "현제 철문 파괴 시간은 " + BattleManager.secondsToString(KukgaMain.config.getInt("territory.ironDoorBreakTime"))
											+ "입니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "초기화":
									sender.sendMessage(KukgaMain.prefix + "진짜 설정을 초기화 하시겠습니까?");
									sender.sendMessage(KukgaMain.prefix + "진짜 초기화를 하시려면, [/국가 설정 초기화 confirm] 을 입력하세요");
									sender.sendMessage("/국가 설정 으로 설정할 수 있는 설정중 권한설정이 아닌것만 초기화 되며, 영토나 팀 설정은 영향받지 않습니다");
									break;
								default:
									sender.sendMessage("사용 가능한 설정들:");
									for(String s : settings) {
										sender.sendMessage(KukgaMain.prefix + "/국가 설정 " + s);
									}
									break;
							}
							return true;
						}else if(args.length > 2) {
							try {
								switch(args[1]) {
									case "권한설정":
										if(args.length==3) {
											sender.sendMessage(KukgaMain.prefix + "왕과 개척자 권한을 지정합니다");
											if(args[2].equals("시민") || args[2].equals("왕") || args[2].equals("개척자"))
												sender.sendMessage("사용법:[/국가 설정 권한설정 " + args[2] + " <플레이어이름>]");
											else sender.sendMessage("사용법:[/국가 설정 권한설정 <시민|왕|개척자>] <플레이어이름>]");
										}else {
											UUID target = null;
											if(Bukkit.getServer().getOnlineMode()){
												BufferedReader is = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + args[3]).openStream(), StandardCharsets.UTF_8));
												String line = is.readLine();
												if(line==null){
													sender.sendMessage(args[3] + "이라는 플레이어를 찾을 수 없습니다!");
													return true;
												}
												try {
													target = UUID.fromString((String)((JSONObject) new JSONParser().parse(line)).get("id"));
												} catch(ParseException e){
													e.printStackTrace();
												}
												is.close();
											}else {
												@SuppressWarnings("deprecation")
												OfflinePlayer off = Bukkit.getOfflinePlayer(args[3]);
												if(off==null){
													sender.sendMessage(args[3] + "이라는 플레이어를 찾을 수 없습니다!");
													return true;
												}else target = off.getUniqueId();
											}
												switch(args[2]) {
													case "시민":
														PermissionManager.disablePermission(target, PermissionManager.secondary);
														PermissionManager.disablePermission(target, PermissionManager.create);
														sender.sendMessage(args[3] + "님의 왕 권한과 개척자 권한을 비활성화 시켰습니다");
														break;
													case "왕":
														if(PermissionManager.getPermission(target, PermissionManager.secondary)) {
															PermissionManager.disablePermission(target, PermissionManager.secondary);
															sender.sendMessage(args[3] + "의 왕 권한이 비활성화 되었습니다");
														}
														else {
															PermissionManager.enablePermission(target, PermissionManager.secondary);
															sender.sendMessage(args[3] + "의 왕 권한이 활성화 되었습니다");
														}
														sender.sendMessage("Tip. " + ChatColor.BOLD +  ChatColor.UNDERLINE +
																"오직 왕 권한만 수정되었습니다." + ChatColor.RESET + " 만약 개척자 권한도 같이 수정하시고 싶으시면, [/국가 설정 권한설정 개척자 " + args[3] +
																"] 를 사용하세요");
														break;
													case "개척자":
														if(PermissionManager.getPermission(target, PermissionManager.create)) {
															PermissionManager.disablePermission(target, PermissionManager.create);
															sender.sendMessage(args[3] + "의 개척자 권한이 비활성화 되었습니다");
														}
														else {
															PermissionManager.enablePermission(target, PermissionManager.create);
															sender.sendMessage(args[3] + "의 개척자 권한이 활성화 되었습니다");
														}
														sender.sendMessage("Tip. " + ChatColor.BOLD +  ChatColor.UNDERLINE +
																"오직 개척자 권한만 수정되었습니다." + ChatColor.RESET + " 만약 왕 권한도 같이 수정하시고 싶으시면, [/국가 설정 권한설정 왕 " + args[3] +
																"] 를 사용하세요");
														sender.sendMessage("개척자가 나라를 만들면 자동으로 왕 권한이 생깁니다");
														break;
												}
										}
										break;
									case "다른세계에영토건설허용":
										if("활성화".equals(args[2])){
											sender.sendMessage(KukgaMain.prefix + "다른세계에영토건설허용이 활성화 되었습니다!");
											KukgaMain.config.set("territory.allowbuildterritoryinotherdim", true);
										}else if("비활성화".equals(args[2])){
											sender.sendMessage(KukgaMain.prefix + "다른세계에영토건설허용이 비활성화 되었습니다!");
											KukgaMain.config.set("territory.allowbuildterritoryinotherdim", false);
										}else{
											sender.sendMessage(KukgaMain.prefix + ChatColor.RED + "사용법:[/국가 설정 다른세계에영토건설허용 <활성화|비활성화>]");
										}
										break;
									case "전쟁준비시간":
										KukgaMain.config.set("war.preparing time", Integer.parseInt(args[2]));
										sender.sendMessage(KukgaMain.prefix + "전쟁 준비시간이 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
										break;
									case "전쟁시간":
										KukgaMain.config.set("war.battle time", Integer.parseInt(args[2]));
										sender.sendMessage(KukgaMain.prefix + "전쟁 시간이 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
										break;
									case "재시작이후대기시간":
										KukgaMain.config.set("war.delay message time after server restart", Integer.parseInt(args[2]));
										sender.sendMessage(KukgaMain.prefix + "재시작 이후 대기시간이" + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
										break;
									case "재시작이후준비시간":
										KukgaMain.config.set("war.re-preparing time after server restart", Integer.parseInt(args[2]));
										sender.sendMessage(KukgaMain.prefix + "재시작 이후 준비시간이" + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
										break;
									case "최소거리":
										KukgaMain.config.set("territory.minDistance", Integer.parseInt(args[2]));
										sender.sendMessage(KukgaMain.prefix + "최소 거리가 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "M 로 변경되었습니다!");
										break;
									case "최소아군거리":
										KukgaMain.config.set("territory.minDistanceFriendly", Integer.parseInt(args[2]));
										sender.sendMessage(KukgaMain.prefix + "최소 아군 거리가 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "M 로 변경되었습니다!");
										break;
									case "신호기파괴시간":
										KukgaMain.config.set("territory.beaconBreakTime", Integer.parseInt(args[2]));
										sender.sendMessage(KukgaMain.prefix + "신호기 파괴 시간이 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
										KukgaListener.changeHardness(Material.BEACON, Integer.parseInt(args[2]));
										break;
									case "철문파괴시간":
										KukgaMain.config.set("territory.ironDoorBreakTime", Integer.parseInt(args[2]));
										sender.sendMessage(KukgaMain.prefix + "철문 파괴 시간이 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
										KukgaListener.changeHardness(Material.IRON_DOOR, Integer.parseInt(args[2]));
										KukgaListener.changeHardness(Material.IRON_DOOR_BLOCK, Integer.parseInt(args[2]));
										break;
									case "초기화":
										if(args[2].equals("confirm")) {
											File f = new File(KukgaMain.plugin.getDataFolder() + File.separator + "config.yml");
											if(f.exists())f.delete();
											KukgaMain.plugin.saveDefaultConfig();
											KukgaMain.plugin.reloadConfig();
											KukgaMain.config = KukgaMain.plugin.getConfig();
											sender.sendMessage(KukgaMain.prefix + "초기화 되었습니다!");
										}else {
											sender.sendMessage(KukgaMain.prefix + "진짜 설정을 초기화 하시겠습니까?");
											sender.sendMessage(KukgaMain.prefix + "진짜 초기화를 하시려면, [/국가 설정 초기화 confirm] 을 입력하세요");
											sender.sendMessage("/국가 설정 으로 설정할 수 있는 설정만 초기화 되며, 영토나 팀 설정은 영향받지 않습니다");
										}
										break;
									default:
										sender.sendMessage("사용 가능한 설정들:");
										for(String s : settings) {
											sender.sendMessage(KukgaMain.prefix + "/국가 설정 " + s);
										}
										break;
								}
							} catch (NumberFormatException e) {
								sender.sendMessage("무조건 숫자여야만 합니다!");
							} catch (Exception e){
								e.printStackTrace();
							}
							return true;
						}
						sender.sendMessage("사용 가능한 설정들:");
						for(String s : settings) {
							sender.sendMessage(KukgaMain.prefix + "/국가 설정 " + s);
						}
					}else sender.sendMessage("관리자만 사용 가능합니다!");
					break;
			}
		}else {
			printUsage(sender);
		}
		return right;
	}

	private void printUsage(CommandSender sender) {
		LinkedHashMap<String, Boolean> temp = new LinkedHashMap<>();
		for(String s : available) {
			temp.put(s, false);
		}
		if(!sender.hasPermission("minecraft.command.op")) {
			temp.put("저장", true);
			temp.put("설정", true);
		}
		if(!(sender instanceof Player)) {
			temp.put("초대", true);
			temp.put("추방", true);
			temp.put("전쟁선포", true);
			temp.put("전쟁방어", true);
			temp.put("초대수락", true);
			temp.put("초대거절", true);
			temp.put("탈퇴", true);
			temp.put("생성", true);
			temp.put("삭제", true);
		}else {
			if(!PermissionManager.getPermission(((Player)sender).getUniqueId(), PermissionManager.secondary)){
				temp.put("초대", true);
				temp.put("추방", true);
				temp.put("전쟁선포", true);
				temp.put("전쟁방어", true);
			}else temp.put("탈퇴", true);
			if(!PermissionManager.getPermission(((Player)sender).getUniqueId(), PermissionManager.create)) {
				temp.put("생성", true);
				temp.put("삭제", true);
			}else {
				temp.put("초대수락", true);
				temp.put("초대거절", true);
			}
			if(TeamManager.getNation((Player)sender)==null){
				temp.put("삭제", true);
				temp.put("초대", true);
				temp.put("추방", true);
				temp.put("전쟁선포", true);
				temp.put("전쟁방어", true);
				temp.put("탈퇴", true);
			}
			else {
				temp.put("생성", true);
				temp.put("초대수락", true);
				temp.put("초대거절", true);
			}
		}
		int idx = 0;
		for(String s : temp.keySet()) {
			sender.sendMessage(KukgaMain.prefix + (temp.get(s)?ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH:ChatColor.BOLD) + "/국가 " + s + " - " + lores[idx]);
			idx++;
		}
	}
}
