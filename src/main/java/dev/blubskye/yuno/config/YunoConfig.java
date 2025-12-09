/*
 * Yuno Gasai 2 (Java Edition) - Configuration
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YunoConfig {
    private static final Logger logger = LoggerFactory.getLogger(YunoConfig.class);

    @SerializedName("discord_token")
    private String discordToken;

    @SerializedName("default_prefix")
    private String defaultPrefix = ".";

    @SerializedName("database_path")
    private String databasePath = "yuno.db";

    @SerializedName("master_users")
    private List<String> masterUsers = new ArrayList<>();

    @SerializedName("spam_max_warnings")
    private int spamMaxWarnings = 3;

    @SerializedName("ban_default_image")
    private String banDefaultImage;

    @SerializedName("dm_message")
    private String dmMessage = "I'm just a bot :'(. I can't answer to you.";

    @SerializedName("insufficient_permissions_message")
    private String insufficientPermissionsMessage = "${author} You don't have permission to do that~";

    public static YunoConfig loadFromFile(String path) {
        try (FileReader reader = new FileReader(path)) {
            Gson gson = new Gson();
            YunoConfig config = gson.fromJson(reader, YunoConfig.class);
            if (config == null) {
                config = new YunoConfig();
            }
            config.applyDefaults();
            return config;
        } catch (IOException e) {
            logger.error("Failed to load config from file: {}", e.getMessage());
            return null;
        }
    }

    public static YunoConfig loadFromEnv() {
        YunoConfig config = new YunoConfig();

        String token = System.getenv("DISCORD_TOKEN");
        if (token != null && !token.isEmpty()) {
            config.discordToken = token;
        }

        String prefix = System.getenv("DEFAULT_PREFIX");
        if (prefix != null && !prefix.isEmpty()) {
            config.defaultPrefix = prefix;
        }

        String dbPath = System.getenv("DATABASE_PATH");
        if (dbPath != null && !dbPath.isEmpty()) {
            config.databasePath = dbPath;
        }

        String spamWarnings = System.getenv("SPAM_MAX_WARNINGS");
        if (spamWarnings != null && !spamWarnings.isEmpty()) {
            try {
                config.spamMaxWarnings = Integer.parseInt(spamWarnings);
            } catch (NumberFormatException ignored) {
            }
        }

        String masterUser = System.getenv("MASTER_USER");
        if (masterUser != null && !masterUser.isEmpty()) {
            config.masterUsers.add(masterUser);
        }

        String dmMsg = System.getenv("DM_MESSAGE");
        if (dmMsg != null && !dmMsg.isEmpty()) {
            config.dmMessage = dmMsg;
        }

        config.applyDefaults();
        return config;
    }

    private void applyDefaults() {
        if (defaultPrefix == null || defaultPrefix.isEmpty()) {
            defaultPrefix = ".";
        }
        if (databasePath == null || databasePath.isEmpty()) {
            databasePath = "yuno.db";
        }
        if (dmMessage == null || dmMessage.isEmpty()) {
            dmMessage = "I'm just a bot :'(. I can't answer to you.";
        }
        if (insufficientPermissionsMessage == null || insufficientPermissionsMessage.isEmpty()) {
            insufficientPermissionsMessage = "${author} You don't have permission to do that~";
        }
        if (masterUsers == null) {
            masterUsers = new ArrayList<>();
        }
    }

    public boolean isMasterUser(String userId) {
        return masterUsers.contains(userId);
    }

    public boolean isMasterUser(long userId) {
        return isMasterUser(String.valueOf(userId));
    }

    // Getters
    public String getDiscordToken() {
        return discordToken;
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public List<String> getMasterUsers() {
        return masterUsers;
    }

    public int getSpamMaxWarnings() {
        return spamMaxWarnings;
    }

    public String getBanDefaultImage() {
        return banDefaultImage;
    }

    public String getDmMessage() {
        return dmMessage;
    }

    public String getInsufficientPermissionsMessage() {
        return insufficientPermissionsMessage;
    }

    public String formatInsufficientPermissionsMessage(String authorMention) {
        return insufficientPermissionsMessage.replace("${author}", authorMention);
    }
}
