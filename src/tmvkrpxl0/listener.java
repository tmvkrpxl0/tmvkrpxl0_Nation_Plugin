package tmvkrpxl0;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class listener implements Listener {
	protected static HashMap<Player, Boolean> choose = new HashMap<Player, Boolean>();
	protected static HashMap<Player, Boolean> beacon = new HashMap<Player, Boolean>();
	protected static HashMap<Player, Thread> tasks = new HashMap<>();
	protected static HashMap<Material, Double> blocks = new HashMap<>();
	
	protected listener(Core plugin) {
		new BukkitRunnable() {
			public void run() {
				for(Player p : listener.tasks.keySet()) {
					if(!listener.tasks.get(p).isAlive())listener.tasks.remove(p);
				}
			}
		}.runTaskTimer(plugin, 0, 20);
	}
	
	@EventHandler
	public void onSave(WorldSaveEvent event) {
		Core.save();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		if(Core.patch && event.getPlayer().hasPermission("minecraft.command.op"))event.getPlayer().sendMessage(ChatColor.RED + "국가 플러그인을 사용할 수 없습니다. 사용하시려면 [/국가 설정 패치]를 사용하세요");
		event.getPlayer().setScoreboard(Core.sb);
		event.getPlayer().setHealth(((Damageable)event.getPlayer()).getHealth());
		Core.injector.addPlayer(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if(tasks.containsKey(event.getPlayer())) {
			if(tasks.get(event.getPlayer()).isAlive())tasks.get(event.getPlayer()).interrupt();
			tasks.remove(event.getPlayer());
		}
		Core.injector.removePlayer(event.getPlayer());
	}
	
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		String nation = TeamManager.getNation(event.getPlayer().getName());
		if(TerritoryManager.isInRegion(event.getBlock().getLocation())!=null) {
			Player p = event.getPlayer();
			if(nation == null) {
				p.sendMessage("이 블럭은 " + TerritoryManager.isInRegion(event.getBlock().getLocation()) + "국가의 소유입니다!");
				if(tasks.containsKey(p)) {
					tasks.get(p).interrupt();
					tasks.remove(p);
				}
				event.setCancelled(true);
			}else {
				if(!nation.equals(TerritoryManager.isInRegion(event.getBlock().getLocation()))) {
					if(!(BattleManager.warWithWho(nation)!=null && (BattleManager.getOpponent(nation)).equals(TerritoryManager.isInRegion(event.getBlock().getLocation())))) {
						p.sendMessage("이 블럭은 " + TerritoryManager.isInRegion(event.getBlock().getLocation()) + "국가의 소유입니다!");
						event.setCancelled(true);
						if(tasks.containsKey(p)) {
							tasks.get(p).interrupt();
							tasks.remove(p);
						}
					}
				}else if(event.getBlock().getType().equals(Material.BEACON)) {
					p.sendMessage("아군 신호기는 부술 수 없습니다!");
					if(tasks.containsKey(p)) {
						tasks.get(p).interrupt();
						tasks.remove(p);
					}
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
					if(!(BattleManager.warWithWho(nation)!=null && BattleManager.getOpponent(nation)
							.equals(TerritoryManager.isInRegion(event.getBlock().getLocation())))) {
						event.getPlayer().sendMessage("이 블럭은 " + TerritoryManager.isInRegion(event.getBlock().getLocation()) + "국가의 소유입니다!");
					}else {
						if(event.getBlock().getType().equals(Material.IRON_DOOR)) {
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
			if(event.getMessage().equals(TeamManager.getNation(event.getPlayer().getName()))) {
				event.getPlayer().sendMessage("당신의 국가에 전쟁을 선포할 수 없습니다!");
			}
			else if(TeamManager.getTeamList().contains(event.getMessage())) {
				if(event.getPlayer().getInventory().contains(Core.declarepaper)) {
				BattleManager.declare(TeamManager.getNation(event.getPlayer().getName()), event.getMessage());
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
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if(event.getClickedBlock().getType().equals(Material.BEACON)) {
				if(TerritoryManager.getnationofBeacon(event.getClickedBlock().getLocation())!=null &&
					TerritoryManager.getnationofBeacon(event.getClickedBlock().getLocation()).equals(TeamManager.getNation(event.getPlayer().getName()))) {
					event.setCancelled(true);
					event.getPlayer().openInventory(TerritoryManager.openBeacon(event.getClickedBlock().getLocation()));
					beacon.put(event.getPlayer(), true);
				}
			}
		}
		if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if(event.getPlayer().getItemInHand().isSimilar(Core.defendpaper))event.getPlayer().performCommand("국가 전쟁방어");
			if(event.getPlayer().getItemInHand().isSimilar(Core.declarepaper))event.getPlayer().performCommand("국가 전쟁선포");
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getBlock().getType().equals(Material.BEACON)) {
			if(TeamManager.getNation(event.getPlayer().getName())!=null) {
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
				}else {
					event.getPlayer().sendMessage("국가에 있어야만 사용가능합니다!");
					event.setCancelled(true);
				}
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
						String [] s = event.getCurrentItem().getItemMeta().getLore().get(0).split(":");
						String[] ss = new String[]{s[1], s[2], s[3]};
						for(int i = 0;i<3;i++) {
							String d =  ss[i].replaceAll("[§abcdefghijklmnopqrstuvwxyz ]", "").replace("[", "").replace("]", "");//아아아앙 몰라 그냥 알파벳 다없엘꺼야
							loc[i] = Integer.parseInt(d);
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
	
	protected static void changeHardness(Material material, double hardness) {
		blocks.put(material, hardness);
		try {
			Field st = Reflection.getField(Reflection.getClass("{nms}.Block"), "strength");
			st.setAccessible(true);
			Method magic = Reflection.getClass("{cb}.util.CraftMagicNumbers").getMethod("getBlock", Material.class);
			magic.setAccessible(true);
			st.set(magic.invoke(null, material), 2000F);
		} catch (Exception e) {
			e.printStackTrace();
			Core.sender.sendMessage("블럭의 강도를 지정하는데에 문제가 생겼습니다!");
			Core.sender.sendMessage("매터리얼: " + material.name());
		}
	}
	
	protected static void disable() {
		for(Player p : tasks.keySet()) {
			tasks.get(p).interrupt();
			tasks.remove(p);
		}
		for(Player p : choose.keySet()) {
			p.sendMessage("서버가 재시작 되는 중이니, 선택을 취소시킵니다.");
			choose.remove(p);
		}
		for(Player p : beacon.keySet()) {
			p.closeInventory();
		}
	}
}
