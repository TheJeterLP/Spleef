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
package com.redthirddivision.spleef.utils;

import com.redthirddivision.bukkitgamelib.Game.ArenaState;
import com.redthirddivision.bukkitgamelib.database.MySQL;
import com.redthirddivision.bukkitgamelib.database.SQLite;
import com.redthirddivision.bukkitgamelib.utils.Utils;
import com.redthirddivision.spleef.Main;
import com.redthirddivision.spleef.game.Spleef;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.Location;

/**
 * <strong>Project:</strong> Spleef <br>
 * <strong>File:</strong> DBHandler.java
 *
 * @author <a href="http://jpeter.redthirddivision.com">TheJeterLP</a>
 */
public class DBHandler {
    
    public static void setup() {
        try {
            if (Main.getDB() instanceof MySQL) {
                
                Main.getDB().executeStatement("CREATE TABLE IF NOT EXISTS `" + Config.SQL_TABLE_PREFIX.getString() + "arenas` ("
                        + "`ID` INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, "
                        + "`name` varchar(64) NOT NULL, "
                        + "`state` varchar(64) NOT NULL DEFAULT '" + ArenaState.WAITING.toString() + "', "
                        + "`min` varchar(128) NOT NULL, "
                        + "`max` varchar(128) NOT NULL, "
                        + "`minplayers` INTEGER NOT NULL, "
                        + "`maxplayers` INTEGER NOT NULL, "
                        + "`sign` varchar(128) NOT NULL, "
                        + "`spawnpoint` varchar(128) NOT NULL, "
                        + "`lobbypoint` varchar(128) NOT NULL, "
                        + "`spectatorpoint` varchar(128) NOT NULL, "
                        + "UNIQUE KEY `name` (`name`)"
                        + ");");
            } else if (Main.getDB() instanceof SQLite) {
                Main.getDB().executeStatement("CREATE TABLE IF NOT EXISTS `" + Config.SQL_TABLE_PREFIX.getString() + "arenas` ("
                        + "`ID` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                        + "`name` varchar(64) NOT NULL UNIQUE, "
                        + "`state` varchar(64) NOT NULL DEFAULT '" + ArenaState.WAITING.toString() + "', "
                        + "`min` varchar(128) NOT NULL, "
                        + "`max` varchar(128) NOT NULL, "
                        + "`minplayers` INTEGER NOT NULL, "
                        + "`maxplayers` INTEGER NOT NULL, "
                        + "`sign` varchar(128) NOT NULL, "
                        + "`spawnpoint` varchar(128) NOT NULL, "
                        + "`lobbypoint` varchar(128) NOT NULL, "
                        + "`spectatorpoint` varchar(128) NOT NULL"
                        + ");");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public static Spleef loadGame(int id) {
        try {
            PreparedStatement st = Main.getDB().getPreparedStatement("SELECT * FROM `" + Config.SQL_TABLE_PREFIX.getString() + "arenas` WHERE `id` = ? LIMIT 1;");
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                ArenaState state = ArenaState.valueOf(rs.getString("state").toUpperCase());
                Location min = Utils.deserialLocation(rs.getString("min"));
                Location max = Utils.deserialLocation(rs.getString("max"));
                int minplayers = rs.getInt("minplayers");
                int maxplayers = rs.getInt("maxplayers");
                Location sign = Utils.deserialLocation(rs.getString("sign"));
                Location spawn = Utils.deserialLocation(rs.getString("spawnpoint"));
                Location lobby = Utils.deserialLocation(rs.getString("lobbypoint"));
                Location spectator = Utils.deserialLocation(rs.getString("spectatorpoint"));
                Main.getDB().closeStatement(st);
                Main.getDB().closeResultSet(rs);
                
                Spleef game = new Spleef(id, name, Main.getInstance(), state, new Location[]{min, max}, minplayers, maxplayers, sign, "spleef.join." + name, spawn, lobby, spectator);
                return game;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static void loadArenasFromDB() {
        try {
            Statement s = Main.getDB().getStatement();
            ResultSet rs = s.executeQuery("SELECT `ID` FROM `" + Config.SQL_TABLE_PREFIX.getString() + "arenas`;");
            while (rs.next()) {
                loadGame(rs.getInt("ID"));
            }
            Main.getDB().closeStatement(s);
            Main.getDB().closeResultSet(rs);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void createArena() {
        try {
            int id = -1;
            PreparedStatement st = Main.getDB().getConnection().prepareStatement("INSERT INTO `" + Config.SQL_TABLE_PREFIX.getString() + "arenas` (`name`, `min`, `max`, `minplayers`, `maxplayers`, `sign`, `spawnpoint`, `lobbypoint`, `spectatorpoint`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
            st.setString(1, ArenaStore.name);
            st.setString(2, Utils.serialLocation(ArenaStore.sel[0]));
            st.setString(3, Utils.serialLocation(ArenaStore.sel[1]));
            st.setInt(4, ArenaStore.minplayers);
            st.setInt(5, ArenaStore.maxplayers);
            st.setString(6, Utils.serialLocation(ArenaStore.sign.getLocation()));
            st.setString(7, Utils.serialLocation(ArenaStore.spawnPoint));
            st.setString(8, Utils.serialLocation(ArenaStore.lobbyPoint));
            st.setString(9, Utils.serialLocation(ArenaStore.spectatorPoint));
            st.executeUpdate();
            
            int affectedRows = st.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating table failed, no rows affected.");
            }
            
            ResultSet generatedKeys = st.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
            } else {
                String sql = Config.USE_MYSQL.getBoolean() ? "MySQL" : "SQLite";
                throw new SQLException("Creating user failed, no ID obtained. SQL type: " + sql);
            }
            
            Main.getDB().closeStatement(st);
            loadGame(id);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
}
