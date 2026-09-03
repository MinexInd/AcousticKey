package minex.acoustickey.client.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class VolumeSlider extends SliderWidget {
	private final String label;
	private final Consumer<Integer> onChange;

	public VolumeSlider(int x, int y, int width, int height, String label, int initialValue, Consumer<Integer> onChange) {
		super(x, y, width, height, Text.literal(label + ": " + initialValue), initialValue / 100.0);
		this.label = label;
		this.onChange = onChange;
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		this.setMessage(Text.literal(label + ": " + (int) (this.value * 100)));
	}

	@Override
	protected void applyValue() {
		this.onChange.accept((int) (this.value * 100));
	}
}
