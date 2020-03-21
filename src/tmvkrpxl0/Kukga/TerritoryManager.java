package tmvkrpxl0.Kukga;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import com.gmail.filoghost.holograms.database.HologramDatabase;
import com.gmail.filoghost.holograms.object.CraftHologram;
import org.bukkit.event.inventory.InventoryType;
import tmvkrpxl0.Config.TerritoryInfo;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
class TerritoryManager {
    private static FileConfiguration config;
	protected TerritoryManager(){
		ConfigurationSerialization.registerClass(TerritoryInfo.class, "TerritoryInfo");
		config = YamlConfiguration.loadConfiguration(new File(KukgaMain.plugin.getDataFolder() + File.separator + "영토.yml"));
		if(KukgaMain.hologram!=null) {
			KukgaMain.sender.sendMessage("Hologram Enabled");
			LinkedList<TerritoryInfo> templ = new LinkedList<>();
			for(String nations : config.getKeys(false)) {
				templ.addAll(getTerritories(nations));
			}
			for(Hologram hologram : HolographicDisplaysAPI.getHolograms(KukgaMain.plugin)) {
				KukgaMain.sender.sendMessage("Hologram Firstline: " + hologram.getLines()[0] + " Location: " + hologram.getLocation());
				Iterator<TerritoryInfo> itr = templ.iterator();
				TerritoryInfo info;
				while(itr.hasNext()) {
					info = itr.next();
					int [] beaconloc = info.getBeaconLocation();
					if(new Location(Bukkit.getWorlds().get(info.getDim()), beaconloc[0]+0.5, beaconloc[1]+1.5,
							beaconloc[2]+0.5)
							.equals(hologram.getLocation()))itr.remove();
				}
			}
			int [] beaconloc;
			for(TerritoryInfo info : templ) {
				KukgaMain.sender.sendMessage("info.toString:" + info.toString());
				beaconloc = info.getBeaconLocation();
				Hologram hologram = HolographicDisplaysAPI.createHologram(KukgaMain.hologram, new Location(Bukkit.getWorlds()
						.get(0), beaconloc[0]+0.5, beaconloc[1]+1.5, beaconloc[2]+0.5));
				hologram.addLine(info.getBeaconName());
				hologram.addLine(ChatColor.YELLOW + "[쉬프트]를 누르고 여시면 " + ChatColor.BLUE + "텔레포트" + ChatColor.YELLOW + "하실 수 있습니다");
				hologram.update();
				HologramDatabase.saveHologram((CraftHologram)hologram);
				HologramDatabase.trySaveToDisk();
			}
		}
    }

    protected static void deleteRegion(String nation, int x, int y, int z, int d){
		int[] iloc = new int[]{x,y,z};
		deleteRegion(nation, iloc, d);
	}

