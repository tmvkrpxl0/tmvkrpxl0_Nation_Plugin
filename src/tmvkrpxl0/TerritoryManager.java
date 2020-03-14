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
    
	protected TerritoryManager(){
    	locations = ((TerritoryConfig)KukgaMain.loadFile("영토.yml", TerritoryConfig.class)).getLocations();
		if(KukgaMain.hologram!=null) {
			LinkedList<BeaconInfo> templ = new LinkedList<>();
			for(String s : locations.keySet()) {
				for(BeaconInfo l : locations.get(s)) {
					templ.add(l);
				}
			}
			for(com.gmail.filoghost.holograms.api.Hologram hologram : com.gmail.filoghost.holograms.api.HolographicDisplaysAPI.getHolograms(KukgaMain.hologram)) {
				Iterator<BeaconInfo> itr = templ.iterator();
				while(itr.hasNext()) {
					BeaconInfo info = itr.next();
					int [] beaconloc = info.getBeaconLocation();
					if(new Location(Bukkit.getWorlds().get(0), beaconloc[0]+0.5, beaconloc[1]+1.5, 
							beaconloc[2]+0.5)
							.equals(hologram.getLocation()))itr.remove();
				}
			}
			for(BeaconInfo info : templ) {
				int [] beaconloc = info.getBeaconLocation();
				com.gmail.filoghost.holograms.database.HologramDatabase.saveHologram((com.gmail.filoghost.holograms.object.CraftHologram)
						com.gmail.filoghost.holograms.api.HolographicDisplaysAPI.createHologram(KukgaMain.hologram, new Location(Bukkit.getWorlds()
								.get(0), beaconloc[0]+0.5, beaconloc[1]+1.5, beaconloc[2]+0.5), info.getBeaconName()));
				com.gmail.filoghost.holograms.database.HologramDatabase.trySaveToDisk();
			}
		}
    }
    
	protected static String registerRegion(String nation, Location yloc, String name){
		int [] placed = new int[] {yloc.getBlockX(), yloc.getBlockZ()};
		int [][] placededge = getEdges(placed);
		String r;
		r = isInRegion(nation, placed);
		if(r!=null)return r;
		for(int [] i : placededge) {
			r = isInRegion(nation, i);
			if(r != null)return r;
		}
    	BeaconInfo newbeacon = new BeaconInfo();
    	newbeacon.setBeaconLocation(new int[] {yloc.getBlockX(), yloc.getBlockY(), yloc.getBlockZ()});
    	newbeacon.setBeaconName(name);
    	newbeacon.setRegionEdges(getEdges(placed));
    	if(!locations.containsKey(nation))locations.put(nation, new LinkedList<BeaconInfo>());
    	locations.get(nation).add(newbeacon);
    	if(KukgaMain.hologram!=null) {
    		com.gmail.filoghost.holograms.database.HologramDatabase.saveHologram((com.gmail.filoghost.holograms.object.CraftHologram) 
    				com.gmail.filoghost.holograms.api.HolographicDisplaysAPI.createHologram(KukgaMain.hologram, yloc.add(0.5, 1.5, 0.5), name));
    		com.gmail.filoghost.holograms.database.HologramDatabase.trySaveToDisk();
    	}
        return null;
    }

    protected void save() {
    	KukgaMain.saveFile("영토.yml", TerritoryConfig.class, new TerritoryConfig(locations));
    }
    
    protected static int[][] getEdges(int[] centor){
    	return new int[][] {new int[] {centor[0]-24, centor[1]+25},new int[] {centor[0]+25, centor[1]+25},new int[] {centor[0]-24, centor[1]-24},
    		new int[] {centor[0]+25, centor[1]-24}};
    }
    
    protected static String isInRegion(String nation, Location loc) {
    	return isInRegion(nation, new int [] {loc.getBlockX(), loc.getBlockZ()});
    }
    
    protected static String isInRegion(String from, int x, int z) {
    	return isInRegion(from, new int[] {x,z});
    }
    
	protected static String isInRegion(String from, int [] li){
    	for(String s : locations.keySet()) {
    		for(BeaconInfo lo : locations.get(s)) {
    			int [] tl = new int[] {lo.getBeaconLocation()[0], lo.getBeaconLocation()[2]};
    			if(getDistance(li, tl)<=(s.equals(from)?KukgaMain.Config.getTerritory().get("minDistanceFriendly"):
    				KukgaMain.Config.getTerritory().get("minDistance")))return s;
    			for(int [] edges: lo.getRegionEdges()) {
    				if(getDistance(li, edges)<=(s.equals(from)?KukgaMain.Config.getTerritory().get("minDistanceFriendly"):
    						KukgaMain.Config.getTerritory().get("minDistance")))return s;
    			}
    		}
    	}
    	return null;
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
    				if(getRegionNumber(s)==0) {
    					TeamManager.deleteTeam(s);
    					locations.remove(s);
    			    	KukgaMain.broadcast(s + ChatColor.RED + ChatColor.BOLD + "국가가 멸망했습니다!");
    				}
    				return;
    			}
    		}
    	}
    	if(KukgaMain.hologram!=null) {
    		for(com.gmail.filoghost.holograms.api.Hologram h : com.gmail.filoghost.holograms.api.HolographicDisplaysAPI.getHolograms(KukgaMain.hologram)) {
    			KukgaMain.broadcast(h.getLocation().toString());
    			if(h.getLocation().equals(loc.add(0, 1, 0)))h.delete();
    		}
    	}
    }
	
	protected static void deleteNation(String nation) {
		locations.remove(nation);
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
	
	protected static String getnationofBeacon(int x, int y, int z) {
		int [] iloc = new int[] {x,y,z};
		for(String s : locations.keySet()) {
			for(BeaconInfo ao : locations.get(s)) {
				if(Arrays.equals(ao.getBeaconLocation(), iloc)) {
					return s;
				}
			}
		}
		return null;
	}
	
	protected static String getnationofBeacon(Location loc) {
		return getnationofBeacon(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
}