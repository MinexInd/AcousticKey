package minex.acoustickey.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ToggleButton {
	private final Button button;
	private boolean value;
	private final Consumer<Boolean> onChange;

	public ToggleButton(int x, int y, int width, int height, String label, boolean initial, Consumer<Boolean> onChange) {
		this.value = initial;
		this.onChange = onChange;
		this.button = Button.builder(Component.nullToEmpty(makeLabel(label, initial)), btn -> {
			this.value = !this.value;
			btn.setMessage(Component.nullToEmpty(makeLabel(label, this.value)));
			this.onChange.accept(this.value);
		}).pos(x, y).size(width, height).build();
	}

	private static String makeLabel(String label, boolean val) {
		return label + ": " + (val ? "ON" : "OFF");
	}

	public Button getButton() {
		return button;
	}

	public boolean getValue() {
		return value;
	}
}
