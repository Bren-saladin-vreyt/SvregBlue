package com.svreg.blue;

import com.svreg.blue.config.BlueConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvregBlue implements ClientModInitializer {
	public static final String MOD_ID = "svregblue";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

	/** true when SvregSky is installed too: the grade then runs from SvregSky's screen-look hook */
	public static boolean withSky;

	@Override
	public void onInitializeClient() {
		withSky = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("svregsky");
		BlueConfig.load();
		LOG.info("SvregBlue loaded (SvregSky present: {})", withSky);
	}
}
