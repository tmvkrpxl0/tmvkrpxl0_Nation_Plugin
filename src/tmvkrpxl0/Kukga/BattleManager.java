package tmvkrpxl0.Kukga;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import tmvkrpxl0.Config.BattleInfo;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

class BattleManager {
    private static LinkedHashMap<BattleInfo, BukkitRunnable> timers;
    private static LinkedList<BattleInfo> battleState;
	protected BattleManager(){
		battleState = new LinkedList<>();
		try {
			File f = new File(KukgaMain.plugin.getDataFolder() + File.separator + "전쟁상황.cfg");
			if(!f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			for(String l : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) {
				try {
					battleState.add(new BattleInfo(Integer.parseInt(l.substring(l.indexOf("ReadyTime:") + 10, l.indexOf(" WarTime:"))), Integer.parseInt(l.substring(l.indexOf("WarTime:") + 8)), l.substring(l.indexOf("Starter:")+8, l.indexOf(" Victim:")), l.substring(l.indexOf("Victim:") + 7, (l.indexOf(" ReadyTime:")))));
				} catch(Exception ignored){
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		timers = new LinkedHashMap<>();
        if(!battleState.isEmpty()) {
            int de = KukgaMain.config.getInt("war.delay message time after server restart");
            final int rt = KukgaMain.config.getInt("war.re-preparing time after server restart");
            new BukkitRunnable(){
            @Override
            public void run(){
                KukgaMain.broadcast("서버가 종료되기 전, 전쟁중이던 국가가 있었습니다");
                KukgaMain.broadcast(secondsToString(rt) + "의 추가 준비시간 이후 전쟁을 다시 시작하겠습니다");
                for(BattleInfo info : battleState) {
                	for(String p : TeamManager.getTeam(info.getStarter())) {
                		Bukkit.getPlayerExact(p).sendMessage(KukgaMain.prefix + info.getVictim() + "국과의전쟁은 준비시간 " + 
                	secondsToString(info.getReadyTime()) + " 전쟁시간 " + secondsToString(info.getWarTime()) + "남아있었습니다");
                	}
                	for(String p : TeamManager.getTeam(info.getVictim())) {
                		Bukkit.getPlayerExact(p).sendMessage(KukgaMain.prefix + info.getVictim() + "국과의전쟁은 준비시간 " + 
                            	secondsToString(info.getReadyTime()) + "전쟁시간 " + secondsToString(info.getWarTime()) + "남아있었습니다");
                	}
                	if(info.getReadyTime()<=rt) {
                		info.setReadyTime(rt);
                	}
                	setTimer(info);
                }
            }
            }.runTaskLater(KukgaMain.plugin, de*20);
        }
    }
    protected static byte declare(String s, String v){//sa[0]=건쪽 sa[1]=선포 당한쪽
        if(warWithWho(s)!=null)return 1;//거는쪽에서 이미 전쟁중
        if(warWithWho(v)!=null)return 2;//받는쪽
        if(!TeamManager.getTeamList().contains(v))return 3;//v라는 팀이 없음
		if(TerritoryManager.getRegionNumber(v)==0)return 4;//당하는 쪽이 영토가 없음
        for(String p : TeamManager.getTeam(s)) {
        	Bukkit.getPlayerExact(p).sendMessage(KukgaMain.prefix + v + ChatColor.RED + "국과의 전쟁을 시작합니다.");
        	Bukkit.getPlayerExact(p).sendMessage(KukgaMain.prefix + "준비시간은 총 " + secondsToString(KukgaMain.config.getInt("war.preparing time")) + " 이며, 전쟁시간은 " +
        			secondsToString(KukgaMain.config.getInt("war.battle time")) + "입니다");
        }
        for(String p : TeamManager.getTeam(v)) {
        	Bukkit.getPlayerExact(p).sendMessage(KukgaMain.prefix + s + ChatColor.DARK_RED + "국에서 전쟁을 선포했습니다!");
        	Bukkit.getPlayerExact(p).sendMessage(KukgaMain.prefix + "준비시간은 총 " + secondsToString(KukgaMain.config.getInt("war.preparing time")) + " 이며, 전쟁시간은 " +
        			secondsToString(KukgaMain.config.getInt("war.battle time")) + "입니다");
        }
        BattleInfo info = new BattleInfo(KukgaMain.config.getInt("war.preparing time"), KukgaMain.config.getInt("war.battle time"), s, v);
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
		try {
			File f = new File(KukgaMain.plugin.getDataFolder() + File.separator + "전쟁상황.cfg");
			if(!f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8));
			for(BattleInfo info : battleState){
				writer.write("Starter:" + info.getStarter() + " Victim:" + info.getVictim() + " ReadyTime:" + info.getReadyTime() + " WarTime:" + info.getWarTime());
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static void setTimer(final BattleInfo info){
        //note: runTaskTimer 쓸때, delay 초 뒤에 시작되며, period 마다 한번씩
    	
        timers.put(info, new BukkitRunnable(){
            @Override
            public void run(){
            	if(info.isReady()) {
            		info.setReadyTime(info.getReadyTime()-1);
            		switch(info.getReadyTime()) {
            		case 180:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "준비시간이 3분 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "준비시간이 3분 남았습니다");
            			}
            			break;
            		
            		case 60:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "준비시간이 1분 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "준비시간이 1분 남았습니다");
            			}
            			break;
            		case 30:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "준비시간이 30초 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "준비시간이 30초 남았습니다");
            			}
            			break;
            		case 0:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + ChatColor.RED + "준비시간이 끝났습니다");
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + ChatColor.RED + "이제 곧 " + info.getVictim() + "국가와의 전쟁을 시작합니다..");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + ChatColor.RED + "준비시간이 끝났습니다");
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + ChatColor.RED + "이제 곧 " + info.getStarter() + "국가와의 전쟁을 시작합니다..");
            			}
            		default:
            			if(info.getReadyTime()<=10 && info.getReadyTime() > 0) {
            				for(String s : TeamManager.getTeam(info.getStarter())) {
                				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "준비시간이 " + info.getReadyTime() + "초 남았습니다");
                			}
                			for(String s : TeamManager.getTeam(info.getVictim())) {
                				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "준비시간이 " + info.getReadyTime() + "초 남았습니다");
                			}
            			}
            			break;
            		}
            	}else {
            		info.setWarTime(info.getWarTime()-1);
            		switch(info.getWarTime()) {
            		case 180:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "전쟁시간이 3분 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "전쟁시간이 3분 남았습니다");
            			}
            			break;
            		
            		case 60:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "전쟁시간이 1분 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "전쟁시간이 1분 남았습니다");
            			}
            			break;
            		case 30:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "전쟁시간이 30초 남았습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "전쟁시간이 30초 남았습니다");
            			}
            			break;
            		case 0:
            			for(String s : TeamManager.getTeam(info.getStarter())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + info.getVictim() + "국과의 전쟁이 끝났습니다");
            			}
            			for(String s : TeamManager.getTeam(info.getVictim())) {
            				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + info.getStarter() + "국과의 전쟁이 끝났습니다");
            			}
            			battleState.remove(info);
            			timers.remove(warWithWho(info.getVictim()));
            			this.cancel();
            		default:
            			if(info.getWarTime()<=10 && info.getWarTime()>0) {
            				for(String s : TeamManager.getTeam(info.getStarter())) {
                				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "전쟁시간이 " + info.getWarTime() + "초 남았습니다");
                			}
                			for(String s : TeamManager.getTeam(info.getVictim())) {
                				Bukkit.getPlayerExact(s).sendMessage(KukgaMain.prefix + "전쟁시간이 " + info.getWarTime() + "초 남았습니다");
                			}
            			}
            			break;
            		}
            	}
            }
        });
        timers.get(info).runTaskTimer(KukgaMain.plugin, 20, 20);
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
    	if(info!=null)return nation.equals(info.getStarter())?info.getVictim():info.getStarter();
    	else return "";
    }
}