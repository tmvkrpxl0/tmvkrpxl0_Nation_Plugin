package tmvkrpxl0;

import java.lang.reflect.Field;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class listener implements Listener {
	@EventHandler
	public void onSave(org.bukkit.event.world.WorldSaveEvent event) {
		TeamManager.save();
		BattleManager.save();
		TerritoryManager.save();
	}

	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		String nation = TerritoryManager.isInRegion(event.getBlock().getLocation());
		if (nation != null && TeamManager.getNation(event.getPlayer().getName()) != nation) {
			String opponent = BattleManager.warWithWho(TeamManager.getNation(event.getPlayer().getName()));
			if (opponent != nation)
				event.getPlayer().sendMessage(Core.prefix + ChatColor.RED + "이 블럭은 " + nation + " 국가의 것입니다!");
			else if (event.getBlock().getType().equals(Material.BEACON)
					|| event.getBlock().getType().equals(Material.IRON_DOOR))
				return;
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		try {
			Field blockfield = net.minecraft.server.v1_7_R4.Block.class.getDeclaredField("strength");
			blockfield.setAccessible(true);
			blockfield.setFloat(net.minecraft.server.v1_7_R4.Block.REGISTRY.get("iron_door"), 30.0F);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String nation = TerritoryManager.isInRegion(event.getBlock().getLocation());
		if (nation != null && TeamManager.getNation(event.getPlayer().getName()) != nation) {
			String opponent = BattleManager.warWithWho(TeamManager.getNation(event.getPlayer().getName()));
			if (opponent != nation)
				event.getPlayer().sendMessage(Core.prefix + ChatColor.RED + ChatColor.BOLD + "너네꺼 아니라고");
			else if (event.getBlock().getType().equals(Material.BEACON)
					|| event.getBlock().getType().equals(Material.IRON_DOOR)) {
				
					}
					event.setCancelled(true);
		}
	}

	@EventHandler
	public void onExplosion(EntityExplodeEvent event) {
		for (Block b : event.blockList()) {
			if (TerritoryManager.isInRegion(b.getLocation()) != null)
			event.blockList().remove(b);
		}
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
}