	protected static void deleteRegion(String nation, Location loc){
		deleteRegion(nation, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), Bukkit.getWorlds().indexOf(loc.getWorld()));
	}

	protected static void deleteTeam(String nation){
		if(KukgaMain.hologram!=null){
			int[] iloc;
			for(TerritoryInfo info : getTerritories(nation)){
				for(Hologram hologram : HolographicDisplaysAPI.getHolograms(KukgaMain.plugin)){
					iloc = info.getBeaconLocation();
					if(hologram.getLocation().equals(new Location(Bukkit.getWorlds().get(info.getDim()), iloc[0]+0.5, iloc[1]+1.5, iloc[2]+0.5))){
						HologramDatabase.deleteHologram((CraftHologram)hologram);
						hologram.delete();
						hologram.update();
						HologramDatabase.saveHologram((CraftHologram)hologram);
						HologramDatabase.trySaveToDisk();
					}
				}
			}
		}
		config.set(nation, null);
	}

	protected static void deleteRegion(String nation, int[] loc, int d){
		deleteRegion(nation, loc, d, false);
	}

	protected static void deleteRegion(String nation, int[] loc, int d, boolean msg){
		List<TerritoryInfo> territories = getTerritories(nation);
		Iterator<TerritoryInfo> itr = territories.iterator();
		while(itr.hasNext()){
			TerritoryInfo info = itr.next();
			if(Arrays.equals(info.getBeaconLocation(), loc) && info.getDim() == d){
				itr.remove();
				break;
			}
		}
		config.set(nation, territories);
		for(Hologram hologram : HolographicDisplaysAPI.getHolograms(KukgaMain.plugin)){
			if(hologram.getLocation().equals(new Location(Bukkit.getWorlds().get(d), loc[0]+0.5, loc[1]+1.5, loc[2]+0.5))){
				hologram.delete();
				HologramDatabase.deleteHologram((CraftHologram)hologram);
				hologram.update();
				HologramDatabase.saveHologram((CraftHologram)hologram);
				HologramDatabase.trySaveToDisk();
				break;
			}
		}
		if(config.getList(nation).size()==0 && msg)TeamManager.deleteTeam(nation, nation + "국이 멸망했습니다!");
	}

	protected static String registerRegion(String nation, Location yloc, String name, int dim){
		int [] placed = new int[] {yloc.getBlockX(), yloc.getBlockZ()};
		int [][] placededge = getEdges(placed);
		String r;
		r = isInRegion(nation, placed, dim);
		if(r!=null)return r;
		for(int [] i : placededge) {
			r = isInRegion(nation, i, dim);
			if(r != null)return r;
		}
    	TerritoryInfo newbeacon = new TerritoryInfo(name, new int[] {placed[0], yloc.getBlockY(), placed[1]}, placededge, dim);
		List<TerritoryInfo> list = getTerritories(nation);
		if(list==null)list = new LinkedList<>();
		list.add(newbeacon);
		config.set(nation, list);
    	if(KukgaMain.hologram!=null) {
    		Hologram hologram = HolographicDisplaysAPI.createHologram(KukgaMain.hologram, yloc.add(0.5, 1.5, 0.5));
    		hologram.addLine(name);
			hologram.addLine(ChatColor.YELLOW + "[쉬프트]를 누르고 여시면 " + ChatColor.BLUE + "텔레포트" + ChatColor.YELLOW + "하실 수 있습니다");
			hologram.update();
    		HologramDatabase.saveHologram((CraftHologram)hologram);
    		HologramDatabase.trySaveToDisk();
    	}
        return null;
    }

    protected void save() {
		File f = new File(KukgaMain.plugin.getDataFolder() + File.separator + "영토.yml");
		try {
			if(!f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    protected static int[][] getEdges(int[] centor){
    	return new int[][] {new int[] {centor[0]-24, centor[1]+25},new int[] {centor[0]+25, centor[1]+25},new int[] {centor[0]-24, centor[1]-24},
    		new int[] {centor[0]+25, centor[1]-24}};
    }
    
    protected static String isInRegion(String from, Location loc) {
    	return isInRegion(from, new int [] {loc.getBlockX(), loc.getBlockZ()}, Bukkit.getWorlds().indexOf(loc.getWorld()));
    }

    protected static String isInRegion(String from, int x, int z, int d) {
    	return isInRegion(from, new int[] {x,z}, d);
    }
    
	protected static String isInRegion(String from, int [] li, int d){
    	for(String s : config.getKeys(false)) {
    		for(TerritoryInfo lo : getTerritories(s)) {
    			if(d == lo.getDim()) {
					int[] tl = new int[]{lo.getBeaconLocation()[0], lo.getBeaconLocation()[2]};
					if (getDistance(li, tl, lo.getDim(), d) <= (s.equals(from) ? KukgaMain.config.getInt("territory.minDistanceFriendly") :
							KukgaMain.config.getInt("territory.minDistance"))) return s;
					for (int[] edges : lo.getEdges()) {
						if (getDistance(li, edges, lo.getDim(), d) <= (s.equals(from) ? KukgaMain.config.getInt("territory.minDistanceFriendly") :
								KukgaMain.config.getInt("territory.minDistance"))) return s;
					}
				}
    		}
    	}
    	return null;
    }

	
    protected static double getDistance(int [] p1, int [] p2, int d1, int d2) {
		if(d1==d2)return Math.sqrt((p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1]));
		else return 999999;
    }

    protected static int getRegionNumber(String nation) {
		return config.getList(nation)==null?0:config.getList(nation).size();
    }
    
	protected static Inventory openBeacon(Location loc) {
		String beaconname = "[오류]관리자에게 문의하세요";
		String nation = null;
		int [] iloc = new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
		int amount = 0;
		int idx = 0;
		TerritoryInfo info;
		out:for(String key : config.getKeys(false)){
			idx = 0;
			for(Object obj: config.getList(key)){
				info = (TerritoryInfo)obj;
				if(Arrays.equals(iloc, info.getBeaconLocation())){
					beaconname = info.getBeaconName();
					amount = getRegionNumber(key);
					nation = key;
					break out;
				}
				idx++;
			}
		}
		Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, beaconname);
		{
			ItemStack stack = new ItemStack(Material.BEACON, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + "[" + ChatColor.YELLOW  + "이곳" + ChatColor.WHITE + "]");
			LinkedList<String> lore = new LinkedList<>();
			lore.add(beaconname);
			lore.add(ChatColor.YELLOW + "[x:" + iloc[0] +" y:" + iloc[1] + " z:" + iloc[2] + "]");
			meta.setLore(lore);
			stack.setItemMeta(meta);
			inventory.setItem(4, stack);
		}
		int [] slots = new int[0];
		switch(amount-1) {
		case 1:slots = new int[] {13};
			break;
		case 2:slots = new int[] {12, 14};
			break;
		case 3:slots = new int[] {11, 13, 15};
			break;
		case 4:slots = new int[] {10, 12, 14, 16};
			break;
		}
		Iterator<TerritoryInfo> itr = (getTerritories(nation)).iterator();
		for(int slot : slots) {
			TerritoryInfo ao = itr.next();
			if(Arrays.equals((getTerritories(nation)).get(idx).getBeaconLocation(), ao.getBeaconLocation()))ao = itr.next();
			ItemStack stack = new ItemStack(Material.BEACON, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + "[" + ChatColor.YELLOW  + ao.getBeaconName() + ChatColor.WHITE + "]");
			LinkedList<String> lore = new LinkedList<>();
			lore.add(ChatColor.YELLOW + "[x:" + ao.getBeaconLocation()[0] +" y:" + ao.getBeaconLocation()[1] + " z:" + ao.getBeaconLocation()[2] + "]");
			meta.setLore(lore);
			stack.setItemMeta(meta);
			inventory.setItem(slot, stack);
		}
		return inventory;
	}

	protected static List<TerritoryInfo> getTerritories(String nation){
		return (List<TerritoryInfo>)config.getList(nation);
	}

	protected static String getBeaconOwner(Location loc){
		return getBeaconOwner(new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()});
	}

	protected static String getBeaconOwner(int[] loc){
		for(String s : config.getKeys(false)){
			for(TerritoryInfo info : getTerritories(s)){
				if(Arrays.equals(info.getBeaconLocation(), loc))return s;
			}
		}
		return null;
	}
}