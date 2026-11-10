package com.svreg.blue.config;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/** SvregBlue settings, opened from the "SvregBlue" button in Options. Changes apply live and are saved on close. */
public class BlueScreen extends OptionsSubScreen {
	public BlueScreen(Screen lastScreen, Options options) {
		super(lastScreen, options, Component.translatable("svregblue.options.title"));
	}

	private static Component boolText(String key, boolean v) {
		return Component.empty()
				.append(Component.literal(v ? "ON " : "OFF ").withStyle(v ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
				.append(Component.translatable("svregblue.options." + key));
	}

	/** "ON name" / "OFF name" switch, state first so it reads at once on a phone */
	private static AbstractWidget bool(String key, boolean value, Consumer<Boolean> set) {
		boolean[] state = {value};
		Button b = Button.builder(boolText(key, value), btn -> {
			state[0] = !state[0];
			set.accept(state[0]);
			btn.setMessage(boolText(key, state[0]));
		}).width(150).build();
		String k = "svregblue.options." + key + ".tooltip";
		if (Language.getInstance().has(k)) {
			b.setTooltip(Tooltip.create(Component.translatable(k)));
		}
		return b;
	}

	private static <T> OptionInstance.TooltipSupplier<T> tip(String key) {
		String k = "svregblue.options." + key + ".tooltip";
		return Language.getInstance().has(k) ? OptionInstance.cachedConstantTooltip(Component.translatable(k)) : OptionInstance.noTooltip();
	}

	private static OptionInstance<Integer> slider(String key, int min, int max, int value, IntFunction<String> label, OptionInstance.ValueUpdateListener<Integer> set) {
		return new OptionInstance<>("svregblue.options." + key, tip(key),
				(caption, v) -> Component.empty().append(Component.literal(label.apply(v) + " ").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)).append(caption),
				new OptionInstance.IntRange(min, max, true), Math.max(min, Math.min(max, value)), set);
	}

	private static OptionInstance<Integer> percent(String key, float value, Consumer<Float> set) {
		return slider(key, 0, 100, Math.round(value * 100), v -> v == 0 ? "OFF" : v + "%", v -> set.accept(v / 100F));
	}

	private static OptionInstance<String> choice(String key, List<String> values, String value, OptionInstance.ValueUpdateListener<String> set) {
		return new OptionInstance<>("svregblue.options." + key, tip(key),
				(caption, v) -> Component.translatable("svregblue.options." + key + "." + v),
				new OptionInstance.Enum<>(values, Codec.STRING), values.contains(value) ? value : values.get(0), set);
	}

	private void small(Object... items) {
		List<AbstractWidget> ws = new ArrayList<>();
		for (Object o : items) {
			if (o instanceof OptionInstance<?> oi) {
				ws.add(oi.createButton(this.options, 0, 0, 150));
			} else {
				ws.add((AbstractWidget) o);
			}
		}
		this.list.addSmall(ws);
	}

	@Override
	protected void addOptions() {
		if (this.list == null) {
			return;
		}
		this.list.addHeader(Component.translatable("svregblue.options.header.grade"));
		small(
				bool("enabled", BlueConfig.enabled, v -> BlueConfig.enabled = v),
				choice("mode", List.of("dusk_night", "night", "always"), BlueConfig.mode, v -> BlueConfig.mode = v),
				percent("strength", BlueConfig.strength, v -> BlueConfig.strength = v),
				percent("lift", BlueConfig.lift, v -> BlueConfig.lift = v),
				bool("fog", BlueConfig.fog, v -> BlueConfig.fog = v),
				bool("warm_lights", BlueConfig.warmLights, v -> BlueConfig.warmLights = v));
	}

	@Override
	public void removed() {
		super.removed();
		BlueConfig.save();
	}
}
