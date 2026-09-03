package minex.acoustickey.client;

import minex.acoustickey.AcoustiKey;
import minex.acoustickey.client.gui.AcoustiKeyScreen;
import minex.acoustickey.config.AcoustiKeyConfig;
import minex.acoustickey.sound.SoundPackManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AcoustiKeyClient implements ClientModInitializer {
	private static final String CATEGORY = "key.category.acoustickey.keys";
	private static KeyBinding guiKey;
	private static KeyBinding muteKey;

	public static KeyBinding getGuiKey() { return guiKey; }
	public static KeyBinding getMuteKey() { return muteKey; }

	@Override
	public void onInitializeClient() {
		AcoustiKeyConfig config = AcoustiKeyConfig.load();
		SoundPackManager.getInstance().init(config);

		guiKey = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.acoustickey.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY)
		);
		muteKey = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.acoustickey.mute", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (guiKey.wasPressed()) {
				// Allow opening settings from any screen (menu, world, etc.) and avoid
				// stacking duplicate screens on repeated presses.
				if (client.currentScreen == null || !(client.currentScreen instanceof AcoustiKeyScreen)) {
					client.setScreen(new AcoustiKeyScreen(client.currentScreen));
				}
			}
			if (muteKey.wasPressed()) {
				SoundPackManager manager = SoundPackManager.getInstance();
				manager.getConfig().muted = !manager.getConfig().muted;
				manager.getConfig().save();
			}
		});
	}
}
