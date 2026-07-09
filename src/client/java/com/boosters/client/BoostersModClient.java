package com.boosters.client;

import com.boosters.BoostersStats;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
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
	}
}
