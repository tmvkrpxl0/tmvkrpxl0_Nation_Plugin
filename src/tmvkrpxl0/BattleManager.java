package tmvkrpxl0;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

class BattleManager {
    private static LinkedHashMap<String, int[]> battleState; //int[0] = 준비 [1]=전쟁
    protected static LinkedHashMap<Player, String> choise = new LinkedHashMap<Player, String>();
    protected static int ctime;
    private static int wtime;
    private FileConfiguration data;
	protected BattleManager(){
    		data = YamlConfiguration.loadConfiguration(new File(Core.plugin.getDataFolder(), "전쟁상황.yml"));
    	battleState = new LinkedHashMap<String, int[]>();
    	if(data.getConfigurationSection("전쟁상태")!=null) {
    	Map<String, Object> temp = data.getConfigurationSection("전쟁상태").getValues(false);
    		for(String s : temp.keySet())battleState.put(s, (int[])temp.get(s));
    	}
    	ctime = Core.plugin.getConfig().getInt("war.preparing time");
    	wtime = Core.plugin.getConfig().getInt("war.battle time");
    	
        if(!battleState.isEmpty()) {
            int de = Core.plugin.getConfig().getInt("war.delay message time after server restart");
            int rt = Core.plugin.getConfig().getInt("war.re-preparing time after server restart");
            new BukkitRunnable(){
            @Override
            public void run(){
                Core.broadcast("서버가 종료되기 전, 전쟁중이던 국가가 있었습니다");
                Core.broadcast(rt + "초의 준비시간 이후 전쟁을 다시 시작하겠습니다");
                Core.broadcast("전쟁하고 있던 국가들은 다음과 같습니다");
            }
            }.runTaskLater(Core.plugin, de*20);
            int [] previous;
            for(String s : battleState.keySet()){
                previous = battleState.get(s);
                Bukkit.broadcastMessage(s+ secondsToString((previous[0]==0)?previous[1]:previous[0]));
            }
            new BukkitRunnable(){
                int [] t;
                boolean ready;
                public void run(){
                    Core.broadcast("이제부터 타이머가 재게됩니다");
                    for(String s : battleState.keySet()){
                        ready = (t[0]==0) ? false : true;
                        t = battleState.get(s);
                        setTimer(ready, ready?t[0]:t[1], s.split("vs"));
                    }
                }   
            }.runTaskLater(Core.plugin, rt*20);
        }
    }
    protected static int declare(String [] sa){//sa[0]=건쪽 sa[1]=선포 당한쪽
        if(warWithWho(sa[0])!=null)return 1;//거는쪽에서 이미 전쟁중
        if(warWithWho(sa[1])!=null)return 2;//받는쪽
        Core.broadcast(ChatColor.RED + sa[0] + "국가가" + sa[1] + "국가에게 전쟁을 선포했습니다!");
        Core.broadcast(ChatColor.RED + sa[0] + "국가가" + sa[1] + "국가에게 전쟁을 선포했습니다!");
        Core.broadcast(ChatColor.RED + sa[0] + "국가가" + sa[1] + "국가에게 전쟁을 선포했습니다!");
        Core.broadcast(ChatColor.GOLD + "준비시간" + secondsToString(ctime) + "를 시작합니다..");
        setTimer(true, ctime, sa);
        return 0;// 0이면 성공
    }

    private static String secondsToString(int seconds) {
        return "" + (seconds/60) + "분 " + (seconds%60) + "초";
    }
    protected static String[] warWithWho(String nation){
    	String [] sa;
        for(String s : battleState.keySet()){
        	sa = s.split("vs");
            if(sa[0].equals(nation))return new String[] {nation, sa[1]};
            if(sa[1].equals(nation))return new String[] {sa[0] + nation};
        }
        return null;
    }
    protected void save(){
    	data.createSection("전쟁상태", battleState);
    	try {
			data.save(new File(Core.plugin.getDataFolder(), "전쟁상황.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static void setTimer(boolean ready, int ptime, String [] sa){
        //note: runTaskTimer 쓸때, delay 초 뒤에 시작되며, period 마다 한번씩
        new BukkitRunnable(){

            int time = ptime;
            String rw;
            @Override
            public void run(){
                if(ready)rw="전쟁 준비";
                else rw="전쟁";
                time--;
                battleState.put(sa[0] + "vs" + sa[1], new int[]{ready ? time : 0, ready ? 0 : time});
                if(time==ctime/2)
                    Core.broadcast(
                    sa[0] + "국가와" + sa[1] + "국가의 " + rw +"시간의 절반이 흘렀습니다..");
                if(time==120)Core.broadcast(
                sa[0] + "국가와" + sa[1] + "국가의 " + rw +"시간이 2분 남았습니다..");
                if(time==60)Core.broadcast(
                sa[0] + "국가와" + sa[1] + "국가의 " + rw +"시간이 1분 남았습니다..");
                if(time<6)Core.broadcast(
                sa[0] + "국가와" + sa[1] + "국가의 " + rw +"시간이" + secondsToString(time) + " 남았습니다..");
                if(time==0){
                if(ready){
                Core.broadcast(ChatColor.RED +
                sa[0] + "국가와" + sa[1] + "국가의 전쟁이 시작되었습니다.");
                setTimer(false, wtime, sa);
                cancel();
                }else{
                    Core.broadcast(ChatColor.RED +
                    sa[0] + "국가와" + sa[1] + "국가의 전쟁이 끝났습니다.");
                    battleState.remove(sa[0] + "vs" + sa[1]);
                }
                cancel();
                }
                }
        }.runTaskTimer(Core.plugin, 0, 20);
    }
    
    protected static void remove(String nation) {
    	battleState.remove(warWithWho(nation)[0] + "vs" + warWithWho(nation)[1]);
    }
}