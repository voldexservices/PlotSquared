package com.plotsquared.bukkit.util;

import com.intellectualcrafters.jnbt.ByteArrayTag;
import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.jnbt.IntTag;
import com.intellectualcrafters.jnbt.ListTag;
import com.intellectualcrafters.jnbt.ShortTag;
import com.intellectualcrafters.jnbt.StringTag;
import com.intellectualcrafters.jnbt.Tag;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.block.LocalBlockQueue;
import com.plotsquared.bukkit.object.schematic.StateWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Schematic Handler.
 */
public class BukkitSchematicHandler extends SchematicHandler {

    @Override
    public void getCompoundTag(final String world, final Set<RegionWrapper> regions, final RunnableVal<CompoundTag> whenDone) {
        // async
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                // Main positions
                Location[] corners = MainUtil.getCorners(world, regions);
                final Location bot = corners[0];
                Location top = corners[1];

                final int width = top.getX() - bot.getX() + 1;
                int height = top.getY() - bot.getY() + 1;
                final int length = top.getZ() - bot.getZ() + 1;
                // Main Schematic tag
                final HashMap<String, Tag> schematic = new HashMap<>();
                schematic.put("Width", new ShortTag("Width", (short) width));
                schematic.put("Length", new ShortTag("Length", (short) length));
                schematic.put("Height", new ShortTag("Height", (short) height));
                schematic.put("Materials", new StringTag("Materials", "Alpha"));
                schematic.put("WEOriginX", new IntTag("WEOriginX", 0));
                schematic.put("WEOriginY", new IntTag("WEOriginY", 0));
                schematic.put("WEOriginZ", new IntTag("WEOriginZ", 0));
                schematic.put("WEOffsetX", new IntTag("WEOffsetX", 0));
                schematic.put("WEOffsetY", new IntTag("WEOffsetY", 0));
                schematic.put("WEOffsetZ", new IntTag("WEOffsetZ", 0));
                // Arrays of data types
                final List<CompoundTag> tileEntities = new ArrayList<>();
                final byte[] blocks = new byte[width * height * length];
                final byte[] blockData = new byte[width * height * length];
                // Queue
                final ArrayDeque<RegionWrapper> queue = new ArrayDeque<>(regions);
                TaskManager.runTask(new Runnable() {
                    @Override
                    public void run() {
                        if (queue.isEmpty()) {
                            TaskManager.runTaskAsync(new Runnable() {
                                @Override
                                public void run() {
                                    schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
                                    schematic.put("Data", new ByteArrayTag("Data", blockData));
                                    schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<Tag>()));
                                    schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, tileEntities));
                                    whenDone.value = new CompoundTag("Schematic", schematic);
                                    TaskManager.runTask(whenDone);
                                    System.gc();
                                    System.gc();
                                }
                            });
                            return;
                        }
                        final Runnable regionTask = this;
                        RegionWrapper region = queue.poll();
                        Location pos1 = new Location(world, region.minX, region.minY, region.minZ);
                        Location pos2 = new Location(world, region.maxX, region.maxY, region.maxZ);
                        final int bx = bot.getX();
                        final int bz = bot.getZ();
                        final int p1x = pos1.getX();
                        final int p1z = pos1.getZ();
                        final int p2x = pos2.getX();
                        final int p2z = pos2.getZ();
                        final int bcx = p1x >> 4;
                        final int bcz = p1z >> 4;
                        final int tcx = p2x >> 4;
                        final int tcz = p2z >> 4;
                        final int sy = pos1.getY();
                        final int ey = pos2.getY();
                        // Generate list of chunks
                        final ArrayList<ChunkLoc> chunks = new ArrayList<>();
                        for (int x = bcx; x <= tcx; x++) {
                            for (int z = bcz; z <= tcz; z++) {
                                chunks.add(new ChunkLoc(x, z));
                            }
                        }
                        final World worldObj = Bukkit.getWorld(world);
                        // Main thread
                        TaskManager.runTask(new Runnable() {
                            @Override
                            public void run() {
                                long start = System.currentTimeMillis();
                                while (!chunks.isEmpty() && System.currentTimeMillis() - start < 20) {
                                    // save schematics
                                    ChunkLoc chunk = chunks.remove(0);
                                    Chunk bc = worldObj.getChunkAt(chunk.x, chunk.z);
                                    if (!bc.load(false)) {
                                        continue;
                                    }
                                    int X = chunk.x;
                                    int Z = chunk.z;
                                    int xxb = X << 4;
                                    int zzb = Z << 4;
                                    int xxt = xxb + 15;
                                    int zzt = zzb + 15;

                                    if (X == bcx) {
                                        xxb = p1x;
                                    }
                                    if (X == tcx) {
                                        xxt = p2x;
                                    }
                                    if (Z == bcz) {
                                        zzb = p1z;
                                    }
                                    if (Z == tcz) {
                                        zzt = p2z;
                                    }
                                    for (int y = sy; y <= Math.min(255, ey); y++) {
                                        int ry = y - sy;
                                        int i1 = ry * width * length;
                                        for (int z = zzb; z <= zzt; z++) {
                                            int rz = z - bz;
                                            int i2 = i1 + rz * width;
                                            for (int x = xxb; x <= xxt; x++) {
                                                int rx = x - bx;
                                                int index = i2 + rx;
                                                Block block = worldObj.getBlockAt(x, y, z);
                                                Material id = block.getType();
                                                switch (id) {
                                                    case AIR:
                                                    case GRASS:
                                                    case COBBLESTONE:
                                                    case GRAVEL:
                                                    case GOLD_ORE:
                                                    case IRON_ORE:
                                                    case GLASS:
                                                    case LAPIS_ORE:
                                                    case LAPIS_BLOCK:
                                                    case SANDSTONE:
                                                    case WEB:
                                                    case DEAD_BUSH:
                                                    case YELLOW_FLOWER:
                                                    case BROWN_MUSHROOM:
                                                    case RED_MUSHROOM:
                                                    case GOLD_BLOCK:
                                                    case IRON_BLOCK:
                                                    case BRICK:
                                                    case TNT:
                                                    case BOOKSHELF:
                                                    case MOSSY_COBBLESTONE:
                                                    case OBSIDIAN:
                                                    case FIRE:
                                                    case REDSTONE_WIRE:
                                                    case DIAMOND_ORE:
                                                    case DIAMOND_BLOCK:
                                                    case WORKBENCH:
                                                    case SOIL:
                                                    case BEDROCK:
                                                    case WATER:
                                                    case STATIONARY_WATER:
                                                    case LAVA:
                                                    case STATIONARY_LAVA:
                                                    case REDSTONE_ORE:
                                                    case GLOWING_REDSTONE_ORE:
                                                    case SNOW:
                                                    case ICE:
                                                    case SNOW_BLOCK:
                                                    case CACTUS:
                                                    case CLAY:
                                                    case SUGAR_CANE:
                                                    case FENCE:
                                                    case NETHERRACK:
                                                    case SOUL_SAND:
                                                    case IRON_FENCE:
                                                    case THIN_GLASS:
                                                    case MELON_BLOCK:
                                                    case MYCEL:
                                                    case NETHER_BRICK:
                                                    case NETHER_FENCE:
                                                    case ENDER_STONE:
                                                    case DRAGON_EGG:
                                                    case EMERALD_ORE:
                                                    case EMERALD_BLOCK:
                                                    case SLIME_BLOCK:
                                                    case BARRIER:
                                                    case SEA_LANTERN:
                                                    case HAY_BLOCK:
                                                    case HARD_CLAY:
                                                    case COAL_BLOCK:
                                                    case PACKED_ICE:
                                                    case DOUBLE_STONE_SLAB2:
                                                    case STONE_SLAB2:
                                                    case SPRUCE_FENCE:
                                                    case BIRCH_FENCE:
                                                    case JUNGLE_FENCE:
                                                    case DARK_OAK_FENCE:
                                                    case ACACIA_FENCE:
                                                        break;
                                                    case CHEST:
                                                    case ENDER_CHEST:
                                                    case POTATO:
                                                    case POWERED_RAIL:
                                                    case COMMAND:
                                                    case MOB_SPAWNER:
                                                    case HOPPER:
                                                    case JUKEBOX:
                                                    case NOTE_BLOCK:
                                                    case SKULL:
                                                    case BEACON:
                                                    case STANDING_BANNER:
                                                    case WALL_BANNER:
                                                    case SIGN_POST:
                                                    case WALL_SIGN:
                                                    case ENCHANTMENT_TABLE:
                                                    case BREWING_STAND:
                                                    case DETECTOR_RAIL:
                                                    case RAILS:
                                                    case ACTIVATOR_RAIL:
                                                    case FURNACE:
                                                    case BURNING_FURNACE:
                                                    case FLOWER_POT:
                                                    case TRAPPED_CHEST:
                                                    case REDSTONE_COMPARATOR_OFF:
                                                    case REDSTONE_COMPARATOR_ON:
                                                    case DROPPER:
                                                    case DISPENSER:
                                                    case REDSTONE_LAMP_OFF:
                                                    case REDSTONE_LAMP_ON:
                                                    case PISTON_STICKY_BASE:
                                                    case PISTON_BASE:
                                                    case DAYLIGHT_DETECTOR:
                                                    case DAYLIGHT_DETECTOR_INVERTED:
                                                        // TODO implement fully
                                                        BlockState state = block.getState();
                                                        if (state != null) {
                                                            StateWrapper wrapper = new StateWrapper(state);
                                                            CompoundTag rawTag = wrapper.getTag();
                                                            if (rawTag != null) {
                                                                Map<String, Tag> values = new HashMap<>(rawTag.getValue());
                                                                values.put("id", new StringTag("id", wrapper.getId()));
                                                                values.put("x", new IntTag("x", x));
                                                                values.put("y", new IntTag("y", y));
                                                                values.put("z", new IntTag("z", z));
                                                                CompoundTag tileEntityTag = new CompoundTag(values);
                                                                tileEntities.add(tileEntityTag);
                                                            }
                                                        }
                                                    default:
                                                        blockData[index] = block.getData();
                                                }
                                                // For optimization reasons, we are not supporting custom data types
                                                // Especially since the most likely reason beyond  this range is modded servers in which the blocks
                                                // have NBT
                                                //                                        if (type > 255) {
                                                //                                            if (addBlocks == null) {
                                                //                                                addBlocks = new byte[(blocks.length >> 1) + 1];
                                                //                                            }
                                                //                                            addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
                                                // (addBlocks[index >> 1] & 0xF0) | ((type >> 8) & 0xF) : (addBlocks[index >> 1] & 0xF) | (((type
                                                // >> 8) & 0xF) << 4));
                                                //                                        }
                                                blocks[index] = (byte) id.getId();
                                            }
                                        }
                                    }
                                }
                                if (!chunks.isEmpty()) {
                                    TaskManager.runTaskLater(this, 1);
                                } else {
                                    regionTask.run();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public boolean restoreTile(LocalBlockQueue queue, CompoundTag ct, int x, int y, int z) {
        return new StateWrapper(ct).restoreTag(queue.getWorld(), x, y, z);
    }
}
