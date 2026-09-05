package minex.acoustickey.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class VolumeSlider extends AbstractSliderButton {
	private final String label;
	private final Consumer<Integer> onChange;

	public VolumeSlider(int x, int y, int width, int height, String label, int initialValue, Consumer<Integer> onChange) {
		super(x, y, width, height, Component.literal(label + ": " + initialValue), initialValue / 100.0);
		this.label = label;
		this.onChange = onChange;
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		this.setMessage(Component.literal(label + ": " + (int) (this.value * 100)));
	}

	@Override
	protected void applyValue() {
		this.onChange.accept((int) (this.value * 100));
	}
}
