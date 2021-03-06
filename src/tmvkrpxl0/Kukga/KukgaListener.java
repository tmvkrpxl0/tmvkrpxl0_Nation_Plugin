package tmvkrpxl0.Kukga;


import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import tmvkrpxl0.Config.BattleInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings({"deprecation", "unused"})
public class KukgaListener implements Listener {
	protected static HashMap<Player, Boolean> choose = new HashMap<>();
	protected static HashMap<Player, Boolean> beacon = new HashMap<>();
	protected static HashMap<Player, Thread> tasks = new HashMap<>();
	protected static HashMap<Material, Double> blocks = new HashMap<>();
	
	protected KukgaListener(KukgaMain plugin) {
		new BukkitRunnable() {
			public void run() {
				for(Player p : KukgaListener.tasks.keySet()) {
					if(!KukgaListener.tasks.get(p).isAlive())KukgaListener.tasks.remove(p);
				}
			}
		}.runTaskTimer(plugin, 0, 20);
	}
	
	@EventHandler
	public void onSave(WorldSaveEvent event) {
		KukgaMain.save();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player p = event.getPlayer();
		p.setHealth(p.getHealth());
		KukgaMain.injector.addPlayer(p);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		if(tasks.containsKey(p)) {
			if(tasks.get(p).isAlive())tasks.get(p).interrupt();
			tasks.remove(p);
		}
		KukgaMain.injector.removePlayer(p);
	}
	
