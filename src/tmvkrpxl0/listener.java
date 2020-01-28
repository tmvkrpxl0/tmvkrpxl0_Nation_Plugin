package tmvkrpxl0;


import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.WorldSaveEvent;

import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.PacketPlayOutBlockBreakAnimation;

public class listener implements Listener {
	@EventHandler
	public void onSave(WorldSaveEvent event) {
		TeamManager.save();
		BattleManager.save();
		TerritoryManager.save();
		Core.broadcast("국가 정보들이 저장되었습니다!");
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		event.getPlayer().setScoreboard(Core.sb);
		event.getPlayer().setHealth(((Damageable)event.getPlayer()).getHealth());
	}
	
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		if(event.getBlock().getType().equals(Material.DIRT))event.setCancelled(true);
		net.minecraft.server.v1_7_R4.Block t = ((CraftWorld)event.getPlayer().getWorld()).getHandle().getType(
				event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
		Core.broadcast("" + t.getDamage((EntityHuman)((CraftPlayer)event.getPlayer()).getHandle(), ((CraftWorld)event.getPlayer().getWorld()).getHandle(), 
				event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ()));
		if (event.getBlock().getType().equals(Material.IRON_DOOR)) {
			event.setCancelled(true);
		}
		if (event.getBlock().getType().equals(Material.BEACON)) {
			event.setCancelled(true);
		}
		PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(new Random().nextInt(), event.getBlock().getX(), event.getBlock().getY(),
				event.getBlock().getZ(), 6);
	((CraftServer)Bukkit.getServer()).getHandle().sendPacketNearby(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(),
				64, 0, packet);
		String nation = TerritoryManager.isInRegion(event.getBlock().getLocation());
		if (nation != null && TeamManager.getNation(event.getPlayer().getName()) != nation) {
			String opponent = BattleManager.warWithWho(TeamManager.getNation(event.getPlayer().getName()));
			if (opponent != nation) {
				event.getPlayer().sendMessage(Core.prefix + ChatColor.RED + "이 블럭은 " + nation + " 국가의 것입니다!");
				event.setCancelled(true);
			}
			else if (event.getBlock().getType().equals(Material.BEACON)) {
					event.setCancelled(true);
					//CustomBeacon.BlockDamage(event);
				}
			else if (event.getBlock().getType().equals(Material.IRON_DOOR)) {
				event.setCancelled(true);
				//CustomIronDoor.BlockDamage(event);
			}
		}		
	}

	
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType().equals(Material.BEACON)) {
			event.setCancelled(true);
		}
		if (event.getBlock().getType().equals(Material.IRON_DOOR)) {
			event.setCancelled(true);
		}
		String nation = TerritoryManager.isInRegion(event.getBlock().getLocation());
		if (nation != null && TeamManager.getNation(event.getPlayer().getName()) != nation) {
			String opponent = BattleManager.warWithWho(TeamManager.getNation(event.getPlayer().getName()));
			if (opponent != nation) {
				event.getPlayer().sendMessage(Core.prefix + ChatColor.RED + ChatColor.BOLD + "너네꺼 아니라고");
				event.setCancelled(true);
				}
			else if (event.getBlock().getType().equals(Material.BEACON)) {
						event.setCancelled(true);
						//CustomBeacon.BlockBreak(event);
					}
			else if (event.getBlock().getType().equals(Material.IRON_DOOR)) {
				event.setCancelled(true);
				//CustomIronDoor.BlockBreak(event);
			}
					
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
