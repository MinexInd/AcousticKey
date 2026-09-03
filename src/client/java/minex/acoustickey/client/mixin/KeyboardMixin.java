package minex.acoustickey.client.mixin;

import minex.acoustickey.sound.SoundPackManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
	@Inject(method = "onKey", at = @At("HEAD"), cancellable = false)
	private void acoustickey$onKey(long window, int action, KeyInput input, CallbackInfo ci) {
		if (window != net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle()) {
			return;
		}

		SoundPackManager manager = SoundPackManager.getInstance();
		if (action == 1) {
			manager.playKeyDown(input.key());
		} else if (action == 0) {
			manager.playKeyUp(input.key());
		}
	}
}
