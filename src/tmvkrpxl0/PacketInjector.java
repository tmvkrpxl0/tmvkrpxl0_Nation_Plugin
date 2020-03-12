package tmvkrpxl0;
import java.lang.reflect.Field;

import org.bukkit.entity.Player;

import net.minecraft.util.io.netty.channel.Channel;
/*
 * This class is made by Fr33style
 */
public class PacketInjector implements PacketInjectorInterface{

	private Field EntityPlayer_playerConnection;
	private Class<?> PlayerConnection;
	private Field PlayerConnection_networkManager;

	private Class<?> NetworkManager;
	private Field k;
	private Field m;

	public PacketInjector() {
		try {
			EntityPlayer_playerConnection = Reflection.getField(Reflection.getClass("{nms}.EntityPlayer"), "playerConnection");

			PlayerConnection = Reflection.getClass("{nms}.PlayerConnection");
			PlayerConnection_networkManager = Reflection.getField(PlayerConnection, "networkManager");

			NetworkManager = Reflection.getClass("{nms}.NetworkManager");
			k = Reflection.getField(NetworkManager, "k");
			m = Reflection.getField(NetworkManager, "m");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	@Override
	public void addPlayer(Player p) {
		try {
			Channel ch = getChannel(getNetworkManager(Reflection.getNmsPlayer(p)));
			if(ch.pipeline().get("PacketInjector") == null) {
				PacketHandler h = new PacketHandler(p);
				ch.pipeline().addBefore("packet_handler", "PacketInjector", h);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	@Override
	public void removePlayer(Player p) {
		try {
			Channel ch = getChannel(getNetworkManager(Reflection.getNmsPlayer(p)));
			if(ch.pipeline().get("PacketInjector") != null) {
				ch.pipeline().remove("PacketInjector");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private Object getNetworkManager(Object ep) {
		Object obj = Reflection.getFieldValue(EntityPlayer_playerConnection, ep);
		return Reflection.getFieldValue(PlayerConnection_networkManager, obj);
	}

	private Channel getChannel(Object networkManager) {
		Channel ch = null;
		try {
			ch = Reflection.getFieldValue(k, networkManager);
		} catch (Exception e) {
			ch = Reflection.getFieldValue(m, networkManager);
		}
		return ch;
	}
}