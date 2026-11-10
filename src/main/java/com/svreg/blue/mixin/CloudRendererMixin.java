package com.svreg.blue.mixin;

import com.svreg.blue.SvregBlue;
import com.svreg.blue.look.BlueGrade;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Without SvregSky: grade the world right before the clouds are drawn (after terrain, sky and entities). */
@Mixin(CloudRenderer.class)
public abstract class CloudRendererMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void svregblue$grade(int color, CloudStatus cloudStatus, float bottomY, int range, Vec3 cameraPosition, long gameTime, float partialTicks, CallbackInfo ci) {
		if (!SvregBlue.withSky) {
			BlueGrade.render();
		}
	}
}
