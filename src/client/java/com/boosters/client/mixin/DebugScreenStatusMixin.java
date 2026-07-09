package com.boosters.client.mixin;

import com.boosters.BoostersConfig;
import com.boosters.BoostersStats;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Appends a live status line to the F3 debug screen's world-info column showing
 * what Boosters is actually doing right now (not just that it's installed) - how
 * many AI steps/block entity ticks/entities/particles it skipped in the last second.
 * extractLines(..., false) builds the right-hand (world info) column; injecting at
 * HEAD lets us append before vanilla renders the already-populated line list.
 */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenStatusMixin {

	@Inject(method = "extractLines", at = @At("HEAD"))
	private void boosters$appendStatusLine(GuiGraphicsExtractor extractor, List<String> lines, boolean isLeftColumn,
			CallbackInfo ci) {
		if (isLeftColumn || !BoostersConfig.get().showF3Status) {
			return;
		}

		lines.add("");
		lines.add("[Boosters] AI throttled: " + BoostersStats.aiStepsSkippedPerSecond() + "/s, "
				+ "block entities throttled: " + BoostersStats.blockEntityTicksSkippedPerSecond() + "/s");
		lines.add("[Boosters] Entities culled: " + BoostersStats.entitiesCulledPerSecond() + "/s, "
				+ "particles dropped: " + BoostersStats.particlesDroppedPerSecond() + "/s");
	}
}
