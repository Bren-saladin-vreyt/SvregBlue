package com.svreg.blue.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.svreg.blue.SvregBlue;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** SvregBlue settings, saved to config/svregblue.json. */
public final class BlueConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/** the saved shape; field names are the json keys */
	private static final class Data {
		boolean enabled = true;
		String mode = "dusk_night";
		int strength = 80;
		int lift = 60;
		boolean fog = true;
		boolean warmLights = true;
	}

	public static boolean enabled = true;
	/** dusk_night, night or always */
	public static String mode = "dusk_night";
	/** 0..1: how far the world is pulled toward the blue palette */
	public static float strength = 0.80F;
	/** 0..1: how much pure black is lifted to navy */
	public static float lift = 0.60F;
	public static boolean fog = true;
	public static boolean warmLights = true;

	private BlueConfig() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("svregblue.json");
	}

	public static void load() {
		try {
			Path f = file();
			if (!Files.exists(f)) {
				save();
				return;
			}
			Data d = GSON.fromJson(Files.readString(f), Data.class);
			if (d == null) {
				return;
			}
			enabled = d.enabled;
			mode = switch (d.mode == null ? "" : d.mode) {
				case "night", "always" -> d.mode;
				default -> "dusk_night";
			};
			strength = clamp(d.strength) / 100F;
			lift = clamp(d.lift) / 100F;
			fog = d.fog;
			warmLights = d.warmLights;
		} catch (Exception e) {
			SvregBlue.LOG.warn("SvregBlue: could not read the config, using defaults", e);
		}
	}

	public static void save() {
		try {
			Data d = new Data();
			d.enabled = enabled;
			d.mode = mode;
			d.strength = Math.round(strength * 100);
			d.lift = Math.round(lift * 100);
			d.fog = fog;
			d.warmLights = warmLights;
			Files.createDirectories(file().getParent());
			Files.writeString(file(), GSON.toJson(d));
		} catch (Exception e) {
			SvregBlue.LOG.warn("SvregBlue: could not save the config", e);
		}
	}

	private static int clamp(int v) {
		return Math.max(0, Math.min(100, v));
	}
}
