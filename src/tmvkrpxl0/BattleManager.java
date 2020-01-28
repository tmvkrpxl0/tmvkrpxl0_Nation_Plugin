package tmvkrpxl0;

import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

@SuppressWarnings("unchecked")
class BattleManager {
    private static LinkedHashMap<String[], int[]> battleState; //int[0] = 준비 [1]=전쟁
    private static final int ctime = 300;//config로 설정 가능하게 할것. Note.단위는 2개다 초다
    private static final int wtime = 600;//config로 설정 가능하게 할것.
    protected BattleManager(){
        battleState = (LinkedHashMap<String[], int[]>) Core.loadfile("plugins/Kukga/battle.yml");
        if(battleState==null)battleState = new LinkedHashMap<String[], int[]>();
        else{
            int de = 10;//config.yml로 설정 가능하게 할것
            int rt = 60;//이거도 설정 가능하게 할것.
            new BukkitRunnable(){
            @Override
            public void run(){
                Core.broadcast("서버가 종료되기 전, 전쟁중이던 국가가 있었습니다");
                Core.broadcast(rt + "초의 준비시간 이후 전쟁을 다시 시작하겠습니다");
                Core.broadcast("전쟁하고 있던 국가들은 다음과 같습니다");
            }
            }.runTaskLater(Core.plugin, de*20);
            int [] previous;
            for(String [] s : battleState.keySet()){
                previous = battleState.get(s);
                Bukkit.broadcastMessage(s[0] + " vs " + s[1] + ": " + 
                                        secondsToString((previous[0]==0)?previous[1]:previous[0]));
            }
            new BukkitRunnable(){
                int [] t;
                boolean ready;
                public void run(){
                    Core.broadcast("이제부터 타이머가 재게됩니다");
                    for(String [] s : battleState.keySet()){
                        ready = (t[0]==0) ? false : true;
                        t = battleState.get(s);
                        setTimer(ready, ready?t[0]:t[1], s[0], s[1]);
                    }
                }   
            }.runTaskLater(Core.plugin, rt*20);
        }
    }
    protected static int declare(String st1, String st2){//st1=건쪽 st2=선포 당한쪽
        if(warWithWho(st1)!=null)return 1;//거는쪽에서 이미 전쟁중
        if(warWithWho(st2)!=null)return 2;//받는쪽
        Core.broadcast(ChatColor.RED + st1 + "국가가" + st2 + "국가에게 전쟁을 선포했습니다!");
        Core.broadcast(ChatColor.RED + st1 + "국가가" + st2 + "국가에게 전쟁을 선포했습니다!");
        Core.broadcast(ChatColor.RED + st1 + "국가가" + st2 + "국가에게 전쟁을 선포했습니다!");
        Core.broadcast(ChatColor.GOLD + "준비시간" + secondsToString(ctime) + "를 시작합니다..");
        setTimer(true, ctime, st1, st2);
        return 0;// 0이면 성공
    }

    private static String secondsToString(int seconds) {
        return "" + (seconds/60) + "분 " + (seconds%60) + "초";
    }
    protected static String warWithWho(String nation){
    	if(battleState.keySet() == null)Core.broadcast("asdf");
        for(String [] s : battleState.keySet()){
            return (s[0].equals(nation))?s[1]:s[0];
        }
        return null;
    }
    protected static void save(){
        Core.savefile("plugins/Kukga/battle.yml", battleState);
    }
    
    private static void setTimer(boolean ready, int ptime, String st1, String st2){
        //note: runTaskTimer 쓸때, delay 초 뒤에 시작되며, period 마다 한번씩
        new BukkitRunnable(){

            int time = ptime;
            String rw;
            @Override
            public void run(){
                if(ready)rw="전쟁 준비";
                else rw="전쟁";
                time--;
                battleState.put(new String[]{st1, st2}, new int[]{ready ? time : 0, ready ? 0 : time});
                if(time==ctime/2)
                    Core.broadcast(
                    st1 + "국가와" + st2 + "국가의 " + rw +"시간의 절반이 흘렀습니다..");
                if(time==120)Core.broadcast(
                st1 + "국가와" + st2 + "국가의 " + rw +"시간이 2분 남았습니다..");
                if(time==60)Core.broadcast(
                st1 + "국가와" + st2 + "국가의 " + rw +"시간이 1분 남았습니다..");
                if(time<6)Core.broadcast(
                st1 + "국가와" + st2 + "국가의 " + rw +"시간이" + secondsToString(time) + " 남았습니다..");
                if(time==0){
                if(ready){
                Core.broadcast(ChatColor.RED +
                st1 + "국가와" + st2 + "국가의 전쟁이 시작되었습니다.");
                setTimer(false, wtime, st1, st2);
                cancel();
                }else{
                    Core.broadcast(ChatColor.RED +
                    st1 + "국가와" + st2 + "국가의 전쟁이 끝났습니다.");
                    battleState.remove(new String[]{st1, st2});
                }
                cancel();
                }
                }
        }.runTaskTimer(Core.plugin, 0, 20);
    }
    
    protected static void surrender(String nation) {
    	
    }
    
    
}