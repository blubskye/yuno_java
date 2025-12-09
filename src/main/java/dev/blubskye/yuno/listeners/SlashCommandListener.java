/*
 * Yuno Gasai 2 (Java Edition) - Slash Command Listener
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno.listeners;

import dev.blubskye.yuno.YunoBot;
import dev.blubskye.yuno.commands.FunCommands;
import dev.blubskye.yuno.commands.ModerationCommands;
import dev.blubskye.yuno.commands.UtilityCommands;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlashCommandListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);

    private final YunoBot bot;
    private final ModerationCommands moderationCommands;
    private final UtilityCommands utilityCommands;
    private final FunCommands funCommands;

    public SlashCommandListener(YunoBot bot) {
        this.bot = bot;
        this.moderationCommands = new ModerationCommands(bot);
        this.utilityCommands = new UtilityCommands(bot);
        this.funCommands = new FunCommands(bot);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        logger.debug("Received slash command: {} from {}", commandName, event.getUser().getName());

        switch (commandName) {
            // Utility commands
            case "ping" -> utilityCommands.handlePing(event);
            case "help" -> utilityCommands.handleHelp(event);
            case "source" -> utilityCommands.handleSource(event);
            case "prefix" -> utilityCommands.handlePrefix(event);
            case "auto-clean" -> utilityCommands.handleAutoClean(event);
            case "delay" -> utilityCommands.handleDelay(event);
            case "xp" -> utilityCommands.handleXp(event);
            case "leaderboard" -> utilityCommands.handleLeaderboard(event);

            // Moderation commands
            case "ban" -> moderationCommands.handleBan(event);
            case "kick" -> moderationCommands.handleKick(event);
            case "unban" -> moderationCommands.handleUnban(event);
            case "timeout" -> moderationCommands.handleTimeout(event);
            case "clean" -> moderationCommands.handleClean(event);
            case "mod-stats" -> moderationCommands.handleModStats(event);

            // Fun commands
            case "8ball" -> funCommands.handle8Ball(event);

            default -> logger.warn("Unknown slash command: {}", commandName);
        }
    }
}
