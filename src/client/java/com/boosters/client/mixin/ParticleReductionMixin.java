package com.boosters.client.mixin;

import com.boosters.BoostersConfig;
import com.boosters.BoostersStats;
import com.boosters.compat.ModCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Drops a fraction of particle spawns (density multiplier) and skips particles
 * spawned far from the camera entirely, since createParticle legitimately
 * returning null is part of the vanilla contract (see e.g. no-op providers).
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleReductionMixin {

	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
	private void boosters$reduceParticles(ParticleOptions options, double x, double y, double z,
			double dx, double dy, double dz, CallbackInfoReturnable<Particle> cir) {
		BoostersConfig config = BoostersConfig.get();
		if (!config.enableParticleReduction) {
			return;
		}

		Player player = Minecraft.getInstance().player;
		if (player != null) {
			double distSq = player.distanceToSqr(x, y, z);
			double cullDist = config.particleCullDistance * ModCompat.distanceMultiplier();
			double cullSq = cullDist * cullDist;
			if (distSq > cullSq) {
				BoostersStats.incrementParticlesDropped();
				cir.setReturnValue(null);
				return;
			}
		}

		if (config.particleDensityMultiplier < 1.0
				&& ThreadLocalRandom.current().nextDouble() >= config.particleDensityMultiplier) {
			BoostersStats.incrementParticlesDropped();
			cir.setReturnValue(null);
		}
	}
}
