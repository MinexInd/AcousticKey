package minex.acoustickey.client.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ToggleButton {
	private final ButtonWidget button;
	private boolean value;
	private final Consumer<Boolean> onChange;

	public ToggleButton(int x, int y, int width, int height, String label, boolean initial, Consumer<Boolean> onChange) {
		this.value = initial;
		this.onChange = onChange;
		this.button = ButtonWidget.builder(Text.of(makeLabel(label, initial)), btn -> {
			this.value = !this.value;
			btn.setMessage(Text.of(makeLabel(label, this.value)));
			this.onChange.accept(this.value);
		}).position(x, y).size(width, height).build();
	}

	private static String makeLabel(String label, boolean val) {
		return label + ": " + (val ? "ON" : "OFF");
	}

	public ButtonWidget getButton() {
		return button;
	}

	public boolean getValue() {
		return value;
	}
}
