package tmvkrpxl0.Config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;


@SerializableAs("TerritoryInfo")
public class TerritoryInfo implements ConfigurationSerializable {
    private final int [] BeaconLocation;
    private final int [][] Edges;
    private final String BeaconName;
    private final int dim;

    public TerritoryInfo(String BeaconName, int[] BeaconLocation, int[][] Edges, int dim){
        this.BeaconLocation = BeaconLocation;
        this.Edges = Edges;
        this.BeaconName = BeaconName;
        this.dim = dim;
    }

    @Override
    public Map<String, Object> serialize(){
        Map<String, Object> map = new LinkedHashMap<>();
        List<Integer> loc = new LinkedList<>();
        for(int i : BeaconLocation){
            loc.add(i);
        }
        List<List<Integer>> edges = new LinkedList<>();
        List<Integer> temp;
        for(int [] e : Edges){
            temp = new LinkedList<>();
            for(int i : e){
                temp.add(i);
            }
            edges.add(temp);
        }
        map.put("beaconName", BeaconName);
        map.put("beaconLocation", loc);
        map.put("regionEdges", edges);
        map.put("dimension", dim);
        return map;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public static TerritoryInfo deserialize(Map<String, Object> map){
        List<Integer> loc = (List<Integer>) map.get("beaconLocation");
        int[] beaconloc = new int[]{loc.get(0), loc.get(1), loc.get(2)};
        List<List<Integer>> edge = (List<List<Integer>>) map.get("regionEdges");
        int[][] e = new int[4][2];
        for(int i = 0;i<4;i++){
            for(int j=0;j<2;j++){
                e[i][j] = edge.get(i).get(j);
            }
        }
        return new TerritoryInfo((String)map.get("beaconName"), beaconloc, e, (int)map.get("dimension"));
    }

    public int [] getBeaconLocation(){
        return BeaconLocation;
    }

    public int[][] getEdges(){
        return Edges;
    }

    public String getBeaconName(){
        return BeaconName;
    }

    public int getDim(){
        return dim;
    }

    @Override
    public String toString(){
        return "Location: " + Arrays.toString(BeaconLocation) + " Name: " + BeaconName + " Edges: " + Arrays.deepToString(Edges);
    }
}
