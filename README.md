# Boosters

A Fabric performance mod for Minecraft 26.2. A NeoForge port with the same feature set lives in [`neoforge/`](neoforge/).

Features:

- one-click presets: **Quality / Balanced / Performance / Extreme** (plus Custom once you tweak anything) - pick how aggressive it is without touching individual sliders
- throttles AI for distant mobs (configurable distance/intervals)
- client-side culling of off-screen/out-of-range entities + nametags + ground shadows
- throttles ticking of distant block entities (furnaces, signs, etc. - excluding hoppers/pistons/beacons/conduits/brewing stands/sculk so gameplay logic isn't affected)
- reduces particle count based on distance and density
- limits the render distance of block entity renderer detail (chests, signs, skulls, banners)
- optional memory cleanup when you leave a world back to the menu (returns unused RAM to the OS - runs only in the menu, never during gameplay, so no stutter)
- detects installed optimization mods (Sodium, Lithium, C2ME, EntityCulling, Starlight, FerriteCore, Krypton, Noisium, ModernFix, ImmediatelyFast, VMP, Bobby, Iris) and automatically defers to dedicated solutions (e.g. EntityCulling) instead of duplicating their work
- automatically pulls thresholds in further when Sodium is detected, since Sodium removes the GPU bottleneck and makes the CPU-side throttling pay off more
- live status line on the F3 debug screen showing what it's actually doing right now (throttled/culled counts per second), not just that it's installed

## Pair it with Sodium

Boosters throttles game logic and decides what gets sent to the renderer - it doesn't replace the chunk renderer itself, that's a completely different (and much bigger) undertaking. If you want the biggest FPS gain from world rendering, install [Sodium](https://modrinth.com/mod/sodium) alongside Boosters. They don't overlap: Sodium rewrites how chunks get drawn, Boosters reduces how much game logic and how many entities/particles need to be considered in the first place.

## In-game settings

- With [Mod Menu](https://modrinth.com/mod/modmenu) installed (requires [Cloth Config API](https://modrinth.com/mod/cloth-config)): Options → Mods → Boosters → Config
- Without Mod Menu: a keybind (default `;`, rebindable in Controls) opens the same settings screen
- Config is saved to `config/boosters.json`

## Requirements

- JDK 25
- Cloth Config API (required, for the settings screen)
- Mod Menu (optional, for convenient access from the mod list)

## Build

```
./gradlew build
```

## Run the dev client

```
./gradlew runClient
```
