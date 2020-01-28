package tmvkrpxl0;

import java.util.LinkedHashMap;
import org.bukkit.Location;

class TerritoryManager {
    private static LinkedHashMap<String, int[][]> locations; //국가별 신호기 위치 저장
    //인덱스 순서는 Z 생겨먹은거 5번째 원소는 신호기 위치 나머지 4개는 모서리
    //5개 전부 0번은 x 1번은 y
    @SuppressWarnings("unchecked")
    protected TerritoryManager(){
        locations = (LinkedHashMap<String, int[][]>) Core.loadfile("plugins/Kukga/locations.yml");
        if(locations==null)locations = new LinkedHashMap<String, int[][]>();
    }

    protected static boolean registerRegion(String nation, Location loc){
    	if(isInRegion(loc)!=null || isInRegion(loc.add(-24, 0, 25))!=null || isInRegion(loc.add(25, 0, 25))!=null ||
    			isInRegion(loc.add(-24, 0, -24))!=null || isInRegion(loc.add(25, 0, -24))!=null)return false;
    	locations.put(nation, new int [][] {new int[]{loc.getBlockX()-24, loc.getBlockY()+25},new int[]{loc.getBlockX()+25, loc.getBlockY()+25},
    					new int[]{loc.getBlockX()-24, loc.getBlockY()-24},new int[]{loc.getBlockX()+24, loc.getBlockY()-24},
    						new int[]{loc.getBlockX(), loc.getBlockY()},});
        return true;
    }

    protected static void save() {
        Core.savefile("plugins/Kukga/locations.yml", locations);
    }

    protected static String isInRegion(Location loc){
    	for(String s : locations.keySet()) {
    		for(int [] i : locations.get(s)) {
    			//Math. sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    			if(Math.sqrt((i[0])*(loc.getX()) * (i[0])*(loc.getX()) + (i[1])*(loc.getY()) * (i[1])*(loc.getY())) <= 75) {
    				return s;
    			}
    		}
    	}
    	return null;
    }
    
    protected static void deleteRegion(String nation) {
    	locations.remove(nation);
    }
}