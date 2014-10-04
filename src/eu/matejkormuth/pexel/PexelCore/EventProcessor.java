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
package eu.matejkormuth.pexel.PexelCore;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import eu.matejkormuth.pexel.PexelCore.areas.AreaFlag;
import eu.matejkormuth.pexel.PexelCore.areas.Areas;
import eu.matejkormuth.pexel.PexelCore.areas.ProtectedArea;
import eu.matejkormuth.pexel.PexelCore.bans.BanUtils;
import eu.matejkormuth.pexel.PexelCore.chat.ChatManager;
import eu.matejkormuth.pexel.PexelCore.chat.SubscribeMode;
import eu.matejkormuth.pexel.PexelCore.core.Log;
import eu.matejkormuth.pexel.PexelCore.core.StorageEngine;
import eu.matejkormuth.pexel.PexelCore.menu.InventoryMenu;
import eu.matejkormuth.pexel.PexelCore.particles.EntityAnimationPlayer;
import eu.matejkormuth.pexel.PexelCore.particles.ParticleAnimation;
import eu.matejkormuth.pexel.PexelCore.particles.ParticleFrame;
import eu.matejkormuth.pexel.PexelCore.util.Lang;
import eu.matejkormuth.pexel.PexelCore.util.ParticleEffect2;
import eu.matejkormuth.pexel.PexelNetworking.Server;

/**
 * Event processor for pexel.
 * 
 * @author Mato Kormuth
 * 
 */
public class EventProcessor implements Listener {
    public EventProcessor() {
        Bukkit.getPluginManager().registerEvents(this, Pexel.getCore());
    }
    
    @EventHandler
    private void onPlayerMove(final PlayerMoveEvent event) {
        // FIXME: Temporarly removed.
        
        if (event.getPlayer().isSprinting()) {
            if (StorageEngine.getProfile(event.getPlayer().getUniqueId()).getParticleType() != null) {
                for (double i = 0; i < 1.5; i += 0.20D) {
                    Location diff = event.getTo().subtract(event.getFrom()).multiply(
                            1.2F);
                    StorageEngine.getProfile(event.getPlayer().getUniqueId()).getParticleType().display(
                            event.getFrom().subtract(diff).clone().add(0, i, 0), 0.50F,
                            0.20F, 0.50F, 1, 8);
                }
            }
        }
        
    }
    
    @EventHandler
    private void onBlockBreak(final BlockBreakEvent event) {
        if (!this.hasPermission(event.getBlock().getLocation(), event.getPlayer(),
                AreaFlag.BLOCK_BREAK))
            event.setCancelled(true);
    }
    
    @EventHandler
    private void onPlayerRespawn(final PlayerRespawnEvent event) {
        event.getPlayer().teleport(Pexel.getHubLocation());
    }
    
    @EventHandler
    private void onBlockPlace(final BlockPlaceEvent event) {
        if (!this.hasPermission(event.getBlock().getLocation(), event.getPlayer(),
                AreaFlag.BLOCK_PLACE))
            event.setCancelled(true);
    }
    
