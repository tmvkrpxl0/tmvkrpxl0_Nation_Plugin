package tmvkrpxl0;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import tmvkrpxl0.Config.BeaconInfo;
import tmvkrpxl0.Config.TerritoryConfig;

class TerritoryManager {
    private static Map<String, List<BeaconInfo>> locations;
    protected static int minDistance;
    protected static int minDistanceFriendly;

    
	protected TerritoryManager(){
    	locations = ((TerritoryConfig)Core.loadFile("영토.yml", TerritoryConfig.class)).getLocations();
		minDistance = Core.Config.getTerritory().get("minDistance");
		minDistanceFriendly = Core.Config.getTerritory().get("minDistanceFriendly");
    }
    
	protected static String registerRegion(String nation, Location yloc, String name){
		int [] placed = new int[] {yloc.getBlockX(), yloc.getBlockZ()};
		int [][] placededge = getEdges(placed);
		String r;
		r = isInRegion(placed);
		if(r!=null)return r;
		for(int [] i : placededge) {
			r = isInRegion(i);
			if(r != null)return r;
		}
    	BeaconInfo newbeacon = new BeaconInfo();
    	newbeacon.setBeaconLocation(new int[] {yloc.getBlockX(), yloc.getBlockY(), yloc.getBlockZ()});
    	newbeacon.setBeaconName(name);
    	newbeacon.setRegionEdges(getEdges(placed));
    	if(!locations.containsKey(nation))locations.put(nation, new LinkedList<BeaconInfo>());
    	locations.get(nation).add(newbeacon);
        return null;
    }

    protected void save() {
    	Core.saveFile("영토.yml", TerritoryConfig.class, new TerritoryConfig(locations));
    }
    
    protected static int[][] getEdges(int[] centor){
    	return new int[][] {new int[] {centor[0]-24, centor[1]+25},new int[] {centor[0]+25, centor[1]+25},new int[] {centor[0]-24, centor[1]-24},
    		new int[] {centor[0]+25, centor[1]-24}};
    }
    
    protected static String isInRegion(Location loc) {
    	return isInRegion(new int [] {loc.getBlockX(), loc.getBlockZ()});
    }
    
	protected static String isInRegion(int [] li){
    	for(String s : locations.keySet()) {
    		for(BeaconInfo lo : locations.get(s)) {
    			int [] tl = new int[] {lo.getBeaconLocation()[0], lo.getBeaconLocation()[2]};
    			if(getDistance(li, tl)<=minDistance)return s;
    			for(int [] edges: lo.getRegionEdges()) {
    				if(getDistance(li, edges)<=minDistance)return s;
    			}
    		}
    	}
    	return null;
    }
    
    protected static void deleteNation(String nation) {
    	locations.remove(nation);
    	Core.broadcast(nation + ChatColor.RED + ChatColor.BOLD + "국가가 멸망했습니다!");
    }
    
    
	protected static void deleteRegion(Location loc){
    	int [] temp = new int[] {loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
    	for(String s : locations.keySet()) {
    		for(BeaconInfo lo : locations.get(s)) {
    			if(Arrays.equals(lo.getBeaconLocation(), temp)) {
    				locations.get(s).remove(lo);
    				for(String p : TeamManager.getTeam(s)) {
    					Bukkit.getPlayerExact(p).sendMessage(ChatColor.RED + "성 " + lo.getBeaconName() + "이(가) 파괴되었습니다!");
    				}
    				for(String p : TeamManager.getTeam(BattleManager.getOpponent(s))) {
    					Bukkit.getPlayerExact(p).sendMessage(ChatColor.GREEN + "상대 국가의 성이 파괴되었습니다!");
    				}
    				if(getRegionNumber(s)==0)TeamManager.deleteTeam(s);
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
    
	protected static void addNation(String nation) {
		locations.put(nation, new LinkedList<BeaconInfo>());
	}
    
	protected static Inventory openBeacon(Location loc) {
		int [] iloc = new int[] {loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
		int amount = 0;
		int idx = 0;
		String nation = null;
		String beaconname = "[오류] 관리자에게 문의하세요";
		
		out:for(String s : locations.keySet()) {
			idx = 0;
			for(BeaconInfo ao : locations.get(s)) {
				if(Arrays.equals(ao.getBeaconLocation(),iloc)) {
					beaconname = ao.getBeaconName();
					amount = locations.get(s).size();
					nation = s;
					break out;
				}
				idx++;
			}	
		}
		Inventory inventory = Bukkit.createInventory(null, 54, beaconname);
		Iterator<BeaconInfo> itr = locations.get(nation).iterator();
		{ItemStack stack = new ItemStack(Material.BEACON, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "[" + ChatColor.YELLOW  + "이곳" + ChatColor.WHITE + "]");
		LinkedList<String> lore = new LinkedList<String>();
		lore.add(beaconname);
		lore.add(ChatColor.YELLOW + "[x:" + iloc[0] +" y:" + iloc[1] + " z:" + iloc[2] + "]");
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
			BeaconInfo ao = itr.next();
			if(Arrays.equals(locations.get(nation).get(idx).getBeaconLocation(), (ao.getBeaconLocation())))ao = itr.next();
			ItemStack stack = new ItemStack(Material.BEACON, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + "[" + ChatColor.YELLOW  + ao.getBeaconName() + ChatColor.WHITE + "]");
			LinkedList<String> lore = new LinkedList<String>();
			lore.add(ChatColor.YELLOW + "[x:" + ao.getBeaconLocation()[0] +" y:" + ao.getBeaconLocation()[1] + " z:" + ao.getBeaconLocation()[2] + "]");
			meta.setLore(lore);
			stack.setItemMeta(meta);
			inventory.setItem(slot, stack);
		}
		return inventory;
	}
	
	
	protected static String getnationofBeacon(Location loc) {
		int [] iloc = new int[] {loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
		for(String s : locations.keySet()) {
			for(BeaconInfo ao : locations.get(s)) {
				if(Arrays.equals(ao.getBeaconLocation(), iloc)) {
					return s;
				}
			}
		}
		return null;
	}
}