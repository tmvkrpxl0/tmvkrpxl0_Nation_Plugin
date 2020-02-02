package tmvkrpxl0;


import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class listener implements Listener {
	protected static HashMap<Player, Boolean> choose = new HashMap<Player, Boolean>();
	protected static HashMap<Player, Boolean> mining = new HashMap<Player, Boolean>();
	protected static HashMap<Player, Boolean> beacon = new HashMap<Player, Boolean>();

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
		String nation = TeamManager.getNation(event.getPlayer().getName());
		if(TerritoryManager.isInRegion(event.getBlock().getLocation())!=null) {
			if(nation == null) {
				event.getPlayer().sendMessage("이 블럭은 " + TerritoryManager.isInRegion(event.getBlock().getLocation()) + "국가의 소유입니다!");
				event.setCancelled(true);
			}else {
				if(!nation.equals(TerritoryManager.isInRegion(event.getBlock().getLocation()))) {
					String [] bs = BattleManager.warWithWho(nation);
					if(!(bs!=null && (bs[0].equals(nation)?bs[1]:bs[0]).equals(TerritoryManager.isInRegion(event.getBlock().getLocation())))) {
						event.getPlayer().sendMessage("이 블럭은 " + TerritoryManager.isInRegion(event.getBlock().getLocation()) + "국가의 소유입니다!");
						event.setCancelled(true);
					}else {
						if(event.getBlock().getType().equals(Material.IRON_DOOR)) {
							event.getPlayer().sendMessage("죄송합니다, 철문을 부술 아이템은 현제 구현되지 않았습니다.");
							event.setCancelled(true);
						}else if(event.getBlock().getType().equals(Material.BEACON)) {
							mining.put(event.getPlayer(), true);
							event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 30, 3), false);
						}else event.setCancelled(true);
					}
				}else if(event.getBlock().getType().equals(Material.BEACON)) {
					event.getPlayer().sendMessage("아군 신호기는 부술 수 없습니다!");
					event.setCancelled(true);
				}
			}
		}
	}

	
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		String nation = TeamManager.getNation(event.getPlayer().getName());
		if(TerritoryManager.isInRegion(event.getBlock().getLocation())!=null) {
			if(nation == null) {
				event.getPlayer().sendMessage("이 블럭은 " + TerritoryManager.isInRegion(event.getBlock().getLocation()) + "국가의 소유입니다!");
				event.setCancelled(true);
			}else{
				if(!nation.equals(TerritoryManager.isInRegion(event.getBlock().getLocation()))) {
					String [] bs = BattleManager.warWithWho(nation);
					if(!(bs!=null && (bs[0].equals(nation)?bs[1]:bs[0]).equals(TerritoryManager.isInRegion(event.getBlock().getLocation())))) {
						event.getPlayer().sendMessage("이 블럭은 " + TerritoryManager.isInRegion(event.getBlock().getLocation()) + "국가의 소유입니다!");
					}else {
						if(event.getBlock().getType().equals(Material.IRON_DOOR)) {
							event.getPlayer().sendMessage("죄송합니다, 철문을 부술 아이템은 현제 구현되지 않았습니다.");
							event.setCancelled(true);
						}
						if(event.getBlock().getType().equals(Material.BEACON)) {
							TerritoryManager.deleteRegion(event.getBlock().getLocation());
						}
					}
				}else if(event.getBlock().getType().equals(Material.BEACON)) {
					event.getPlayer().sendMessage("아군 신호기는 부술 수 없습니다!");
					event.setCancelled(true);
				}
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
				event.getPlayer().sendMessage("불가능합니다!");
			}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		if(choose.containsKey(event.getPlayer()) && choose.get(event.getPlayer())) {
			if(TeamManager.getTeamList().contains(event.getMessage())) {
				if(event.getPlayer().getInventory().contains(Core.declarepaper)) {
				BattleManager.declare(new String [] {TeamManager.getNation(event.getPlayer().getName()), event.getMessage()});
				event.getPlayer().getInventory().remove(Core.declarepaper);
				}else event.getPlayer().sendMessage("전쟁 선포권이 없습니다!");
			}
			else event.getPlayer().sendMessage(event.getMessage() + "라는 국가가 존재하지 않습니다!");
			choose.remove(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType().equals(Material.BEACON)) {
			if(TerritoryManager.getnationofBeacon(event.getClickedBlock().getLocation())!=null) {
			event.setCancelled(true);
			event.getPlayer().openInventory(TerritoryManager.openBeacon(event.getClickedBlock().getLocation()));
			beacon.put(event.getPlayer(), true);
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(TeamManager.getNation(event.getPlayer().getName())!=null) {
			if(event.getBlock().getType().equals(Material.BEACON)) {
				if(!event.getItemInHand().getItemMeta().hasDisplayName()) {
					event.getPlayer().sendMessage("도대체 이런일이 어떻게 벌어진건지 모르겠지만, 당신은 허용되지 않은 신호기를 들고 있습니다.");
					event.setCancelled(true);
				}
				else {
					if(TerritoryManager.getRegionNumber(TeamManager.getNation(event.getPlayer().getName()))>=5) {
						event.getPlayer().sendMessage("당신의 국가는 너무 많은 신호기를 가지고 있습니다!");
						event.setCancelled(true);
					}else {
				String nation = TerritoryManager.registerRegion(TeamManager.getNation(event.getPlayer().getName()), event.getBlock().getLocation(),
						event.getItemInHand().getItemMeta().getDisplayName());
				if(nation!=null) {
					if(TeamManager.getNation(event.getPlayer().getName()).equals(nation)) {
						event.getPlayer().sendMessage("당신의 다른 영지와 너무 가깝습니다!");
						event.setCancelled(true);
					}else {
					event.getPlayer().sendMessage("이 지역은 " + nation + "국가의 땅입니다!");
					event.setCancelled(true);
					}
				}
				}
			}
			}
		}else {
			event.getPlayer().sendMessage("국가에 있어야만 사용가능합니다!");
			event.setCancelled(true);
		}
		}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause().equals(DamageCause.ENTITY_EXPLOSION) || event.getCause().equals(DamageCause.BLOCK_EXPLOSION)) 
			event.setCancelled(true);
		}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event){
		if(beacon.containsKey(event.getWhoClicked())) {
			if(event.getRawSlot()<54) {
				if(event.getAction().name().contains("PICKUP")) {
						if(!event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.WHITE + "[" + ChatColor.YELLOW  + "이곳" + ChatColor.WHITE + "]")) {
					//위에꺼 그냥 [이곳] 이라는 이름의 아이템 눌렀는지 확인하는건데 색 들어가 있어서 헤헤..
					if(event.getCurrentItem().getType().equals(Material.BEACON)) {
						int [] loc = new int[3];
						String [] s = event.getCurrentItem().getItemMeta().getLore().get(0).split(", ");
						for(int i = 0;i<3;i++) {
							String ss =  s[i].replaceAll("[§abcdefghijklmnopqrstuvwxyz]", "").replace("[", "").replace("]", "");//아아아앙 몰라 그냥 알파벳 다없엘꺼야
							loc[i] = Integer.parseInt(ss);
						}
						event.setCancelled(true);
						event.getWhoClicked().teleport(new Location(Bukkit.getWorlds().get(0), loc[0], loc[1], loc[2]));
						event.getWhoClicked().closeInventory();
						beacon.remove(event.getWhoClicked());
					}
				}
			}
				event.setCancelled(true);
			}else if(event.getAction().name().equals("MOVE_TO_OTHER_INVENTORY"))event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if(beacon.containsKey(event.getWhoClicked())) {
			for(Integer slot : event.getRawSlots()) {
				if(slot<54)event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(beacon.containsKey(event.getPlayer()))beacon.remove(event.getPlayer());
	}
}
