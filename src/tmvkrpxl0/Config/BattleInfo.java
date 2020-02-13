package tmvkrpxl0.Config;

public class BattleInfo {
	private String Starter;
	private String Victim;
	private int Time;
	private boolean ready;
	public BattleInfo() {
		Time=0;
		ready=true;
	}
	
	public BattleInfo(String s, String v, int t, boolean r) {
		Starter = s;
		Victim = v;
		Time=t;
		ready = r;
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
	public int getTime() {
		return Time;
	}
	
	public void setTime(int t) {
		Time=t;
	}
	
	public void setReady(boolean r) {
		ready = r;
	}
	
	public boolean getReady() {
		return ready;
	}
}
