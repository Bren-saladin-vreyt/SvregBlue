package com.svreg.blue.mixin;

import com.svreg.blue.look.BlueGrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * With SvregSky: grade right before SvregSky's own screen look (film tint, painterly, anime, pixel art...),
 * so those effects sit on top of the blue world. SvregSky calls this every frame, also when its looks are off.
 * When SvregSky isn't installed this mixin simply has nothing to attach to.
 */
@Pseudo
@Mixin(targets = "com.svreg.sky.look.PostLook", remap = false)
public abstract class SvregSkyPostLookMixin {
	@Inject(method = "render()V", at = @At("HEAD"), require = 0, remap = false)
	private static void svregblue$grade(CallbackInfo ci) {
		BlueGrade.render();
	}
}
