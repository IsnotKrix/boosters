package com.boosters.client;

import com.boosters.BoostersConfig;
import com.boosters.BoostersPreset;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Builds the Cloth Config screen mapping every {@link BoostersConfig} field to
 * a toggle/slider/field, split into one category per optimization feature.
 *
 * <p>Every entry sets a default value matching {@link BoostersConfig}'s own field
 * initializer, so Cloth Config's built-in reset-to-default arrow works everywhere,
 * not just on the preset selector.
 */
public final class BoostersConfigScreen {

	private static final Style NOTE_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);

	private BoostersConfigScreen() {
	}

	/** Wraps informational/honesty-qualifier text (as opposed to action items) in a consistent, muted style. */
	private static Component note(String text) {
		return Component.literal(text).setStyle(NOTE_STYLE);
	}

	public static Screen build(Screen parent) {
		BoostersConfig config = BoostersConfig.get();

		// Snapshot the preset at open + a holder the preset dropdown writes into, so the
		// saving runnable (which runs after every field's save consumer) can decide whether
		// the user explicitly switched presets or hand-edited a value.
		BoostersPreset presetAtOpen = config.preset;
		BoostersPreset[] selectedPreset = { config.preset };

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("Boosters").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.setTransparentBackground(true)
				.setAlwaysShowTabs(true)
				.setShouldTabsSmoothScroll(true)
				.setShouldListSmoothScroll(true)
				.setSavingRunnable(() -> {
					BoostersPreset chosen = selectedPreset[0];
					if (chosen != BoostersPreset.CUSTOM && chosen != presetAtOpen) {
						// User explicitly picked a different preset - apply its values,
						// overriding whatever the individual sliders wrote this session.
						chosen.applyTo(config);
						config.preset = chosen;
					} else {
						// Preset left as-is (or set to Custom): keep the individually edited
						// values and label the config as whichever preset they still match.
						config.preset = BoostersPreset.matching(config);
					}
					BoostersConfig.save();
				});

		ConfigEntryBuilder entry = builder.entryBuilder();

		ConfigCategory presets = builder.getOrCreateCategory(Component.literal("Presets"));
		presets.addEntry(entry.startBooleanToggle(Component.literal("Enable Boosters"), config.enabled)
				.setDefaultValue(true)
				.setSaveConsumer(v -> config.enabled = v)
				.setTooltip(Component.literal("Master switch. Turns every throttle/culling feature below off at once, "
						+ "regardless of their individual settings - useful for quickly A/B testing or troubleshooting."))
				.build());
		presets.addEntry(entry.startEnumSelector(Component.literal("Preset"), BoostersPreset.class, config.preset)
				.setEnumNameProvider(p -> Component.literal(((BoostersPreset) p).displayName()))
				.setDefaultValue(BoostersPreset.PERFORMANCE)
				.setSaveConsumer(v -> selectedPreset[0] = v)
				.setTooltip(Component.literal("Quality = near-vanilla, small gain. Extreme = max FPS, most visible culling. "
						+ "Pick one and save, then reopen the screen to see the values it applied. "
						+ "Editing any value by hand switches this to Custom."))
				.build());
		presets.addEntry(entry.startTextDescription(note(
				"Presets set every distance/interval/density below at once. The individual tabs let you fine-tune "
						+ "from there (which turns the preset into Custom)."))
				.build());

		ConfigCategory ai = builder.getOrCreateCategory(Component.literal("Mob AI"));
		ai.addEntry(entry.startTextDescription(note(
				"Vanilla already skips ticking mobs outside simulation distance and already staggers some AI work. "
						+ "This adds a more aggressive, distance-graduated throttle on top. The benefit scales with how "
						+ "many mobs are nearby - farms/villages/mob clusters see the most gain; a lone player exploring "
						+ "with few mobs around will notice little to nothing."))
				.build());
		ai.addEntry(entry.startBooleanToggle(Component.literal("Enable AI throttling"), config.enableAiThrottle)
				.setDefaultValue(true)
				.setSaveConsumer(v -> config.enableAiThrottle = v)
				.setTooltip(Component.literal("Distant mobs update sensing/AI/navigation less often."))
				.build());
		ai.addEntry(entry.startIntSlider(Component.literal("Throttle start distance"), config.aiThrottleStartDistance, 4, 128)
				.setDefaultValue(24)
				.setSaveConsumer(v -> config.aiThrottleStartDistance = v)
				.setTooltip(Component.literal("Distance from the nearest player (blocks) where AI throttling starts."))
				.build());
		ai.addEntry(entry.startIntSlider(Component.literal("Throttle max distance"), config.aiThrottleMaxDistance, 16, 256)
				.setDefaultValue(96)
				.setSaveConsumer(v -> config.aiThrottleMaxDistance = v)
				.setTooltip(Component.literal("Distance at which AI throttling reaches its slowest interval."))
				.build());
		ai.addEntry(entry.startIntSlider(Component.literal("Interval (nearby, ticks)"), config.aiThrottleIntervalTicks, 1, 20)
				.setDefaultValue(3)
				.setSaveConsumer(v -> config.aiThrottleIntervalTicks = v)
				.setTooltip(Component.literal("How many ticks between AI updates for the closest throttled mobs."))
				.build());
		ai.addEntry(entry.startIntSlider(Component.literal("Interval (far away, ticks)"), config.aiThrottleMaxIntervalTicks, 1, 40)
				.setDefaultValue(16)
				.setSaveConsumer(v -> config.aiThrottleMaxIntervalTicks = v)
				.setTooltip(Component.literal("How many ticks between AI updates for mobs at/beyond max distance."))
				.build());

		ConfigCategory entities = builder.getOrCreateCategory(Component.literal("Entity Rendering"));
		entities.addEntry(entry.startTextDescription(note(
				"Vanilla already skips rendering entities outside your view frustum (behind you, off-screen). "
						+ "This adds a hard distance cutoff on top, regardless of whether the entity is in view - useful "
						+ "when you're looking toward a farm/base with a lot of entities loaded at once."))
				.build());
		entities.addEntry(entry.startBooleanToggle(Component.literal("Enable entity culling"), config.enableEntityCulling)
				.setDefaultValue(true)
				.setSaveConsumer(v -> config.enableEntityCulling = v)
				.setTooltip(Component.literal("Stops rendering entities beyond the max render distance below, even if they're in view."))
				.build());
		entities.addEntry(entry.startDoubleField(Component.literal("Max render distance"), config.entityCullingMaxDistance)
				.setMin(8.0).setMax(512.0)
				.setDefaultValue(72.0)
				.setSaveConsumer(v -> config.entityCullingMaxDistance = v)
				.setTooltip(Component.literal("Entities farther than this from the camera are never rendered."))
				.build());
		entities.addEntry(entry.startDoubleField(Component.literal("Nametag culling distance"), config.entityDetailCullDistance)
				.setMin(4.0).setMax(256.0)
				.setDefaultValue(32.0)
				.setSaveConsumer(v -> config.entityDetailCullDistance = v)
				.setTooltip(Component.literal("Beyond this distance, nametags and ground shadows stop rendering, even though the entity itself still does."))
				.build());

		ConfigCategory blockEntities = builder.getOrCreateCategory(Component.literal("Block Entities"));
		blockEntities.addEntry(entry.startBooleanToggle(Component.literal("Enable block entity throttling"), config.enableBlockEntityThrottle)
				.setDefaultValue(true)
				.setSaveConsumer(v -> config.enableBlockEntityThrottle = v)
				.setTooltip(Component.literal("Distant block entities (chests, furnaces, signs, ...) tick less often."))
				.build());
		blockEntities.addEntry(entry.startIntSlider(Component.literal("Throttle start distance"), config.blockEntityThrottleStartDistance, 4, 128)
				.setDefaultValue(32)
				.setSaveConsumer(v -> config.blockEntityThrottleStartDistance = v)
				.setTooltip(Component.literal("Distance from the nearest player (blocks) where block entity throttling starts. Max distance is always 3x this."))
				.build());
		blockEntities.addEntry(entry.startIntSlider(Component.literal("Max interval (ticks)"), config.blockEntityThrottleIntervalTicks, 1, 40)
				.setDefaultValue(6)
				.setSaveConsumer(v -> config.blockEntityThrottleIntervalTicks = v)
				.setTooltip(Component.literal("How many ticks between updates for the farthest throttled block entities."))
				.build());
		blockEntities.addEntry(entry.startStrList(Component.literal("Excluded types (substring)"), new ArrayList<>(config.blockEntityThrottleExcludeTypes))
				.setDefaultValue(Arrays.asList("hopper", "piston", "beacon", "conduit", "brewing_stand", "sculk_sensor", "sculk_shrieker"))
				.setSaveConsumer(v -> config.blockEntityThrottleExcludeTypes = new ArrayList<>(v))
				.setTooltip(Component.literal("E.g. hopper, piston, beacon - these types always tick normally."))
				.build());

		ConfigCategory items = builder.getOrCreateCategory(Component.literal("Items & XP Orbs"));
		items.addEntry(entry.startBooleanToggle(Component.literal("Enable item/XP orb throttling"), config.enableItemThrottle)
				.setDefaultValue(true)
				.setSaveConsumer(v -> config.enableItemThrottle = v)
				.setTooltip(Component.literal("Dropped items and XP orbs far from every player tick less often - useful for uncollected farm piles."))
				.build());
		items.addEntry(entry.startIntSlider(Component.literal("Throttle start distance"), config.itemThrottleStartDistance, 4, 128)
				.setDefaultValue(32)
				.setSaveConsumer(v -> config.itemThrottleStartDistance = v)
				.setTooltip(Component.literal("Distance from the nearest player (blocks) where item/XP orb throttling starts. Max distance is always 3x this."))
				.build());
		items.addEntry(entry.startIntSlider(Component.literal("Max interval (ticks)"), config.itemThrottleIntervalTicks, 1, 40)
				.setDefaultValue(6)
				.setSaveConsumer(v -> config.itemThrottleIntervalTicks = v)
				.setTooltip(Component.literal("How many ticks between updates for the farthest throttled items/orbs. "
						+ "Physics, stack merging and despawning still happen, just less often - nothing freezes permanently."))
				.build());

		ConfigCategory particles = builder.getOrCreateCategory(Component.literal("Particles"));
		particles.addEntry(entry.startTextDescription(note(
				"Works alongside vanilla's own Particles video setting (All/Decreased/Minimal), not instead of it - "
						+ "if you already run Minimal, the extra effect here will be smaller. This adds a distance cutoff "
						+ "vanilla's setting doesn't have, on top of the density reduction."))
				.build());
		particles.addEntry(entry.startBooleanToggle(Component.literal("Enable particle reduction"), config.enableParticleReduction)
				.setDefaultValue(true)
				.setSaveConsumer(v -> config.enableParticleReduction = v)
				.setTooltip(Component.literal("Drops a fraction of particle spawns and culls particles spawned far from the camera."))
				.build());
		particles.addEntry(entry.startDoubleField(Component.literal("Particle density (0-1)"), config.particleDensityMultiplier)
				.setMin(0.0).setMax(1.0)
				.setDefaultValue(0.35)
				.setSaveConsumer(v -> config.particleDensityMultiplier = v)
				.setTooltip(Component.literal("Fraction of particles actually allowed to spawn. 1.0 = vanilla amount, 0.0 = none."))
				.build());
		particles.addEntry(entry.startDoubleField(Component.literal("Particle culling distance"), config.particleCullDistance)
				.setMin(2.0).setMax(128.0)
				.setDefaultValue(24.0)
				.setSaveConsumer(v -> config.particleCullDistance = v)
				.setTooltip(Component.literal("Particles requested farther than this from the camera are always dropped."))
				.build());

		ConfigCategory ber = builder.getOrCreateCategory(Component.literal("Block Entity Renderers"));
		ber.addEntry(entry.startBooleanToggle(Component.literal("Limit block entity renderer distance"), config.enableBlockEntityRendererCulling)
				.setDefaultValue(true)
				.setSaveConsumer(v -> config.enableBlockEntityRendererCulling = v)
				.setTooltip(Component.literal("Shortens the render distance for chest lids, sign text, skulls, banners, etc."))
				.build());
		ber.addEntry(entry.startDoubleField(Component.literal("Renderer distance multiplier (0.1-1.0)"), config.blockEntityRendererDistanceMultiplier)
				.setMin(0.1).setMax(1.0)
				.setDefaultValue(0.35)
				.setSaveConsumer(v -> config.blockEntityRendererDistanceMultiplier = v)
				.setTooltip(Component.literal("Multiplier applied to vanilla's default 64-block block entity renderer view distance."))
				.build());

		ConfigCategory compat = builder.getOrCreateCategory(Component.literal("Compatibility"));
		compat.addEntry(entry.startTextDescription(note(
				"Boosters throttles game logic and culls what gets sent to the renderer - it does not replace "
						+ "the chunk renderer itself. For the biggest FPS gain from world rendering, pair it with Embeddium."))
				.build());
		compat.addEntry(entry.startBooleanToggle(Component.literal("Defer to other culling mods"), config.deferToDedicatedEntityCullingMods)
				.setDefaultValue(true)
				.setSaveConsumer(v -> config.deferToDedicatedEntityCullingMods = v)
				.setTooltip(Component.literal("When a dedicated entity culling mod is detected (e.g. EntityCulling), Boosters disables its own to avoid duplicating it."))
				.build());
		compat.addEntry(entry.startBooleanToggle(Component.literal("Extra aggressive with Embeddium"), config.extraAggressiveWithEmbeddium)
				.setDefaultValue(true)
				.setSaveConsumer(v -> config.extraAggressiveWithEmbeddium = v)
				.setTooltip(Component.literal("When Embeddium is detected, pulls all distance thresholds in by 25% - Embeddium removes the GPU bottleneck, so throttling the remaining CPU-side work harder pays off more."))
				.build());

		ConfigCategory debug = builder.getOrCreateCategory(Component.literal("Debug"));
		debug.addEntry(entry.startBooleanToggle(Component.literal("Show F3 status line"), config.showF3Status)
				.setDefaultValue(true)
				.setSaveConsumer(v -> config.showF3Status = v)
				.setTooltip(Component.literal("Shows live per-second throttle/culling counters on the F3 debug screen."))
				.build());

		return builder.build();
	}
}
