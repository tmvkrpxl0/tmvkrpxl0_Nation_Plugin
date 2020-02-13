package tmvkrpxl0.Config;

import java.util.LinkedList;
import java.util.List;

public class BattleStateConfig {
	private List<BattleInfo> BattleState;
	public BattleStateConfig(List<BattleInfo> m) {
		BattleState = m;
	}
	
	public List<BattleInfo> getBattleState(){
		return BattleState;
	}
	public void setBattleState(List<BattleInfo> m) {
		BattleState = m;
	}
	
	public BattleStateConfig() {
		BattleState = new LinkedList<BattleInfo>();
	}
}
