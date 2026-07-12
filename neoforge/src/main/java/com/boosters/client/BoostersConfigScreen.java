package com.boosters.client;

import com.boosters.BoostersConfig;
import com.boosters.BoostersPreset;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

/**
 * Builds the Cloth Config screen mapping every {@link BoostersConfig} field to
 * a toggle/slider/field, split into one category per optimization feature.
 */
public final class BoostersConfigScreen {
	private BoostersConfigScreen() {
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
				.setTitle(Component.literal("Boosters"))
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
		presets.addEntry(entry.startEnumSelector(Component.literal("Preset"), BoostersPreset.class, config.preset)
				.setEnumNameProvider(p -> Component.literal(((BoostersPreset) p).displayName()))
				.setDefaultValue(BoostersPreset.PERFORMANCE)
				.setSaveConsumer(v -> selectedPreset[0] = v)
				.setTooltip(Component.literal("Quality = near-vanilla, small gain. Extreme = max FPS, most visible culling. "
						+ "Pick one and save, then reopen the screen to see the values it applied. "
						+ "Editing any value by hand switches this to Custom."))
				.build());
		presets.addEntry(entry.startTextDescription(Component.literal(
				"Presets set every distance/interval/density below at once. The individual tabs let you fine-tune "
						+ "from there (which turns the preset into Custom)."))
				.build());

		ConfigCategory ai = builder.getOrCreateCategory(Component.literal("Mob AI"));
		ai.addEntry(entry.startBooleanToggle(Component.literal("Enable AI throttling"), config.enableAiThrottle)
				.setSaveConsumer(v -> config.enableAiThrottle = v)
				.setTooltip(Component.literal("Distant mobs update sensing/AI/navigation less often."))
				.build());
		ai.addEntry(entry.startIntSlider(Component.literal("Throttle start distance"), config.aiThrottleStartDistance, 4, 128)
				.setSaveConsumer(v -> config.aiThrottleStartDistance = v)
				.build());
		ai.addEntry(entry.startIntSlider(Component.literal("Throttle max distance"), config.aiThrottleMaxDistance, 16, 256)
				.setSaveConsumer(v -> config.aiThrottleMaxDistance = v)
				.build());
		ai.addEntry(entry.startIntSlider(Component.literal("Interval (nearby, ticks)"), config.aiThrottleIntervalTicks, 1, 20)
				.setSaveConsumer(v -> config.aiThrottleIntervalTicks = v)
				.build());
		ai.addEntry(entry.startIntSlider(Component.literal("Interval (far away, ticks)"), config.aiThrottleMaxIntervalTicks, 1, 40)
				.setSaveConsumer(v -> config.aiThrottleMaxIntervalTicks = v)
				.build());

		ConfigCategory entities = builder.getOrCreateCategory(Component.literal("Entity Rendering"));
		entities.addEntry(entry.startBooleanToggle(Component.literal("Enable entity culling"), config.enableEntityCulling)
				.setSaveConsumer(v -> config.enableEntityCulling = v)
				.build());
		entities.addEntry(entry.startDoubleField(Component.literal("Max render distance"), config.entityCullingMaxDistance)
				.setMin(8.0).setMax(512.0)
				.setSaveConsumer(v -> config.entityCullingMaxDistance = v)
				.build());
		entities.addEntry(entry.startDoubleField(Component.literal("Nametag culling distance"), config.entityDetailCullDistance)
				.setMin(4.0).setMax(256.0)
				.setSaveConsumer(v -> config.entityDetailCullDistance = v)
				.build());

		ConfigCategory blockEntities = builder.getOrCreateCategory(Component.literal("Block Entities"));
		blockEntities.addEntry(entry.startBooleanToggle(Component.literal("Enable block entity throttling"), config.enableBlockEntityThrottle)
				.setSaveConsumer(v -> config.enableBlockEntityThrottle = v)
				.build());
		blockEntities.addEntry(entry.startIntSlider(Component.literal("Throttle start distance"), config.blockEntityThrottleStartDistance, 4, 128)
				.setSaveConsumer(v -> config.blockEntityThrottleStartDistance = v)
				.build());
		blockEntities.addEntry(entry.startIntSlider(Component.literal("Max interval (ticks)"), config.blockEntityThrottleIntervalTicks, 1, 40)
				.setSaveConsumer(v -> config.blockEntityThrottleIntervalTicks = v)
				.build());
		blockEntities.addEntry(entry.startStrList(Component.literal("Excluded types (substring)"), new ArrayList<>(config.blockEntityThrottleExcludeTypes))
				.setSaveConsumer(v -> config.blockEntityThrottleExcludeTypes = new ArrayList<>(v))
				.setTooltip(Component.literal("E.g. hopper, piston, beacon - these types always tick normally."))
				.build());

		ConfigCategory particles = builder.getOrCreateCategory(Component.literal("Particles"));
		particles.addEntry(entry.startBooleanToggle(Component.literal("Enable particle reduction"), config.enableParticleReduction)
				.setSaveConsumer(v -> config.enableParticleReduction = v)
				.build());
		particles.addEntry(entry.startDoubleField(Component.literal("Particle density (0-1)"), config.particleDensityMultiplier)
				.setMin(0.0).setMax(1.0)
				.setSaveConsumer(v -> config.particleDensityMultiplier = v)
				.build());
		particles.addEntry(entry.startDoubleField(Component.literal("Particle culling distance"), config.particleCullDistance)
				.setMin(2.0).setMax(128.0)
				.setSaveConsumer(v -> config.particleCullDistance = v)
				.build());

		ConfigCategory ber = builder.getOrCreateCategory(Component.literal("Block Entity Renderers"));
		ber.addEntry(entry.startBooleanToggle(Component.literal("Limit block entity renderer distance"), config.enableBlockEntityRendererCulling)
				.setSaveConsumer(v -> config.enableBlockEntityRendererCulling = v)
				.setTooltip(Component.literal("Shortens the render distance for chest lids, sign text, skulls, banners, etc."))
				.build());
		ber.addEntry(entry.startDoubleField(Component.literal("Renderer distance multiplier (0.1-1.0)"), config.blockEntityRendererDistanceMultiplier)
				.setMin(0.1).setMax(1.0)
				.setSaveConsumer(v -> config.blockEntityRendererDistanceMultiplier = v)
				.build());

		ConfigCategory compat = builder.getOrCreateCategory(Component.literal("Compatibility"));
		compat.addEntry(entry.startTextDescription(Component.literal(
				"Boosters throttles game logic and culls what gets sent to the renderer - it does not replace "
						+ "the chunk renderer itself. For the biggest FPS gain from world rendering, pair it with Embeddium."))
				.build());
		compat.addEntry(entry.startBooleanToggle(Component.literal("Defer to other culling mods"), config.deferToDedicatedEntityCullingMods)
				.setSaveConsumer(v -> config.deferToDedicatedEntityCullingMods = v)
				.setTooltip(Component.literal("When a dedicated entity culling mod is detected (e.g. EntityCulling), Boosters disables its own to avoid duplicating it."))
				.build());
		compat.addEntry(entry.startBooleanToggle(Component.literal("Extra aggressive with Embeddium"), config.extraAggressiveWithEmbeddium)
				.setSaveConsumer(v -> config.extraAggressiveWithEmbeddium = v)
				.setTooltip(Component.literal("When Embeddium is detected, pulls all distance thresholds in by 25% - Embeddium removes the GPU bottleneck, so throttling the remaining CPU-side work harder pays off more."))
				.build());

		ConfigCategory memory = builder.getOrCreateCategory(Component.literal("Memory"));
		memory.addEntry(entry.startBooleanToggle(Component.literal("Cleanup memory when leaving a world"), config.enableMenuMemoryCleanup)
				.setSaveConsumer(v -> config.enableMenuMemoryCleanup = v)
				.setTooltip(Component.literal("Releases unused memory back to the OS when you return to the main menu. "
						+ "Runs only on disconnect, never during gameplay, so it can't cause a stutter. "
						+ "Modest cleanup - for deep in-game RAM optimization, pair with FerriteCore."))
				.build());

		ConfigCategory debug = builder.getOrCreateCategory(Component.literal("Debug"));
		debug.addEntry(entry.startBooleanToggle(Component.literal("Show F3 status line"), config.showF3Status)
				.setSaveConsumer(v -> config.showF3Status = v)
				.setTooltip(Component.literal("Shows live per-second throttle/culling counters on the F3 debug screen."))
				.build());

		return builder.build();
	}
}
