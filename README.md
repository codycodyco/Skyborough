# Skyborough

**A community-oriented SkyBlock home system for Minecraft servers, built on [uSkyBlock](https://github.com/uskyblock/uSkyBlock).**

Skyborough adds a floating central city and automatically allocates player home islands around it. Rather than focusing on traditional SkyBlock progression, it provides a shared community hub and personal building spaces.

## Features

- **Central city** — Register an existing city or generate a floating island as the community hub.
- **Automatic home allocation** — Assign islands on a grid around the city, filling nearby available spaces first.
- **Region protection** — Protect the city with WorldGuard while retaining uSkyBlock's island protection.
- **Home management** — Claim an island, teleport home, and return to the central city.
- **Custom schematics** — Reuse uSkyBlock's schematic system for home templates.
- **Home-friendly gameplay** — Preserve inventories, equipment, ender chests, permissions, and economy data; disable island resets in Skyborough mode.
- **Optional mode** — Keep existing uSkyBlock behavior when Skyborough is disabled.

## Requirements

- Paper
- WorldEdit
- WorldGuard

**Development target:** Paper 26.2 / Java 25.

Skyborough is currently a **development preview**. Full in-game compatibility testing is still in progress.

## Getting Started

1. Build the plugin JAR and install it with WorldEdit and WorldGuard.
2. Set up a dedicated void world for player homes.
3. Merge the following settings into `plugins/uSkyBlock/config.yml`:

```yaml
options:
  general:
    worldName: skyworld
  island:
    height: 150
    distance: 128
    protectionRange: 96

skyborough:
  enabled: true
  city-gap: 32
```

4. Restart the server.
5. Register an existing city or generate a new floating island.

Do not replace the entire configuration file with this snippet.

### Commands

| Command | Description |
| --- | --- |
| `/borough claim` | Claim a home island |
| `/borough home` | Teleport home |
| `/borough spawn` | Return to the central city |
| `/borough pos1` | Mark the first city boundary position |
| `/borough pos2` | Mark the second city boundary position |
| `/borough register` | Register an existing city |
| `/borough generate [radius]` | Generate a floating city island (16–96; default 48) |
| `/borough finish` | Finalize an interrupted city generation after inspection |

Alias: `/sb`

Permissions:

- `skyborough.use` — Available to all players by default.
- `skyborough.admin` — Administrator commands, OP by default.

For configuration details and administration, see [SKYBOROUGH.md](SKYBOROUGH.md).

## Building

Build with Gradle:

```bash
./gradlew :uSkyBlock-Core:test :uSkyBlock-Plugin:shadowJar
```

The resulting JAR is in `uSkyBlock-Plugin/build/libs/`.

Skyborough retains uSkyBlock's internal plugin identity and data structure for compatibility. **Do not install it alongside the original uSkyBlock plugin.**

## Project Status

Skyborough is under active development. Current limitations include fixed island heights and protection sizes, no built-in home selection GUI, and no automatic city relocation.

## Credits

Skyborough is a fork of [uSkyBlock](https://github.com/uskyblock/uSkyBlock). Thanks to the original authors and contributors for their work.

## License

Licensed under the [GNU General Public License v3.0](LICENSE.txt), following the original uSkyBlock project.
