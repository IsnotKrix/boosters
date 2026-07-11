package com.boosters.client;

import com.boosters.BoostersConfig;
import com.boosters.BoostersMod;
import com.boosters.BoostersStats;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;

public class BoostersModClient implements ClientModInitializer {

	private static KeyMapping openConfigKey;
	private static int statsTickCounter;

	@Override
	public void onInitializeClient() {
		// Client-side rendering hooks (entity culling, particle throttling, F3 line) live in the mixin package.

		openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.boosters.open_config",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_SEMICOLON,
				KeyMapping.Category.MISC));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.consumeClick()) {
				if (client.canInterruptScreen()) {
					client.setScreenAndShow(BoostersConfigScreen.build(null));
				}
			}

			if (++statsTickCounter >= 20) {
				statsTickCounter = 0;
				BoostersStats.snapshotAndReset();
			}
		});

		// Honest "RAM optimizer": when the player leaves a world back to the menu, hint the
		// JVM to release the now-unused world/chunk/entity memory back to the OS. This fires
		// only on disconnect - never during gameplay - so it can't cause an in-game stutter.
		// It's a modest cleanup, not a magic FPS trick (deep in-game RAM work is FerriteCore's job).
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (BoostersConfig.get().enableMenuMemoryCleanup) {
				System.gc();
				BoostersMod.LOGGER.info("Left world - hinted a memory cleanup to return unused RAM to the OS.");
			}
		});
	}
}
