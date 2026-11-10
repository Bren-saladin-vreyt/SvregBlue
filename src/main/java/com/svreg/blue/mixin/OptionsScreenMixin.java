package com.svreg.blue.mixin;

import com.svreg.blue.SvregBlue;
import com.svreg.blue.config.BlueScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
	protected OptionsScreenMixin(Component title) {
		super(title);
	}

	/** a "SvregBlue" button in the top-left of Options, just under SvregSky's (or in its place when SvregSky is absent) */
	@Inject(method = "init", at = @At("TAIL"))
	private void svregblue$button(CallbackInfo ci) {
		Screen self = this;
		int y = SvregBlue.withSky ? 30 : 6;
		this.addRenderableWidget(Button.builder(Component.translatable("svregblue.options.button"),
				b -> this.minecraft.gui.setScreen(new BlueScreen(self, this.minecraft.options))).bounds(6, y, 98, 20).build());
	}
}