    @EventHandler
    private void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (!this.hasPermission(event.getPlayer().getLocation(), event.getPlayer(),
                AreaFlag.PLAYER_DROPITEM))
            event.setCancelled(true);
    }
    
    @EventHandler
    private void onPlayerDamageByEntity(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player)
            if (!this.hasPermission(event.getEntity().getLocation(),
                    (Player) event.getEntity(), AreaFlag.PLAYER_GETDAMAGE))
                event.setCancelled(true);
        
        if (event.getDamager() instanceof Player)
            if (!this.hasPermission(event.getDamager().getLocation(),
                    (Player) event.getDamager(), AreaFlag.PLAYER_DODAMAGE))
                event.setCancelled(true);
    }
    
    @EventHandler
    private void onPlayerDamageByBlock(final EntityDamageByBlockEvent event) {
        if (event.getEntity() instanceof Player)
            if (!this.hasPermission(event.getEntity().getLocation(),
                    (Player) event.getEntity(), AreaFlag.PLAYER_GETDAMAGE))
                event.setCancelled(true);
    }
    
    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.SIGN
                    || event.getClickedBlock().getType() == Material.SIGN_POST) {
                if (event.getPlayer().isOp()
                        && event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
                        || !event.getPlayer().isOp()) {
                    Sign sign = (Sign) event.getClickedBlock().getState();
                    String[] lines = sign.getLines();
                    if (lines.length > 1) {
                        String command = lines[0].trim();
                        if (command.equalsIgnoreCase("[Server]")) {
                            ByteArrayDataOutput out = ByteStreams.newDataOutput();
                            out.writeUTF("Connect");
                            out.writeUTF(lines[1]);
                            event.getPlayer().sendPluginMessage(Pexel.getCore(),
                                    "BungeeCord", out.toByteArray());
                        }
                        else if (command.equalsIgnoreCase("[Warp]")) {
                            event.getPlayer().performCommand("warp " + lines[1]);
                        }
                        else if (command.equalsIgnoreCase("[Matchmaking]")) {
                            Pexel.getMatchmaking().processSign(lines, event.getPlayer());
                        }
                        else if (command.equalsIgnoreCase("[World]")) {
                            World w = Bukkit.getWorld(lines[1]);
                            if (w == null)
                                event.getPlayer().sendMessage(
                                        ChatManager.error(Lang.getTranslation("worldnotfound")));
                            else
                                event.getPlayer().teleport(w.getSpawnLocation());
                        }
                    }
                }
            }
            
            if (event.getItem() != null) {
                if (event.getItem().hasItemMeta()
                        && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(
                                "gun")) {
                    Vector direction = event.getPlayer().getEyeLocation().getDirection();
                    Snowball projectile = (Snowball) event.getPlayer().getWorld().spawnEntity(
                            event.getPlayer().getEyeLocation().add(direction.multiply(2)),
                            EntityType.SNOWBALL);
                    projectile.setVelocity(direction.multiply(2));
                    for (int i = 0; i < 25; i++) {
                        Firework fw = (Firework) event.getPlayer().getWorld().spawnEntity(
                                direction.add(direction).toLocation(
                                        event.getPlayer().getWorld()),
                                EntityType.FIREWORK);
                        fw.setTicksLived(2);
                    }
                }
            }
        }
    }
    
    @EventHandler
    private void onPlayerPortal(final PlayerPortalEvent event) {
        // Pass the event further...
        StorageEngine.gateEnter(event.getPlayer(), event.getPlayer().getLocation());
    }
    
    @EventHandler
    private void onChat(final AsyncPlayerChatEvent event) {
        ChatManager.__processChatEvent(event);
        /*
         * if (event.getPlayer().isOp()) event.setFormat(ChatManager.chatPlayerOp(event.getMessage(),
         * event.getPlayer())); else event.setFormat(ChatManager.chatPlayer(event.getMessage(), event.getPlayer()));
         */
    }
    
    @EventHandler
    private void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof InventoryMenu) {
            if (event.getWhoClicked() instanceof Player) {
                ((InventoryMenu) event.getInventory().getHolder()).inventoryClick(
                        (Player) event.getWhoClicked(), event.getSlot());
                event.setCancelled(true);
                if (((InventoryMenu) event.getInventory().getHolder()).shouldClose(event.getSlot())) {
                    event.getView().close();
                }
            }
        }
    }
    
    @EventHandler
    private void onPlayerLogin(final PlayerLoginEvent event) {
        // Check for ban
        if (Pexel.getBans().isBanned(event.getPlayer(), Server.THIS_SERVER)) {
            event.disallow(
                    Result.KICK_BANNED,
                    BanUtils.formatBannedMessage(Pexel.getBans().getBan(
                            event.getPlayer(), Server.THIS_SERVER)));
        }
        
        if (event.getHostname().contains("login"))
            Pexel.getAuth().authenticateIp(event.getPlayer(), event.getHostname());
        
        if (event.getPlayer().getName().equalsIgnoreCase("dobrakmato")) {
            ParticleAnimation animation = new ParticleAnimation();
            double x = 0;
            double y = 0;
            for (int i = 0; i < 20; i++) {
                x = Math.sin(i / 3.14F);
                y = Math.cos(i / 3.14F);
                Log.info("Generated frame X:" + x + ", Y:" + y);
                animation.addFrame(new ParticleFrame(
                        Arrays.asList(new ParticleFrame.Particle(x, 2.5, y,
                                ParticleEffect2.HEART))));
            }
            
            EntityAnimationPlayer player = new EntityAnimationPlayer(animation,
                    event.getPlayer(), true);
            player.play();
        }
    }
    
    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        // Load profile to memory or create empty profile.
        StorageEngine.loadProfile(event.getPlayer().getUniqueId());
        // Register chat channels.
        ChatManager.CHANNEL_GLOBAL.subscribe(event.getPlayer(), SubscribeMode.READ);
        ChatManager.CHANNEL_LOBBY.subscribe(event.getPlayer(), SubscribeMode.READ_WRITE);
    }
    
    @EventHandler
    private void onPlayerLeave(final PlayerQuitEvent event) {
        //Leave party
        if (StorageEngine.getProfile(event.getPlayer().getUniqueId()).getParty() != null) {
            StorageEngine.getProfile(event.getPlayer().getUniqueId()).getParty().removePlayer(
                    event.getPlayer());
            StorageEngine.getProfile(event.getPlayer().getUniqueId()).setParty(null);
        }
        
        // Force save of player's profile.
        StorageEngine.saveProfile(event.getPlayer().getUniqueId());
    }
    
    private boolean hasPermission(final Location location, final Player player,
            final AreaFlag flag) {
        ProtectedArea area = Areas.findArea(location);
        if (area != null) {
            if (!area.getPlayerFlag(flag, player.getUniqueId())) {
                // if (area.getPlayerFlag(AreaFlag.AREA_CHAT_PERMISSIONDENIED,
                // player.getUniqueId()))
                player.getPlayer().sendMessage(
                        ChatManager.error("You don't have permission for '"
                                + flag.toString() + "' in this area!"));
                return false;
            }
            return true;
        }
        return true;
    }
}
