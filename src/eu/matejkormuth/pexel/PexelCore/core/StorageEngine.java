// @formatter:off
/*
 * Pexel Project - Minecraft minigame server platform. 
 * Copyright (C) 2014 Matej Kormuth <http://www.matejkormuth.eu>
 * 
 * This file is part of Pexel.
 * 
 * Pexel is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Pexel is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
// @formatter:on

package eu.matejkormuth.pexel.PexelCore.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent;

import eu.matejkormuth.pexel.PexelCore.PexelCore;
import eu.matejkormuth.pexel.PexelCore.areas.AreaFlag;
import eu.matejkormuth.pexel.PexelCore.areas.Lobby;
import eu.matejkormuth.pexel.PexelCore.areas.ProtectedArea;
import eu.matejkormuth.pexel.PexelCore.arenas.AbstractArena;
import eu.matejkormuth.pexel.PexelCore.arenas.ArenaOption;
import eu.matejkormuth.pexel.PexelCore.arenas.DisconnectReason;
import eu.matejkormuth.pexel.PexelCore.arenas.MapData;
import eu.matejkormuth.pexel.PexelCore.minigame.Minigame;

/**
 * All data of plugin is stored in this class.
 * 
 * @author Mato Kormuth
 * 
 */
@SuppressWarnings("deprecation")
// SuppressWarning - Because of ArenaOption.
public class StorageEngine {
    private static final Map<UUID, PlayerProfile>   profiles    = new HashMap<UUID, PlayerProfile>();
    private static final Map<String, Minigame>      minigames   = new HashMap<String, Minigame>();
    private static final Map<String, ProtectedArea> areas       = new HashMap<String, ProtectedArea>();
    private static final Map<String, AbstractArena> arenas      = new HashMap<String, AbstractArena>();
    private static final Map<String, Class<?>>      aliases     = new HashMap<String, Class<?>>();
    private static final Map<String, Lobby>         lobbies     = new HashMap<String, Lobby>();
    private static final Map<String, TeleportGate>  gates       = new HashMap<String, TeleportGate>();
    private static boolean                          initialized = false;
    
    /**
     * Initializes static obeject of storage engine.
     * 
     * @param core
     *            pexel core
     */
    public static void initialize(final PexelCore core) {
        if (!StorageEngine.initialized)
            StorageEngine.initialized = true;
    }
    
    /**
     * Returns UUIDs of player's friends.
     * 
     * @param player
     *            player
     * @return lsit of friends
     */
    public static List<UUID> getFriends(final Player player) {
        return StorageEngine.profiles.get(player.getUniqueId()).getFriends();
    }
    
    /**
     * Returns UUIDs of player's foes.
     * 
     * @param player
     * @return
     */
    public static List<UUID> getFoes(final Player player) {
        return StorageEngine.profiles.get(player.getUniqueId()).getFoes();
    }
    
    /**
     * Returns map of areas.
     * 
     * @return
     */
    public static Map<String, ProtectedArea> getAreas() {
        return StorageEngine.areas;
    }
    
    /**
     * Returns profile of specified player.
     * 
     * @param player
     * @return
     */
    public static PlayerProfile getProfile(final UUID player) {
        return profiles.get(player);
    }
    
    /**
     * Returns minigame by its name.
     * 
     * @param name
     *            name of minigame
     * @return minigame object
     */
    public static Minigame getMinigame(final String name) {
        return StorageEngine.minigames.get(name);
    }
    
    /**
     * Registers minigame.
     * 
     * @param minigame
     */
    public static void addMinigame(final Minigame minigame) {
        StorageEngine.minigames.put(minigame.getName(), minigame);
    }
    
    /**
     * Registers mingiame's arena.
     * 
     * @param arena
     */
    public static void addArena(final AbstractArena arena) {
        StorageEngine.arenas.put(arena.getName(), arena);
        StorageEngine.areas.put(arena.getName(), arena);
    }
    
