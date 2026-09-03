package minex.acoustickey.client.mixin;

import minex.acoustickey.sound.SoundPackManager;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
	@Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = false)
	private void acoustickey$onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
		if (window != net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle()) {
			return;
		}

		SoundPackManager manager = SoundPackManager.getInstance();
		if (action == 1) {
			manager.playMouseDown(input.button());
		} else if (action == 0) {
			manager.playMouseUp(input.button());
		}
	}
}
