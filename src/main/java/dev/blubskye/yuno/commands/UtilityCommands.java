/*
 * Yuno Gasai 2 (Java Edition) - Utility Commands
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno.commands;

import dev.blubskye.yuno.YunoBot;
import dev.blubskye.yuno.database.GuildSettings;
import dev.blubskye.yuno.database.UserXp;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class UtilityCommands {
    private final YunoBot bot;

    public UtilityCommands(YunoBot bot) {
        this.bot = bot;
    }

    // Slash Commands

    public void handlePing(SlashCommandInteractionEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        event.reply(String.format(
                "\uD83D\uDC93 **Pong!**\nI'm always here for you~ \uD83D\uDC95\n\n**Latency:** %dms",
                gatewayPing
        )).queue();
    }

    public void handleHelp(SlashCommandInteractionEvent event) {
        event.reply("""
                \uD83D\uDC95 **Yuno's Commands** \uD83D\uDC95
                *"Let me show you everything I can do for you~"* \uD83D\uDC97

                **\uD83D\uDD2A Moderation**
                `/ban` - Ban a user
                `/kick` - Kick a user
                `/unban` - Unban a user
                `/timeout` - Timeout a user
                `/clean` - Delete messages
                `/mod-stats` - View moderation stats

                **\u2699\uFE0F Utility**
                `/ping` - Check latency
                `/prefix` - Set server prefix
                `/auto-clean` - Configure auto-clean
                `/delay` - Delay auto-clean
                `/source` - View source code
                `/help` - This menu

                **\u2728 Leveling**
                `/xp` - Check XP and level
                `/leaderboard` - Server rankings

                **\uD83C\uDFB1 Fun**
                `/8ball` - Ask the magic 8-ball

                \uD83D\uDC95 *Yuno is always watching over you~* \uD83D\uDC95
                """).queue();
    }

    public void handleSource(SlashCommandInteractionEvent event) {
        event.reply("""
                \uD83D\uDCDC **Source Code**
                *"I have nothing to hide from you~"* \uD83D\uDC95

                **Java Version**: https://github.com/blubskye/yuno_java
                **C Version**: https://github.com/blubskye/yuno_c
                **C++ Version**: https://github.com/blubskye/yuno_cpp
                **Rust Version**: https://github.com/blubskye/yuno_rust
                **Original JS**: https://github.com/japaneseenrichmentorganization/Yuno-Gasai-2

                Licensed under **AGPL-3.0** \uD83D\uDC97
                """).queue();
    }

    public void handlePrefix(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply(bot.getConfig().formatInsufficientPermissionsMessage(event.getUser().getAsMention()))
                    .setEphemeral(true).queue();
            return;
        }

        String newPrefix = event.getOption("prefix").getAsString();

        if (newPrefix.length() > 5) {
            event.reply("\uD83D\uDC94 Prefix too long! Max 5 characters~").setEphemeral(true).queue();
            return;
        }

        bot.getDatabase().setPrefix(event.getGuild().getIdLong(), newPrefix);

        event.reply(String.format(
                "\uD83D\uDD27 **Prefix Updated!**\nNew prefix is now: `%s` \uD83D\uDC95",
                newPrefix
        )).queue();
    }

    public void handleAutoClean(SlashCommandInteractionEvent event) {
        event.reply("\uD83E\uDDF9 Auto-clean configuration~ \uD83D\uDC95").queue();
    }

    public void handleDelay(SlashCommandInteractionEvent event) {
        int minutes = event.getOption("minutes") != null ?
                (int) event.getOption("minutes").getAsLong() : 5;

        event.reply(String.format(
                "\u23F3 **Delay Requested**\nI'll wait %d more minutes before cleaning~ \uD83D\uDC95",
                minutes
        )).queue();
    }

    public void handleXp(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user") != null ?
                event.getOption("user").getAsUser() : event.getUser();

        UserXp userXp = bot.getDatabase().getUserXp(targetUser.getIdLong(), event.getGuild().getIdLong());

        int nextLevel = userXp.getLevel() + 1;
        long xpForNext = (long) nextLevel * nextLevel * 100;
        int progress = (int) ((userXp.getXp() * 100) / Math.max(xpForNext, 1));

        event.reply(String.format(
                "\u2728 **XP Stats**\n%s's progress~ \uD83D\uDC95\n\n" +
                        "**Level:** %d\n" +
                        "**XP:** %d\n" +
                        "**Progress to Next:** %d%%",
                targetUser.getAsMention(), userXp.getLevel(), userXp.getXp(), progress
        )).queue();
    }

    public void handleLeaderboard(SlashCommandInteractionEvent event) {
        List<UserXp> topUsers = bot.getDatabase().getLeaderboard(event.getGuild().getIdLong(), 10);

        StringBuilder sb = new StringBuilder();
        sb.append("\uD83C\uDFC6 **Server Leaderboard**\n*\"Look who's been the most active~\"* \uD83D\uDC95\n\n");

        if (topUsers.isEmpty()) {
            sb.append("No one has earned XP yet~");
        } else {
            for (int i = 0; i < topUsers.size(); i++) {
                UserXp user = topUsers.get(i);
                String medal = switch (i) {
                    case 0 -> "\uD83E\uDD47";
                    case 1 -> "\uD83E\uDD48";
                    case 2 -> "\uD83E\uDD49";
                    default -> "";
                };
                sb.append(String.format("%s %d. <@%d> - Level %d (%d XP)\n",
                        medal, i + 1, user.getUserId(), user.getLevel(), user.getXp()));
            }
        }

        event.reply(sb.toString()).queue();
    }

    // Prefix Commands

    public void handlePingPrefix(MessageReceivedEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        event.getChannel().sendMessage(String.format(
                "\uD83D\uDC93 **Pong!**\nI'm always here for you~ \uD83D\uDC95\n\n**Latency:** %dms",
                gatewayPing
        )).queue();
    }

    public void handleHelpPrefix(MessageReceivedEvent event) {
        String prefix = bot.getDatabase().getPrefix(event.getGuild().getIdLong(), bot.getConfig().getDefaultPrefix());

        event.getChannel().sendMessage(String.format("""
                \uD83D\uDC95 **Yuno's Commands** \uD83D\uDC95
                *"Let me show you everything I can do for you~"* \uD83D\uDC97
                Prefix: `%s`

                **\uD83D\uDD2A Moderation**
                `ban` - Ban a user
                `kick` - Kick a user
                `unban` - Unban a user
                `timeout` - Timeout a user
                `clean` - Delete messages
                `mod-stats` - View moderation stats

                **\u2699\uFE0F Utility**
                `ping` - Check latency
                `prefix` - Set server prefix
                `delay` - Delay auto-clean
                `source` - View source code
                `help` - This menu

                **\u2728 Leveling**
                `xp` - Check XP and level
                `leaderboard` - Server rankings

                **\uD83C\uDFB1 Fun**
                `8ball` - Ask the magic 8-ball

                \uD83D\uDC95 *Yuno is always watching over you~* \uD83D\uDC95
                """, prefix)).queue();
    }

    public void handleSourcePrefix(MessageReceivedEvent event) {
        event.getChannel().sendMessage("""
                \uD83D\uDCDC **Source Code**
                *"I have nothing to hide from you~"* \uD83D\uDC95

                **Java Version**: https://github.com/blubskye/yuno_java
                **C Version**: https://github.com/blubskye/yuno_c
                **C++ Version**: https://github.com/blubskye/yuno_cpp
                **Rust Version**: https://github.com/blubskye/yuno_rust
                **Original JS**: https://github.com/japaneseenrichmentorganization/Yuno-Gasai-2

                Licensed under **AGPL-3.0** \uD83D\uDC97
                """).queue();
    }

    public void handlePrefixPrefix(MessageReceivedEvent event, String args) {
        if (args == null || args.isEmpty()) {
            String currentPrefix = bot.getDatabase().getPrefix(
                    event.getGuild().getIdLong(), bot.getConfig().getDefaultPrefix());
            event.getChannel().sendMessage(String.format("\uD83D\uDC95 Current prefix: `%s`", currentPrefix)).queue();
            return;
        }

        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.getChannel().sendMessage(
                    bot.getConfig().formatInsufficientPermissionsMessage(event.getAuthor().getAsMention())
            ).queue();
            return;
        }

        if (args.length() > 5) {
            event.getChannel().sendMessage("\uD83D\uDC94 Prefix too long! Max 5 characters~").queue();
            return;
        }

        bot.getDatabase().setPrefix(event.getGuild().getIdLong(), args);

        event.getChannel().sendMessage(String.format(
                "\uD83D\uDD27 **Prefix Updated!**\nNew prefix is now: `%s` \uD83D\uDC95",
                args
        )).queue();
    }

    public void handleAutoCleanPrefix(MessageReceivedEvent event) {
        event.getChannel().sendMessage("\uD83E\uDDF9 Auto-clean configuration~ \uD83D\uDC95").queue();
    }

    public void handleDelayPrefix(MessageReceivedEvent event, String args) {
        int minutes = 5;
        if (args != null && !args.isEmpty()) {
            try {
                minutes = Integer.parseInt(args.trim());
                if (minutes <= 0) minutes = 5;
            } catch (NumberFormatException ignored) {
            }
        }

        event.getChannel().sendMessage(String.format(
                "\u23F3 **Delay Requested**\nI'll wait %d more minutes before cleaning~ \uD83D\uDC95",
                minutes
        )).queue();
    }

    public void handleXpPrefix(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        UserXp userXp = bot.getDatabase().getUserXp(userId, event.getGuild().getIdLong());

        int nextLevel = userXp.getLevel() + 1;
        long xpForNext = (long) nextLevel * nextLevel * 100;
        int progress = (int) ((userXp.getXp() * 100) / Math.max(xpForNext, 1));

        event.getChannel().sendMessage(String.format(
                "\u2728 **XP Stats**\n%s's progress~ \uD83D\uDC95\n\n" +
                        "**Level:** %d\n" +
                        "**XP:** %d\n" +
                        "**Progress to Next:** %d%%",
                event.getAuthor().getAsMention(), userXp.getLevel(), userXp.getXp(), progress
        )).queue();
    }

    public void handleLeaderboardPrefix(MessageReceivedEvent event) {
        List<UserXp> topUsers = bot.getDatabase().getLeaderboard(event.getGuild().getIdLong(), 10);

        StringBuilder sb = new StringBuilder();
        sb.append("\uD83C\uDFC6 **Server Leaderboard**\n*\"Look who's been the most active~\"* \uD83D\uDC95\n\n");

        if (topUsers.isEmpty()) {
            sb.append("No one has earned XP yet~");
        } else {
            for (int i = 0; i < topUsers.size(); i++) {
                UserXp user = topUsers.get(i);
                String medal = switch (i) {
                    case 0 -> "\uD83E\uDD47";
                    case 1 -> "\uD83E\uDD48";
                    case 2 -> "\uD83E\uDD49";
                    default -> "";
                };
                sb.append(String.format("%s %d. <@%d> - Level %d (%d XP)\n",
                        medal, i + 1, user.getUserId(), user.getLevel(), user.getXp()));
            }
        }

        event.getChannel().sendMessage(sb.toString()).queue();
    }
}
