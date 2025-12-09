/*
 * Yuno Gasai 2 (Java Edition) - Moderation Commands
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno.commands;

import dev.blubskye.yuno.YunoBot;
import dev.blubskye.yuno.database.ModAction;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModerationCommands {
    private static final Logger logger = LoggerFactory.getLogger(ModerationCommands.class);
    private static final Pattern USER_MENTION_PATTERN = Pattern.compile("<@!?(\\d+)>");

    private final YunoBot bot;

    public ModerationCommands(YunoBot bot) {
        this.bot = bot;
    }

    // Slash Commands

    public void handleBan(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply(bot.getConfig().formatInsufficientPermissionsMessage(event.getUser().getAsMention()))
                    .setEphemeral(true).queue();
            return;
        }

        User targetUser = event.getOption("user").getAsUser();
        String reason = event.getOption("reason") != null ?
                event.getOption("reason").getAsString() : "No reason provided";

        event.getGuild().ban(targetUser, 0, TimeUnit.SECONDS)
                .reason(reason)
                .queue(
                        success -> {
                            logModAction(event.getGuild().getIdLong(), event.getUser().getIdLong(),
                                    targetUser.getIdLong(), "ban", reason);

                            event.reply(String.format(
                                    "\uD83D\uDD2A **Banned!**\nThey won't bother you anymore~ \uD83D\uDC95\n\n" +
                                            "**User:** %s\n**Moderator:** %s\n**Reason:** %s",
                                    targetUser.getAsMention(), event.getUser().getAsMention(), reason
                            )).queue();
                        },
                        error -> event.reply("\uD83D\uDC94 Failed to ban user: " + error.getMessage())
                                .setEphemeral(true).queue()
                );
    }

    public void handleKick(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.reply(bot.getConfig().formatInsufficientPermissionsMessage(event.getUser().getAsMention()))
                    .setEphemeral(true).queue();
            return;
        }

        Member targetMember = event.getOption("user").getAsMember();
        String reason = event.getOption("reason") != null ?
                event.getOption("reason").getAsString() : "No reason provided";

        if (targetMember == null) {
            event.reply("\uD83D\uDC94 User not found in this server~").setEphemeral(true).queue();
            return;
        }

        targetMember.kick()
                .reason(reason)
                .queue(
                        success -> {
                            logModAction(event.getGuild().getIdLong(), event.getUser().getIdLong(),
                                    targetMember.getIdLong(), "kick", reason);

                            event.reply(String.format(
                                    "\uD83D\uDC62 **Kicked!**\nGet out! \uD83D\uDCA2\n\n" +
                                            "**User:** %s\n**Moderator:** %s\n**Reason:** %s",
                                    targetMember.getUser().getAsMention(), event.getUser().getAsMention(), reason
                            )).queue();
                        },
                        error -> event.reply("\uD83D\uDC94 Failed to kick user: " + error.getMessage())
                                .setEphemeral(true).queue()
                );
    }

    public void handleUnban(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply(bot.getConfig().formatInsufficientPermissionsMessage(event.getUser().getAsMention()))
                    .setEphemeral(true).queue();
            return;
        }

        String userIdStr = event.getOption("user_id").getAsString();
        String reason = event.getOption("reason") != null ?
                event.getOption("reason").getAsString() : "No reason provided";

        long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            event.reply("\uD83D\uDC94 Invalid user ID~").setEphemeral(true).queue();
            return;
        }

        event.getGuild().unban(UserSnowflake.fromId(userId))
                .reason(reason)
                .queue(
                        success -> {
                            logModAction(event.getGuild().getIdLong(), event.getUser().getIdLong(),
                                    userId, "unban", reason);

                            event.reply(String.format(
                                    "\uD83D\uDC95 **Unbanned!**\nI'm giving them another chance~ Be good this time!\n\n" +
                                            "**User:** <@%d>\n**Moderator:** %s\n**Reason:** %s",
                                    userId, event.getUser().getAsMention(), reason
                            )).queue();
                        },
                        error -> event.reply("\uD83D\uDC94 Failed to unban user: " + error.getMessage())
                                .setEphemeral(true).queue()
                );
    }

    public void handleTimeout(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply(bot.getConfig().formatInsufficientPermissionsMessage(event.getUser().getAsMention()))
                    .setEphemeral(true).queue();
            return;
        }

        Member targetMember = event.getOption("user").getAsMember();
        long minutes = event.getOption("minutes").getAsLong();
        String reason = event.getOption("reason") != null ?
                event.getOption("reason").getAsString() : "No reason provided";

        if (targetMember == null) {
            event.reply("\uD83D\uDC94 User not found in this server~").setEphemeral(true).queue();
            return;
        }

        targetMember.timeoutFor(Duration.ofMinutes(minutes))
                .reason(reason)
                .queue(
                        success -> {
                            String fullReason = String.format("%s (%d minutes)", reason, minutes);
                            logModAction(event.getGuild().getIdLong(), event.getUser().getIdLong(),
                                    targetMember.getIdLong(), "timeout", fullReason);

                            event.reply(String.format(
                                    "\u23F0 **Timed Out!**\nThink about what you did~ \uD83D\uDE24\n\n" +
                                            "**User:** %s\n**Duration:** %d minutes\n**Moderator:** %s\n**Reason:** %s",
                                    targetMember.getUser().getAsMention(), minutes, event.getUser().getAsMention(), reason
                            )).queue();
                        },
                        error -> event.reply("\uD83D\uDC94 Failed to timeout user: " + error.getMessage())
                                .setEphemeral(true).queue()
                );
    }

    public void handleClean(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply(bot.getConfig().formatInsufficientPermissionsMessage(event.getUser().getAsMention()))
                    .setEphemeral(true).queue();
            return;
        }

        int amount = event.getOption("amount") != null ?
                (int) event.getOption("amount").getAsLong() : 10;

        if (amount < 1 || amount > 100) {
            event.reply("\uD83D\uDC94 Please specify between 1 and 100 messages~").setEphemeral(true).queue();
            return;
        }

        event.deferReply().setEphemeral(true).queue();

        event.getChannel().getHistory().retrievePast(amount).queue(messages -> {
            if (messages.isEmpty()) {
                event.getHook().sendMessage("\uD83D\uDC94 No messages to delete~").queue();
                return;
            }

            event.getChannel().asTextChannel().deleteMessages(messages).queue(
                    success -> event.getHook().sendMessage(
                            String.format("\uD83E\uDDF9 Deleted %d messages~ \uD83D\uDC95", messages.size())
                    ).queue(),
                    error -> event.getHook().sendMessage("\uD83D\uDC94 Failed to delete messages: " + error.getMessage()).queue()
            );
        });
    }

    public void handleModStats(SlashCommandInteractionEvent event) {
        var actions = bot.getDatabase().getModActions(event.getGuild().getIdLong(), 100);

        event.reply(String.format(
                "\uD83D\uDCCA **Moderation Statistics**\nLook at all we've done together~ \uD83D\uDC95\n\n" +
                        "**Total Actions:** %d",
                actions.size()
        )).queue();
    }

    // Prefix Commands

    public void handleBanPrefix(MessageReceivedEvent event, String args) {
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.getChannel().sendMessage(
                    bot.getConfig().formatInsufficientPermissionsMessage(event.getAuthor().getAsMention())
            ).queue();
            return;
        }

        if (args == null || args.isEmpty()) {
            event.getChannel().sendMessage("\uD83D\uDC94 Please specify a user to ban~").queue();
            return;
        }

        String[] parts = args.split("\\s+", 2);
        Long userId = parseUserMention(parts[0]);
        String reason = parts.length > 1 ? parts[1] : "No reason provided";

        if (userId == null) {
            event.getChannel().sendMessage("\uD83D\uDC94 I couldn't find that user~").queue();
            return;
        }

        event.getGuild().ban(UserSnowflake.fromId(userId), 0, TimeUnit.SECONDS)
                .reason(reason)
                .queue(
                        success -> {
                            logModAction(event.getGuild().getIdLong(), event.getAuthor().getIdLong(),
                                    userId, "ban", reason);

                            event.getChannel().sendMessage(String.format(
                                    "\uD83D\uDD2A **Banned!**\nThey won't bother you anymore~ \uD83D\uDC95\n\n" +
                                            "**User:** <@%d>\n**Moderator:** %s\n**Reason:** %s",
                                    userId, event.getAuthor().getAsMention(), reason
                            )).queue();
                        },
                        error -> event.getChannel().sendMessage("\uD83D\uDC94 Failed to ban user: " + error.getMessage()).queue()
                );
    }

    public void handleKickPrefix(MessageReceivedEvent event, String args) {
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.getChannel().sendMessage(
                    bot.getConfig().formatInsufficientPermissionsMessage(event.getAuthor().getAsMention())
            ).queue();
            return;
        }

        if (args == null || args.isEmpty()) {
            event.getChannel().sendMessage("\uD83D\uDC94 Please specify a user to kick~").queue();
            return;
        }

        String[] parts = args.split("\\s+", 2);
        Long userId = parseUserMention(parts[0]);
        String reason = parts.length > 1 ? parts[1] : "No reason provided";

        if (userId == null) {
            event.getChannel().sendMessage("\uD83D\uDC94 I couldn't find that user~").queue();
            return;
        }

        event.getGuild().kick(UserSnowflake.fromId(userId))
                .reason(reason)
                .queue(
                        success -> {
                            logModAction(event.getGuild().getIdLong(), event.getAuthor().getIdLong(),
                                    userId, "kick", reason);

                            event.getChannel().sendMessage(String.format(
                                    "\uD83D\uDC62 **Kicked!**\nGet out! \uD83D\uDCA2\n\n" +
                                            "**User:** <@%d>\n**Moderator:** %s\n**Reason:** %s",
                                    userId, event.getAuthor().getAsMention(), reason
                            )).queue();
                        },
                        error -> event.getChannel().sendMessage("\uD83D\uDC94 Failed to kick user: " + error.getMessage()).queue()
                );
    }

    public void handleUnbanPrefix(MessageReceivedEvent event, String args) {
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.getChannel().sendMessage(
                    bot.getConfig().formatInsufficientPermissionsMessage(event.getAuthor().getAsMention())
            ).queue();
            return;
        }

        if (args == null || args.isEmpty()) {
            event.getChannel().sendMessage("\uD83D\uDC94 Please specify a user ID to unban~").queue();
            return;
        }

        String[] parts = args.split("\\s+", 2);
        long userId;
        try {
            userId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("\uD83D\uDC94 Invalid user ID~").queue();
            return;
        }
        String reason = parts.length > 1 ? parts[1] : "No reason provided";

        event.getGuild().unban(UserSnowflake.fromId(userId))
                .reason(reason)
                .queue(
                        success -> {
                            logModAction(event.getGuild().getIdLong(), event.getAuthor().getIdLong(),
                                    userId, "unban", reason);

                            event.getChannel().sendMessage(String.format(
                                    "\uD83D\uDC95 **Unbanned!**\nI'm giving them another chance~ Be good this time!\n\n" +
                                            "**User:** <@%d>\n**Moderator:** %s\n**Reason:** %s",
                                    userId, event.getAuthor().getAsMention(), reason
                            )).queue();
                        },
                        error -> event.getChannel().sendMessage("\uD83D\uDC94 Failed to unban user: " + error.getMessage()).queue()
                );
    }

    public void handleTimeoutPrefix(MessageReceivedEvent event, String args) {
        if (!event.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            event.getChannel().sendMessage(
                    bot.getConfig().formatInsufficientPermissionsMessage(event.getAuthor().getAsMention())
            ).queue();
            return;
        }

        if (args == null || args.isEmpty()) {
            event.getChannel().sendMessage("\uD83D\uDC94 Usage: timeout <user> <minutes> [reason]~").queue();
            return;
        }

        String[] parts = args.split("\\s+", 3);
        if (parts.length < 2) {
            event.getChannel().sendMessage("\uD83D\uDC94 Usage: timeout <user> <minutes> [reason]~").queue();
            return;
        }

        Long userId = parseUserMention(parts[0]);
        if (userId == null) {
            event.getChannel().sendMessage("\uD83D\uDC94 I couldn't find that user~").queue();
            return;
        }

        long minutes;
        try {
            minutes = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("\uD83D\uDC94 Invalid duration~").queue();
            return;
        }

        String reason = parts.length > 2 ? parts[2] : "No reason provided";

        event.getGuild().retrieveMemberById(userId).queue(member -> {
            member.timeoutFor(Duration.ofMinutes(minutes))
                    .reason(reason)
                    .queue(
                            success -> {
                                String fullReason = String.format("%s (%d minutes)", reason, minutes);
                                logModAction(event.getGuild().getIdLong(), event.getAuthor().getIdLong(),
                                        userId, "timeout", fullReason);

                                event.getChannel().sendMessage(String.format(
                                        "\u23F0 **Timed Out!**\nThink about what you did~ \uD83D\uDE24\n\n" +
                                                "**User:** <@%d>\n**Duration:** %d minutes\n**Moderator:** %s",
                                        userId, minutes, event.getAuthor().getAsMention()
                                )).queue();
                            },
                            error -> event.getChannel().sendMessage("\uD83D\uDC94 Failed to timeout user: " + error.getMessage()).queue()
                    );
        }, error -> event.getChannel().sendMessage("\uD83D\uDC94 User not found in this server~").queue());
    }

    public void handleCleanPrefix(MessageReceivedEvent event, String args) {
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getChannel().sendMessage(
                    bot.getConfig().formatInsufficientPermissionsMessage(event.getAuthor().getAsMention())
            ).queue();
            return;
        }

        int amount = 10;
        if (args != null && !args.isEmpty()) {
            try {
                amount = Integer.parseInt(args.trim());
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("\uD83D\uDC94 Invalid amount~").queue();
                return;
            }
        }

        if (amount < 1 || amount > 100) {
            event.getChannel().sendMessage("\uD83D\uDC94 Please specify between 1 and 100 messages~").queue();
            return;
        }

        final int deleteAmount = amount;
        event.getChannel().getHistory().retrievePast(amount + 1).queue(messages -> {
            // Remove the command message itself
            messages.removeIf(m -> m.getId().equals(event.getMessage().getId()));
            if (messages.size() > deleteAmount) {
                messages = messages.subList(0, deleteAmount);
            }

            if (messages.isEmpty()) {
                event.getChannel().sendMessage("\uD83D\uDC94 No messages to delete~").queue();
                return;
            }

            event.getChannel().asTextChannel().deleteMessages(messages).queue(
                    success -> event.getChannel().sendMessage(
                            String.format("\uD83E\uDDF9 Deleted messages~ \uD83D\uDC95")
                    ).queue(msg -> msg.delete().queueAfter(3, TimeUnit.SECONDS)),
                    error -> event.getChannel().sendMessage("\uD83D\uDC94 Failed to delete messages: " + error.getMessage()).queue()
            );
        });
    }

    public void handleModStatsPrefix(MessageReceivedEvent event) {
        var actions = bot.getDatabase().getModActions(event.getGuild().getIdLong(), 100);

        event.getChannel().sendMessage(String.format(
                "\uD83D\uDCCA **Moderation Statistics**\nLook at all we've done together~ \uD83D\uDC95\n\n" +
                        "**Total Actions:** %d",
                actions.size()
        )).queue();
    }

    // Helper methods

    private Long parseUserMention(String mention) {
        Matcher matcher = USER_MENTION_PATTERN.matcher(mention);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        }
        try {
            return Long.parseLong(mention);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void logModAction(long guildId, long moderatorId, long targetId, String actionType, String reason) {
        ModAction action = new ModAction();
        action.setGuildId(guildId);
        action.setModeratorId(moderatorId);
        action.setTargetId(targetId);
        action.setActionType(actionType);
        action.setReason(reason);
        action.setTimestamp(System.currentTimeMillis() / 1000);
        bot.getDatabase().logModAction(action);
    }
}
