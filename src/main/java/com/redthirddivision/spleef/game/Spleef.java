/*
 * Copyright 2015 Joey Peter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redthirddivision.spleef.game;

import com.redthirddivision.bukkitgamelib.Game;
import com.redthirddivision.bukkitgamelib.Main;
import com.redthirddivision.bukkitgamelib.Minigame;
import com.redthirddivision.bukkitgamelib.arena.PlayerData;
import com.redthirddivision.bukkitgamelib.utils.Utils.MessageType;
import com.redthirddivision.spleef.utils.BlockContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * <strong>Project:</strong> Spleef <br>
 * <strong>File:</strong> Spleef.java
 *
 * @author <a href="http://jpeter.redthirddivision.com">TheJeterLP</a>
 */
public class Spleef extends Game {

    private final Location spawn, lobby, spectator;
    private final Random r = new Random();
    private final List<BlockContainer> broken = new ArrayList<>();

    public Spleef(int id, String name, Minigame owner, ArenaState state, Location[] selection, int minplayers, int maxplayers, Location sign, String joinPermission, Location spawn, Location lobby, Location spectator) {
        super(id, name, owner, state, selection, minplayers, maxplayers, sign, joinPermission);
        this.spawn = spawn;
        this.lobby = lobby;
        this.spectator = spectator;
    }

    @Override
    public void onPlayerAddToArena(Player p) {
    }

    @Override
    public void onPlayerRemoveFromArena(Player p) {
        p.teleport(lobby);
    }

    @Override
    public void onPlayerStartSpectating(Player p) {
        p.teleport(spectator);
    }

    @Override
    public void onArenaStart() {
        for (PlayerData pd : getAlivePlayers()) {
            Location spawnPoint = this.spawn;
            spawnPoint.setX(r.nextInt(5) + spawnPoint.getBlockX());
            pd.getPlayer().teleport(spawnPoint);
            sendMessage(pd.getPlayer(), MessageType.INFO, "Start digging!");
        }
    }

    @Override
    public void onArenaStop() {
        for (PlayerData pd : getAllDatas()) {
            pd.getPlayer().teleport(lobby);
        }
        resetBlocks();
    }

    @Override
    public void onStatusChange() {
    }

    public void breakBlock(Block b) {
        if (b.getType() != Material.GRASS) return;
        broken.add(new BlockContainer(b));
        b.setType(Material.AIR);
        b.breakNaturally();
    }

    public void resetBlocks() {
        Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {

            @Override
            public void run() {
                for (BlockContainer block : broken) {
                    block.getLoc().getBlock().setType(block.getMat());
                    block.getLoc().getBlock().setData(block.getData());
                    block.getLoc().getBlock().getState().update(true);
                }
                broken.clear();
            }
        });
    }

}