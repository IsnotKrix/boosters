package com.boosters;

import com.boosters.compat.ModCompat;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(BoostersMod.MODID)
public class BoostersMod {
	public static final String MODID = "boosters";
	public static final Logger LOGGER = LogUtils.getLogger();

	public BoostersMod(IEventBus modEventBus, ModContainer modContainer) {
		BoostersConfig.load();
		ModCompat.init();
		LOGGER.info("Boosters loaded - AI throttle interval={} ticks, block entity throttle interval={} ticks",
				BoostersConfig.get().aiThrottleIntervalTicks,
				BoostersConfig.get().blockEntityThrottleIntervalTicks);
	}
}
