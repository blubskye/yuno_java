/*
 * Yuno Gasai 2 (Java Edition)
 * "I'll protect this server forever... just for you~" <3
 *
 * Copyright (C) 2025 blubskye
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.blubskye.yuno;

import dev.blubskye.yuno.config.YunoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class YunoGasai {
    private static final Logger logger = LoggerFactory.getLogger(YunoGasai.class);

    public static void main(String[] args) {
        printBanner();

        String configPath = "config.json";
        if (args.length > 0) {
            configPath = args[0];
        } else {
            String envPath = System.getenv("CONFIG_PATH");
            if (envPath != null && !envPath.isEmpty()) {
                configPath = envPath;
            }
        }

        YunoConfig config;
        File configFile = new File(configPath);

        if (configFile.exists()) {
            logger.info("Loading config from {}~", configPath);
            config = YunoConfig.loadFromFile(configPath);
        } else {
            logger.info("Config file not found, checking environment...");
            config = YunoConfig.loadFromEnv();
        }

        if (config == null || config.getDiscordToken() == null || config.getDiscordToken().isEmpty()
                || config.getDiscordToken().equals("YOUR_DISCORD_BOT_TOKEN_HERE")) {
            logger.error("No valid Discord token provided!");
            logger.error("Set DISCORD_TOKEN environment variable or add it to config.json");
            System.exit(1);
        }

        logger.info("Yuno is waking up... please wait~");

        try {
            YunoBot bot = new YunoBot(config);
            bot.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Yuno is shutting down... goodbye, my love~");
                bot.shutdown();
            }));

        } catch (Exception e) {
            logger.error("Failed to start Yuno: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("    ======================================================");
        System.out.println("             Yuno Gasai 2 (Java Edition)");
        System.out.println("             \"I'll protect you forever~\"");
        System.out.println("    ======================================================");
        System.out.println();
    }
}
