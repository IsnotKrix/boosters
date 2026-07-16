package com.boosters;

import com.boosters.client.BoostersConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers.
@Mod(value = BoostersMod.MODID, dist = Dist.CLIENT)
// @EventBusSubscriber only reaches the game event bus (NeoForge.EVENT_BUS); RegisterKeyMappingsEvent
// is a mod-bus event, so it's registered explicitly below via the injected modEventBus instead.
@EventBusSubscriber(modid = BoostersMod.MODID, value = Dist.CLIENT)
public class BoostersModClient {

	private static KeyMapping openConfigKey;
	private static int statsTickCounter;

	public BoostersModClient(IEventBus modEventBus, ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class,
				(c, parent) -> BoostersConfigScreen.build(parent));
		modEventBus.addListener(BoostersModClient::onRegisterKeyMappings);
	}

	private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		openConfigKey = new KeyMapping(
				"key.boosters.open_config",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_SEMICOLON,
				KeyMapping.Category.MISC);
		event.register(openConfigKey);
	}

	@SubscribeEvent
	static void onClientTick(ClientTickEvent.Post event) {
		Minecraft client = Minecraft.getInstance();

		while (openConfigKey.consumeClick()) {
			if (client.canInterruptScreen()) {
				client.setScreenAndShow(BoostersConfigScreen.build(null));
			}
		}

		if (++statsTickCounter >= 20) {
			statsTickCounter = 0;
			BoostersStats.snapshotAndReset();
		}
	}
}
