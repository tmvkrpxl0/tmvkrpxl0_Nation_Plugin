package tmvkrpxl0;

import java.util.LinkedHashMap;
import org.bukkit.Location;
import org.bukkit.World;

class TerritoryManager {
    private static LinkedHashMap<String, Location> beacons; //국가별 신호기 위치 저장
    private static final World overworld = Core.plugin.getServer().getWorlds().get(0);
    private static final int minDistance = 120;
    @SuppressWarnings("unchecked")
    protected TerritoryManager(){
        LinkedHashMap<String, double[]> temp = (LinkedHashMap<String, double[]>) Core.loadfile("plugins/Kukga/beacons.yml");
        if(temp==null){
            beacons = new LinkedHashMap<String, Location>();
        }else{
            double [] l;
            for(String s : temp.keySet()){
                l = temp.get(s);
                beacons.put(s, new Location(overworld, l[0], l[1], l[2]));
            }
        }
    }

    protected static boolean registerRegion(String nation, Location loc){
        for(String s : beacons.keySet()){
            if(loc.distance(beacons.get(s)) <= minDistance)return false;
        }
        beacons.put(nation, loc);
        return true;
    }

    protected static void save() {
        LinkedHashMap<String, double[]> temp = new LinkedHashMap<String, double[]>();
        Location t;
        for(String s : beacons.keySet()){
            t = beacons.get(s);
            temp.put(s, new double[]{t.getX(), t.getY(), t.getZ()});
        }
        Core.savefile("plugins/Kukga/beacons.yml", temp);
    }

    protected static String isInRegion(Location loc){
        Location t;
        for(String s : beacons.keySet()){
            t = beacons.get(s);
            if(t.getX()-24 < loc.getX() && loc.getX() < t.getX()+25 &&
                t.getZ()-24 < loc.getZ() && loc.getZ() < t.getZ()+25)return s;
        }
        return null;
    }
}