package tmvkrpxl0;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

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
	  for(Field f : NetworkManager.getDeclaredFields()) {
		  if(f.getType().equals(java.util.Queue.class))k = f;
		  else if(f.getType().equals(Reflection.getClass("{netty}.Channel")))m = f;
		  }
	  } catch (Throwable t) {
	  t.printStackTrace();
	  }
	  }

	  protected void addPlayer(Player p) {
	  try {
		  Method pipe = Reflection.getClass("{netty}.Channel").getMethod("pipeline");
		  pipe.setAccessible(true);
		  Object chpipe = pipe.invoke(getChannel(getNetworkManager(Reflection.getNmsPlayer(p))));
		  Method get = chpipe.getClass().getMethod("get", String.class);
		  get.setAccessible(true);
	  if(get.invoke(chpipe, "PacketInjector") == null) {
		  Method addBefore = chpipe.getClass().getMethod("addBefore", String.class, String.class, Reflection.getClass("{netty}.ChannelHandler"));
		  addBefore.setAccessible(true);
		  try {
			  Class.forName("net.minecraft.util.io.netty.channel.ChannelDuplexHandler");
			  addBefore.invoke(chpipe, "packet_handler", "PacketInjector", new PacketHandler(p));
		  }catch(ClassNotFoundException e) {
			  addBefore.invoke(chpipe, "packet_handler", "PacketInjector", new PacketHandlerNew(p));
		  }
	  }
	  } catch (Throwable t) {
	  t.printStackTrace();
	  }
	  }

	  protected void removePlayer(Player p) {
	  try {
	  Method pipeline = Reflection.getClass("{netty}.Channel").getMethod("pipeline");
	  pipeline.setAccessible(true);
	  Object chpipe = pipeline.invoke(getChannel(getNetworkManager(Reflection.getNmsPlayer(p))));
	  Method get = chpipe.getClass().getMethod("get", String.class);
	  get.setAccessible(true);
	  if(get.invoke(chpipe, "PacketInjector") != null) {
		  Method remove = chpipe.getClass().getMethod("remove", String.class);
		  remove.setAccessible(true);
		  remove.invoke(chpipe, "PacketInjector");
	  }
	  } catch (Throwable t) {
	      t.printStackTrace();
	  }
	  }

	  private Object getNetworkManager(Object ep) {
		  Object obj = Reflection.getFieldValue(EntityPlayer_playerConnection, ep);
		  return Reflection.getFieldValue(PlayerConnection_networkManager, obj);
	  }

	  private Object getChannel(Object networkManager) {
		  Object ch = null;
		  try {
		  ch = Reflection.getFieldValue(k, networkManager);
		  if(!ch.getClass().equals(Reflection.getClass("{netty}.Channel")))ch = Reflection.getFieldValue(m, networkManager);
		  } catch (Exception e) {
		  ch = Reflection.getFieldValue(m, networkManager);
		  }
		  return ch;
		  }
	}