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
package eu.matejkormuth.pexel.network;

import net.md_5.bungee.BungeeCord;

/**
 * Class that executes {@link Proxy} functions on BungeeCord server.
 */
public class BungeeProxy implements Proxy {
    @Override
    public ProxyBrand getBrand() {
        return ProxyBrand.BUNGEE_CORD;
    }
    
    @Override
    public void connect(final PlayerInfo player, final ServerInfo target) {
        BungeeCord.getInstance()
                .getPlayer(player.uuid)
                .connect(BungeeCord.getInstance().getServerInfo(target.getName()));
    }
}
