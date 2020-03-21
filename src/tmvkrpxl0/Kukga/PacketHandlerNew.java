package tmvkrpxl0.Kukga;

import java.lang.reflect.Method;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/*
 * This class is originally made by Fr33Style
 * and edited by me
 * i do not own this
 */

//이 클래스 파일은 1.8 버전 이상 라이브러리를 사용해 컴파일 한 이후, 1.7.10에서 컴파일한 파일에 추가해야 합니다!
//원레라면 Reflection을 활용하여 클래스 파일 1개로 만들 계획이었으나, ChannelDuplexHandler위치가 달라져 2개로 만들어야만 했습니다.
public class PacketHandlerNew extends ChannelDuplexHandler {
	private Player p;
	public PacketHandlerNew(final Player p) {
		this.p = p;
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		super.write(ctx, msg, promise);
	}
	@Override
	public void channelRead(ChannelHandlerContext channel, Object m) throws Exception {
		if(m.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInBlockDig")) {
			Object position = Reflection.getFieldValue(m, "a");
			final Class<?> bp = Objects.requireNonNull(Reflection.getClass("{nms}.BlockPosition"));
			Method getx = bp.getSuperclass().getDeclaredMethod("getX");
			getx.setAccessible(true);
			Method gety = bp.getSuperclass().getDeclaredMethod("getY");
			Method getz = bp.getSuperclass().getDeclaredMethod("getZ");
			getz.setAccessible(true);
			final int x = (int) getx.invoke(position);
			final int y = (int) gety.invoke(position);
			final int z = (int) getz.invoke(position);
			try{
				final String nation = TeamManager.getNation(p);
				final int worldidx = Bukkit.getWorlds().indexOf(p.getWorld());
				final String owner = TerritoryManager.isInRegion(null, x, z, worldidx);
				final String e = Reflection.getFieldValue(m, "c").toString();
				final Material type = Bukkit.getWorlds().get(worldidx).getBlockAt(x, y, z).getType();
				if(KukgaListener.blocks.containsKey(type) && (owner==null || !type.equals(Material.BEACON) || !owner.equals(nation))) {
					if(e.equals("ABORT_DESTROY_BLOCK") || e.equals("STOP_DESTRY_BLOCK")) {
						new BukkitRunnable() {
							public void run() {
								p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
								p.removePotionEffect(PotionEffectType.FAST_DIGGING);
							}
						}.runTask(KukgaMain.plugin);
						if(KukgaListener.tasks.containsKey(p)) {// to avoid bug
							Reflection.sendAllPacket(Objects.requireNonNull(Reflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation"))
									.getConstructor(int.class, bp, int.class).newInstance(
											p.getUniqueId().hashCode(), bp.getConstructor(int.class, int.class, int.class).newInstance(x, y, z), -1));
							KukgaListener.tasks.get(p).interrupt();
							KukgaListener.tasks.remove(p);
						}
					}else if(e.equals("START_DESTROY_BLOCK")) {
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
									while(mining < 10) {
										if(isInterrupted())interrupt();
										Reflection.sendAllPacket(Objects.requireNonNull(Reflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation"))
												.getConstructor(int.class, bp, int.class).newInstance(
														p.getUniqueId().hashCode(), bp.getConstructor(int.class, int.class, int.class).newInstance(x, y, z), mining));
										mining++;
										if(!KukgaListener.blocks.containsKey(Bukkit.getWorlds().get(worldidx).getBlockAt(x, y, z).getType())){
											interrupt();
											sleep(1);
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
													TerritoryManager.deleteRegion(owner, block.getLocation());
												}else block.breakNaturally();
												p.getWorld().spigot().playEffect(p.getLocation(), Effect.STEP_SOUND);
												Reflection.sendAllPacket(Objects.requireNonNull(Reflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation"))
														.getConstructor(int.class, bp, int.class).newInstance(
																p.getUniqueId().hashCode(), bp.getConstructor(int.class, int.class, int.class).newInstance(x, y, z), -1));
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}.runTask(KukgaMain.plugin);
								} catch (InterruptedException ignored) {
								} catch (Exception e){
									e.printStackTrace();
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
			}catch (InterruptedException ignored){
			}catch (Exception e2){
				e2.printStackTrace();
			}

		}
		super.channelRead(channel, m);
	}

}
