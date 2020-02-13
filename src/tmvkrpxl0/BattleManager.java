package tmvkrpxl0;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;
import tmvkrpxl0.Config.BattleInfo;
import tmvkrpxl0.Config.BattleStateConfig;

class BattleManager {
    private static List<BattleInfo> battleState; //int[0] = 준비 [1]=전쟁
    protected static LinkedHashMap<Player, String> choise = new LinkedHashMap<Player, String>();
    private static int warTime = Core.Config.getWar().get("battle time");
    private static int readyTime = Core.Config.getWar().get("preparing time");
	protected BattleManager(){
		battleState = ((BattleStateConfig)Core.loadFile("전쟁상황.yml", BattleStateConfig.class)).getBattleState();
    	
        if(!battleState.isEmpty()) {
            int de = Core.plugin.getConfig().getInt("war.delay message time after server restart");
            int rt = Core.plugin.getConfig().getInt("war.re-preparing time after server restart");
            new BukkitRunnable(){
            @Override
            public void run(){
                Core.broadcast("서버가 종료되기 전, 전쟁중이던 국가가 있었습니다");
                Core.broadcast(rt + "초의 준비시간 이후 전쟁을 다시 시작하겠습니다");
                for(BattleInfo info : battleState) {
                	for(String p : TeamManager.getTeam(info.getStarter())) {
                		Bukkit.getPlayerExact(p).sendMessage(Core.prefix + info.getVictim() + "국과의전쟁은 " + 
                				(info.getReady()?("준비시간 " + secondsToString(info.getTime()) + "전쟁시간 " + secondsToString(warTime)):
                					("준비시간 0초 전쟁시간 " + secondsToString(info.getTime()))) + " 남았습니다");
                	}
                	for(String p : TeamManager.getTeam(info.getVictim())) {
                		Bukkit.getPlayerExact(p).sendMessage(Core.prefix + info.getVictim() + "국과의전쟁은 " + 
                				(info.getReady()?("준비시간 " + secondsToString(info.getTime()) + "전쟁시간 " + secondsToString(warTime)):
                					("준비시간 0초 전쟁시간 " + secondsToString(info.getTime()))) + " 남았습니다");
                	}
                	if(!info.getReady() || (info.getReady() && info.getTime()<=rt)) {
                		info.setReady(true);
                		info.setTime(rt);
                	}
                }
            }
            }.runTaskLater(Core.plugin, de*20);
        }
    }
    protected static byte declare(String s, String v){//sa[0]=건쪽 sa[1]=선포 당한쪽
        if(warWithWho(s)!=null)return 1;//거는쪽에서 이미 전쟁중
        if(warWithWho(v)!=null)return 2;//받는쪽
        for(String p : TeamManager.getTeam(s)) {
        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + v + ChatColor.RED + "국과의 전쟁을 시작합니다.");
        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + "준비시간은 총 " + secondsToString(readyTime) + " 이며, 전쟁시간은 " +
        			secondsToString(warTime) + "입니다");
        }
        for(String p : TeamManager.getTeam(v)) {
        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + s + ChatColor.DARK_RED + "국에서 전쟁을 선포했습니다!");
        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + "준비시간은 총 " + secondsToString(readyTime) + " 이며, 전쟁시간은 " +
        			secondsToString(warTime) + "입니다");
        }
        BattleInfo info = new BattleInfo(s, v, readyTime, true);
        battleState.add(info);
        setTimer(info);
        return 0;// 0이면 성공
    }

    protected static String secondsToString(int seconds) {
    	String r = "" + ((seconds/60)>0?(seconds/60) + "분 ":"");
    	r+= (seconds%60) + "초";
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
        new BukkitRunnable(){
            @Override
            public void run(){
            	info.setTime(info.getTime()-1);
            	if(info.getTime()==300 || info.getTime()==180 || info.getTime()==60 || info.getTime()<=5) {
            		for(String p : TeamManager.getTeam(info.getStarter())) {
                    	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + info.getVictim() + "국과의 전쟁에서 현제 남은" + (info.getReady()?"준비":"전쟁") + "시간은" +
                    			secondsToString(info.getTime()) + "남았습니다.");
                    }
                    for(String p : TeamManager.getTeam(info.getVictim())) {
                    	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + info.getStarter() + "국과의 전쟁에서 남은" + (info.getReady()?"준비":"전쟁") + "시간은" +
                    			secondsToString(info.getTime()) + "남았습니다.");
                    }
            	}
            	if(info.getTime()==0) {
            		if(info.getReady()) {
            			info.setReady(false);
            			info.setTime(warTime);
            			for(String p : TeamManager.getTeam(info.getStarter())) {
                        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + info.getVictim() + ChatColor.RED + "국과의 전쟁을 시작합니다");
                        }
                        for(String p : TeamManager.getTeam(info.getVictim())) {
                        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + info.getStarter() + ChatColor.RED + "국과의 전쟁을 시작합니다");
                        }
            		}else {
            			for(String p : TeamManager.getTeam(info.getStarter())) {
                        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + info.getVictim() + ChatColor.RED + "국과의 전쟁이 끝났습니다");
                        }
                        for(String p : TeamManager.getTeam(info.getVictim())) {
                        	Bukkit.getPlayerExact(p).sendMessage(Core.prefix + info.getStarter() + ChatColor.RED + "국과의 전쟁이 끝났습니다");
                        }
                        battleState.remove(info);
                        this.cancel();
            		}
            	}
            }
        }.runTaskTimer(Core.plugin, 20, 20);
    }
    
    protected static void remove(String nation) {
    	battleState.remove(warWithWho(nation));
    }
    
    protected static String getOpponent(String nation) {
    	BattleInfo info = warWithWho(nation);
    	return nation.equals(info.getStarter())?info.getVictim():info.getStarter();
    }
}