    /**
     * Returns minigame arenas count.
     * 
     * @return count of minigame arenas.
     */
    public static int getMinigameArenasCount() {
        return StorageEngine.arenas.size();
    }
    
    /**
     * Returns count of mingiame.
     * 
     * @return count of minigame
     */
    public static int getMinigamesCount() {
        return StorageEngine.minigames.size();
    }
    
    protected static Map<String, Minigame> getMinigames() {
        return StorageEngine.minigames;
    }
    
    public static Map<String, AbstractArena> getArenas() {
        return StorageEngine.arenas;
    }
    
    public static AbstractArena getArena(final String arenaName) {
        return StorageEngine.arenas.get(arenaName);
    }
    
    public static void addGate(final String name, final TeleportGate gate) {
        StorageEngine.gates.put(name, gate);
    }
    
    public static TeleportGate getGate(final String name) {
        return StorageEngine.gates.get(name);
    }
    
    public static void removeGate(final String name) {
        StorageEngine.gates.remove(name);
    }
    
    @SuppressWarnings("rawtypes")
    public static void registerArenaAlias(final Class arenaClass, final String alias) {
        StorageEngine.aliases.put(alias, arenaClass);
    }
    
    @SuppressWarnings("rawtypes")
    public static Class getByAlias(final String alias) {
        return StorageEngine.aliases.get(alias);
    }
    
    public static Map<String, Class<?>> getAliases() {
        return StorageEngine.aliases;
    }
    
    public static void addLobby(final Lobby lobby) {
        StorageEngine.lobbies.put(lobby.getName(), lobby);
        StorageEngine.areas.put(lobby.getName(), lobby);
    }
    
    public static Lobby getLobby(final String lobbyName) {
        return StorageEngine.lobbies.get(lobbyName);
    }
    
    /**
     * Saves player's profile to file.
     * 
     * @param uniqueId
     */
    public static void saveProfile(final UUID uniqueId) {
        Log.info("Saving profile for " + uniqueId.toString() + " to disk...");
        StorageEngine.profiles.get(uniqueId).save(Paths.playerProfile(uniqueId));
    }
    
    /**
     * Loads player profile from disk or creates an empty one.
     * 
     * @param uniqueId
     */
    public static void loadProfile(final UUID uniqueId) {
        File f = new File(Paths.playerProfile(uniqueId));
        if (f.exists()) {
            Log.info("Load profile for " + uniqueId + "...");
            StorageEngine.profiles.put(uniqueId,
                    PlayerProfile.load(Paths.playerProfile(uniqueId)));
        }
        else {
            Log.info("Creating new profile for " + uniqueId.toString());
            StorageEngine.profiles.put(uniqueId, new PlayerProfile(uniqueId));
        }
    }
    
