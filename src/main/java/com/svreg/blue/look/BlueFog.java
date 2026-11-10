package com.svreg.blue.look;

import com.svreg.blue.config.BlueConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;

/**
 * Kabut Biru: thick blue-hour fog like the foggy forest road reference. Trees and hills fade into the haze a few
 * dozen blocks out, so the world gets that soft "everything melts into blue" depth. Only the distances change
 * (the colour comes from the fog tint), so it costs nothing on the phone.
 */
public final class BlueFog {
	/** 1 under open sky, eases toward 0.35 in caves and under roofs so you don't go blind indoors */
	private static float open = 1;
	private static long lastNanos;

	private BlueFog() {
	}

	public static void apply(FogData fog, Camera camera, ClientLevel level, float renderDistance) {
		if (!BlueConfig.thickFog || BlueConfig.fogDensity <= 0.001F) {
			return;
		}
		float blue = BlueTime.amount();
		if (blue <= 0.001F) {
			return;
		}
		// ease the indoor/outdoor change over ~1.5 s
		long now = System.nanoTime();
		float dt = lastNanos == 0 ? 0 : Math.min(0.2F, (now - lastNanos) / 1e9F);
		lastNanos = now;
		float target = level.canSeeSky(camera.blockPosition()) ? 1 : 0.35F;
		open += (target - open) * Math.min(1, dt * 1.5F);

		float d = BlueConfig.fogDensity * open;
		float k = blue * Math.min(1, 0.4F + BlueConfig.strength); // follows the grade, never weaker than 40%
		// thickest setting: haze from 6 blocks, gone by 44; thinnest: just a soft veil near the render distance
		float end = lerp(renderDistance, 44, d);
		float start = lerp(end * 0.6F, 6, d);
		float newEnd = lerp(fog.environmentalEnd, end, k);
		float newStart = lerp(fog.environmentalStart, start, k);
		// rain fog can already be thicker: keep whichever is closer
		fog.environmentalEnd = Math.min(fog.environmentalEnd, newEnd);
		fog.environmentalStart = Math.min(fog.environmentalStart, Math.min(newStart, fog.environmentalEnd - 1));
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}
}
