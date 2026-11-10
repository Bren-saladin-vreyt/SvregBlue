package com.svreg.blue.mixin;

import com.svreg.blue.look.BlueFog;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Kabut Biru: pulls the overworld fog in closer during the blue hour */
@Mixin(value = AtmosphericFogEnvironment.class, priority = 1100)
public abstract class FogSetupMixin {
	@Inject(method = "setupFog", at = @At("TAIL"))
	private void svregblue$thickFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
		BlueFog.apply(fog, camera, level, renderDistance);
	}
}
