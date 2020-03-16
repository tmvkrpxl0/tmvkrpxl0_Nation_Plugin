package tmvkrpxl0.Kukga;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelPromise;

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
			 int e = (int) KukgaReflection.getFieldValue(m, "e");
			 int x = (int) KukgaReflection.getFieldValue(m, "a");
			 int y = (int) KukgaReflection.getFieldValue(m, "b");
			 int z = (int) KukgaReflection.getFieldValue(m, "c");
			 if(!TeamManager.getNation(p).equals(TerritoryManager.isInRegion(TeamManager.getNation(p), x, z)) && KukgaListener.blocks.containsKey(p.getWorld().getBlockAt(x, y, z).getType())) {
				 if(e == 1|| e == 2) {
					new BukkitRunnable() {
						public void run() {
							p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
							p.removePotionEffect(PotionEffectType.FAST_DIGGING);
						}
					}.runTask(KukgaMain.plugin);
					if(KukgaListener.tasks.containsKey(p)) {// to avoid bug
						KukgaReflection.sendAllPacket(KukgaReflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation")
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
					 Thread t = new Thread() {
							int mining = 0;
							public void run() {
									try {
										while(mining < 10 && !isInterrupted()) {
											KukgaReflection.sendAllPacket(KukgaReflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation")
													.getConstructor(int.class, int.class, int.class, int.class, int.class).newInstance(p.getUniqueId().hashCode(), 
															x, y, z, mining));
											mining++;
											Thread.sleep((long)(KukgaListener.blocks.get(p.getWorld().getBlockAt(x, y, z).getType())/10.0*1000));
										}
											Thread.sleep(1);
											new BukkitRunnable() {
												@SuppressWarnings("deprecation")
												public void run() {
													try {
														new BukkitRunnable() {
															 public void run() {
																 p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
																 p.removePotionEffect(PotionEffectType.FAST_DIGGING);
															 }
														 }.runTask(KukgaMain.plugin);
														org.bukkit.block.Block block = p.getWorld().getBlockAt(x, y, z);
														if(block.getType().equals(Material.BEACON))TerritoryManager.deleteRegion(block.getLocation());
														block.breakNaturally();
														p.getWorld().spigot().playEffect(p.getLocation(), Effect.STEP_SOUND);
														KukgaReflection.sendAllPacket(KukgaReflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation")
																.getConstructor(int.class, int.class, int.class, int.class, int.class).newInstance(p.getUniqueId().hashCode(), x, y, z, -1));
													} catch (Exception e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
												}
											}.runTask(KukgaMain.plugin);
									} catch (Exception e) {
								}
							}
						};
						if(KukgaListener.tasks.containsKey(p) && KukgaListener.tasks.get(p).isAlive()) {
							KukgaListener.tasks.get(p).interrupt();
							KukgaListener.tasks.remove(p);
						}
						KukgaListener.tasks.put(p, t);
						t.start();
				 }
			 }
	 }
		 super.channelRead(channel, m);
		 }
	 
}
