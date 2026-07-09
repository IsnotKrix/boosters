package com.boosters.client.mixin;

import com.boosters.BoostersConfig;
import com.boosters.compat.ModCompat;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scales down the default 64-block view distance used by
 * {@link BlockEntityRenderer#shouldRender} for expensive block entity renderers
 * (chests, signs with text, skulls, banners, ...). This is distinct from chunk
 * (block model) rendering, which vanilla/Sodium already culls well - BERs run
 * extra per-frame draw calls on top of the block model and scale with how many
 * are simultaneously visible (bases, villages, storage rooms).
 */
@Mixin(BlockEntityRenderer.class)
public interface BlockEntityRendererDistanceMixin {

	@Inject(method = "getViewDistance", at = @At("HEAD"), cancellable = true)
	private void boosters$scaleViewDistance(CallbackInfoReturnable<Integer> cir) {
		BoostersConfig config = BoostersConfig.get();
		if (!config.enableBlockEntityRendererCulling) {
			return;
		}

		int scaled = (int) Math.max(8, Math.round(64 * config.blockEntityRendererDistanceMultiplier * ModCompat.distanceMultiplier()));
		cir.setReturnValue(scaled);
	}
}
