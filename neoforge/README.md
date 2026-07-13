# Boosters (NeoForge)

A NeoForge port of [Boosters](https://www.curseforge.com/minecraft/mc-mods/boosters/), a performance mod for Minecraft 26.2. Same feature set as the Fabric build:

- one-click presets: **Quality / Balanced / Performance / Extreme** (plus Custom once you tweak anything) - pick how aggressive it is without touching individual sliders
- throttles AI for distant mobs (configurable distance/intervals)
- client-side culling of off-screen/out-of-range entities + nametags + ground shadows
- throttles ticking of distant block entities (furnaces, signs, etc. - excluding hoppers/pistons/beacons/conduits/brewing stands/sculk so gameplay logic isn't affected)
- throttles ticking of distant dropped items and XP orbs (farm/mob-drop piles) - physics, stack merging and despawning still happen, just less often
- reduces particle count based on distance and density
- limits the render distance of block entity renderer detail (chests, signs, skulls, banners)
- optional memory cleanup when you leave a world back to the menu (returns unused RAM to the OS - runs only in the menu, never during gameplay, so no stutter)
- detects installed optimization mods (FerriteCore, Krypton, Noisium, ModernFix, VMP, Iris, EntityCulling) and automatically defers to dedicated solutions (e.g. EntityCulling) instead of duplicating their work
- automatically pulls thresholds in further when Embeddium is detected, since Embeddium removes the GPU bottleneck and makes the CPU-side throttling pay off more
- live status line on the F3 debug screen showing what it's actually doing right now (throttled/culled counts per second), not just that it's installed

## Pair it with Embeddium

Boosters throttles game logic and decides what gets sent to the renderer - it doesn't replace the chunk renderer itself, that's a completely different (and much bigger) undertaking. If you want the biggest FPS gain from world rendering, install [Embeddium](https://modrinth.com/mod/embeddium) alongside Boosters. They don't overlap: Embeddium rewrites how chunks get drawn, Boosters reduces how much game logic and how many entities/particles need to be considered in the first place.

## In-game settings

- Options → Mods → Boosters → Config (uses NeoForge's built-in config screen extension point, backed by Cloth Config API)
- Keybind (default `;`, rebindable in Controls) also opens the same settings screen
- Config is saved to `config/boosters.json`

## Requirements

- JDK 25
- Cloth Config API (required, for the settings screen)

## Build

```
./gradlew build
```

## Run the dev client

```
./gradlew runClient
```
