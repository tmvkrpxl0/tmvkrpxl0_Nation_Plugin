package tmvkrpxl0;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

class TerritoryManager {
    private static LinkedHashMap<String, LinkedList<ArrayList<Object>>> locations; //국가별 성들의 신호기 위치, 모서리, 이름(이름은 **"무조건"** [역할]신호기 이름 처럼 지을것), 신호기 내구도 그리고 철문 내구도 저장
    //위치 역할 이름 내구도는 인덱스가 같으면 같은 신호기로 취급
    //[0] 은 위치, 반환값은 int[], 왼쪽[]은 신호기 위치. 밝기 변화를 위해 XYZ3개다 넣을것.
    //[1] 은 모서리, 반환값은 int[][], 왼쪽[]은  모서리 구별(0번부터 3번까지 Z순서로), 오른쪽[]은 XZ 좌표 저장
    //[2] 는 이름 & 역할, 반환값은 String
    //TODO:복구수단 알아내기
    protected static int minDistance;//TODO:Config로 설정하기
    protected static int minDistanceFriendly;//아군성끼리 거리 제한
    private FileConfiguration data;
    @SuppressWarnings("unchecked")
    protected TerritoryManager(){
    		data = YamlConfiguration.loadConfiguration(new File(Core.plugin.getDataFolder(), "영토.yml"));
    	locations = new LinkedHashMap<String, LinkedList<ArrayList<Object>>>();
    	minDistance = Core.plugin.getConfig().getInt("Territory.minDistance");
    	minDistanceFriendly = Core.plugin.getConfig().getInt("Territory.minDistanceFriendly");
    	if(data.getConfigurationSection("영토정보")!=null) {
    		Map<String, Object> t = data.getConfigurationSection("영토정보").getValues(false);
        	for(String s : t.keySet()) {
        		locations.put(s, new LinkedList<ArrayList<Object>>((ArrayList<ArrayList<Object>>)t.get(s)));
        	}
    	}
    }

	@SuppressWarnings("unchecked")
	protected static String registerRegion(String nation, Location yloc, String name){
		int [] loc = new int[] {yloc.getBlockX(), yloc.getBlockZ()};
		int [][] ita = getEdges(loc);
    	for(String s: locations.keySet()) {
    		for(ArrayList<Object> lo : locations.get(s)) {
				ArrayList<Integer> tai = new ArrayList<Integer>((ArrayList<Integer>) lo.get(0));
    			tai.remove(1);
    			int [] temp = Arrays.stream(tai.toArray(new Integer[] {})).mapToInt(Integer::intValue).toArray();
    			if(getDistance(temp, loc)<=(s.equals(nation)?minDistanceFriendly:minDistance))return s;//신호기들끼리 거리계산
    			for(ArrayList<Integer> edg: ((ArrayList<ArrayList<Integer>>)lo.get(1))) {
    				if(getDistance(Arrays.stream(edg.toArray(new Integer[] {})).mapToInt(Integer::intValue).toArray(), loc)<=(s.equals(nation)?minDistanceFriendly:minDistance))return s;//신호기와 모서리 거리 계산
    			}
    			for(int [] li : ita) {
    				if(getDistance(li, temp)<=(s.equals(nation)?minDistanceFriendly:minDistance))return s;//모서리와 신호기 거리계산
    				for(ArrayList<Integer> ned: ((ArrayList<ArrayList<Integer>>)lo.get(1))) {
    					if(getDistance(li, Arrays.stream(ned.toArray(new Integer[] {})).mapToInt(Integer::intValue).toArray())<=(s.equals(nation)?minDistanceFriendly:minDistance))return s;//모서리들끼리 거리계산
    				}
    			}
    		}
    	}
    	LinkedList<ArrayList<Object>> llo = locations.get(nation);
    	ArrayList<Object> lo = new ArrayList<Object>(3);
    	ArrayList<Integer> beaconloc = new ArrayList<>();
    	beaconloc.add(yloc.getBlockX());
    	beaconloc.add(yloc.getBlockY());
    	beaconloc.add(yloc.getBlockZ());
    	lo.add(beaconloc);
    	ArrayList<ArrayList<Integer>> Ita = new ArrayList<ArrayList<Integer>>();
    	for(int i=0;i<4;i++) {
    		ArrayList<Integer> temp = new ArrayList<Integer>();
    		for(int j=0;j<2;j++) {
    			temp.add(ita[i][j]);
    		}
    		Ita.add(temp);
    	}
    	lo.add(Ita);
		lo.add(name);
		llo.add(lo);
		locations.put(nation, llo);
        return null;
    }

