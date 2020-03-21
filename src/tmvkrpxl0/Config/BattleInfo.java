package tmvkrpxl0.Config;

public class BattleInfo{
    private int ReadyTime;
    private int WarTime;
    private String Starter;
    private String Victim;
    public BattleInfo(int ReadyTime, int WarTime, String Starter, String Victim){
        this.ReadyTime = ReadyTime;
        this.WarTime = WarTime;
        this.Starter = Starter;
        this.Victim = Victim;
    }

    public boolean isReady(){
        return ReadyTime>0;
    }

    public void setReadyTime(int r){
        ReadyTime = r;
    }

    public void setWarTime(int w){
        WarTime = w;
    }

    public String getStarter(){
        return Starter;
    }

    public String getVictim(){
        return Victim;
    }

    public int getReadyTime(){
        return ReadyTime;
    }

    public int getWarTime(){
        return WarTime;
    }
}
