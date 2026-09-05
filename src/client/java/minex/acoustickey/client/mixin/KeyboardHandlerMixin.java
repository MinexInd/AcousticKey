package minex.acoustickey.client.mixin;

import minex.acoustickey.sound.SoundPackManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = false)
	private void acoustickey$onKey(long window, int action, KeyEvent input, CallbackInfo ci) {
		if (window != net.minecraft.client.Minecraft.getInstance().getWindow().handle()) {
			return;
		}

		SoundPackManager manager = SoundPackManager.getInstance();
		if (action == 1) {
			manager.playKeyDown(input.input());
		} else if (action == 0) {
			manager.playKeyUp(input.input());
		}
	}
}
