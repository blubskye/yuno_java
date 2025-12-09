/*
 * Yuno Gasai 2 (Java Edition) - Guild Settings
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno.database;

public class GuildSettings {
    private long guildId;
    private String prefix = ".";
    private boolean spamFilterEnabled = false;
    private boolean levelingEnabled = true;

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isSpamFilterEnabled() {
        return spamFilterEnabled;
    }

    public void setSpamFilterEnabled(boolean spamFilterEnabled) {
        this.spamFilterEnabled = spamFilterEnabled;
    }

    public boolean isLevelingEnabled() {
        return levelingEnabled;
    }

    public void setLevelingEnabled(boolean levelingEnabled) {
        this.levelingEnabled = levelingEnabled;
    }
}
