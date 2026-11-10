package com.svreg.blue.look;

import com.svreg.blue.config.BlueConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

/** How blue the world should be right now (0..1), from the time of day and the chosen "Jam Biru" mode. */
public final class BlueTime {
	private BlueTime() {
	}

	/** height of the sun: 1 at noon, 0 at sunrise/sunset, -1 at midnight (from the overworld clock) */
	public static float sunHeight(Minecraft mc) {
		long t = Math.floorMod(mc.level.getOverworldClockTime(), 24000L);
		double phase = t / 24000.0 - 0.25; // tick 6000 = noon
		return (float) Math.cos(phase * Math.PI * 2);
	}

	/** 0..1 blue amount before the strength setting */
	public static float amount() {
		Minecraft mc = Minecraft.getInstance();
		if (!BlueConfig.enabled || mc.level == null || mc.level.dimension() != Level.OVERWORLD) {
			return 0;
		}
		float y = sunHeight(mc);
		return switch (BlueConfig.mode) {
			case "always" -> 1;
			case "night" -> smooth(0.0F, -0.22F, y);
			default -> smooth(0.30F, -0.06F, y); // starts while the sun is still low, full a little after sunset
		};
	}

	/** the final strength used by the grade and the fog */
	public static float level() {
		return amount() * BlueConfig.strength;
	}

	static float smooth(float e0, float e1, float x) {
		float t = Math.max(0, Math.min(1, (x - e0) / (e1 - e0)));
		return t * t * (3 - 2 * t);
	}
}
