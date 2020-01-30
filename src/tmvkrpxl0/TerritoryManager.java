package tmvkrpxl0;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

class TerritoryManager {
    private static LinkedHashMap<String, LinkedList<ArrayList<Object>>> locations; //국가별 성들의 신호기 위치, 모서리, 이름(이름은 **"무조건"** [역할]신호기 이름 처럼 지을것), 신호기 내구도 그리고 철문 내구도 저장
    //위치 역할 이름 내구도는 인덱스가 같으면 같은 신호기로 취급
    //[0] 은 위치, 반환값은 int[], 왼쪽[]은 신호기 위치. 밝기 변화를 위해 XYZ3개다 넣을것.
    //[1] 은 모서리, 반환값은 int[][], 왼쪽[]은  모서리 구별(0번부터 3번까지 Z순서로), 오른쪽[]은 XZ 좌표 저장
    //[2] 는 이름 & 역할, 반환값은 String
    //TODO:복구수단 알아내기
    protected static int minDistance = 128;//TODO:Config로 설정하기
    protected static int minDistanceFriendly = 64;//아군성끼리 거리 제한
    private FileConfiguration data;
    @SuppressWarnings("unchecked")
    protected TerritoryManager(){
    		data = YamlConfiguration.loadConfiguration(new File(Core.plugin.getDataFolder(), "영토.yml"));
    	locations = new LinkedHashMap<String, LinkedList<ArrayList<Object>>>();
    	if(data.getConfigurationSection("영토정보")!=null) {
    		Map<String, Object> t = data.getConfigurationSection("영토정보").getValues(false);
        	for(String s : t.keySet()) {
        		LinkedList<ArrayList<Object>> pu = new LinkedList<ArrayList<Object>>();
        		for(ArrayList<Object> al : (ArrayList<ArrayList<Object>>)t.get(s)) {
        			ArrayList<Object> apu = new ArrayList<Object>();
        			for(Object o: al) {
        				apu.add(o);
        			}
        			pu.add(apu);
        		}
        		locations.put(s, pu);
        	}
    	}
    	new BukkitRunnable() {
    		int light = 0;
    		boolean down = false;
    		public void run() {
    			for(String s : locations.keySet()) {
    				for(ArrayList<Object> lo : locations.get(s)) {
    					int [] tia = Arrays.stream((Integer[])lo.get(0)).mapToInt(Integer::intValue).toArray();
    					LightChanger.createLightSource(new Location(Bukkit.getWorlds().get(0),
    							tia[0], tia[1], tia[2]), light);
    					if(down)light--;
    					else light++;
    					if(light==15)down=true;
    					else if(light==0)down=false;
    				}
    			}
    		}
    	}.runTaskTimer(Core.plugin, 0, 2);
    }

	protected static String registerRegion(String nation, Location yloc, String name){
		int [] loc = new int[] {yloc.getBlockX(), yloc.getBlockZ()};
		int [][] ita = getEdges(loc);
    	for(String s: locations.keySet()) {
    		for(ArrayList<Object> lo : locations.get(s)) {
    			int [] temp = new int[] {((Integer[])lo.get(0))[0], ((Integer[])lo.get(0))[2]};
    			if(getDistance(temp, loc)<=(s.equals(nation)?minDistanceFriendly:minDistance))return s;//신호기들끼리 거리계산
    			for(Integer[] edg: ((Integer[][])lo.get(1))) {
    				if(getDistance(Arrays.stream(edg).mapToInt(Integer::intValue).toArray(), loc)<=(s.equals(nation)?minDistanceFriendly:minDistance))return s;//신호기와 모서리 거리 계산
    			}
    			for(int [] li : ita) {
    				if(getDistance(li, temp)<=(s.equals(nation)?minDistanceFriendly:minDistance))return s;//모서리와 신호기 거리계산
    				for(int[] ned: ((int[][])lo.get(1))) {
    					if(getDistance(li, ned)<=(s.equals(nation)?minDistanceFriendly:minDistance))return s;//모서리들끼리 거리계산
    				}
    			}
    		}
    	}
    	LinkedList<ArrayList<Object>> llo;
    	if(!locations.containsKey(nation))llo = new LinkedList<ArrayList<Object>>();
    	else llo = locations.get(nation);
    	ArrayList<Object> lo = new ArrayList<Object>(3);
    	lo.add(new Integer[] {yloc.getBlockX(), yloc.getBlockY(), yloc.getBlockZ()});
    	Integer [][] Ita = new Integer[4][2];
    	for(int i=0;i<4;i++) {
    		for(int j=0;j<2;j++) {
    			Ita[i][j] = ita[i][j];
    		}
    	}
    	lo.add(Ita);
		lo.add(name);
		llo.add(new ArrayList<Object>(lo));
		locations.put(nation, llo);
        return null;
    }

    protected void save() {
    	registerRegion("test", Bukkit.getPlayerExact("tmvkrpxl0").getLocation(), "testbeaconname");
    	data.set("영토정보", locations);
    	try {
			data.save(new File(Core.plugin.getDataFolder(), "영토.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    protected static int[][] getEdges(int[] centor){
    	return new int[][] {new int[] {centor[0]-24, centor[1]+25},new int[] {centor[0]+25, centor[1]+25},new int[] {centor[0]-24, centor[1]-24},
    		new int[] {centor[0]+25, centor[1]-24}};
    }
    
	protected static String isInRegion(Location loc){
    	int [] li = new int[] {loc.getBlockX(), loc.getBlockZ()};
    	for(String s : locations.keySet()) {
    		for(ArrayList<Object> lo : locations.get(s)) {
    			int [] tl = new int[] {((Integer[])lo.get(1))[0], ((Integer[])lo.get(1))[1]};
    			if(getDistance(li, tl)<=minDistance)return s;
    			for(Integer [] edges: (Integer[][])lo.get(1)) {
    				if(getDistance(li, Arrays.stream(edges).mapToInt(Integer::intValue).toArray())<=minDistance)return s;
    			}
    		}
    	}
    	return null;
    }
    
    protected static void deleteNation(String nation) {
    	locations.remove(nation);
    	Core.broadcast(nation + ChatColor.RED + ChatColor.BOLD + "국가가 멸망했습니다!");
    }
    
    protected static void deleteRegion(Location loc) {
    	int [] iloc = new int[] {loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
    	for(String s : locations.keySet()) {
    		for(ArrayList<Object> lo : locations.get(s)) {
    			if(((int[])lo.get(0)).equals(iloc))locations.get(s).remove(lo);
    		}
    	}
    }
    protected static double getDistance(int [] p1, int [] p2) {
    	return Math.sqrt((p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1]));
    }
}