package tmvkrpxl0;

import java.lang.reflect.Field;

import org.bukkit.entity.Player;

import net.minecraft.util.io.netty.channel.Channel;

/*Author of this Class is Fr33Style
I do not own this class
and I didn't make this class
*/

public class PacketInjector {

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
	  return Reflection.getFFieldValue(PlayerConnection_networkManager, Reflection.getFFieldValue(EntityPlayer_playerConnection, ep));
	  }

	  private Channel getChannel(Object networkManager) {
	  Channel ch = null;
	  try {
	  ch = Reflection.getFFieldValue(k, networkManager);
	  } catch (Exception e) {
	  ch = Reflection.getFFieldValue(m, networkManager);
	  }
	  return ch;
	  }
	}