package tmvkrpxl0;


import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class listener implements Listener {
	protected static HashMap<Player, Boolean> choose = new HashMap<Player, Boolean>();
	protected static HashMap<Player, Boolean> mining = new HashMap<Player, Boolean>();

	@EventHandler
	public void onSave(WorldSaveEvent event) {
		Core.save();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		event.getPlayer().setScoreboard(Core.sb);
		event.getPlayer().setHealth(((Damageable)event.getPlayer()).getHealth());
		Core.injector.addPlayer(event.getPlayer());
	}
	
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		String nation = TerritoryManager.isInRegion(event.getBlock().getLocation());
		if (nation != null && TeamManager.getNation(event.getPlayer().getName()) != nation) {
			String [] o = BattleManager.warWithWho(TeamManager.getNation(event.getPlayer().getName()));
			String opponent = o[0].equals(TeamManager.getNation(event.getPlayer().getName()))?o[1]:o[0];
			if (opponent != nation) {
				event.getPlayer().sendMessage(Core.prefix + ChatColor.RED + "이 블럭은 " + nation + " 국가의 것입니다!");
				event.setCancelled(true);
			}
			else { 
				if (event.getBlock().getType().equals(Material.BEACON)) {
						event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 30, 4), false);
						mining.put(event.getPlayer(), true);
					}
				else event.setCancelled(true);
			}
		}		
	}

	
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		String nation = TerritoryManager.isInRegion(event.getBlock().getLocation());
		if (nation != null && TeamManager.getNation(event.getPlayer().getName()) !=null && TeamManager.getNation(event.getPlayer().getName()) != nation) {
			String [] o = BattleManager.warWithWho(TeamManager.getNation(event.getPlayer().getName()));
			String opponent = o[0].equals(TeamManager.getNation(event.getPlayer().getName()))?o[1]:o[0];
			if (opponent != nation) {
				event.getPlayer().sendMessage(Core.prefix + ChatColor.RED + ChatColor.BOLD + "너네꺼 아니라고");
				event.setCancelled(true);
				}
			else if (event.getBlock().getType().equals(Material.BEACON)) {
						
						
					}
			else if (event.getBlock().getType().equals(Material.IRON_DOOR)) {
				
				
			}
					
		}
	}

	@EventHandler
	public void onExplosion(EntityExplodeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent event) {
		if (TerritoryManager.isInRegion(event.getRetractLocation()) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPistonPush(BlockPistonExtendEvent event) {
		for(Block b : event.getBlocks()){
			if(TerritoryManager.isInRegion(b.getLocation())!=null){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onProjectileHit(PlayerTeleportEvent event){
		if(event.getCause() == TeleportCause.ENDER_PEARL && 
			TerritoryManager.isInRegion(event.getTo())!=null &&
			TeamManager.getNation(event.getPlayer().getName())!=TerritoryManager.isInRegion(event.getTo())){
				event.setCancelled(false);
				Core.broadcast(ChatColor.RED + "불가능합니다!");
			}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if(choose.containsKey(event.getPlayer()) && choose.get(event.getPlayer())) {
			if(TeamManager.getTeamList().contains(event.getMessage())) {
				BattleManager.declare(new String [] {TeamManager.getNation(event.getPlayer().getName()), event.getMessage()});
				choose.remove(event.getPlayer());
			}
		}
	}
}
