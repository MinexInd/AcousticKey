package minex.acoustickey.client.gui;

import minex.acoustickey.client.AcoustiKeyClient;
import minex.acoustickey.client.gui.widgets.ScrollablePackList;
import minex.acoustickey.client.gui.widgets.VolumeSlider;
import minex.acoustickey.config.AcoustiKeyConfig;
import minex.acoustickey.sound.SoundPackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
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
	private Button muteBtn;
	private Button keyUpBtn;
	private Button mouseBtn;
	private Button randomBtn;

	private int panelX;
	private int panelW;

	public AcoustiKeyScreen(Screen parent) {
		super(Component.literal("AcousticKey"));
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
		this.addRenderableWidget(keyboardList);

		mouseList = new ScrollablePackList(
			innerX + listW + GAP, listY, listW, listH,
			SoundPackManager.getInstance().getMousePacks(),
			SoundPackManager.getInstance().getCurrentMousePack(),
			pack -> SoundPackManager.getInstance().setCurrentMousePack(pack)
		);
		this.addRenderableWidget(mouseList);

		int volY = listY + listH + 30;
		int volW = listW - 80;
		keyboardVolume = new VolumeSlider(innerX + 80, volY, volW, 18,
			"Keyboard", config.keyboardVolume, val -> {
				config.keyboardVolume = val;
				config.save();
			});
		this.addRenderableWidget(keyboardVolume);

		mouseVolume = new VolumeSlider(innerX + listW + GAP + 80, volY, volW, 18,
			"Mouse", config.mouseVolume, val -> {
				config.mouseVolume = val;
				config.save();
			});
		this.addRenderableWidget(mouseVolume);

		int btnY = volY + 32;
		int btnW = (listW - 4) / 2;
		int btnH = 22;

		muteBtn = Button.builder(toggleLabel("Mute", config.muted), btn -> {
			config.muted = !config.muted;
			config.save();
			btn.setMessage(toggleLabel("Mute", config.muted));
		}).pos(innerX, btnY).size(btnW, btnH).build();
		this.addRenderableWidget(muteBtn);

		keyUpBtn = Button.builder(toggleLabel("Key-Up", config.keyUpEnabled), btn -> {
			config.keyUpEnabled = !config.keyUpEnabled;
			config.save();
			btn.setMessage(toggleLabel("Key-Up", config.keyUpEnabled));
		}).pos(innerX + btnW + 4, btnY).size(btnW, btnH).build();
		this.addRenderableWidget(keyUpBtn);

		mouseBtn = Button.builder(toggleLabel("Mouse", config.mouseSoundsEnabled), btn -> {
			config.mouseSoundsEnabled = !config.mouseSoundsEnabled;
			config.save();
			btn.setMessage(toggleLabel("Mouse", config.mouseSoundsEnabled));
		}).pos(innerX + listW + GAP, btnY).size(btnW, btnH).build();
		this.addRenderableWidget(mouseBtn);

		randomBtn = Button.builder(toggleLabel("Random", config.randomSoundsEnabled), btn -> {
			config.randomSoundsEnabled = !config.randomSoundsEnabled;
			config.save();
			btn.setMessage(toggleLabel("Random", config.randomSoundsEnabled));
		}).pos(innerX + listW + GAP + btnW + 4, btnY).size(btnW, btnH).build();
		this.addRenderableWidget(randomBtn);

		int footerY = this.height - 50;
		int footBtnW = 88;
		this.addRenderableWidget(Button.builder(Component.literal("Refresh"), btn -> {
			SoundPackManager.getInstance().reloadPacks();
			this.refreshLists();
		}).pos(innerX, footerY).size(footBtnW, 22).build());

		this.addRenderableWidget(Button.builder(Component.literal("Open Folder"), btn -> {
			openSoundpackFolder();
		}).pos(innerX + footBtnW + 6, footerY).size(footBtnW + 30, 22).build());

		this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> this.onClose())
			.pos(panelX + panelW - PAD - footBtnW, footerY)
			.size(footBtnW, 22)
			.build());
	}

	private void refreshLists() {
		this.removeWidget(keyboardList);
		this.removeWidget(mouseList);
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
		this.addRenderableWidget(keyboardList);

		mouseList = new ScrollablePackList(
			innerX + listW + GAP, listY, listW, listH,
			SoundPackManager.getInstance().getMousePacks(),
			SoundPackManager.getInstance().getCurrentMousePack(),
			pack -> SoundPackManager.getInstance().setCurrentMousePack(pack)
		);
		this.addRenderableWidget(mouseList);
	}

	private static String bindName(net.minecraft.client.KeyMapping key, String fallback) {
		return key != null ? key.getTranslatedKeyMessage().getString() : fallback;
	}

	private static Component toggleLabel(String name, boolean on) {
		String state = on ? "\u00A7aON" : "\u00A7cOFF";
		return Component.literal(name + ": " + state);
	}

	private void openSoundpackFolder() {
		Path kbDir = Minecraft.getInstance().gameDirectory.toPath()
					.resolve("config/acoustickey/soundpacks");
		try {
			Files.createDirectories(kbDir);
			// Use Minecraft's own folder-opening mechanism so it opens in the file manager.
			Util.getPlatform().openPath(kbDir);
		} catch (IOException e) {
			if (Minecraft.getInstance().player != null) {
				Minecraft.getInstance().player.sendSystemMessage(
					Component.literal("Could not open folder")
				);
			}
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
		ctx.fill(panelX, 0, panelX + panelW, this.height, PANEL_BG);
		ctx.fill(panelX, 0, panelX + 1, this.height, PANEL_ACCENT);
		ctx.fill(panelX + panelW - 1, 0, panelX + panelW, this.height, PANEL_ACCENT);

		super.extractRenderState(ctx, mouseX, mouseY, delta);

		int innerX = panelX + PAD;
		int innerW = panelW - PAD * 2;
		int listW = (innerW - GAP) / 2;
		int listY = 50;

		ctx.centeredText(this.font,
			Component.literal("\u00A7l\u00A7fAcousticKey"),
			panelX + panelW / 2, 12, 0xFFFFFFFF);
		ctx.centeredText(this.font,
			Component.literal("\u00A77Sound on every keystroke"),
			panelX + panelW / 2, 26, TEXT_DIM);

		ctx.text(this.font,
			Component.literal("\u00A7bKEYBOARD PACKS"),
			innerX, listY - 14, TEXT_LABEL, true);
		ctx.text(this.font,
			Component.literal("\u00A7bMOUSE PACKS"),
			innerX + listW + GAP, listY - 14, TEXT_LABEL, true);

		int volY = listY + 210 + 30;
		ctx.text(this.font,
			Component.literal("\u00A7bVolume"),
			innerX, volY - 14, TEXT_LABEL, true);

		int btnY = volY + 32;
		ctx.text(this.font,
			Component.literal("\u00A7bBehavior"),
			innerX, btnY - 14, TEXT_LABEL, true);

		String kbName = SoundPackManager.getInstance().getCurrentKeyboardPack() != null
			? SoundPackManager.getInstance().getCurrentKeyboardPack().getDisplayName() : "None";
		String mName = SoundPackManager.getInstance().getCurrentMousePack() != null
			? SoundPackManager.getInstance().getCurrentMousePack().getDisplayName() : "None";

		ctx.fill(panelX + PAD, this.height - 18, panelX + panelW - PAD, this.height - 17, 0x33FFFFFF);
		ctx.centeredText(this.font,
			Component.literal("\u00A77" + bindName(AcoustiKeyClient.getGuiKey(), "K") + " = GUI   \u00A78\u2502   \u00A77" + bindName(AcoustiKeyClient.getMuteKey(), "M") + " = Mute   \u00A78\u2502   \u00A7f" + kbName + " \u00A78\u2502 \u00A7f" + mName),
			panelX + panelW / 2, this.height - 14, TEXT_DIM);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.gui.setScreen(parent);
		}
	}
}
