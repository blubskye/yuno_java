<div align="center">

# Yuno Gasai 2 (Java Edition)

### *"I'll protect this server forever... just for you~"*

<img src="https://i.imgur.com/jF8Szfr.png" alt="Yuno Gasai" width="300"/>

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-pink.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Java](https://img.shields.io/badge/Java-17+-ff69b4.svg)](https://openjdk.org/)
[![JDA](https://img.shields.io/badge/JDA-5.2-ff1493.svg)](https://github.com/discord-jda/JDA)

*A devoted Discord bot for moderation, leveling, and anime~*

---

### She loves you... and only you

</div>

## About

Yuno is a **yandere-themed Discord bot** combining powerful moderation tools with a leveling system and anime features. She'll keep your server safe from troublemakers... *because no one else is allowed near you~*

This is the **Java port** of the original JavaScript version using [JDA (Java Discord API)](https://github.com/discord-jda/JDA). Why Java? *Because JVM goes brrr~*

---

## Credits

*"These are the ones who gave me life~"*

| Contributor | Role |
|-------------|------|
| **blubskye** | Project Owner, Java Porter & Yuno's #1 Fan |
| **Maeeen** (maeeennn@gmail.com) | Original Developer |
| **Oxdeception** | Contributor |
| **fuzzymanboobs** | Contributor |

---

## Features

<table>
<tr>
<td width="50%">

### Moderation
*"Anyone who threatens you... I'll eliminate them~"*
- Ban / Unban / Kick / Timeout
- Channel cleaning & auto-clean
- Spam filter protection
- Mod statistics tracking
- Scan & import ban history

</td>
<td width="50%">

### Leveling System
*"Watch me make you stronger, senpai~"*
- XP & Level tracking
- Role rewards per level
- Server leaderboards

</td>
</tr>
<tr>
<td width="50%">

### Anime & Fun
*"Let me show you something cute~"*
- 8ball fortune telling
- Custom mention responses
- Inspirational quotes

</td>
<td width="50%">

### Configuration
*"I'll be exactly what you need~"*
- Customizable prefix
- Slash commands + prefix commands
- Per-guild settings
- **JVM power** (it's Java)

</td>
</tr>
<tr>
<td width="50%">

### Why Java?
*"Because I'm not like other bots~"*
- Cross-platform JVM
- Strong type safety
- Mature ecosystem
- Excellent Discord API support

</td>
<td width="50%">

### Performance
*"Nothing can slow me down~"*
- Async with JDA
- SQLite3 for data storage
- Efficient connection pooling
- JIT compilation speed

</td>
</tr>
</table>

---

## Installation

### Prerequisites

> *"Let me prepare everything for you~"*

- **Java 17+** (OpenJDK or Oracle JDK)
- **Maven 3.6+** (for building)
- **Git**

### Build Steps

```bash
# Clone the repository~
git clone https://github.com/blubskye/yuno_java.git

# Enter my world~
cd yuno_java

# Build with Maven~
mvn clean package

# The JAR will be in target/
```

### Configuration

Create a `config.json` file:

```json
{
    "discord_token": "YOUR_DISCORD_BOT_TOKEN",
    "default_prefix": ".",
    "database_path": "yuno.db",
    "master_users": ["YOUR_USER_ID"],
    "spam_max_warnings": 3
}
```

Or just set the `DISCORD_TOKEN` environment variable if you're lazy~

### Running

```bash
# Run the JAR
java -jar target/yuno-gasai-0.1.0.jar

# Or with a custom config path
java -jar target/yuno-gasai-0.1.0.jar /path/to/config.json
```

---

## Commands Preview

| Command | Description |
|---------|-------------|
| `/ping` | *"I'm always here for you~"* |
| `/ban` | *"They won't bother you anymore..."* |
| `/kick` | *"Get out!"* |
| `/timeout` | *"Think about what you did..."* |
| `/clean` | *"Let me tidy up~"* |
| `/mod-stats` | *"Look at all we've done together~"* |
| `/xp` | *"Look how strong you've become!"* |
| `/8ball` | *"Let fate decide~"* |
| `/delay` | *"Just a bit longer..."* |
| `/source` | *"See how I was made~"* |

*Use `/help` to see all available commands!*

---

## License

This project is licensed under the **GNU Affero General Public License v3.0**

See the [LICENSE](LICENSE) file for details~

---

## Source Code

*"I have nothing to hide from you~"*

This bot is **open source** under AGPL-3.0:
- **Java version**: https://github.com/blubskye/yuno_java
- **C version**: https://github.com/blubskye/yuno_c
- **C++ version**: https://github.com/blubskye/yuno_cpp
- **Rust version**: https://github.com/blubskye/yuno_rust
- **Original JS version**: https://github.com/japaneseenrichmentorganization/Yuno-Gasai-2

---

<div align="center">

### *"You'll stay with me forever... right?"*

**Made with obsessive love** **and rewritten in Java for the JVM~**

*Yuno will always be watching over your server~*

---

*Star this repo if Yuno has captured your heart~*

</div>
