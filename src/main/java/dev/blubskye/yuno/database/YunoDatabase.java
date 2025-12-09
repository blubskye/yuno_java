/*
 * Yuno Gasai 2 (Java Edition) - Database
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class YunoDatabase {
    private static final Logger logger = LoggerFactory.getLogger(YunoDatabase.class);

    private Connection connection;
    private final String databasePath;

    public YunoDatabase(String databasePath) {
        this.databasePath = databasePath;
    }

    public void open() throws SQLException {
        String url = "jdbc:sqlite:" + databasePath;
        connection = DriverManager.getConnection(url);
        logger.info("Database connection established~");
        initialize();
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed~");
            } catch (SQLException e) {
                logger.error("Error closing database: {}", e.getMessage());
            }
        }
    }

    private void initialize() throws SQLException {
        // Guild settings table
        executeUpdate("""
            CREATE TABLE IF NOT EXISTS guild_settings (
                guild_id TEXT PRIMARY KEY,
                prefix TEXT DEFAULT '.',
                spam_filter_enabled INTEGER DEFAULT 0,
                leveling_enabled INTEGER DEFAULT 1
            )
        """);

        // User XP table
        executeUpdate("""
            CREATE TABLE IF NOT EXISTS user_xp (
                user_id TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                xp INTEGER DEFAULT 0,
                level INTEGER DEFAULT 0,
                PRIMARY KEY (user_id, guild_id)
            )
        """);

        // Mod actions table
        executeUpdate("""
            CREATE TABLE IF NOT EXISTS mod_actions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                guild_id TEXT NOT NULL,
                moderator_id TEXT NOT NULL,
                target_id TEXT NOT NULL,
                action_type TEXT NOT NULL,
                reason TEXT,
                timestamp INTEGER NOT NULL
            )
        """);

        // Auto-clean config table
        executeUpdate("""
            CREATE TABLE IF NOT EXISTS auto_clean_config (
                guild_id TEXT NOT NULL,
                channel_id TEXT NOT NULL,
                interval_minutes INTEGER DEFAULT 60,
                message_count INTEGER DEFAULT 100,
                enabled INTEGER DEFAULT 1,
                PRIMARY KEY (guild_id, channel_id)
            )
        """);

        // Spam warnings table
        executeUpdate("""
            CREATE TABLE IF NOT EXISTS spam_warnings (
                user_id TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                warnings INTEGER DEFAULT 0,
                last_warning INTEGER,
                PRIMARY KEY (user_id, guild_id)
            )
        """);

        // Create indexes
        executeUpdate("CREATE INDEX IF NOT EXISTS idx_mod_actions_guild ON mod_actions(guild_id)");
        executeUpdate("CREATE INDEX IF NOT EXISTS idx_mod_actions_moderator ON mod_actions(moderator_id)");
        executeUpdate("CREATE INDEX IF NOT EXISTS idx_user_xp_guild ON user_xp(guild_id)");

        logger.info("Database initialized~");
    }

    private void executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    // Guild Settings
    public GuildSettings getGuildSettings(long guildId) {
        String sql = "SELECT prefix, spam_filter_enabled, leveling_enabled FROM guild_settings WHERE guild_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(guildId));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                GuildSettings settings = new GuildSettings();
                settings.setGuildId(guildId);
                settings.setPrefix(rs.getString("prefix"));
                settings.setSpamFilterEnabled(rs.getInt("spam_filter_enabled") == 1);
                settings.setLevelingEnabled(rs.getInt("leveling_enabled") == 1);
                return settings;
            }
        } catch (SQLException e) {
            logger.error("Error getting guild settings: {}", e.getMessage());
        }
        return null;
    }

    public void setGuildSettings(GuildSettings settings) {
        String sql = """
            INSERT OR REPLACE INTO guild_settings (guild_id, prefix, spam_filter_enabled, leveling_enabled)
            VALUES (?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(settings.getGuildId()));
            stmt.setString(2, settings.getPrefix());
            stmt.setInt(3, settings.isSpamFilterEnabled() ? 1 : 0);
            stmt.setInt(4, settings.isLevelingEnabled() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error setting guild settings: {}", e.getMessage());
        }
    }

    public String getPrefix(long guildId, String defaultPrefix) {
        GuildSettings settings = getGuildSettings(guildId);
        return settings != null ? settings.getPrefix() : defaultPrefix;
    }

    public void setPrefix(long guildId, String prefix) {
        GuildSettings settings = getGuildSettings(guildId);
        if (settings == null) {
            settings = new GuildSettings();
            settings.setGuildId(guildId);
            settings.setLevelingEnabled(true);
        }
        settings.setPrefix(prefix);
        setGuildSettings(settings);
    }

    // XP/Leveling
    public UserXp getUserXp(long userId, long guildId) {
        String sql = "SELECT xp, level FROM user_xp WHERE user_id = ? AND guild_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(userId));
            stmt.setString(2, String.valueOf(guildId));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UserXp xp = new UserXp();
                xp.setUserId(userId);
                xp.setGuildId(guildId);
                xp.setXp(rs.getLong("xp"));
                xp.setLevel(rs.getInt("level"));
                return xp;
            }
        } catch (SQLException e) {
            logger.error("Error getting user XP: {}", e.getMessage());
        }
        UserXp xp = new UserXp();
        xp.setUserId(userId);
        xp.setGuildId(guildId);
        return xp;
    }

    public void addXp(long userId, long guildId, long amount) {
        String sql = """
            INSERT INTO user_xp (user_id, guild_id, xp, level) VALUES (?, ?, ?, 0)
            ON CONFLICT(user_id, guild_id) DO UPDATE SET xp = xp + ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(userId));
            stmt.setString(2, String.valueOf(guildId));
            stmt.setLong(3, amount);
            stmt.setLong(4, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error adding XP: {}", e.getMessage());
        }
    }

    public void setLevel(long userId, long guildId, int level) {
        String sql = "UPDATE user_xp SET level = ? WHERE user_id = ? AND guild_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, level);
            stmt.setString(2, String.valueOf(userId));
            stmt.setString(3, String.valueOf(guildId));
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error setting level: {}", e.getMessage());
        }
    }

    public List<UserXp> getLeaderboard(long guildId, int limit) {
        List<UserXp> leaderboard = new ArrayList<>();
        String sql = "SELECT user_id, xp, level FROM user_xp WHERE guild_id = ? ORDER BY xp DESC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(guildId));
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserXp xp = new UserXp();
                xp.setUserId(Long.parseLong(rs.getString("user_id")));
                xp.setGuildId(guildId);
                xp.setXp(rs.getLong("xp"));
                xp.setLevel(rs.getInt("level"));
                leaderboard.add(xp);
            }
        } catch (SQLException e) {
            logger.error("Error getting leaderboard: {}", e.getMessage());
        }
        return leaderboard;
    }

    // Mod Actions
    public void logModAction(ModAction action) {
        String sql = """
            INSERT INTO mod_actions (guild_id, moderator_id, target_id, action_type, reason, timestamp)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(action.getGuildId()));
            stmt.setString(2, String.valueOf(action.getModeratorId()));
            stmt.setString(3, String.valueOf(action.getTargetId()));
            stmt.setString(4, action.getActionType());
            stmt.setString(5, action.getReason());
            stmt.setLong(6, action.getTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error logging mod action: {}", e.getMessage());
        }
    }

    public List<ModAction> getModActions(long guildId, int limit) {
        List<ModAction> actions = new ArrayList<>();
        String sql = """
            SELECT id, moderator_id, target_id, action_type, reason, timestamp
            FROM mod_actions WHERE guild_id = ? ORDER BY timestamp DESC LIMIT ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(guildId));
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ModAction action = new ModAction();
                action.setId(rs.getLong("id"));
                action.setGuildId(guildId);
                action.setModeratorId(Long.parseLong(rs.getString("moderator_id")));
                action.setTargetId(Long.parseLong(rs.getString("target_id")));
                action.setActionType(rs.getString("action_type"));
                action.setReason(rs.getString("reason"));
                action.setTimestamp(rs.getLong("timestamp"));
                actions.add(action);
            }
        } catch (SQLException e) {
            logger.error("Error getting mod actions: {}", e.getMessage());
        }
        return actions;
    }

    public ModStats getModStats(long guildId, long moderatorId) {
        ModStats stats = new ModStats();
        String sql = """
            SELECT action_type, COUNT(*) as count FROM mod_actions
            WHERE guild_id = ? AND moderator_id = ? GROUP BY action_type
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(guildId));
            stmt.setString(2, String.valueOf(moderatorId));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String type = rs.getString("action_type");
                int count = rs.getInt("count");
                switch (type) {
                    case "ban" -> stats.setBanCount(count);
                    case "kick" -> stats.setKickCount(count);
                    case "timeout" -> stats.setTimeoutCount(count);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting mod stats: {}", e.getMessage());
        }
        return stats;
    }

    // Auto-clean
    public AutoCleanConfig getAutoCleanConfig(long guildId, long channelId) {
        String sql = """
            SELECT interval_minutes, message_count, enabled
            FROM auto_clean_config WHERE guild_id = ? AND channel_id = ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(guildId));
            stmt.setString(2, String.valueOf(channelId));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                AutoCleanConfig config = new AutoCleanConfig();
                config.setGuildId(guildId);
                config.setChannelId(channelId);
                config.setIntervalMinutes(rs.getInt("interval_minutes"));
                config.setMessageCount(rs.getInt("message_count"));
                config.setEnabled(rs.getInt("enabled") == 1);
                return config;
            }
        } catch (SQLException e) {
            logger.error("Error getting auto-clean config: {}", e.getMessage());
        }
        return null;
    }

    public void setAutoCleanConfig(AutoCleanConfig config) {
        String sql = """
            INSERT OR REPLACE INTO auto_clean_config (guild_id, channel_id, interval_minutes, message_count, enabled)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(config.getGuildId()));
            stmt.setString(2, String.valueOf(config.getChannelId()));
            stmt.setInt(3, config.getIntervalMinutes());
            stmt.setInt(4, config.getMessageCount());
            stmt.setInt(5, config.isEnabled() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error setting auto-clean config: {}", e.getMessage());
        }
    }

    public void removeAutoCleanConfig(long guildId, long channelId) {
        String sql = "DELETE FROM auto_clean_config WHERE guild_id = ? AND channel_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(guildId));
            stmt.setString(2, String.valueOf(channelId));
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error removing auto-clean config: {}", e.getMessage());
        }
    }

    public List<AutoCleanConfig> getAllAutoCleanConfigs() {
        List<AutoCleanConfig> configs = new ArrayList<>();
        String sql = """
            SELECT guild_id, channel_id, interval_minutes, message_count, enabled
            FROM auto_clean_config WHERE enabled = 1
        """;
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                AutoCleanConfig config = new AutoCleanConfig();
                config.setGuildId(Long.parseLong(rs.getString("guild_id")));
                config.setChannelId(Long.parseLong(rs.getString("channel_id")));
                config.setIntervalMinutes(rs.getInt("interval_minutes"));
                config.setMessageCount(rs.getInt("message_count"));
                config.setEnabled(rs.getInt("enabled") == 1);
                configs.add(config);
            }
        } catch (SQLException e) {
            logger.error("Error getting all auto-clean configs: {}", e.getMessage());
        }
        return configs;
    }

    // Spam warnings
    public void addSpamWarning(long userId, long guildId) {
        String sql = """
            INSERT INTO spam_warnings (user_id, guild_id, warnings, last_warning) VALUES (?, ?, 1, ?)
            ON CONFLICT(user_id, guild_id) DO UPDATE SET warnings = warnings + 1, last_warning = ?
        """;
        long now = System.currentTimeMillis() / 1000;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(userId));
            stmt.setString(2, String.valueOf(guildId));
            stmt.setLong(3, now);
            stmt.setLong(4, now);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error adding spam warning: {}", e.getMessage());
        }
    }

    public int getSpamWarnings(long userId, long guildId) {
        String sql = "SELECT warnings FROM spam_warnings WHERE user_id = ? AND guild_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(userId));
            stmt.setString(2, String.valueOf(guildId));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("warnings");
            }
        } catch (SQLException e) {
            logger.error("Error getting spam warnings: {}", e.getMessage());
        }
        return 0;
    }

    public void resetSpamWarnings(long userId, long guildId) {
        String sql = "DELETE FROM spam_warnings WHERE user_id = ? AND guild_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(userId));
            stmt.setString(2, String.valueOf(guildId));
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error resetting spam warnings: {}", e.getMessage());
        }
    }
}
