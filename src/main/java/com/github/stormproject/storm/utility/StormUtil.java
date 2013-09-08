/*
 * This file is part of Storm.
 *
 * Storm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Storm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Storm.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.github.stormproject.storm.utility;

import com.github.stormproject.storm.Storm;
import com.google.common.collect.Sets;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that provides a plethora of utility functions used in storm.
 *
 * @author Icyene, xiaomao
 */

public class StormUtil {

    /**
     * All biomes that rain can fall in.
     */
    public static final Set<Biome> rainBiomes = asSet(Biome.EXTREME_HILLS,
            Biome.FOREST, Biome.FOREST_HILLS,
            Biome.JUNGLE,
            Biome.JUNGLE_HILLS, Biome.MUSHROOM_ISLAND,
            Biome.MUSHROOM_SHORE,
            Biome.PLAINS, Biome.OCEAN, Biome.RIVER,
            Biome.SWAMPLAND, Biome.SKY,
            Biome.SMALL_MOUNTAINS);
    /**
     * All desert biomes.
     */
    public static final Set<Biome> desertBiomes = asSet(Biome.DESERT, Biome.DESERT_HILLS);
    /**
     * All forest biomes.
     */
    public static final Set<Biome> forestBiomes = asSet(Biome.FOREST, Biome.FOREST_HILLS);
    /**
     * All jungle biomes.
     */
    public static final Set<Biome> jungleBiomes = asSet(Biome.JUNGLE_HILLS, Biome.MUSHROOM_ISLAND);
    /**
     * All snow biomes.
     */
    public static final Set<Biome> snowyBiomes = asSet(Biome.FROZEN_OCEAN, Biome.FROZEN_RIVER,
            Biome.ICE_MOUNTAINS,
            Biome.ICE_PLAINS, Biome.TAIGA,
            Biome.TAIGA_HILLS);
    private static Object shield;
    private static Logger log;

