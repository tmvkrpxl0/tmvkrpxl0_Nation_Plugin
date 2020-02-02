package tmvkrpxl0;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_7_R4.EntityLightning;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntityWeather;
import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;


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
			"전쟁방어 - 다른 국가가 선포한 전쟁을 취소시킵니다",
			"저장 - 국가 구성원 정보를 저장합니다.",
			"설정 - 국가 플러그인을 설정합니다",
			"test "};
			//중요: 명령어를 추가할 때, 설명은 적지 않더라도 공백은 뒤에 쓸것 예시: "xxxx " 처럼 뒤에 공백)
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(Core.prefix + "오직 플레이어만 사용할 수 있습니다!");
			return true;
		}
		boolean right = false;
		if(args.length > 0) {
			for(String s : available) {
				if(args[0].equals("ㅎㅎ") || args[0].equals(s.substring(0, s.indexOf(' ')))) {
					right=true;
					break;
				}
			}
			if(!right)printUsage(sender);
			Player p = (Player) sender;
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
						if(TeamManager.getNation(p.getName())!=null) {
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
					}else p.sendMessage("전쟁선포권이 없습니다!");
				}else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "전쟁방어:":
				if(p.hasPermission("kukga.secondary"))
					if(p.getInventory().contains(Core.defendpaper))
						if(BattleManager.warWithWho(TeamManager.getNation(p.getName()))!=null) {
							BattleManager.remove(TeamManager.getNation(p.getName()));
							sender.sendMessage("전쟁 방어권을 사용하여, 전쟁을 무효화 시키는데 성공했습니다!");
							String [] bs = BattleManager.warWithWho(TeamManager.getNation(p.getName()));
							Bukkit.getPlayerExact(TeamManager.getTeam((bs[0].equals(TeamManager.getNation(p.getName()))?bs[1]:bs[0])).get(0)).sendMessage(
									"적 국가가 전쟁 방어권을 사용하여, 전쟁이 취소되었습니다!");
						}else sender.sendMessage("전쟁중이 아닙니다!");
					else sender.sendMessage("전쟁 방어권이 없습니다!");
				else sender.sendMessage("이 명령어를 사용하시려면 왕이어야만 합니다!");
				break;
			case "저장":
				if((p).hasPermission("minecraft.command.op"))
				Core.save();
				else sender.sendMessage("오직 관리자만 사용가능합니다!");
				break;
			case "test":
				if(!p.hasPermission("minecraft.command.op")) {
					p.sendMessage("테스트 명령어 입니다! 오직 관리자만 사용가능합니다!");
					break;
				}
				p.sendMessage("테스트 명령어가 존재하지 않을 수 있습니다");
				break;
			case "ㅎㅎ":
				if(!p.hasPermission("minecraft.command.op"))break;
				Location loc = p.getLocation().add(0, 22, 0);
				CustomThread t = new CustomThread() {
					double x1 = 50;
					double x2 = -50; 
					public void run() {
						while((x1 - x2)>0.5 && st) {
							PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("flame", 
									(float) (loc.getX() + x1), (float) (loc.getY()), (float) (loc.getZ()), 0, 1, 3, 0, 55);
							PacketPlayOutWorldParticles packet1 = new PacketPlayOutWorldParticles("fireworksSpark", 
									(float) (loc.getX() + x1-0.5), (float) (loc.getY()), (float) (loc.getZ()), 0, 0, 0, 0, 50);
							PacketPlayOutWorldParticles packet2 = new PacketPlayOutWorldParticles("flame",
									(float) (loc.getX() + x2), (float) (loc.getY()), (float) (loc.getZ()), 0, 1, 3, 0, 55);
							PacketPlayOutWorldParticles packet3 = new PacketPlayOutWorldParticles("fireworksSpark", 
									(float) (loc.getX() + x2+0.5), (float) (loc.getY()), (float) (loc.getZ()), 0, 0, 0, 0, 50);
							
								((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
								((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet2);
								((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet3);
								((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet1);
							x1-=0.1;
							x2+=0.1;
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
								CustomThread t = new CustomThread() {
									double y = 20;
									public void run() {
										PacketPlayOutWorldParticles pack = new PacketPlayOutWorldParticles("flame", 
												(float) (loc.getX() + x1), (float) (loc.getY()), (float) (loc.getZ()), 0, 1, 1, 1, 300);
											((CraftPlayer)p).getHandle().playerConnection.sendPacket(pack);
										while(y > 0.5 && st) {
											PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("fireworksSpark", 
													(float) (loc.getX() + x1), (float) (loc.getY()-20 + y), (float) (loc.getZ()), 1, 0, 1, 0, 125);
												((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
											y-=0.1;
											try {
												Thread.sleep(10);
											} catch (InterruptedException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
										CustomThread t = new CustomThread() {
											int [] xs = new int[32];
											int [] zs = new int[32];
											public void run() {
												for(int i = 0;i<32;i++) {
													if(!st)break;
													xs[i] = new Random().nextInt()%3;
													zs[i] = new Random().nextInt()%3;
												}
												CustomThread t = new CustomThread() {
													double radius = 0;
													public void run() {
														while(radius<=15 && st) {
															double x = radius * Math.cos(radius);
															double z = radius * Math.sin(radius);
															PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("fireworksSpark", 
																(float) (loc.getX() + x), (float) (loc.getY()-20), (float) (loc.getZ() + z), 0, 0, 0, 0, 1);
																((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
															radius+=0.05;
															try {
																Thread.sleep(5);
															} catch (InterruptedException e) {
																// TODO Auto-generated catch block
																e.printStackTrace();
															}
														}
															CustomThread t = new CustomThread() {
																public void run() {
																	double x=0;
																	double z=0;
																	double radius = 0;
																	for(int i = 0;i<10;i++) {
																		final int ti = i;
																		radius = 0;
																	while(radius <=15 && st) {
																		x = radius * Math.cos(radius+i);
																		z = radius * Math.sin(radius+i);
																		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("fireworksSpark", 
																			(float) (loc.getX() + x), (float) (loc.getY()-20), (float) (loc.getZ() + z), 0, 0, 0, 0, 1);
																			((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
																		radius+=0.05;
																		try {
																			Thread.sleep(1);
																		} catch (InterruptedException e) {
																			// TODO Auto-generated catch block
																			e.printStackTrace();
																		}
																	}
																		((CraftPlayer)p).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityWeather(
																				new EntityLightning(((CraftPlayer)p).getHandle().getWorld(), loc
																						.getX() + x, loc.getY()-20, loc.getZ()+z, false, false)));
																		((CraftPlayer)p).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(
																				"ambient.weather.thunder", loc.getX()+x, loc
																				.getY()-20, loc.getZ()+z, 10000.0F, 63));
																	
																	CustomThread t = new CustomThread() {
																		double x;
																		double z;
																		double d = 15;
																		public void run() {
																			while(d<=30 && st) {
																				x = 15 * Math.cos(d+ti);
																				z = 15 * Math.sin(d+ti);
																				PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("fireworksSpark", 
																						(float) (loc.getX() + x), (float) (loc.getY()-20), (float) (loc.getZ() + z), 0, 0, 0, 0, 50);
																					((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
																				d+=0.5;
																				try {
																					Thread.sleep(50);
																				} catch (InterruptedException e) {
																					// TODO Auto-generated catch block
																					e.printStackTrace();
																				}
																			}
																				((CraftPlayer)p).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityWeather(
																						new EntityLightning(((CraftPlayer)p).getHandle().getWorld(), loc
																								.getX() + x, loc.getY()-20, loc.getZ()+z, false, false)));
																				((CraftPlayer)p).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(
																						"ambient.weather.thunder", loc.getX()+x, loc
																						.getY()-20, loc.getZ()+z, 10000.0F, 63));
																		}
																	};
																	Core.threads.add(t);
																	t.start();
																	}
																}
															};
															Core.threads.add(t);
															t.start();
															for(int i = 0;i<=6;i++) {
															double d = 15;
															while(d<=30 && st) {
																double x = (15 + i) * Math.cos(d);
																double z = (15 + i) * Math.sin(d);
																PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("fireworksSpark", 
																		(float) (loc.getX() + x), (float) (loc.getY()-20), (float) (loc.getZ() + z), 0, 0, 0, 0, 1);
																	((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
																d+=0.05;
																try {
																	Thread.sleep(5);
																} catch (InterruptedException e) {
																	// TODO Auto-generated catch block
																	e.printStackTrace();
																}
															}
															}
															double d = 21;
															while(d<=100 && st) {
																double x = d * Math.cos(d);
																double z = d * Math.sin(d);
																 PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("fireworksSpark", 
																		(float) (loc.getX() + x), (float) (loc.getY()-20), (float) (loc.getZ() + z), 0, 0, 0, 0, 1);
																		((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
																 d+=0.05;
																 try {
																		Thread.sleep(3);
																	} catch (InterruptedException e) {
																		// TODO Auto-generated catch block
																		e.printStackTrace();
																	}
															}
													}
												};
												Core.threads.add(t);
												t.start();
												n:for(int j = 0;j<100;j++) {
													for(int i = 0;i<32;i++) {
														if(!st)break n;
														PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("fireworksSpark", 
																(float) (loc.getX() + xs[i]/100.0*j), 
																	(float) (loc.getY()-20), (float) (loc.getZ() + zs[i]/100.0*j), 0, 0, 0, 0, 1);
															((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
													}
													try {
														Thread.sleep(10);
													} catch (InterruptedException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
												}
											}
										};
										Core.threads.add(t);
										t.start();
									}
								};
								Core.threads.add(t);
								t.start();
					}				
				};
				Core.threads.add(t);
				t.start();
				CustomThread a = new CustomThread() {
					public void run() {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						double i=0;
						while(i<50 && st) {
							double x1 = 15 * Math.cos(i);
							double z1 = 15 * Math.sin(i);
							CustomThread t = new CustomThread() {
								double j=0;
								@SuppressWarnings("deprecation")
								public void run() {
									while(j<50 && st) {
										double x2 = 15 * Math.cos(j);
										double z2 = 15 * Math.sin(j);
										PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("flame", 
												(float) (loc.getX() + x1 + x2), (float) (loc.getY()-20),
												(float) (loc.getZ() + z1 + z2), 0, 0, 0, 0, 1);
										for(Player p : Bukkit.getOnlinePlayers()) {
											((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
										}
										j+=0.1;
										try {
											Thread.sleep(20);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							};
							Core.threads.add(t);
							t.start();
							i+=1;
							try {
								Thread.sleep(20);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				};
				Core.threads.add(a);
				a.start();
				break;
				
			}
		}else {
			printUsage(sender);
		}
		return right;
	}
	
	public static void createCircle(Location loc, int radius, double time, int dense) {
		new Thread() {
			double repeat = 0;
			@SuppressWarnings("deprecation")
			public void run() {
				while(repeat<=100) {
					double x = radius * Math.cos(repeat);
					double z = radius * Math.sin(repeat);
					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("fireworksSpark", (float) (loc.getX() + x), (float) (loc.getY()), (float) (loc.getZ() + z), 200, 200, 200, 200, 1);
					for(Player online : Bukkit.getOnlinePlayers()) {
						((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
					}
					try {
						Thread.sleep(100/dense);
					}catch(InterruptedException e) {
						e.printStackTrace();
						}
					repeat+=100.0/time;
					}
				Core.broadcast("END!");
			}
		}.start();;
	}
	
	private void printUsage(CommandSender sender) {
			for(String s : available) {
				sender.sendMessage(Core.prefix +  ChatColor.BOLD + "/국가 " + s);
			}
	}
}
