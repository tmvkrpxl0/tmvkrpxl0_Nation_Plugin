package tmvkrpxl0;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

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
	 @Override public void channelRead(ChannelHandlerContext c, Object m) throws Exception { 
		 if(m.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInBlockDig") && listener.mining.containsKey(p) && listener.mining.get(p)) {
			 String e = Reflection.getFieldValue(m, "e").toString();
			 Core.broadcast("mining");
			 if(e.equals("1") || e.equals("2")) {
				 p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
				 listener.mining.remove(p);
			 }
			 super.channelRead(c, m);
	 } else { super.channelRead(c, m); } }
	 
}
