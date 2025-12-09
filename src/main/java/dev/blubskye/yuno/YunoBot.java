/*
 * Yuno Gasai 2 (Java Edition) - Bot Core
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno;

import dev.blubskye.yuno.commands.*;
import dev.blubskye.yuno.config.YunoConfig;
import dev.blubskye.yuno.database.YunoDatabase;
import dev.blubskye.yuno.listeners.MessageListener;
import dev.blubskye.yuno.listeners.ReadyListener;
import dev.blubskye.yuno.listeners.SlashCommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class YunoBot {
    private static final Logger logger = LoggerFactory.getLogger(YunoBot.class);

    private final YunoConfig config;
    private final YunoDatabase database;
    private JDA jda;

    public YunoBot(YunoConfig config) {
        this.config = config;
        this.database = new YunoDatabase(config.getDatabasePath());
    }

    public void start() throws Exception {
        // Initialize database
        try {
            database.open();
        } catch (SQLException e) {
            logger.error("Failed to open database: {}", e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }

        // Build JDA instance
        jda = JDABuilder.createDefault(config.getDiscordToken())
                .setActivity(Activity.watching("over you~ | /help"))
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(
                        new ReadyListener(this),
                        new MessageListener(this),
                        new SlashCommandListener(this)
                )
                .build();

        // Wait for JDA to be ready
        jda.awaitReady();

        // Register slash commands
        registerSlashCommands();

        logger.info("Yuno is online! I'm watching over your servers for you~");
    }

    private void registerSlashCommands() {
        logger.info("Registering slash commands~");

        jda.updateCommands().addCommands(
                // Utility commands
                Commands.slash("ping", "Check if Yuno is awake~"),
                Commands.slash("help", "See what Yuno can do for you~"),
                Commands.slash("source", "See Yuno's source code~"),
                Commands.slash("prefix", "Set server command prefix~")
                        .addOptions(new OptionData(OptionType.STRING, "prefix", "The new prefix (max 5 characters)", true)),

                // Moderation commands
                Commands.slash("ban", "Ban a user from the server~")
                        .addOptions(
                                new OptionData(OptionType.USER, "user", "The user to ban", true),
                                new OptionData(OptionType.STRING, "reason", "Reason for the ban", false)
                        ),
                Commands.slash("kick", "Kick a user from the server~")
                        .addOptions(
                                new OptionData(OptionType.USER, "user", "The user to kick", true),
                                new OptionData(OptionType.STRING, "reason", "Reason for the kick", false)
                        ),
                Commands.slash("unban", "Unban a user~")
                        .addOptions(
                                new OptionData(OptionType.STRING, "user_id", "The user ID to unban", true),
                                new OptionData(OptionType.STRING, "reason", "Reason for the unban", false)
                        ),
                Commands.slash("timeout", "Timeout a user~")
                        .addOptions(
                                new OptionData(OptionType.USER, "user", "The user to timeout", true),
                                new OptionData(OptionType.INTEGER, "minutes", "Duration in minutes", true),
                                new OptionData(OptionType.STRING, "reason", "Reason for the timeout", false)
                        ),
                Commands.slash("clean", "Delete messages from a channel~")
                        .addOptions(new OptionData(OptionType.INTEGER, "amount", "Number of messages to delete", false)),
                Commands.slash("mod-stats", "View moderation statistics~"),

                // Leveling commands
                Commands.slash("xp", "Check XP and level~")
                        .addOptions(new OptionData(OptionType.USER, "user", "User to check (optional)", false)),
                Commands.slash("leaderboard", "View server XP leaderboard~"),

                // Fun commands
                Commands.slash("8ball", "Ask the magic 8-ball~")
                        .addOptions(new OptionData(OptionType.STRING, "question", "Your question", true)),

                // Config commands
                Commands.slash("auto-clean", "Configure auto-clean for a channel~"),
                Commands.slash("delay", "Delay auto-clean for this channel~")
                        .addOptions(new OptionData(OptionType.INTEGER, "minutes", "Minutes to delay", false))
        ).queue(
                success -> logger.info("Successfully registered {} slash commands~", success.size()),
                error -> logger.error("Failed to register slash commands: {}", error.getMessage())
        );
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
        }
        database.close();
        logger.info("Yuno has gone to sleep... see you next time~");
    }

    public YunoConfig getConfig() {
        return config;
    }

    public YunoDatabase getDatabase() {
        return database;
    }

    public JDA getJda() {
        return jda;
    }

    public boolean isMasterUser(long userId) {
        return config.isMasterUser(userId);
    }
}
