package com.boosters.client;

import com.boosters.BoostersConfig;
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

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("Boosters"))
				.setSavingRunnable(BoostersConfig::save);

		ConfigEntryBuilder entry = builder.entryBuilder();

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
		compat.addEntry(entry.startBooleanToggle(Component.literal("Defer to other culling mods"), config.deferToDedicatedEntityCullingMods)
				.setSaveConsumer(v -> config.deferToDedicatedEntityCullingMods = v)
				.setTooltip(Component.literal("When a dedicated entity culling mod is detected (e.g. EntityCulling), Boosters disables its own to avoid duplicating it."))
				.build());

		ConfigCategory debug = builder.getOrCreateCategory(Component.literal("Debug"));
		debug.addEntry(entry.startBooleanToggle(Component.literal("Show F3 status line"), config.showF3Status)
				.setSaveConsumer(v -> config.showF3Status = v)
				.setTooltip(Component.literal("Shows live per-second throttle/culling counters on the F3 debug screen."))
				.build());

		return builder.build();
	}
}
