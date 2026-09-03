package minex.acoustickey.client.mixin;

import minex.acoustickey.sound.SoundPackManager;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
	@Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = false)
	private void acoustickey$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
		if (window != net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle()) {
			return;
		}

		SoundPackManager manager = SoundPackManager.getInstance();
		if (action == 1) {
			manager.playMouseDown(button);
		} else if (action == 0) {
			manager.playMouseUp(button);
		}
	}
}
