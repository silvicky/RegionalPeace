package io.silvicky.peace;

import io.silvicky.peace.command.Peace;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionalPeace implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "RegionalPeace";
    public static final Logger LOGGER = LoggerFactory.getLogger("regional-peace");
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Loading RegionalPeace...");
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Peace.register(dispatcher));
	}
}