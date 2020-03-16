package tmvkrpxl0.Kukga;
import java.lang.reflect.Field;
import java.util.Queue;

import org.bukkit.entity.Player;

import io.netty.channel.Channel;


/*
 * This class is made by Fr33style
 */
public class PacketInjectorNew implements PacketInjectorInterface{

	private Field EntityPlayer_playerConnection;
	private Class<?> PlayerConnection;
	private Field PlayerConnection_networkManager;

	private Class<?> NetworkManager;
	private Field k;
	private Field m;
	public PacketInjectorNew() {
		try {
			EntityPlayer_playerConnection = KukgaReflection.getField(KukgaReflection.getClass("{nms}.EntityPlayer"), "playerConnection");
			PlayerConnection = KukgaReflection.getClass("{nms}.PlayerConnection");
			PlayerConnection_networkManager = KukgaReflection.getField(PlayerConnection, "networkManager");

			NetworkManager = KukgaReflection.getClass("{nms}.NetworkManager");
			boolean access;
			for(Field f : NetworkManager.getDeclaredFields()) {
				access = f.isAccessible();
				f.setAccessible(true);
				if(f.getType().equals(Queue.class)) k=f;
				else if(f.getType().equals(Channel.class))m=f;
				else f.setAccessible(access);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	@Override
	public void addPlayer(Player p) {
		try {
			Channel ch = getChannel(getNetworkManager(KukgaReflection.getNmsPlayer(p)));
			if(ch.pipeline().get("PacketInjector") == null) {
				PacketHandlerNew h = new PacketHandlerNew(p);
				ch.pipeline().addBefore("packet_handler", "PacketInjector", h);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void removePlayer(Player p) {
		try {
			Channel ch = getChannel(getNetworkManager(KukgaReflection.getNmsPlayer(p)));
			if(ch.pipeline().get("PacketInjector") != null) {
				ch.pipeline().remove("PacketInjector");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private Object getNetworkManager(Object ep) {
		Object obj = KukgaReflection.getFieldValue(EntityPlayer_playerConnection, ep);
		return KukgaReflection.getFieldValue(PlayerConnection_networkManager, obj);
	}

	private Channel getChannel(Object networkManager) {
		Channel ch = null;
		try {
			ch = KukgaReflection.getFieldValue(k, networkManager);
		} catch (Exception e) {
			ch = KukgaReflection.getFieldValue(m, networkManager);
		}
		return ch;
	}
}