    static {
        try {
            Plugin sh = Bukkit.getServer().getPluginManager().getPlugin("Shield");
            if (sh != null)
                // Don't blame me for this. Sheild doesn't have a Maven I can use, and I am too lazy
                // to use my own local repository.
                shield = Reflection.declaredMethod("getAPI").in(sh).withParameters().invoke();
            log = Logger.getLogger("storm");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Controls minecraft's rain flag in world data.
     *
     * @param world Bukkit world object
     * @param flag  whether to set it to raining or not
     */
    public static void setRainNoEvent(World world, boolean flag) {
        world.setStorm(true);
        world.setWeatherDuration(flag ? Integer.MAX_VALUE : 0);
    /*    try {
            WorldData data = ((CraftWorld) world).getHandle().worldData;
            Reflection.field("isRaining").in(data).set(flag);
            Reflection.field("rainTicks").in(data).set(flag ? Integer.MAX_VALUE : 0);
        } catch (Exception ex) {
            world.setStorm(true); //Can still set the storm
        }     */
    }

    /**
     * Controls minecraft's thunderstorm flag in world data.
     *
     * @param world Bukkit world object
     * @param flag  whether to set it to thundering or not
     */
    public static void setThunderNoEvent(World world, boolean flag) {
        world.setStorm(true);
        world.setThundering(true);
        world.setThunderDuration(flag ? Integer.MAX_VALUE : 0);
        world.setWeatherDuration(flag ? Integer.MAX_VALUE : 0);
      /*  try {
            WorldData data = ((CraftWorld) world).getHandle().worldData;
            Reflection.field("isThundering").in(data).set(flag);
            Reflection.field("thunderTicks").in(data).set(flag ? Integer.MAX_VALUE : 0);
        } catch (Exception ex) {
            world.setStorm(true); //Can still set the storm
        } */
    }

    /**
     * Logs a message generated by storm, with level INFO.
     *
     * @param logM Log message
     */
    public static void log(String logM) {
        log.log(Level.INFO, logM);
    }

    /**
     * Logs a message generated by storm.
     *
     * @param level Log level
     * @param logM  Log message
     */
    public static void log(Level level, String logM) {
        log.log(level, logM);
    }

    /**
     * Broadcasts a message to all players.
     *
     * @param message message to send
     */
    public static void broadcast(String message) {
        if (!message.isEmpty()) {
            Bukkit.getServer().broadcastMessage(parseColors(message));
        }
    }

    /**
     * Broadcasts a message to all players in a world.
     *
     * @param message message to send
     * @param world   Bukkit world object
     */
    public static void broadcast(String message, World world) {
        log("(" + world.getName().toUpperCase() + ") " + message);
        if (world != null)
            for (Player p : world.getPlayers()) {
                message(p, message);
            }
    }

    /**
     * Broadcasts a message to all players in a world.
     *
     * @param message message to send
     * @param world   world name
     */
    public static void broadcast(String message, String world) {
        World bw;
        if ((bw = Bukkit.getWorld(world)) == null)
            return;
        log(message);
        broadcast(message, bw);
    }

    /**
     * Messages a player.
     *
     * @param player  player name
     * @param message message to send
     */
    public static void message(Player player, String message) {
        player.sendMessage(parseColors(message));
    }

    /**
     * Translates the color codes in given string.
     *
     * @param msg the string to convert
     * @return the converted string
     */
    public static String parseColors(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Damages a player within range.
     *
     * @param location the centre of the range
     * @param radius   the radius where players within are damaged
     * @param damage   the amount of damage to cause
     * @param message  the message to notify the player about the cause of the damage
     */
    public static void damageNearbyPlayers(Location location, double radius, int damage, String message) {
        for (Player p : getNearbyPlayers(location, radius)) {
            damagePlayer(p, message, damage);
        }
    }

    /**
     * Damages a player within range, if the player doesn't have a permission.
     *
     * @param location   the centre of the range
     * @param radius     the radius where players within are damaged
     * @param damage     the amount of damage to cause
     * @param message    the message to notify the player about the cause of the damage
     * @param permission the permission the user must have to avoid damage
     */
    public static void damageNearbyPlayers(Location location, double radius, int damage, String message, String permission) {
        for (Player p : getNearbyPlayers(location, radius)) {
            if (!p.hasPermission(permission)) {
                damagePlayer(p, message, damage);
            }
        }
    }

    public static void playSound(Player to, Sound sound, Location loc, float volume, float data) {
        to.playSound(loc, sound, volume, data);
    }

    public static void playSound(Player to, Sound sound, float volume, float data) {
        playSound(to, sound, to.getLocation(), volume, data);
    }

    public static void playSoundNearby(Location origin, double radius, Sound sound, float volume, float data) {
        for (Player p : getNearbyPlayers(origin, radius)) {
            playSound(p, sound, origin, volume, data);
        }
    }

    /**
     * Damages a player.
     *
     * @param player  Bukkit player object
     * @param message message to send to player
     * @param damage  damage to cause on player, in hearts
     */
    public static void damagePlayer(Player player, String message, int damage) {
        if (player.getGameMode() != GameMode.CREATIVE && player.getHealth() != 0) {
            player.damage(damage);
            message(player, message);
        }
    }

    /**
     * Checks if a block is protected by WorldGuard.
     *
     * @param block Bukkit block object to check
     * @return boolean of whether it's protected
     */
    public static boolean isBlockProtected(Block block) {
        if (shield != null) {
            return Reflection.declaredMethod("isInRegion").in(shield).withReturnType(Boolean.class).withParameters(Location.class).invoke(block.getLocation());
        } else {
            return false;
        }
    }

    /**
     * Gets player within range.
     *
     * @param location the centre of the range
     * @param radius   the radius of the range
     * @return a set of Bukkit player objects
     */
    public static Set<Player> getNearbyPlayers(Location location, double radius) {
        Set<Player> playerList = new HashSet<Player>();
        World locWorld = location.getWorld();
        radius *= radius;

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getWorld().equals(locWorld)) {
                if (p.getLocation().distanceSquared(location) <= radius) {
                    playerList.add(p);
                }
            }
        }

        return playerList;
    }

    /**
     * Applies a list of transformation on a block, if the block is not protected.
     *
     * @param toTransform     the Bukkit block object to transform
     * @param transformations the list if transformations to apply
     */
    public static void transform(Block toTransform, List<List<String>> transformations) {
        if (isBlockProtected(toTransform)) {
            return;
        }

        for (List<String> toCheck : transformations) {
            ArrayList<String[]> stateIndex = new ArrayList<String[]>();

            for (int i = 0; i != 2; ++i) {
                String got = toCheck.get(i);

                if (got.contains(":")) { // Check for data _ appended.
                    stateIndex.add(got.split(":"));
                } else {
                    stateIndex.add(new String[]{got, "0"});
                }
            }

            String[] curState = stateIndex.get(0), toState = stateIndex.get(1);

            if (Integer.valueOf(curState[0]) == toTransform.getTypeId()
                    && Integer.valueOf(curState[1]) == toTransform.getData()) {
                toTransform.setTypeIdAndData(Integer.valueOf(toState[0]), Byte
                        .parseByte(toState[1]), true);
                return;
            }
        }
    }

    /**
     * Selects a random isFromFile chunk in world.
     *
     * @param world Bukkit world object
     * @return the chunk selected
     */
    public static Chunk pickChunk(World world) {
        Chunk[] loadedChunks = world.getLoadedChunks();
        if (loadedChunks.length <= 0)
            return null;
        return loadedChunks[Storm.random.nextInt(loadedChunks.length)];
    }

    /**
     * Clears the texture on a player. In reality, sets the texture to default.
     *
     * @param player Bukkit player object
     */
    public static void clearTexture(Player player) {
        String tx = Storm.wConfigs.get(player.getWorld().getName()).Textures_Default__Texture__Path;
        if (tx != null)
            player.setTexturePack(tx);
    }

    /**
     * Checks if a player is exposed to the sky, i.e. above ground.
     *
     * @param player Bukkit player object
     * @return boolean
     */
    public static boolean isPlayerUnderSky(Player player) {
        return isLocationUnderSky(player.getLocation());
    }

    /**
     * Checks if a entity is exposed to the sky, i.e. above ground.
     *
     * @param entity Bukkit entity object
     * @return boolean
     */
    public static boolean isEntityUnderSky(Entity entity) {
        return isLocationUnderSky(entity.getLocation());
    }

    /**
     * Checks if a location is exposed to sky, i.e. above ground.
     *
     * @param loc Bukkit location object
     * @return boolean
     */

    public static boolean isLocationUnderSky(Location loc) {
        return loc.getWorld().getHighestBlockYAt(loc) <= loc.getBlockY();
    }

    /**
     * Checks if the location is near a block in the list on the same layer.
     *
     * @param location the location
     * @param blocks   the collection of blocks to search
     * @param radius   the radius to search
     * @return boolean
     */
    public static boolean isLocationNearBlock(Location location, Collection<Integer> blocks, int radius) {
        World world = location.getWorld();
        int x = (int) location.getX(), y = (int) location.getY(), z = (int) location.getZ();

        for (int ox = radius; ox > -radius; ox--)
            for (int oz = radius; oz > -radius; oz--)
                if (blocks.contains(world.getBlockAt(x + ox, y, z + oz).getTypeId()))
                    return false;
        return true;
    }

    public static Location getSurface(Location loc) {
        return getSurface(loc, 15);
    }

    public static Location getSurface(Location location, int tolerance) {
        //TODO Make better
        return location.getWorld().getHighestBlockAt(location).getLocation();
    }

    /**
     * Returns a HashSet containing all the arguments passed in.
     *
     * @param objects All the objects to include.
     * @param <T>     The type of all the objects.
     * @return a Set of all the elements
     */
    public static <T> Set<T> asSet(T... objects) {
        Set<T> set = new HashSet<T>();
        Collections.addAll(set, objects);
        return set;
    }

    public static Class[] getClasses(Object... objects) {
        Class[] classes = new Class[objects.length];
        for (int i = 0; i < objects.length; ++i)
            classes[i] = objects.getClass();
        return classes;
    }

    /**
     * Can the biome rain?
     *
     * @param b biome to check
     * @return boolean
     */
    public static boolean isRainy(Biome b) {
        return rainBiomes.contains(b);
    }

    /**
     * Is the biome a forest?
     *
     * @param b biome to check
     * @return boolean
     */
    public static boolean isForest(Biome b) {
        return forestBiomes.contains(b) || jungleBiomes.contains(b);
    }

    /**
     * Is the biome a desert?
     *
     * @param b biome to check
     * @return boolean
     */
    public static boolean isDesert(Biome b) {
        return desertBiomes.contains(b);
    }

    /**
     * Is the biome covered with snow?
     *
     * @param b biome to check
     * @return boolean
     */
    public static boolean isTundra(Biome b) {
        return snowyBiomes.contains(b);
    }

    /**
     * Can the biome snow?
     *
     * @param b biome to check
     * @return boolean
     */
    public static boolean isSnowy(Biome b) {
        return isTundra(b);
    }

    public Location getSurfaceAt(Location origin) {
        Chunk chunky = origin.getChunk();
        int x = origin.getBlockX(), z = origin.getBlockZ(), surface = 0, envid = origin.getWorld().getEnvironment().getId();
        Set dimmap = Sets.newHashSet(envid == 0 ? new int[]{1, 2, 3, 7, 15, 16} : envid == -1 ? 87 : 121);
        for (int y = 0; y != 256; y++)
            if (dimmap.contains(chunky.getBlock(x, y, z).getTypeId())) surface = y;
        return chunky.getBlock(x, surface, z).getLocation();
    }
}