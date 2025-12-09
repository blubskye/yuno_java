/*
 * Yuno Gasai 2 (Java Edition) - Fun Commands
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno.commands;

import dev.blubskye.yuno.YunoBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Random;

public class FunCommands {
    private static final String[] EIGHTBALL_RESPONSES = {
            // Positive
            "It is certain~ \uD83D\uDC95",
            "It is decidedly so~ \uD83D\uDC97",
            "Without a doubt~ \uD83D\uDC96",
            "Yes, definitely~ \uD83D\uDC95",
            "You may rely on it~ \uD83D\uDC97",
            "As I see it, yes~ \u2728",
            "Most likely~ \uD83D\uDC95",
            "Outlook good~ \uD83D\uDC96",
            "Yes~ \uD83D\uDC97",
            "Signs point to yes~ \u2728",

            // Neutral
            "Reply hazy, try again~ \uD83E\uDD14",
            "Ask again later~ \uD83D\uDCAD",
            "Better not tell you now~ \uD83D\uDE0F",
            "Cannot predict now~ \uD83D\uDD2E",
            "Concentrate and ask again~ \uD83D\uDCAB",

            // Negative
            "Don't count on it~ \uD83D\uDC94",
            "My reply is no~ \uD83D\uDE24",
            "My sources say no~ \uD83D\uDCA2",
            "Outlook not so good~ \uD83D\uDE1E",
            "Very doubtful~ \uD83D\uDC94"
    };

    private final YunoBot bot;
    private final Random random;

    public FunCommands(YunoBot bot) {
        this.bot = bot;
        this.random = new Random();
    }

    private String getRandomResponse() {
        return EIGHTBALL_RESPONSES[random.nextInt(EIGHTBALL_RESPONSES.length)];
    }

    // Slash Commands

    public void handle8Ball(SlashCommandInteractionEvent event) {
        String question = event.getOption("question").getAsString();
        String response = getRandomResponse();

        event.reply(String.format(
                "\uD83C\uDFB1 **Magic 8-Ball**\n\n" +
                        "**Question:** %s\n\n" +
                        "**Answer:** %s\n\n" +
                        "*shakes the 8-ball mysteriously*",
                question, response
        )).queue();
    }

    // Prefix Commands

    public void handle8BallPrefix(MessageReceivedEvent event, String args) {
        if (args == null || args.isEmpty()) {
            event.getChannel().sendMessage("\uD83D\uDC94 You need to ask a question~ \uD83C\uDFB1").queue();
            return;
        }

        String response = getRandomResponse();

        event.getChannel().sendMessage(String.format(
                "\uD83C\uDFB1 **Magic 8-Ball**\n\n" +
                        "**Question:** %s\n\n" +
                        "**Answer:** %s\n\n" +
                        "*shakes the 8-ball mysteriously*",
                args, response
        )).queue();
    }
}
