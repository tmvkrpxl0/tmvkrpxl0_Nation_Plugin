package tmvkrpxl0.Kukga;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Queue;

import org.bukkit.entity.Player;

import io.netty.channel.Channel;


/*
 * This class is made by Fr33style
 */
public class PacketInjectorNew implements PacketInjectorInterface{

	private Field EntityPlayer_playerConnection;
	private Field PlayerConnection_networkManager;

	private Field k;
	private Field m;
	public PacketInjectorNew() {
		try {
			EntityPlayer_playerConnection = Reflection.getField(Objects.requireNonNull(Reflection.getClass("{nms}.EntityPlayer")), "playerConnection");
			Class<?> playerConnection = Reflection.getClass("{nms}.PlayerConnection");
			assert playerConnection != null;
			PlayerConnection_networkManager = Reflection.getField(playerConnection, "networkManager");

			Class<?> networkManager = Reflection.getClass("{nms}.NetworkManager");
			boolean access;
			assert networkManager != null;
			for(Field f : networkManager.getDeclaredFields()) {
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
			Channel ch = getChannel(getNetworkManager(Reflection.getNmsPlayer(p)));
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
		Channel ch;
		try {
			ch = Reflection.getFieldValue(k, networkManager);
		} catch (Exception e) {
			ch = Reflection.getFieldValue(m, networkManager);
		}
		return ch;
	}
}