package tmvkrpxl0.Config;

public class BattleInfo {
	private String Starter;
	private String Victim;
	private int ReadyTime;
	private int WarTime;
	public BattleInfo() {
		ReadyTime=0;
		WarTime=0;
	}
	
	public BattleInfo(String s, String v, int rt, int wt) {
		Starter = s;
		Victim = v;
		ReadyTime= rt;
		WarTime = wt;
	}
	
	public String getStarter() {
		return Starter;
	}
	
	public void setStarter(String s) {
		Starter = s;
	}
	
	public String getVictim() {
		return Victim;
	}
	
	public void setVictim(String v) {
		Victim = v;
	}
	public int getReadyTime() {
		return ReadyTime;
	}
	
	public void setReadyTime(int t) {
		ReadyTime=t;
	}
	
	public int getWarTime() {
		return WarTime;
	}
	
	public void setWarTime(int wt) {
		WarTime = wt;
	}
}
