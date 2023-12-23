package dev.bluevista.contrail;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Config(name = ContrailMod.MODID)
@Environment(EnvType.CLIENT)
public class ContrailConfig implements ConfigData {

	public static ContrailConfig getInstance() {
		return AutoConfig.getConfigHolder(ContrailConfig.class).getConfig();
	}

	boolean enabled = true;

	@ConfigEntry.BoundedDiscrete(min = 1, max = 16)
	int maxTrailCount = 3;

	@ConfigEntry.BoundedDiscrete(min = 1, max = 100)
	@ConfigEntry.Gui.Tooltip
	int trailChance = 10;

	@ConfigEntry.BoundedDiscrete(min = 10, max = 5000)
	int length = 600;

	@ConfigEntry.BoundedDiscrete(min = 256, max = 512)
	int height = 300;

	@ConfigEntry.BoundedDiscrete(min = 1, max = 200)
	int speed = 40;

	@ConfigEntry.BoundedDiscrete(min = 1, max = 25)
	int thickness = 10;

}
