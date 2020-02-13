package tmvkrpxl0;

import java.lang.reflect.Method;

import org.bukkit.Effect;
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
			 Class<?> bp = Reflection.getClass("{nms}.BlockPosition");
			 Method getx = bp.getSuperclass().getDeclaredMethod("getX");
			 getx.setAccessible(true);
			 Method gety = bp.getSuperclass().getDeclaredMethod("getY");
			 gety.setAccessible(true);
			 Method getz = bp.getSuperclass().getDeclaredMethod("getZ");
			 getz.setAccessible(true);
			 int x = (int) getx.invoke(position);
			 int y = (int) gety.invoke(position);
			 int z = (int) getz.invoke(position);
			 String e = Reflection.getFieldValue(m, "c").toString();
			 if(e.equals("ABORT_DESTROY_BLOCK") || e.equals("STOP_DESTRY_BLOCK")) {
				 if(listener.blocks.containsKey(p.getWorld().getBlockAt(x, y, z).getType())) {
					 new BukkitRunnable() {
						 public void run() {
							 p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
							 p.removePotionEffect(PotionEffectType.FAST_DIGGING);
						 }
					 }.runTask(Core.plugin);
					 if(listener.tasks.containsKey(p)) {// to avoid bug
						 Reflection.sendAllPacket(Reflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation")
							 .getConstructor(int.class, bp, int.class).newInstance(
									 p.getUniqueId().hashCode(), bp.getConstructor(int.class, int.class, int.class).newInstance(x, y, z), -1));
						 listener.tasks.get(p).interrupt();
						 listener.tasks.remove(p);
				 }
			 }
			 }else if(e.equals("START_DESTROY_BLOCK") && listener.blocks.containsKey(p.getWorld().getBlockAt(x, y, z).getType())) {
				 new BukkitRunnable() {
					 public void run() {
						 p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 9999*20, 17), true);
						 p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 9999*20, 0), true);
					 }
				 }.runTask(Core.plugin);
				 Thread t = new Thread() {
						int mining = 0;
						public void run() {
								try {
									while(mining < 10 && !isInterrupted()) {
										Reflection.sendAllPacket(Reflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation")
												 .getConstructor(int.class, bp, int.class).newInstance(
														 p.getUniqueId().hashCode(), bp.getConstructor(int.class, int.class, int.class).newInstance(x, y, z), mining));
										mining++;
										Thread.sleep((long)(listener.blocks.get(p.getWorld().getBlockAt(x, y, z).getType())/10.0*1000));
									}
										Thread.sleep(1);
										new BukkitRunnable() {
											public void run() {
												try {
													p.getWorld().getBlockAt(x, y, z).breakNaturally();
													p.getWorld().spigot().playEffect(p.getLocation(), Effect.STEP_SOUND);
													Reflection.sendAllPacket(Reflection.getClass("{nms}.PacketPlayOutBlockBreakAnimation")
															 .getConstructor(int.class, bp, int.class).newInstance(
																	 p.getUniqueId().hashCode(), bp.getConstructor(int.class, int.class, int.class).newInstance(x, y, z), -1));
												} catch (Exception e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
										}.runTask(Core.plugin);
								} catch (Exception e) {
							}
						}
					};
					if(listener.tasks.containsKey(p) && listener.tasks.get(p).isAlive()) {
						listener.tasks.get(p).interrupt();
						listener.tasks.remove(p);
					}
					listener.tasks.put(p, t);
					t.start();
			 }
	 }
		 super.channelRead(channel, m);
		 }
	 
}
