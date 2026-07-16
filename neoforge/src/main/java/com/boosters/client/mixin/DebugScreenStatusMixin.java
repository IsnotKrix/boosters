package com.boosters.client.mixin;

import com.boosters.BoostersConfig;
import com.boosters.BoostersStats;
import com.boosters.compat.ModCompat;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Appends a live status line to the F3 debug screen's world-info column showing
 * what Boosters is actually doing right now (not just that it's installed) - how
 * many AI steps/block entity ticks/entities/particles it skipped in the last second.
 *
 * <p>{@code extractLines} runs every frame regardless of whether the debug screen is
 * actually visible - Gui#extractRenderState builds it unconditionally as part of the
 * normal HUD state pass, it's only the drawing that's conditional. Without an explicit
 * {@link #showDebugScreen()} check here, this used to string-concatenate 4 lines and
 * allocate a new list entry on every single rendered frame, forever, even with F3
 * closed - a much bigger, always-on cost than the (already fixed) per-tick throttle
 * checks. This was almost certainly the actual cause of the measured FPS regression.
 */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenStatusMixin {

	@Shadow
	public abstract boolean showDebugScreen();

	// Computed once and cached - looking this up is cheap, but extractLines runs every
	// rendered frame, and a mod-container lookup has no reason to happen more than once.
	@Unique
	private static String boosters$versionLine;

	// "boosters <mod version>+<minecraft version>-neoforge", e.g. "boosters 1.2.0+26.2-neoforge".
	private static String boosters$versionLine() {
		if (boosters$versionLine == null) {
			String modVersion = ModList.get().getModContainerById("boosters")
					.map(c -> c.getModInfo().getVersion().toString())
					.orElse("?");
			String mcVersion = SharedConstants.getCurrentVersion().name();
			boosters$versionLine = "boosters " + modVersion + "+" + mcVersion + "-neoforge";
		}
		return boosters$versionLine;
	}

	@Inject(method = "extractLines", at = @At("HEAD"))
	private void boosters$appendStatusLine(GuiGraphicsExtractor extractor, List<String> lines, boolean isLeftColumn,
			CallbackInfo ci) {
		if (isLeftColumn || !showDebugScreen() || !BoostersConfig.get().showF3Status) {
			return;
		}

		BoostersConfig config = BoostersConfig.get();
		if (!config.enabled) {
			lines.add("");
			lines.add("[Boosters] " + boosters$versionLine() + " - disabled (master switch off)");
			return;
		}

		lines.add("");
		lines.add("[Boosters] " + boosters$versionLine() + " - preset: " + config.preset.displayName());
		lines.add("[Boosters] AI throttled: " + BoostersStats.aiStepsSkippedPerSecond() + "/s, "
				+ "block entities throttled: " + BoostersStats.blockEntityTicksSkippedPerSecond() + "/s");
		lines.add("[Boosters] Items/XP orbs throttled: " + BoostersStats.itemTicksSkippedPerSecond() + "/s");
		lines.add("[Boosters] Entities culled: " + BoostersStats.entitiesCulledPerSecond() + "/s, "
				+ "particles dropped: " + BoostersStats.particlesDroppedPerSecond() + "/s");
		lines.add("[Boosters] Detected: " + ModCompat.detectedModsSummary()
				+ " - entity culling deferred: " + (ModCompat.shouldDeferEntityCulling() ? "yes" : "no"));
	}
}
