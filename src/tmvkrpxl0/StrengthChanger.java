package tmvkrpxl0;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.wrappers.BlockPosition;

import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_7_R4.PacketPlayOutWorldEvent;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;

//CREDIT: ewoudje

public class StrengthChanger implements Listener {

    private Map<Player, BukkitTask> task;
    private Field pim_f, pim_k, pim_d, pim_j;
    private static List<Block> changedBlocks;
    private static Field strength;
    private final Core plugin;

    public StrengthChanger(Core plugin) {
        this.plugin = plugin;
        task = new HashMap<>();
        changedBlocks = new ArrayList<>();
        try {
            strength = Block.class.getDeclaredField("strength");
            pim_f = PlayerInteractManager.class.getDeclaredField("f");
            pim_k = PlayerInteractManager.class.getDeclaredField("k");
            pim_d = PlayerInteractManager.class.getDeclaredField("d");
            pim_j = PlayerInteractManager.class.getDeclaredField("j");
            strength.setAccessible(true);
            pim_f.setAccessible(true);
            pim_k.setAccessible(true);
            pim_d.setAccessible(true);
            pim_j.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setStrength(Block block, float strengthValue) {
        try {
        	strength.setFloat(block, strengthValue);
            changedBlocks.add(block);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final CraftPlayer player = (CraftPlayer) event.getPlayer();
        final EntityPlayer handle = player.getHandle();
        task.put(event.getPlayer(), plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            boolean wasMining = false;
            @Override
            public void run() {
                try {
                    int k = pim_k.getInt(handle.playerInteractManager);
                    if ((pim_j.getBoolean(handle.playerInteractManager) || pim_d.getBoolean(handle.playerInteractManager) && changedBlocks.contains(handle.world.getType(((BlockPosition) pim_f.get(handle.playerInteractManager)).getX(),
                    		((BlockPosition) pim_f.get(handle.playerInteractManager)).getY(), ((BlockPosition) pim_f.get(handle.playerInteractManager)).getZ())))) {
                        handle.playerConnection.sendPacket(new PacketPlayOutBlockBreakAnimation(0, ((BlockPosition) pim_f.get(handle.playerInteractManager)).getX(), 
                        		((BlockPosition) pim_f.get(handle.playerInteractManager)).getY(), ((BlockPosition) pim_f.get(handle.playerInteractManager)).getZ(), k));
                        if (k >= 10) {
                            handle.playerInteractManager.a(((BlockPosition) pim_f.get(handle.playerInteractManager)).getX(), ((BlockPosition) pim_f.get(handle.playerInteractManager)).getY(),
                            		((BlockPosition) pim_f.get(handle.playerInteractManager)).getZ());
                        }
                        wasMining = true;
                    } else if (wasMining) {
                        wasMining = false;
                        handle.playerConnection.sendPacket(new PacketPlayOutBlockBreakAnimation(0, ((BlockPosition) pim_f.get(handle.playerInteractManager)).getX(),
                        		((BlockPosition) pim_f.get(handle.playerInteractManager)).getY(), ((BlockPosition) pim_f.get(handle.playerInteractManager)).getZ(), -1));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1));

    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (task.get(event.getPlayer()) != null) task.remove(event.getPlayer());
    }

    @SuppressWarnings("deprecation")
	@EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	try {
			pim_k.set(((CraftPlayer)event.getPlayer()).getHandle().playerInteractManager, 0);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Location loc = event.getBlock().getLocation();
        ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutWorldEvent(2001, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), event.getBlock().getTypeId(), false));
    }

}