package tmvkrpxl0;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class listener implements Listener{
	@EventHandler
	public void onSave(WorldSaveEvent event) {
		Bukkit.broadcastMessage("국가 플러그인의 팀 정보를 저장합니다..");
		Core.teammanager.save();
	}
}
