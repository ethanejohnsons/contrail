package dev.bluevista.contrail;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Config(name = ContrailMod.MODID)
public class ContrailConfig implements ConfigData {

	public static ContrailConfig getInstance() {
		return AutoConfig.getConfigHolder(ContrailConfig.class).getConfig();
	}

	boolean enabled = true;

	@ConfigEntry.BoundedDiscrete(min = 1, max = 64)
	int maxTrailCount = 3;

	@ConfigEntry.BoundedDiscrete(min = 1, max = 100)
	@ConfigEntry.Gui.Tooltip
	int trailChance = 10;

	@ConfigEntry.BoundedDiscrete(min = 200, max = 2_000)
	int length = 600;

	@ConfigEntry.BoundedDiscrete(min = 200, max = 1_000)
	int height = 300;

	@ConfigEntry.BoundedDiscrete(min = 1, max = 200)
	int speed = 40;

	@ConfigEntry.BoundedDiscrete(min = 1, max = 50)
	int thickness = 10;

}
