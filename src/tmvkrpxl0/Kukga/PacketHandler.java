package tmvkrpxl0.Kukga;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelPromise;

import java.util.Objects;

/*
 * This class is originally made by Fr33Style
 * and edited by me
 * i do not own this
 */
public class PacketHandler extends ChannelDuplexHandler {
	private Player p;
	public PacketHandler(final Player p) {
		this.p = p;
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		super.write(ctx, msg, promise);
	}
	@Override
	public void channelRead(ChannelHandlerContext channel, Object m) throws Exception {
		if(m.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInBlockDig")) {
			final int e = (int) Reflection.getFieldValue(m, "e");
			final int x = (int) Reflection.getFieldValue(m, "a");
			final int y = (int) Reflection.getFieldValue(m, "b");
			final int z = (int) Reflection.getFieldValue(m, "c");
			try{
				final int worldidx = Bukkit.getWorlds().indexOf(p.getWorld());
				final Material type = Bukkit.getWorlds().get(worldidx).getBlockAt(x, y, z).getType();
				String nation = TeamManager.getNation(p);
				final String owner = TerritoryManager.isInRegion(null, x, z, worldidx);
				if(KukgaListener.blocks.containsKey(type) && (owner==null || !type.equals(Material.BEACON) || !owner.equals(nation))) {
					if(e == 1|| e == 2) {
						new BukkitRunnable() {
							public void run() {
								p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
								p.removePotionEffect(PotionEffectType.FAST_DIGGING);
							}
						}.runTask(KukgaMain.plugin);
						if(KukgaListener.tasks.containsKey(p)) {// to avoid bug
							Reflection.sendAllPacket(Objects.requireNonNull(Reflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation"))
									.getConstructor(int.class, int.class, int.class, int.class, int.class).newInstance(
											p.getUniqueId().hashCode(), x, y, z, -1));
							KukgaListener.tasks.get(p).interrupt();
							KukgaListener.tasks.remove(p);
						}
					}else if(e == 0) {
						new BukkitRunnable() {
							public void run() {
								p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 9999*20, 17), true);
								p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 9999*20, 0), true);
							}
						}.runTask(KukgaMain.plugin);
						Thread miningThread = new Thread() {
							int mining = 0;
							public void run() {
								try {
									while(mining < 10) {
										if(isInterrupted())interrupt();
										Reflection.sendAllPacket(Objects.requireNonNull(Reflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation"))
												.getConstructor(int.class, int.class, int.class, int.class, int.class).newInstance(p.getUniqueId().hashCode(),
														x, y, z, mining));
										mining++;
										if(!KukgaListener.blocks.containsKey(Bukkit.getWorlds().get(worldidx).getBlockAt(x, y, z).getType())){
											interrupt();
											sleep(1);
											return;
										}
										sleep((long)(KukgaListener.blocks.get(type)/10.0*1000));
									}
									sleep(1);
									new BukkitRunnable() {
										public void run() {
											try {
												new BukkitRunnable() {
													public void run() {
														p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
														p.removePotionEffect(PotionEffectType.FAST_DIGGING);
													}
												}.runTask(KukgaMain.plugin);
												Block block = Bukkit.getWorlds().get(worldidx).getBlockAt(x, y, z);
												if(type.equals(Material.BEACON)){
													block.setType(Material.AIR);
													TerritoryManager.deleteRegion(owner, x, y, z, worldidx);
												}else block.breakNaturally();
												Bukkit.getWorlds().get(worldidx).spigot().playEffect(p.getLocation(), Effect.STEP_SOUND);
												Reflection.sendAllPacket(Objects.requireNonNull(Reflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation"))
														.getConstructor(int.class, int.class, int.class, int.class, int.class).newInstance(p.getUniqueId().hashCode(), x, y, z, -1));
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}.runTask(KukgaMain.plugin);
								} catch (InterruptedException ignored){
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						};
						if(KukgaListener.tasks.containsKey(p) && KukgaListener.tasks.get(p).isAlive()) {
							KukgaListener.tasks.get(p).interrupt();
							KukgaListener.tasks.remove(p);
						}
						KukgaListener.tasks.put(p, miningThread);
						miningThread.start();
					}
				}
			}catch (InterruptedException ignored){
			}catch (Exception e2){
				e2.printStackTrace();
			}

		}
		super.channelRead(channel, m);
	}

}
