package tmvkrpxl0;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;

import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.EnumSkyBlock;
import net.minecraft.server.v1_7_R4.IWorldAccess;
import net.minecraft.server.v1_7_R4.PlayerChunkMap;
import net.minecraft.server.v1_7_R4.World;
import net.minecraft.server.v1_7_R4.WorldServer;

public class LightChanger {
    private static Method cachedPlayerChunk;
    private static Field cachedDirtyField;
    
    // For choosing an adjacent air block
    private static BlockFace[] SIDES = { 
            BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, 
            BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    
    /**
     * Create light with level at a location. 
     * @param loc - which block to update.
     * @param level - the new light level.
     */
    public static void createLightSource(Location loc, int level) {
        WorldServer nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
        int oldLevel = loc.getBlock().getLightLevel();
        
        // Sets the light source at the location to the level
        nmsWorld.b(EnumSkyBlock.BLOCK, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), level);
        
        // Send packets to the area telling players to see this level
        updateChunk(nmsWorld, loc);
        
        // If you comment this out it is more likely to get light sources you can't remove
        // but if you do comment it, light is consistent on relog and what not.
        nmsWorld.b(EnumSkyBlock.BLOCK, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), oldLevel);
    }

    private static Block getAdjacentAirBlock(Block block) {
        // Find the first adjacent air block
        for (BlockFace face : SIDES) {
            // Don't use these sides
            if (block.getY() == 0x0 && face == BlockFace.DOWN)
                continue;
            if (block.getY() == 0xFF && face == BlockFace.UP)
                continue;
            
            Block candidate = block.getRelative(face); 

            if (candidate.getType().isTransparent()) {
                return candidate;
            }
        }
        return block;
    }
    
    /**
     * Gets all the chunks touching/diagonal to the chunk the location is in and updates players with them.
     * @param loc - location to the block that was updated.
     */
    @SuppressWarnings("rawtypes")
    private static void updateChunk(WorldServer nmsWorld, Location loc) {
        try {
            PlayerChunkMap map = nmsWorld.getPlayerChunkMap();
            IWorldAccess access = countLightUpdates(loc.getWorld(), map);
            nmsWorld.addIWorldAccess(access);

            // Update the light itself
            Block adjacent = getAdjacentAirBlock(loc.getBlock());
            nmsWorld.A(adjacent.getX(), adjacent.getY(), adjacent.getZ());
            
            // Remove it again
            Field field = World.class.getDeclaredField("u");
            field.setAccessible(true);
            ((List) field.get(nmsWorld)).remove(access);

            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;
            
            // Make sure the block itself is marked
            map.flagDirty(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            
            // Look for player chunks immediately around the block
            for (int dX = -1; dX <= 1; dX++) {
                for (int dZ =-1; dZ <=1; dZ++) {
                    // That class is package private unfortunately
                    Object playerChunk = getPlayerCountMethod().invoke(map, chunkX + dX, chunkZ + dZ, false);

                    if (playerChunk != null) {
                        Field dirtyField = getDirtyField(playerChunk);
                        
                        // Minecraft will automatically send out a Packet51MapChunk for us, 
                        // with only those segments (16x16x16) that are needed.
                        if ((Integer) dirtyField.get(playerChunk) > 0) {
                            dirtyField.set(playerChunk, 64);
                        }
                    }
                }
            }
            map.flush();
            
        } catch (SecurityException e) {
            throw new RuntimeException("Access denied", e);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Reflection problem.", e);
        }
    }
    
    private static IWorldAccess countLightUpdates(final org.bukkit.World world, final PlayerChunkMap map) {
        return new IWorldAccess() {         
            @Override
            //markBlockForUpdate
            public void a(int x, int y, int z) {
                map.flagDirty(x, y, z);
            }
            
            @Override
            //markBlockForRenderUpdate
            public void b(int x, int y, int z) {
                map.flagDirty(x, y, z);
            }
            
            @Override
            //destroyBlockPartially
            public void b(int arg0, int arg1, int arg2, int arg3, int arg4) { }
                        
            @Override
            //playAuxSFX
            public void a(EntityHuman arg0, int arg1, int arg2, int arg3, int arg4, int arg5) { }
                        
            @Override
            //markBlockRangeForRenderUpdate
            public void a(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
                // Ignore
            }
            
            @Override
            //broadcastSound
            public void a(int arg0, int arg1, int arg2, int arg3, int arg4) { }
            
            @Override
            //playSound
            public void a(String arg0, double arg1, double arg2, double arg3, float arg4, float arg5) {
            }
            
            @Override
            //playSoundToNearExcept
            public void a(EntityHuman arg0, String arg1, double arg2, double arg3, double arg4, float arg5,float arg6) {
            }
            
            @Override
            //spawnParticle
            public void a(String arg0, double arg1, double arg2, double arg3, double arg4, double arg5, double arg6) { }
            
            @Override
            //playRecord
            public void a(String arg0, int arg1, int arg2, int arg3) { }
            
            @Override
            //onEntityCreate
            public void a(Entity arg0) { }
            
            @Override
            //onEntityDestroy (probably)
            public void b(Entity arg0) { }

			@Override
			public void b() {
				// TODO Auto-generated method stub
				
			}
        };
    }
    
    private static Method getPlayerCountMethod() throws NoSuchMethodException, SecurityException {
        if (cachedPlayerChunk == null) {
            cachedPlayerChunk = PlayerChunkMap.class.getDeclaredMethod("a", int.class, int.class, boolean.class);
            cachedPlayerChunk.setAccessible(true);
        }
        return cachedPlayerChunk;
    }
    
    private static Field getDirtyField(Object playerChunk) throws NoSuchFieldException, SecurityException {
        if (cachedDirtyField == null) {
            cachedDirtyField = playerChunk.getClass().getDeclaredField("dirtyCount");
            cachedDirtyField.setAccessible(true);
        }
        return cachedDirtyField;
    }   
}
