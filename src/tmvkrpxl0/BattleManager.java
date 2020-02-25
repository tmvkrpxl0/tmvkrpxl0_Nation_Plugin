package tmvkrpxl0;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tmvkrpxl0.Config.BattleInfo;
import tmvkrpxl0.Config.BattleStateConfig;

class BattleManager {
    private static List<BattleInfo> battleState; //int[0] = 준비 [1]=전쟁
    private static LinkedHashMap<BattleInfo, BukkitRunnable> timers;
    protected static LinkedHashMap<Player, String> choise = new LinkedHashMap<Player, String>();
	protected BattleManager(){
		battleState = ((BattleStateConfig)Core.loadFile("전쟁상황.yml", BattleStateConfig.class)).getBattleState();
    	timers = new LinkedHashMap<>();
        if(!battleState.isEmpty()) {
            int de = Core.Config.getWar().get("delay message time after server restart");
            int rt = Core.Config.getWar().get("re-preparing time after server restart");
            new BukkitRunnable(){
            @Override
            public void run(){
                Core.broadcast("서버가 종료되기 전, 전쟁중이던 국가가 있었습니다");
                Core.broadcast(secondsToString(rt) + "의 추가 준비시간 이후 전쟁을 다시 시작하겠습니다");
                for(BattleInfo info : battleState) {
                	for(String p : TeamManager.getTeam(info.getStarter())) {
                		Bukkit.getPlayerExact(p).sendMessage(Core.prefix + info.getVictim() + "국과의전쟁은 준비시간 " + 
                	secondsToString(info.getReadyTime()) + " 전쟁시간 " + secondsToString(info.getWarTime()) + "남아있었습니다");
                	}
                	for(String p : TeamManager.getTeam(info.getVictim())) {
                		Bukkit.getPlayerExact(p).sendMessage(Core.prefix + info.getVictim() + "국과의전쟁은 준비시간 " + 
                            	secondsToString(info.getReadyTime()) + "전쟁시간 " + secondsToString(info.getWarTime()) + "남아있었습니다");
                	}
                	if(info.getReadyTime()<=rt) {
                		info.setReadyTime(rt);
                	}
                	setTimer(info);
                }
            }
            }.runTaskLater(Core.plugin, de*20);
        }
    }
    protected static byte declare(String s, String v){//sa[0]=건쪽 sa[1]=선포 당한쪽
        if(warWithWho(s)!=null)return 1;//거는쪽에서 이미 전쟁중
        if(warWithWho(v)!=null)return 2;//받는쪽
        if(!TeamManager.getTeamList().contains(v))return 3;
        for(String p : TeamManager.getTeam(s)) {
        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + v + ChatColor.RED + "국과의 전쟁을 시작합니다.");
        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + "준비시간은 총 " + secondsToString(Core.Config.getWar().get("preparing time")) + " 이며, 전쟁시간은 " +
        			secondsToString(Core.Config.getWar().get("battle time")) + "입니다");
        }
        for(String p : TeamManager.getTeam(v)) {
        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + s + ChatColor.DARK_RED + "국에서 전쟁을 선포했습니다!");
        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + "준비시간은 총 " + secondsToString(Core.Config.getWar().get("preparing time")) + " 이며, 전쟁시간은 " +
        			secondsToString(Core.Config.getWar().get("battle time")) + "입니다");
        }
        BattleInfo info = new BattleInfo(s, v, Core.Config.getWar().get("preparing time"), Core.Config.getWar().get("battle time"));
        battleState.add(info);
        setTimer(info);
        return 0;// 0이면 성공
    }

    protected static String secondsToString(int seconds) {
    	String r = "" + ((seconds/60)>0?(seconds/60) + "분 ":"");
    	r+= ((seconds%60)>0?(seconds%60) + "초":"");
        return r;
    }
    protected static BattleInfo warWithWho(String nation){
        for(BattleInfo s : battleState){
        	if(nation.equals(s.getStarter()) || nation.equals(s.getVictim()))return s;
        }
        return null;
    }
    protected void save(){
    	Core.saveFile("전쟁상황.yml", BattleStateConfig.class, new BattleStateConfig(battleState));
    }
    
    private static void setTimer(BattleInfo info){
        //note: runTaskTimer 쓸때, delay 초 뒤에 시작되며, period 마다 한번씩
    	
        timers.put(info, new BukkitRunnable(){
            @Override
            public void run(){
            	if(info.getReadyTime()>0) {
            		info.setReadyTime(info.getReadyTime()-1);
            		switch(info.getReadyTime()) {
            		case 180:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "준비시간이 3분 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "준비시간이 3분 남았습니다");
            			}
            			break;
            		
            		case 60:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "준비시간이 1분 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "준비시간이 1분 남았습니다");
            			}
            			break;
            		case 30:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "준비시간이 30초 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "준비시간이 30초 남았습니다");
            			}
            			break;
            		case 0:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + ChatColor.RED + "준비시간이 끝났습니다");
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + ChatColor.RED + "이제 곧 " + info.getVictim() + "국가와의 전쟁을 시작합니다..");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + ChatColor.RED + "준비시간이 끝났습니다");
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + ChatColor.RED + "이제 곧 " + info.getStarter() + "국가와의 전쟁을 시작합니다..");
            			}
            		default:
            			if(info.getReadyTime()<=10 && info.getReadyTime() > 0) {
            				for(String s : TeamManager.getTeam(info.getStarter())) {
                				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "준비시간이 " + info.getReadyTime() + "초 남았습니다");
                			}
                			for(String s : TeamManager.getTeam(info.getVictim())) {
                				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "준비시간이 " + info.getReadyTime() + "초 남았습니다");
                			}
            			}
            			break;
            		}
            	}else {
            		info.setWarTime(info.getWarTime()-1);
            		switch(info.getWarTime()) {
            		case 180:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "전쟁시간이 3분 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "전쟁시간이 3분 남았습니다");
            			}
            			break;
            		
            		case 60:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "전쟁시간이 1분 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "전쟁시간이 1분 남았습니다");
            			}
            			break;
            		case 30:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "전쟁시간이 30초 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "전쟁시간이 30초 남았습니다");
            			}
            			break;
            		case 0:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + info.getVictim() + "국과의 전쟁이 끝났습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + info.getStarter() + "국과의 전쟁이 끝났습니다");
            			}
            			battleState.remove(info);
            			timers.remove(warWithWho(info.getVictim()));
            			this.cancel();
            		default:
            			if(info.getWarTime()<=10 && info.getWarTime()>0) {
            				for(String s : TeamManager.getTeam(info.getStarter())) {
                				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "전쟁시간이 " + info.getWarTime() + "초 남았습니다");
                			}
                			for(String s : TeamManager.getTeam(info.getVictim())) {
                				Bukkit.getPlayerExact(s).sendMessage(Core.prefix + "전쟁시간이 " + info.getWarTime() + "초 남았습니다");
                			}
            			}
            			break;
            		}
            	}
            }
        });
        timers.get(info).runTaskTimer(Core.plugin, 20, 20);
    }
    
    protected static void remove(String nation) {
    	BattleInfo info = warWithWho(nation);
    	if(timers.containsKey(info))
    		timers.get(info).cancel();
    	timers.remove(info);
    	battleState.remove(info);
    }
    
    protected static String getOpponent(String nation) {
    	BattleInfo info = warWithWho(nation);
    	return nation.equals(info.getStarter())?info.getVictim():info.getStarter();
    }
}