    protected void save() {
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
    
	@SuppressWarnings("unchecked")
	protected static String isInRegion(Location loc){
    	int [] li = new int[] {loc.getBlockX(), loc.getBlockZ()};
    	for(String s : locations.keySet()) {
    		for(ArrayList<Object> lo : locations.get(s)) {
    			int [] tl = new int[] {((ArrayList<Integer>)lo.get(0)).get(0), ((ArrayList<Integer>)lo.get(0)).get(2)};
    			if(getDistance(li, tl)<=minDistance)return s;
    			for(ArrayList<Integer> edges: (ArrayList<ArrayList<Integer>>)lo.get(1)) {
    				if(getDistance(li, Arrays.stream(edges.toArray(new Integer[] {})).mapToInt(Integer::intValue).toArray())<=minDistance)return s;
    			}
    		}
    	}
    	return null;
    }
    
    protected static void deleteNation(String nation) {
    	locations.remove(nation);
    	Core.broadcast(nation + ChatColor.RED + ChatColor.BOLD + "국가가 멸망했습니다!");
    }
    
    @SuppressWarnings("unchecked")
	protected static void deleteRegion(Location loc){
    	ArrayList<Integer> temp = new ArrayList<Integer>();
    	temp.add(loc.getBlockX());
    	temp.add(loc.getBlockY());
    	temp.add(loc.getBlockZ());
    	for(String s : locations.keySet()) {
    		for(ArrayList<Object> lo : locations.get(s)) {
    			if(((ArrayList<Integer>)lo.get(0)).equals(temp)) {
    				LinkedList<ArrayList<Object>> ne = locations.get(s);
    				ne.remove(lo);
    				locations.put(s, ne);
    				String [] bs = BattleManager.warWithWho(s);
    				for(String p : TeamManager.getTeam(s)) {
    					Bukkit.getPlayerExact(p).sendMessage(ChatColor.RED + "성 " + ((String)lo.get(2)) + "이(가) 파괴되었습니다!");
    				}
    				for(String p : TeamManager.getTeam((bs[0].equals(s)?bs[1]:bs[0]))) {
    					Bukkit.getPlayerExact(p).sendMessage(ChatColor.GREEN + "상대 국가의 성이 파괴되었습니다!");
    				}
    				return;
    			}
    		}
    	}
    }
    protected static double getDistance(int [] p1, int [] p2) {
    	return Math.sqrt((p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1]));
    }

    protected static int getRegionNumber(String nation) {
    	return locations.get(nation).size();
    }
    
	@SuppressWarnings("unchecked")
	protected static Inventory openBeacon(Location loc) {
		ArrayList<Integer> iloc = new ArrayList<Integer>();
		iloc.add(loc.getBlockX());
		iloc.add(loc.getBlockY());
		iloc.add(loc.getBlockZ());
		int amount = 0;
		int idx = 0;
		String nation = null;
		String beaconname = "[오류] 관리자에게 문의하세요";
		out:for(String s : locations.keySet()) {
			idx = 0;
			for(ArrayList<Object> ao : locations.get(s)) {
				if(((ArrayList<Integer>)ao.get(0)).equals(iloc)) {
					beaconname = (String) ao.get(2);
					amount = locations.get(s).size();
					nation = s;
					break out;
				}
				idx++;
			}	
		}
		Inventory inventory = Bukkit.createInventory(null, 54, beaconname);
		Iterator<ArrayList<Object>> itr = locations.get(nation).iterator();
		{ItemStack stack = new ItemStack(Material.BEACON, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "[" + ChatColor.YELLOW  + "이곳" + ChatColor.WHITE + "]");
		LinkedList<String> lore = new LinkedList<String>();
		lore.add(beaconname);
		lore.add(ChatColor.YELLOW + iloc.toString());
		meta.setLore(lore);
		stack.setItemMeta(meta);
		inventory.setItem(4, stack);}
		int [] slots = new int[0];
		switch(amount-1) {
		case 1:slots = new int[] {31};
			break;
		case 2:slots = new int[] {30, 32};
			break;
		case 3:slots = new int[] {29, 31, 33};
			break;
		case 4:slots = new int[] {28, 30, 32, 34};
			break;
		}
		for(int slot : slots) {
			ArrayList<Object> ao = itr.next();
			if(locations.get(nation).get(idx).get(0).equals(ao.get(0)))ao = itr.next();
			ItemStack stack = new ItemStack(Material.BEACON, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + "[" + ChatColor.YELLOW  + ((String)ao.get(2)) + ChatColor.WHITE + "]");
			LinkedList<String> lore = new LinkedList<String>();
			lore.add(ChatColor.YELLOW + ao.get(0).toString());
			meta.setLore(lore);
			stack.setItemMeta(meta);
			inventory.setItem(slot, stack);
		}
		return inventory;
	}
	
	@SuppressWarnings("unchecked")
	protected static String getnationofBeacon(Location loc) {
		ArrayList<Integer> iloc = new ArrayList<Integer>();
		iloc.add(loc.getBlockX());
		iloc.add(loc.getBlockY());
		iloc.add(loc.getBlockZ());
		for(String s : locations.keySet()) {
			for(ArrayList<Object> ao : locations.get(s)) {
				if(((ArrayList<Integer>)ao.get(0)).equals(iloc))return s;
			}
		}
		return null;
	}
	
	protected static boolean addNation(String nation) {
		if(locations.containsKey(nation))return false;
		locations.put(nation, new LinkedList<ArrayList<Object>>());
		return true;
	}
}