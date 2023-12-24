package dev.bluevista.contrail;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ContrailMod implements ClientModInitializer {

	public static final String MODID = "contrail";
	public static final Logger LOGGER = LogManager.getLogger("Contrail");
	public static final List<Contrail> CONTRAILS = new ArrayList<>();

	public static boolean isDev() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public void onInitializeClient() {
		LOGGER.info("Chemtrails are real.");

		// Events
		ClientTickEvents.START_WORLD_TICK.register(this::onWorldTick);
		WorldRenderEvents.AFTER_SETUP.register(this::onWorldRender);

		// Config
		AutoConfig.register(ContrailConfig.class, GsonConfigSerializer::new);
	}

	private void onWorldTick(ClientWorld world) {
		var client = MinecraftClient.getInstance();
		var config = ContrailConfig.getInstance();

		// if contrails are disabled, or the max number of contrails was decreased, clear all contrails
		if (!config.enabled || config.maxTrailCount < CONTRAILS.size()) {
			CONTRAILS.clear();
			if (isDev()) LOGGER.info("All contrails have been cleared.");
			return;
		}

		// try to spawn a new contrail
		if (
			client.player != null &&
			client.player.age % 20 == 0 &&
			CONTRAILS.size() < config.maxTrailCount &&
			world.getRandom().nextDouble() <= config.trailChance * 0.01f
		) {
			CONTRAILS.add(new Contrail());
		}

		// copy to avoid concurrency problems
		List.copyOf(CONTRAILS).forEach(contrail -> contrail.tick(world));
	}

	private void onWorldRender(WorldRenderContext ctx) {
		CONTRAILS.forEach(trail -> trail.render(ctx));
	}

}
