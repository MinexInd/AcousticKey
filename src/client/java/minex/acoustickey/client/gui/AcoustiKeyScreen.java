package minex.acoustickey.client.gui;

import minex.acoustickey.client.AcoustiKeyClient;
import minex.acoustickey.client.gui.widgets.ScrollablePackList;
import minex.acoustickey.client.gui.widgets.VolumeSlider;
import minex.acoustickey.config.AcoustiKeyConfig;
import minex.acoustickey.sound.SoundPackManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AcoustiKeyScreen extends Screen {

	private static final int PAD = 14;
	private static final int GAP = 12;
	private static final int SECTION_H = 18;

	private static final int PANEL_BG = 0xF010101C;
	private static final int PANEL_ACCENT = 0xFF4444CC;
	private static final int SUBPANEL_BG = 0xFF141420;
	private static final int SUBPANEL_BORDER = 0xFF333355;
	private static final int TEXT_DIM = 0xFF888899;
	private static final int TEXT_LABEL = 0xFF88BBFF;

	private final Screen parent;
	private final AcoustiKeyConfig config;

	private ScrollablePackList keyboardList;
	private ScrollablePackList mouseList;
	private VolumeSlider keyboardVolume;
	private VolumeSlider mouseVolume;
	private ButtonWidget muteBtn;
	private ButtonWidget keyUpBtn;
	private ButtonWidget mouseBtn;
	private ButtonWidget randomBtn;

	private int panelX;
	private int panelW;

	public AcoustiKeyScreen(Screen parent) {
		super(Text.literal("AcousticKey"));
		this.parent = parent;
		this.config = SoundPackManager.getInstance().getConfig();
	}

	@Override
	protected void init() {
		int maxPanelW = 520;
		panelW = Math.min(maxPanelW, this.width - 80);
		panelX = (this.width - panelW) / 2;

		int innerX = panelX + PAD;
		int innerW = panelW - PAD * 2;
		int listW = (innerW - GAP) / 2;
		int listY = 50;
		int listH = 210;

		keyboardList = new ScrollablePackList(
			innerX, listY, listW, listH,
			SoundPackManager.getInstance().getKeyboardPacks(),
			SoundPackManager.getInstance().getCurrentKeyboardPack(),
			pack -> SoundPackManager.getInstance().setCurrentKeyboardPack(pack)
		);
		this.addDrawableChild(keyboardList);

		mouseList = new ScrollablePackList(
			innerX + listW + GAP, listY, listW, listH,
			SoundPackManager.getInstance().getMousePacks(),
			SoundPackManager.getInstance().getCurrentMousePack(),
			pack -> SoundPackManager.getInstance().setCurrentMousePack(pack)
		);
		this.addDrawableChild(mouseList);

		int volY = listY + listH + 30;
		int volW = listW - 80;
		keyboardVolume = new VolumeSlider(innerX + 80, volY, volW, 18,
			"Keyboard", config.keyboardVolume, val -> {
				config.keyboardVolume = val;
				config.save();
			});
		this.addDrawableChild(keyboardVolume);

		mouseVolume = new VolumeSlider(innerX + listW + GAP + 80, volY, volW, 18,
			"Mouse", config.mouseVolume, val -> {
				config.mouseVolume = val;
				config.save();
			});
		this.addDrawableChild(mouseVolume);

		int btnY = volY + 32;
		int btnW = (listW - 4) / 2;
		int btnH = 22;

		muteBtn = ButtonWidget.builder(toggleLabel("Mute", config.muted), btn -> {
			config.muted = !config.muted;
			config.save();
			btn.setMessage(toggleLabel("Mute", config.muted));
		}).position(innerX, btnY).size(btnW, btnH).build();
		this.addDrawableChild(muteBtn);

		keyUpBtn = ButtonWidget.builder(toggleLabel("Key-Up", config.keyUpEnabled), btn -> {
			config.keyUpEnabled = !config.keyUpEnabled;
			config.save();
			btn.setMessage(toggleLabel("Key-Up", config.keyUpEnabled));
		}).position(innerX + btnW + 4, btnY).size(btnW, btnH).build();
		this.addDrawableChild(keyUpBtn);

		mouseBtn = ButtonWidget.builder(toggleLabel("Mouse", config.mouseSoundsEnabled), btn -> {
			config.mouseSoundsEnabled = !config.mouseSoundsEnabled;
			config.save();
			btn.setMessage(toggleLabel("Mouse", config.mouseSoundsEnabled));
		}).position(innerX + listW + GAP, btnY).size(btnW, btnH).build();
		this.addDrawableChild(mouseBtn);

		randomBtn = ButtonWidget.builder(toggleLabel("Random", config.randomSoundsEnabled), btn -> {
			config.randomSoundsEnabled = !config.randomSoundsEnabled;
			config.save();
			btn.setMessage(toggleLabel("Random", config.randomSoundsEnabled));
		}).position(innerX + listW + GAP + btnW + 4, btnY).size(btnW, btnH).build();
		this.addDrawableChild(randomBtn);

		int footerY = this.height - 50;
		int footBtnW = 88;
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), btn -> {
			SoundPackManager.getInstance().reloadPacks();
			this.refreshLists();
		}).position(innerX, footerY).size(footBtnW, 22).build());

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Open Folder"), btn -> {
			openSoundpackFolder();
		}).position(innerX + footBtnW + 6, footerY).size(footBtnW + 30, 22).build());

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> this.close())
			.position(panelX + panelW - PAD - footBtnW, footerY)
			.size(footBtnW, 22)
			.build());
	}

	private void refreshLists() {
		this.remove(keyboardList);
		this.remove(mouseList);
		int innerX = panelX + PAD;
		int innerW = panelW - PAD * 2;
		int listW = (innerW - GAP) / 2;
		int listY = 50;
		int listH = 210;

		keyboardList = new ScrollablePackList(
			innerX, listY, listW, listH,
			SoundPackManager.getInstance().getKeyboardPacks(),
			SoundPackManager.getInstance().getCurrentKeyboardPack(),
			pack -> SoundPackManager.getInstance().setCurrentKeyboardPack(pack)
		);
		this.addDrawableChild(keyboardList);

		mouseList = new ScrollablePackList(
			innerX + listW + GAP, listY, listW, listH,
			SoundPackManager.getInstance().getMousePacks(),
			SoundPackManager.getInstance().getCurrentMousePack(),
			pack -> SoundPackManager.getInstance().setCurrentMousePack(pack)
		);
		this.addDrawableChild(mouseList);
	}

	private static String bindName(net.minecraft.client.option.KeyBinding key, String fallback) {
		return key != null ? key.getBoundKeyLocalizedText().getString() : fallback;
	}

	private static Text toggleLabel(String name, boolean on) {
		String state = on ? "\u00A7aON" : "\u00A7cOFF";
		return Text.literal(name + ": " + state);
	}

	private void openSoundpackFolder() {
		Path kbDir = MinecraftClient.getInstance().runDirectory.toPath()
					.resolve("config/acoustickey/soundpacks");
		try {
			Files.createDirectories(kbDir);
			// Use Minecraft's own folder-opening mechanism so it opens in the file manager.
			Util.getOperatingSystem().open(kbDir);
		} catch (IOException e) {
			MinecraftClient.getInstance().inGameHud.setOverlayMessage(
				Text.literal("Could not open folder"), false
			);
		}
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		ctx.fill(panelX, 0, panelX + panelW, this.height, PANEL_BG);
		ctx.fill(panelX, 0, panelX + 1, this.height, PANEL_ACCENT);
		ctx.fill(panelX + panelW - 1, 0, panelX + panelW, this.height, PANEL_ACCENT);

		super.render(ctx, mouseX, mouseY, delta);

		int innerX = panelX + PAD;
		int innerW = panelW - PAD * 2;
		int listW = (innerW - GAP) / 2;
		int listY = 50;

		ctx.drawCenteredTextWithShadow(this.textRenderer,
			Text.literal("\u00A7l\u00A7fAcousticKey"),
			panelX + panelW / 2, 12, 0xFFFFFFFF);
		ctx.drawCenteredTextWithShadow(this.textRenderer,
			Text.literal("\u00A77Sound on every keystroke"),
			panelX + panelW / 2, 26, TEXT_DIM);

		ctx.drawTextWithShadow(this.textRenderer,
			Text.literal("\u00A7bKEYBOARD PACKS"),
			innerX, listY - 14, TEXT_LABEL);
		ctx.drawTextWithShadow(this.textRenderer,
			Text.literal("\u00A7bMOUSE PACKS"),
			innerX + listW + GAP, listY - 14, TEXT_LABEL);

		int volY = listY + 210 + 30;
		ctx.drawTextWithShadow(this.textRenderer,
			Text.literal("\u00A7bVolume"),
			innerX, volY - 14, TEXT_LABEL);

		int btnY = volY + 32;
		ctx.drawTextWithShadow(this.textRenderer,
			Text.literal("\u00A7bBehavior"),
			innerX, btnY - 14, TEXT_LABEL);

		String kbName = SoundPackManager.getInstance().getCurrentKeyboardPack() != null
			? SoundPackManager.getInstance().getCurrentKeyboardPack().getDisplayName() : "None";
		String mName = SoundPackManager.getInstance().getCurrentMousePack() != null
			? SoundPackManager.getInstance().getCurrentMousePack().getDisplayName() : "None";

		ctx.fill(panelX + PAD, this.height - 18, panelX + panelW - PAD, this.height - 17, 0x33FFFFFF);
		ctx.drawCenteredTextWithShadow(this.textRenderer,
			Text.literal("\u00A77" + bindName(AcoustiKeyClient.getGuiKey(), "K") + " = GUI   \u00A78\u2502   \u00A77" + bindName(AcoustiKeyClient.getMuteKey(), "M") + " = Mute   \u00A78\u2502   \u00A7f" + kbName + " \u00A78\u2502 \u00A7f" + mName),
			panelX + panelW / 2, this.height - 14, TEXT_DIM);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		if (this.client != null) {
			this.client.setScreen(parent);
		}
	}
}
