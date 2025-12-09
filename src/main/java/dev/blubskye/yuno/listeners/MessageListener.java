/*
 * Yuno Gasai 2 (Java Edition) - Message Listener
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno.listeners;

import dev.blubskye.yuno.YunoBot;
import dev.blubskye.yuno.commands.FunCommands;
import dev.blubskye.yuno.commands.ModerationCommands;
import dev.blubskye.yuno.commands.UtilityCommands;
import dev.blubskye.yuno.database.GuildSettings;
import dev.blubskye.yuno.database.UserXp;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MessageListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    private final YunoBot bot;
    private final ModerationCommands moderationCommands;
    private final UtilityCommands utilityCommands;
    private final FunCommands funCommands;
    private final Random random;

    public MessageListener(YunoBot bot) {
        this.bot = bot;
        this.moderationCommands = new ModerationCommands(bot);
        this.utilityCommands = new UtilityCommands(bot);
        this.funCommands = new FunCommands(bot);
        this.random = new Random();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bots
        if (event.getAuthor().isBot()) return;

        // Handle DMs
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage(bot.getConfig().getDmMessage()).queue();
            return;
        }

        String content = event.getMessage().getContentRaw();
        String prefix = bot.getDatabase().getPrefix(
                event.getGuild().getIdLong(), bot.getConfig().getDefaultPrefix());

        // Check if message starts with prefix
        if (!content.startsWith(prefix)) {
            // Add XP for chatting
            handleXpGain(event);
            return;
        }

        // Parse command
        String commandContent = content.substring(prefix.length()).trim();
        if (commandContent.isEmpty()) return;

        String[] parts = commandContent.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : null;

        logger.debug("Received prefix command: {} from {}", command, event.getAuthor().getName());

        // Route to command handlers
        switch (command) {
            // Utility commands
            case "ping" -> utilityCommands.handlePingPrefix(event);
            case "help" -> utilityCommands.handleHelpPrefix(event);
            case "source" -> utilityCommands.handleSourcePrefix(event);
            case "prefix" -> utilityCommands.handlePrefixPrefix(event, args);
            case "auto-clean", "autoclean" -> utilityCommands.handleAutoCleanPrefix(event);
            case "delay" -> utilityCommands.handleDelayPrefix(event, args);
            case "xp", "level", "rank" -> utilityCommands.handleXpPrefix(event);
            case "leaderboard", "lb", "top" -> utilityCommands.handleLeaderboardPrefix(event);

            // Moderation commands
            case "ban" -> moderationCommands.handleBanPrefix(event, args);
            case "kick" -> moderationCommands.handleKickPrefix(event, args);
            case "unban" -> moderationCommands.handleUnbanPrefix(event, args);
            case "timeout" -> moderationCommands.handleTimeoutPrefix(event, args);
            case "clean" -> moderationCommands.handleCleanPrefix(event, args);
            case "mod-stats", "modstats" -> moderationCommands.handleModStatsPrefix(event);

            // Fun commands
            case "8ball" -> funCommands.handle8BallPrefix(event, args);
        }
    }

    private void handleXpGain(MessageReceivedEvent event) {
        // Check if leveling is enabled for this guild
        GuildSettings settings = bot.getDatabase().getGuildSettings(event.getGuild().getIdLong());
        if (settings != null && !settings.isLevelingEnabled()) {
            return;
        }

        long userId = event.getAuthor().getIdLong();
        long guildId = event.getGuild().getIdLong();

        // Add random XP (15-25)
        int xpGain = 15 + random.nextInt(11);
        bot.getDatabase().addXp(userId, guildId, xpGain);

        // Check for level up
        UserXp userXp = bot.getDatabase().getUserXp(userId, guildId);
        int newLevel = (int) Math.sqrt(userXp.getXp() / 100.0);

        if (newLevel > userXp.getLevel()) {
            bot.getDatabase().setLevel(userId, guildId, newLevel);

            event.getChannel().sendMessage(String.format(
                    "\u2728 **Level Up!** \u2728\nCongratulations %s! You've reached level **%d**! \uD83D\uDC95",
                    event.getAuthor().getAsMention(), newLevel
            )).queue();
        }
    }
}
