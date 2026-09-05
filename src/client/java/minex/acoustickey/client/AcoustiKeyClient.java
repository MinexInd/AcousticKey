package minex.acoustickey.client;

import minex.acoustickey.AcoustiKey;
import minex.acoustickey.client.gui.AcoustiKeyScreen;
import minex.acoustickey.config.AcoustiKeyConfig;
import minex.acoustickey.sound.SoundPackManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class AcoustiKeyClient implements ClientModInitializer {
	private static final KeyMapping.Category CATEGORY =
			new KeyMapping.Category(Identifier.fromNamespaceAndPath(AcoustiKey.MOD_ID, "keys"));
	private static KeyMapping guiKey;
	private static KeyMapping muteKey;

	public static KeyMapping getGuiKey() { return guiKey; }
	public static KeyMapping getMuteKey() { return muteKey; }

	@Override
	public void onInitializeClient() {
		AcoustiKeyConfig config = AcoustiKeyConfig.load();
		SoundPackManager.getInstance().init(config);

		guiKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.acoustickey.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY)
		);
		muteKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.acoustickey.mute", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (guiKey.consumeClick()) {
				// Allow opening settings from any screen (menu, world, etc.) and avoid
				// stacking duplicate screens on repeated presses.
				if (client.screen == null || !(client.screen instanceof AcoustiKeyScreen)) {
					client.setScreen(new AcoustiKeyScreen(client.screen));
				}
			}
			if (muteKey.consumeClick()) {
				SoundPackManager manager = SoundPackManager.getInstance();
				manager.getConfig().muted = !manager.getConfig().muted;
				manager.getConfig().save();
			}
		});
	}
}
