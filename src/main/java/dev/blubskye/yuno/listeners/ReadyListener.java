/*
 * Yuno Gasai 2 (Java Edition) - Ready Listener
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno.listeners;

import dev.blubskye.yuno.YunoBot;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadyListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ReadyListener.class);

    private final YunoBot bot;

    public ReadyListener(YunoBot bot) {
        this.bot = bot;
    }

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Yuno is online! Logged in as {}~", event.getJDA().getSelfUser().getName());
        logger.info("I'm watching over {} servers for you~", event.getGuildTotalCount());
    }
}