	@EventHandler
	public void onBlockChange(EntityChangeBlockEvent event) {
		if(blocks.containsKey(event.getBlock().getType())) {
			if(!event.getEntityType().equals(EntityType.PLAYER)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		String from = TeamManager.getNation(event.getPlayer());
		String owner = TerritoryManager.isInRegion(null, event.getBlock().getLocation());
		if(owner!=null) {
			final Player p = event.getPlayer();
			if(!owner.equals(from)) {
				BattleInfo info = BattleManager.warWithWho(from);
				if(blocks.containsKey(event.getBlock().getType())) {
					if(info==null || info.getReadyTime()!= 0 || !(info.getStarter().equals(owner) || info.getVictim().equals(owner))) {
						p.sendMessage("이 블럭은 " + owner + " 국가의 소유입니다!");
						event.setCancelled(true);
						if(tasks.containsKey(p)) {
							new BukkitRunnable() {
								 public void run() {
									 p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
									 p.removePotionEffect(PotionEffectType.FAST_DIGGING);
								 }
							 }.runTask(KukgaMain.plugin);
							tasks.get(p).interrupt();
							tasks.remove(p);
						}
					}
				}else {
					if(info==null || info.getReadyTime()!=0 || !(info.getStarter().equals(owner) || info.getVictim().equals(owner)))
						p.sendMessage("이 블럭은 " + owner + " 국가의 소유입니다!");
					event.setCancelled(true);
				}
			}else if(event.getBlock().getType().equals(Material.BEACON) && owner.equals(TerritoryManager.getBeaconOwner(event.getBlock().getLocation()))){
				p.sendMessage("아군 신호기는 부술 수 없습니다!");
				if(tasks.containsKey(p)) {
					new BukkitRunnable() {
						 public void run() {
							 p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
							 p.removePotionEffect(PotionEffectType.FAST_DIGGING);
						 }
					 }.runTask(KukgaMain.plugin);
					tasks.get(p).interrupt();
					tasks.remove(p);
				}
				event.setCancelled(true);
			}
		}
	}

	
	
	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event){
		String owner = TerritoryManager.isInRegion(null, event.getBlock().getLocation());
		if(owner!=null) {
			final Player p = event.getPlayer();
			if(!owner.equals(TeamManager.getNation(p))) {
				BattleInfo info = BattleManager.warWithWho(TeamManager.getNation(event.getPlayer()));
				if(info==null || !(info.getStarter().equals(owner) || info.getVictim().equals(owner)) || info.getReadyTime()>0)
					p.sendMessage("이 블럭은 " + owner + " 국가의 소유입니다!");
				event.setCancelled(true);
			}else if(event.getBlock().getType().equals(Material.BEACON) && owner.equals(TerritoryManager.getBeaconOwner(event.getBlock().getLocation()))) {
				event.getPlayer().sendMessage("아군 신호기는 부술 수 없습니다!");
				if(tasks.containsKey(event.getPlayer())) {
					tasks.remove(event.getPlayer()).interrupt();
					new BukkitRunnable() {
						 public void run() {
							 p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
							 p.removePotionEffect(PotionEffectType.FAST_DIGGING);
						 }
					 }.runTask(KukgaMain.plugin);
				}
				event.setCancelled(true);
			}
		}else if(blocks.containsKey(event.getBlock().getType())){
			new BukkitRunnable() {
				 public void run() {
					 event.getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
					 event.getPlayer().removePotionEffect(PotionEffectType.FAST_DIGGING);
				 }
			 }.runTask(KukgaMain.plugin);
		}
	}

	@EventHandler
	public void onExplosion(EntityExplodeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onProjectileHit(PlayerTeleportEvent event){
		if(event.getCause() == TeleportCause.ENDER_PEARL && 
			TerritoryManager.isInRegion(null, event.getTo())!=null &&
			!TerritoryManager.isInRegion(null, event.getTo()).equals(TeamManager.getNation(event.getPlayer()))){
				event.setCancelled(true);
				event.getPlayer().sendMessage("불가능합니다!");
			}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		Player p = event.getPlayer();
		if(choose.containsKey(p) && choose.get(p)) {
			int result = BattleManager.declare(TeamManager.getNation(p), event.getMessage());
			switch(result) {
			case 1:
				p.sendMessage(event.getMessage() + " 국가는 이미 전쟁중입니다!");
				break;
			case 2:
				p.sendMessage("전쟁중에 다른 국가에 전쟁을 선포할 수 없습니다!");
				break;
			case 3:
				p.sendMessage(event.getMessage() + " 국가를 찾을 수 없습니다!");
				break;
			}
			choose.remove(p);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			String owner = TerritoryManager.isInRegion(null, event.getClickedBlock().getLocation());
			if(owner!=null) {
				if(!owner.equals(TeamManager.getNation(p))) {
					p.sendMessage("이 블럭은 " + owner + " 국가의 소유입니다!");
					event.setCancelled(true);
					return;
				}else if(event.getClickedBlock().getType().equals(Material.BEACON) && owner.equals(TerritoryManager.getBeaconOwner(event.getClickedBlock().getLocation()))){
					if(event.getPlayer().isSneaking()){
						event.setCancelled(true);
						p.openInventory(TerritoryManager.openBeacon(event.getClickedBlock().getLocation()));
						beacon.put(p, true);
					}else if(new Random().nextInt()%8==0){
						event.getPlayer().sendMessage(KukgaMain.prefix + ChatColor.YELLOW + "쉬프트를 누루시고 신호기를 누루시면 " + ChatColor.BLUE + "텔레포트" + ChatColor.YELLOW + "하실 수 있습니다!");
					}
					return;
				}
			}
		}
		if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if(p.getItemInHand().isSimilar(KukgaMain.defendpaper))p.performCommand("국가 전쟁방어");
			if(p.getItemInHand().isSimilar(KukgaMain.declarepaper))p.performCommand("국가 전쟁선포");
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		boolean isBeacon = event.getBlock().getType().equals(Material.BEACON);
		String from = TeamManager.getNation(event.getPlayer());
		String owner = TerritoryManager.isInRegion(from, event.getBlock().getLocation());
		if(owner==null) {
			Player p = event.getPlayer();
			if(isBeacon) {
				if(from!=null) {
					if(event.getItemInHand().getItemMeta().hasDisplayName()) {
						if(TerritoryManager.getRegionNumber(from)<=5) {
							if(KukgaMain.config.getBoolean("territory.allowbuildterritoryinotherdim") || Bukkit.getWorlds().indexOf(event.getBlock().getWorld())==0) {
								String nation = TerritoryManager.registerRegion(from, event.getBlock().getLocation(),
										event.getItemInHand().getItemMeta().getDisplayName(), Bukkit.getWorlds().indexOf(event.getBlock().getLocation().getWorld()));
								if(nation!=null) {
									if(from.equals(nation))p.sendMessage("당신의 다른 영지와 너무 가깝습니다!");
									else p.sendMessage(nation + "국가와 너무 가깝습니다! 더 멀리가세요!");
								}else return;
							}else p.sendMessage("이 서버에서는 다른 세계에 영지를 건설할 수 없습니다!");
						}else p.sendMessage("당신의 국가는 너무 많은 신호기를 가지고 있습니다!");
					}else p.sendMessage(ChatColor.RED + "도대체 이런일이 어떻게 벌어진건지 모르겠지만, 당신은 허용되지 않은 신호기를 들고 있습니다.");
						event.setCancelled(true);
				}else {
					p.sendMessage("국가에 소속되어 있어야 합니다!");
				}
				event.setCancelled(true);
			}
		}else if(!owner.equals(from)) {
					event.getPlayer().sendMessage(owner + "국의 영토 안에서는 블럭을 설치할 수 없습니다!");
					event.setCancelled(true);
				}else if(isBeacon) {
					event.getPlayer().sendMessage("당신의 다른 영지와 너무 가깝습니다!");
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
		Player p = (Player) event.getWhoClicked();
		if(beacon.containsKey(p)) {
			if(event.getRawSlot()<54) {
				if(event.getAction().name().contains("PICKUP")) {
						if(!event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.WHITE + "[" + ChatColor.YELLOW  + "이곳" + ChatColor.WHITE + "]")) {
					//위에꺼 그냥 [이곳] 이라는 이름의 아이템 눌렀는지 확인하는건데 색 들어가 있어서 헤헤..
					if(event.getCurrentItem().getType().equals(Material.BEACON)) {
						int [] loc = new int[3];
						String lore = event.getCurrentItem().getItemMeta().getLore().get(0);
						loc[0] = Integer.parseInt(lore.substring(lore.lastIndexOf("x:")+2, lore.lastIndexOf(" y:")));
						loc[1] = Integer.parseInt(lore.substring(lore.lastIndexOf("y:")+2, lore.lastIndexOf(" z:")));
						loc[2] = Integer.parseInt(lore.substring(lore.lastIndexOf("z:")+2, lore.length()-1));
						event.setCancelled(true);
						event.getWhoClicked().teleport(new Location(Bukkit.getWorlds().get(0), loc[0], loc[1], loc[2]));
						event.getWhoClicked().closeInventory();
						beacon.remove(p);
					}
				}
			}
				event.setCancelled(true);
			}else if(event.getAction().name().equals("MOVE_TO_OTHER_INVENTORY"))event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		Player p = (Player)event.getWhoClicked();
		if(beacon.containsKey(p)) {
			for(Integer slot : event.getRawSlots()) {
				if(slot<54)event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player p = (Player) event.getPlayer();
		beacon.remove(p);
	}
	
	protected static void changeHardness(Material material, double hardness) {
		blocks.put(material, hardness);
		try {
			java.lang.reflect.Field st = Reflection.getField(Objects.requireNonNull(Reflection.getClass("{nms}.Block")), "strength");
			st.setAccessible(true);
			java.lang.reflect.Method magic = Objects.requireNonNull(Reflection.getClass("{cb}.util.CraftMagicNumbers")).getMethod("getBlock", Material.class);
			magic.setAccessible(true);
			st.set(magic.invoke(null, material), 2000F);
		} catch (Exception e) {
			e.printStackTrace();
			KukgaMain.sender.sendMessage("블럭의 강도를 지정하는데에 문제가 생겼습니다!");
			KukgaMain.sender.sendMessage("매터리얼: " + material.name());
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
