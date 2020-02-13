package tmvkrpxl0;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tmvkrpxl0.Config.CustomConfig;


public class Command implements CommandExecutor{
	protected static final String[] available = {"생성 - 국가를 생성합니다", 
			"삭제 - 국가를 삭제합니다",
			"초대 - 다른 사람을 국가에 초대합니다", 
			"초대수락 - 초대를 수락합니다", 
			"초대거절 - 초대를 거절합니다", 
			"추방 - 다른 사람을 국가에서 내쫓습니다", 
			"탈퇴 - 국가에서 나갑니다",
			"승급 - 다른 사람을 국가에서 승급시켜줍니다", 
			"강등 - 다른 사람을 국가에서 강등시켜줍니다", 
			"전쟁선포 - " + ChatColor.RED +" 다른 국가와의 전쟁을 선포합니다", 
			"전쟁방어 - 다른 국가가 선포한 전쟁을 취소시킵니다",
			"저장 - 국가 구성원 정보를 저장합니다.",
			"설정 - 국가 플러그인을 설정합니다",
			"test "};
	protected static final String [] settings = {"전쟁준비시간", 
			"전쟁시간",
			"재시작이후대기시간",
			"재시작이후준비시간",
			"최소거리",
			"최소아군거리",
			"신호기파괴시간",
			"철문파괴시간",
			"초기화",
			"패치"};
			//중요: 명령어를 추가할 때, 설명은 적지 않더라도 공백은 뒤에 쓸것 예시: "xxxx " 처럼 뒤에 공백)
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		Player p = null;
		if(!(sender instanceof Player) && !(args.length >= 1 && (args[0].equals("설정") || args[0].equals("저장")))) {
			sender.sendMessage(Core.prefix + "오직 플레이어만 사용할 수 있습니다!");
			return true;
		}else if(sender instanceof Player)p = (Player) sender;
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
						String name = TeamManager.getNation(sender.getName());
						if(BattleManager.warWithWho(name)!=null) {
							sender.sendMessage("전쟁중에는 사용할 수 없습니다!");
							return true;
						}
						if(TeamManager.getNation(sender.getName())!=null) {
						if(args.length > 1) {
							if(args[1].equals(TeamManager.getNation(sender.getName()))) {
								TeamManager.deleteTeam(TeamManager.getNation(sender.getName()));
								return true;
							}
						}
						sender.sendMessage(ChatColor.DARK_RED + "진짜 국가를 삭제하시려면, /국가 삭제 <국가 이름> 을 적으세요");
						}else sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
					}else sender.sendMessage("오직 나라를 만든 사람만이 사용 가능합니다!");
				break;
			case "초대":
				if(sender.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(sender.getName())==null) {
						sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
						return true;
					}
					if(args.length > 1) {
						if(Bukkit.getPlayerExact(args[1]) != null) {
							if(args[1].equals(sender.getName())) {
								sender.sendMessage("당신을 초대할 수 없습니다!");
								return true;
							}
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
				}else sender.sendMessage("이 명령어를 사용하시려면 왕 또는 관리자이어야만 합니다!");
				break;
			case "초대수락":
				if(TeamManager.getNation(sender.getName())==null) {
					if(sender.hasPermission("kukga.create")) {
						sender.sendMessage("개척자는 초대를 수락할 수 없습니다!");
						return true;
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
									sender.sendMessage("왕 또는 관리자는 추방될 수 없습니다!");
									return true;
								}
								if(args[1].equals(sender.getName())) {
									sender.sendMessage("당신을 추방할 수 없습니다!");
									sender.sendMessage("[/국가 탈퇴] 를 사용하여 나갈 수 있습니다");
									return true;
								}
								Bukkit.getPlayerExact(args[1]).sendMessage(ChatColor.RED + "당신은 " + TeamManager.getNation(args[1]) + "국가에서 추방당하셨습니다!");
								TeamManager.leaveTeam(args[1], TeamManager.getNation(sender.getName()));
							}else sender.sendMessage(args[1] + "님을 당신의 국가에서 찾을 수 없습니다!");
						}else sender.sendMessage("사용법: /국가 추방 <플레이어 이름>");
					}else sender.sendMessage(ChatColor.RED + "국가에 소속되어 있어야 합니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "탈퇴":
				if(!sender.hasPermission("kukga.secondary")) {
					if(TeamManager.getNation(sender.getName())!=null) {
						TeamManager.leaveTeam(sender.getName(), TeamManager.getNation(sender.getName()));
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
				if((p).hasPermission("kukga.secondary")) {
					if((p).getInventory().contains(Core.declarepaper)) {
						sender.sendMessage(Core.prefix + "전쟁선포를 할 국가를 입력하세요");
						listener.choose.put(p, true);
					}else sender.sendMessage("전쟁선포권이 없습니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "전쟁방어":
				if(sender.hasPermission("kukga.secondary")) {
					if(p.getInventory().contains(Core.defendpaper)) {
						if(BattleManager.warWithWho(TeamManager.getNation(sender.getName()))!=null) {
							BattleManager.remove(TeamManager.getNation(sender.getName()));
							for(String pt : TeamManager.getTeam(TeamManager.getNation(sender.getName()))) {
								Bukkit.getPlayerExact(pt).sendMessage(Core.prefix + "전쟁 방어권을 사용하여, " + 
										BattleManager.getOpponent(TeamManager.getNation(pt)) + "국과의 전쟁을 무효화 시켰습니다!");
							}
							for(String pt : TeamManager.getTeam(BattleManager.getOpponent(TeamManager.getNation(sender.getName())))) {
								Bukkit.getPlayerExact(pt).sendMessage(Core.prefix + TeamManager.getNation(sender.getName()) + 
										"국이 전쟁 방어권을 사용해 전쟁을 취소시켰습니다!");
							}
						}else sender.sendMessage("전쟁중이 아닙니다!");
					}else sender.sendMessage("전쟁 방어권이 없습니다!");
			}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "저장":
				if(sender.hasPermission("minecraft.command.op")) {
					if(!Core.patch) {
						Core.save();
					}else sender.sendMessage("저장할 수 없습니다! 버킷을 패치해야 합니다!");
				}else sender.sendMessage("오직 관리자만 사용가능합니다!");
				break;
			case "설정":
				if(sender.hasPermission("minecraft.command.op")) {
					if(args.length == 2) {
							switch(args[1]) {
								case "전쟁준비시간":
									sender.sendMessage(Core.prefix + "현제 전쟁 준비시간은 " + BattleManager.secondsToString(Core.Config.getWar().get("preparing time")) +
											"입니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "전쟁시간":
									sender.sendMessage(Core.prefix + "현제 전쟁 시간은 " + BattleManager.secondsToString(Core.Config.getWar().get("battle time")) +
											"입니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "재시작이후대기시간":
									sender.sendMessage(Core.prefix + "현제 재시작 이후 대기시간은 " + BattleManager.secondsToString(
											Core.Config.getWar().get("delay message time after server restart")) + "입니다");
									sender.sendMessage(Core.prefix + "이 설정은, 서버가 재시작 했을때 전쟁중이던 국가가 있다면 타이머가 다시 시작된다는 메세지를 몇초뒤에 알릴지 조절합니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "재시작이후준비시간":
									sender.sendMessage(Core.prefix + "현제 재시작 이후 준비시간은 " + BattleManager.secondsToString(
											Core.Config.getWar().get("re-preparing time after server restart")) +"입니다");
									sender.sendMessage(Core.prefix + "이 설정은, 서버가 재시작 했을때 전쟁중이던 국가가 있다면 몇초만큼의 준비시간을 추가로 줄지 결정합니다");
									sender.sendMessage(Core.prefix + "만약 남아있는 준비시간이 설정값보다 많다면, 무시됩니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "최소거리":
									sender.sendMessage(Core.prefix + "현제 최소 거리는 " + Core.Config.getTerritory().get("minDistance") +
											"M 입니다");
									sender.sendMessage(Core.prefix + "이 설정은, 영지끼리의 최소 거리를 결정합니다. 이 최소거리안은 다른 영지의 영토로 같이 취급되며, 그 영토 안에서는 영지를 소유하고 있는 국가가 아니라면, " 
											+ "지형 변경, 영지 건설 등이 불가능합니다");
									sender.sendMessage("변경시에는 M로 적어주세요");
									break;
								case "최소아군거리":
									sender.sendMessage(Core.prefix + "현제 최소 아군거리는 " + Core.Config.getTerritory().get("minDistanceFriendly") + "M 입니다.");
									sender.sendMessage(Core.prefix + "이 설정은, 아군 영지끼리 너무 가깝게 놓지 않도록 방지합니다. 이도 최소거리랑 동일하게, 모서리와 신호기로부터 얼마나 떨어져 있는가로 측정합니다");
									sender.sendMessage("변경시에는 M로 적어주세요");
									break;
								case "신호기파괴시간":
									sender.sendMessage(Core.prefix + "현제 신호기 파괴 시간은 " + BattleManager.secondsToString(Core.Config.getTerritory().get("beaconBreakTime"))
									+ "입니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "철문파괴시간":
									sender.sendMessage(Core.prefix + "현제 철문 파괴 시간은 " + BattleManager.secondsToString(Core.Config.getTerritory().get("ironDoorBreakTime"))
									+ "입니다");
									sender.sendMessage("변경시에는 초를 적어주세요");
									break;
								case "초기화":
									sender.sendMessage(Core.prefix + "진짜 설정을 초기화 하시겠습니까?");
									sender.sendMessage(Core.prefix + "진짜 초기화를 하시려면, [/국가 설정 초기화 confirm] 을 입력하세요");
									sender.sendMessage("/국가 설정 으로 설정할 수 있는 설정만 초기화 되며, 영토나 팀 설정은 영향받지 않습니다");
								case "패치":
									if(Core.patch) {
										sender.sendMessage("버킷 파일 이름을 적어주세요 예시:[/국가 설정 패치 Spigot.jar]");
										break;
									}
								default:
									sender.sendMessage("사용 가능한 설정들:");
									for(String s : settings) {
										sender.sendMessage(Core.prefix + "/국가 설정 " + s);
									}
									break;
								}
								return true;
					}else if(args.length > 2) {
						try {
							switch(args[1]) {
							case "전쟁준비시간":
								Core.Config.getWar().put("preparing time", Integer.parseInt(args[2]));
								sender.sendMessage(Core.prefix + "전쟁 준비시간이 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
								break;
							case "전쟁시간":
								Core.Config.getWar().put("battle time", Integer.parseInt(args[2]));
								sender.sendMessage(Core.prefix + "전쟁 시간이 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
								break;
							case "재시작이후대기시간":
								Core.Config.getWar().put("delay message time after server restart", Integer.parseInt(args[2]));
								sender.sendMessage(Core.prefix + "재시작 이후 대기시간이" + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
								break;
							case "재시작이후준비시간":
								Core.Config.getWar().put("re-preparing time after server restart", Integer.parseInt(args[2]));
								sender.sendMessage(Core.prefix + "재시작 이후 준비시간이" + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
								break;
							case "최소거리":
								Core.Config.getTerritory().put("minDistance", Integer.parseInt(args[2]));
								sender.sendMessage(Core.prefix + "최소 거리가 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "M 로 변경되었습니다!");
								break;
							case "최소아군거리":
								Core.Config.getTerritory().put("minDistanceFriendly", Integer.parseInt(args[2]));
								sender.sendMessage(Core.prefix + "최소 아군 거리가 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "M 로 변경되었습니다!");
								break;
							case "신호기파괴시간":
								Core.Config.getTerritory().put("beaconBreakTime", Integer.parseInt(args[2]));
								sender.sendMessage(Core.prefix + "신호기 파괴 시간이 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
								listener.changeHardness(Material.BEACON, Integer.parseInt(args[2]));
								break;
							case "철문파괴시간":
								Core.Config.getTerritory().put("ironDoorBreakTime", Integer.parseInt(args[2]));
								sender.sendMessage(Core.prefix + "철문 파괴 시간이 " + BattleManager.secondsToString(Integer.parseInt(args[2])) + "(으)로 변경되었습니다!");
								listener.changeHardness(Material.IRON_DOOR, Integer.parseInt(args[2]));
								listener.changeHardness(Material.IRON_DOOR_BLOCK, Integer.parseInt(args[2]));
								break;
							case "초기화":
								if(args[2].equals("confirm")) {
									File f = new File(Core.plugin.getDataFolder() + File.separator + "config.yml");
									if(f.exists())f.delete();
									Core.plugin.saveDefaultConfig();
									Core.Config = (CustomConfig) Core.loadFile("config.yml", CustomConfig.class);
									sender.sendMessage(Core.prefix + "초기화 되었습니다!");
								}else {
									sender.sendMessage(Core.prefix + "진짜 설정을 초기화 하시겠습니까?");
									sender.sendMessage(Core.prefix + "진짜 초기화를 하시려면, [/국가 설정 초기화 confirm] 을 입력하세요");
									sender.sendMessage("/국가 설정 으로 설정할 수 있는 설정만 초기화 되며, 영토나 팀 설정은 영향받지 않습니다");
								}
								break;
							case "패치":
								if(Core.patch) {
									Thread t = new Thread() {
										public void run() {
											try {
												byte [] tb = new byte[1024*8];
												int read;
												sender.sendMessage("패치를 시작합니다. 절대 PATCH_TEMP 폴더를 건드리지 마세요");
												Method getf = org.bukkit.plugin.java.JavaPlugin.class.getDeclaredMethod("getFile");
												getf.setAccessible(true);
												File pluginjar = (File) getf.invoke((org.bukkit.plugin.java.JavaPlugin)org.bukkit.Bukkit.getServer().getPluginManager().getPlugin("Kukga"));
												{
													{File libs = new File("PATCH_TEMP" + File.separator + "Libs");
													if(!libs.exists())libs.mkdirs();}
													java.util.jar.JarFile jar = new java.util.jar.JarFile(pluginjar);
												Enumeration<JarEntry> enumEntries = jar.entries();
												while (enumEntries.hasMoreElements()) {
														try {
															Thread.sleep(1);
														} catch (InterruptedException e) {
														}
												    java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
												    File f = new File("PATCH_TEMP" + File.separator + "Libs" + File.separator + file.getName());
												    f.getParentFile().mkdirs();
												    if (file.isDirectory()) { // if its a directory, create it
												        f.mkdirs();
												        continue;
												    }
												    BufferedInputStream bi = new BufferedInputStream(jar.getInputStream(file)); // get the input stream
												    BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
												    while ((read = bi.read(tb, 0, 1024 * 8))!=-1) {  // write contents of 'is' to 'fos'
												        bo.write(tb, 0, read);
												    }
												    Arrays.fill(tb,(byte)0);
													read = 0;
												    bi.close();
												    bo.close();
												}
												jar.close();
												}
												sender.sendMessage("버킷에 Snakeyaml 과 Jackson 라이브러리를 추가합니다");
												sender.sendMessage("이 플러그인의 제작자는 위 두 라이브러리에 대한 저작권을 가지고 있지 않습니다. 위 두 라이브러리는 Apache 2.0 라이선스를 따르며, 오픈소스입니다");
												sender.sendMessage("다되면 다됬다고 메세지가 올것입니다. 아무일도 안일어난다고 해서 뭔가 오류가 난게 아닙니다");
												sender.sendMessage("만약 서버 콘솔을 볼 수 있으시다면 가끔 보시는걸 추천드립니다");
												try {
													Thread.sleep(1);
												} catch (InterruptedException e3) {
												}
												StringBuilder bukkit = new StringBuilder();
												for(int i = 2;i<args.length;i++) {
													bukkit.append(args[i].replaceAll("[/:?*<>|]", "").replace("\\", ""));
													if(args.length - i > 1)bukkit.append(" ");
												}
												{
													
													File j = new File(bukkit.toString());
													java.util.jar.JarFile jar = new java.util.jar.JarFile(j);
												Enumeration<JarEntry> enumEntries = jar.entries();
												File patch = new File("PATCH_TEMP" + File.separator + "Bukkit");
												if(!patch.exists())patch.mkdirs();
												while (enumEntries.hasMoreElements()) {
														try {
															Thread.sleep(1);
														} catch (InterruptedException e) {
														}
												    java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
												    File f = new File("PATCH_TEMP" + File.separator + "Bukkit" + File.separator + file.getName());
												    f.getParentFile().mkdirs();
												    if (file.isDirectory()) { // if its a directory, create it
												        f.mkdirs();
												        continue;
												    }
												    BufferedInputStream bi = new BufferedInputStream(jar.getInputStream(file)); // get the input stream
												    BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
												    while ((read = bi.read(tb, 0, 1024 * 8))!=-1) {  // write contents of 'is' to 'fos'
												        bo.write(tb, 0, read);
												    }
												    Arrays.fill(tb,(byte)0);
													read = 0;
												    bi.close();
												    bo.close();
												}
												jar.close();
												}
												Class<?> futil = null;
													try {
														futil = Class.forName("net.minecraft.util.org.apache.commons.io.FileUtils");
													}catch(ClassNotFoundException e) {
														futil = Class.forName("org.apache.commons.io.FileUtils");
													}
													
												Method copydir = futil.getDeclaredMethod("copyDirectory", File.class, File.class);
												copydir.setAccessible(true);
												Method deldir = futil.getDeclaredMethod("deleteDirectory", File.class);
												deldir.setAccessible(true);
												File bc = new File("PATCH_TEMP" + File.separator + "Bukkit" + File.separator + "com" + File.separator + "fasterxml");
												if(bc.exists())deldir.invoke(null, bc);
												bc.mkdirs();
												File bo = new File("PATCH_TEMP" + File.separator + "Bukkit" + File.separator + "org" + File.separator + "yaml");
												if(bo.exists())deldir.invoke(null, bo);
												bo.mkdirs();
												File tc = new File("PATCH_TEMP" + File.separator + "Libs" + File.separator + "com" + File.separator + "fasterxml");
												File to = new File("PATCH_TEMP" + File.separator + "Libs" + File.separator + "org" + File.separator + "yaml");
												copydir.invoke(null, tc, bc);
												copydir.invoke(null, to, bo);
												jarFile("PATCH_TEMP" + File.separator + "Bukkit", bukkit.toString().replace(".jar", "_PATCHED.jar"), true);
												sender.sendMessage(ChatColor.GREEN + "패치가 끝났습니다. 서버를 종료합니다. 앞으로 서버를 여실때는, _PATCHED.jar로 끝나는 파일로 서버를 여세요");
												sender.sendMessage(ChatColor.BLUE + "만약 서버가 패치 이후 정상적으로 켜지지 않는 문제가 발생한다면, PATCH_TEMP 폴더를 수동으로 .jar 파일로 압축해보세요");
												sender.sendMessage(ChatColor.BLUE + "문제가 없으시다면 지우셔도 됩니다");
												Core.broadcast("5초뒤에 서버가 재시작 됩니다..");
												new org.bukkit.scheduler.BukkitRunnable() {
													public void run() {
														Bukkit.getServer().shutdown();
													}
												}.runTaskLater(Core.plugin, 20*5);
											} catch (Throwable e) {
												sender.sendMessage(ChatColor.RED + "문제가 발생했습니다. 콘솔을 확인해주세요");
												e.printStackTrace();
											}
										}
									};
									t.start();
									break;
								}
							default:
								sender.sendMessage("사용 가능한 설정들:");
								for(String s : settings) {
									sender.sendMessage(Core.prefix + "/국가 설정 " + s);
								}
								break;
							}
						}catch (NumberFormatException e) {
							sender.sendMessage("무조건 숫자여야만 합니다!");
						}
						return true;
					}
					for(String s : settings) {
						sender.sendMessage(Core.prefix + "/국가 설정 " + s);
					}
				}else sender.sendMessage("관리자만 사용 가능합니다!");
				break;
			case "test":
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
	
	static public void jarFile(String fileToJar, String jarFile, boolean excludeContainingFolder)
    	    throws IOException {		
    	    JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarFile));		
    	    File srcFile = new File(fileToJar);
    	    if(excludeContainingFolder && srcFile.isDirectory()) {
    	      for(String fileName : srcFile.list()) {
    	        addToJar("", fileToJar + "/" + fileName, jarOut);
    	      }
    	    } else {
    	      addToJar("", fileToJar, jarOut);
    	    }

    	    jarOut.flush();
    	    jarOut.close();

    	    System.out.println("Successfully created " + jarFile);
    	  }

    	  static private void addToJar(String path, String srcFile, JarOutputStream jarOut)
    	    throws IOException {		
    	    File file = new File(srcFile);
    	    String filePath = "".equals(path) ? file.getName() : path + "/" + file.getName();
    	    if (file.isDirectory()) {
    	      for (String fileName : file.list()) {				
    	        addToJar(filePath, srcFile + "/" + fileName, jarOut);
    	      }
    	    } else {
    	      jarOut.putNextEntry(new JarEntry(filePath));
    	      FileInputStream in = new FileInputStream(srcFile);

    	      byte[] buffer = new byte[8192];
    	      int len;
    	      while ((len = in.read(buffer, 0, 8192)) != -1) {
    	        jarOut.write(buffer, 0, len);
    	      }
    	      in.close();
    	    }
    	  }
}
