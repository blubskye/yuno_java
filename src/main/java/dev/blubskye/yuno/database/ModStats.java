/*
 * Yuno Gasai 2 (Java Edition) - Mod Stats
 * Copyright (C) 2025 blubskye
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package dev.blubskye.yuno.database;

public class ModStats {
    private int banCount = 0;
    private int kickCount = 0;
    private int timeoutCount = 0;

    public int getBanCount() {
        return banCount;
    }

    public void setBanCount(int banCount) {
        this.banCount = banCount;
    }

    public int getKickCount() {
        return kickCount;
    }

    public void setKickCount(int kickCount) {
        this.kickCount = kickCount;
    }

    public int getTimeoutCount() {
        return timeoutCount;
    }

    public void setTimeoutCount(int timeoutCount) {
        this.timeoutCount = timeoutCount;
    }

    public int getTotalActions() {
        return banCount + kickCount + timeoutCount;
    }
}
