package dev.bluevista.contrail;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
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
	private static final List<Contrail> trails = new ArrayList<>();

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
		var config = ContrailConfig.getInstance();

		if (!config.enabled) {
			trails.clear();
			return;
		}

		if (config.maxTrailCount < trails.size()) {
			for (int i = 0; i < trails.size() - config.maxTrailCount; i++) {
				trails.remove(i);
			}
		}

		if (
			MinecraftClient.getInstance().player.age % 20 == 0 &&
			trails.size() < config.maxTrailCount &&
			world.getRandom().nextDouble() <= config.trailChance * 0.01f
		) {
			trails.add(new Contrail());
		}

		int viewDistance = MinecraftClient.getInstance().options.getClampedViewDistance() * 16 * 2;

		for (var trail : List.copyOf(trails)) {
			if (trail.getHorizontalDistanceFromPlayer() > config.length + viewDistance) {
				trails.remove(trail);
			} else {
				trail.tick();
			}
		}
	}

	private void onWorldRender(WorldRenderContext ctx) {
		trails.forEach(trail -> trail.render(ctx));
	}

}