    /**
     * @deprecated deprecated and WILL be removed in future. Use {@link MapData}.
     */
    @Deprecated
    public static void saveData() {
        Log.info("Saving data...");
        // Save lobbies.
        YamlConfiguration yaml_lobbies = new YamlConfiguration();
        int i_lobbies = 0;
        for (Lobby l : StorageEngine.lobbies.values()) {
            yaml_lobbies.set("lobbies.lobby" + i_lobbies + ".name", l.getName());
            yaml_lobbies.set("lobbies.lobby" + i_lobbies + ".checkinterval",
                    l.getCheckInterval());
            
            // Save global flags
            for (AreaFlag flag : AreaFlag.values())
                if (l.getGlobalFlag(flag) != ProtectedArea.defaultFlags.get(flag))
                    yaml_lobbies.set(
                            "lobbies.lobby" + i_lobbies + ".gflags." + flag.toString(),
                            l.getGlobalFlag(flag));
            
            yaml_lobbies.set("lobbies.lobby" + i_lobbies + ".thresholdY",
                    l.getThresholdY());
            l.getRegion().serialize(yaml_lobbies,
                    "lobbies.lobby" + i_lobbies + ".region");
            i_lobbies++;
        }
        try {
            yaml_lobbies.save(new File(Paths.lobbiesPath()));
            Log.info("Saved " + i_lobbies + " lobbies!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Save arenas
        YamlConfiguration yaml_arenas = new YamlConfiguration();
        int i_arenas = 0;
        for (AbstractArena a : StorageEngine.arenas.values()) {
            yaml_arenas.set("arenas.arena" + i_arenas + ".name", a.getName());
            yaml_arenas.set("arenas.arena" + i_arenas + ".type",
                    a.getClass().getSimpleName());
            yaml_arenas.set("arenas.arena" + i_arenas + ".minigame",
                    a.getMinigame().getName());
            yaml_arenas.set("arenas.arena" + i_arenas + ".slots", a.getMaximumSlots());
            // Get options
            for (Field f : a.getClass().getDeclaredFields())
                if (f.isAnnotationPresent(ArenaOption.class)) {
                    if (!f.isAccessible())
                        f.setAccessible(true);
                    
                    try {
                        if (f.get(a) != null) {
                            yaml_arenas.set(
                                    "arenas.arena" + i_arenas + ".options."
                                            + f.getName(), f.get(a).toString());
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        System.out.println("Error while saving arena " + a.getName()
                                + "!");
                        e.printStackTrace();
                    }
                }
            
            // Save global flags
            for (AreaFlag flag : AreaFlag.values())
                if (a.getGlobalFlag(flag) != ProtectedArea.defaultFlags.get(flag))
                    yaml_arenas.set(
                            "arenas.arena" + i_arenas + ".gflags." + flag.toString(),
                            a.getGlobalFlag(flag));
            
            yaml_arenas.set("arenas.arena" + i_arenas + ".owner", a.getOwner());
            if (a.getRegion() != null)
                a.getRegion().serialize(yaml_arenas,
                        "arenas.arena" + i_arenas + ".region");
            i_arenas++;
        }
        try {
            yaml_arenas.save(new File(Paths.arenasPath()));
            Log.info("Saved " + i_arenas + " arenas!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Save gates
        YamlConfiguration yaml_gates = new YamlConfiguration();
        int i_gates = 0;
        for (String key : StorageEngine.gates.keySet()) {
            TeleportGate tg = StorageEngine.gates.get(key);
            yaml_gates.set("gates.gate" + i_gates + ".name", key);
            yaml_gates.set("gates.gate" + i_gates + ".action.type",
                    tg.getAction().getClass().getSimpleName());
            if (tg.getRegion() != null)
                tg.getRegion().serialize(yaml_gates, "gates.gate" + i_gates + ".region");
            i_gates++;
        }
        try {
            yaml_gates.save(new File(Paths.gatesPath()));
            Log.info("Saved " + i_gates + " gates!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void loadData() {
        Log.info("Loading data...");
        
    }
    
    public static void gateEnter(final Player player, final Location location) {
        // Find the right gate
        for (TeleportGate gate : StorageEngine.gates.values())
            if (gate.getRegion().intersects(location))
                gate.teleport(player);
    }
    
    public static void saveArenas() {
        for (AbstractArena arena : StorageEngine.arenas.values()) {
            // Will be removed.
            arena.save(Paths.arenaPath(arena.getBannableName()));
        }
    }
    
    public static void saveProfiles() {
        for (PlayerProfile profile : StorageEngine.profiles.values()) {
            profile.saveXML(Paths.profilePath(profile.getUniqueId().toString()));
        }
    }
    
    public static void __redirectEvent(final String string, final Event event) {
        if (string.equalsIgnoreCase("PlayerQuitEvent")) {
            PlayerQuitEvent quitevent = (PlayerQuitEvent) event;
            for (AbstractArena arena : StorageEngine.arenas.values()) {
                if (arena.contains(quitevent.getPlayer())) {
                    arena.onPlayerLeft(quitevent.getPlayer(),
                            DisconnectReason.PLAYER_DISCONNECT);
                }
            }
        }
    }
}
