package minex.acoustickey.client.mixin;

import minex.acoustickey.sound.SoundPackManager;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Inject(method = "onButton", at = @At("HEAD"), cancellable = false)
	private void acoustickey$onMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
		if (window != net.minecraft.client.Minecraft.getInstance().getWindow().handle()) {
			return;
		}

		SoundPackManager manager = SoundPackManager.getInstance();
		if (action == 1) {
			manager.playMouseDown(input.input());
		} else if (action == 0) {
			manager.playMouseUp(input.input());
		}
	}
}
