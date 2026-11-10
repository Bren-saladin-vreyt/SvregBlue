package com.svreg.blue.mixin;

import com.svreg.blue.look.BlueGrade;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** distance fog leans to the blue-hour haze; works on top of SvregSky's own fog colour */
@Mixin(value = AtmosphericFogEnvironment.class, priority = 1100)
public abstract class FogColorMixin {
	@Inject(method = "getBaseColor", at = @At("TAIL"), cancellable = true)
	private void svregblue$fogColor(ClientLevel level, Camera camera, int renderDistance, float partialTicks, CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(BlueGrade.tintFog(cir.getReturnValueI()));
	}
}
