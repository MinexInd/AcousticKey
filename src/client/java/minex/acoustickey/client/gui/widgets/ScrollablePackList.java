package minex.acoustickey.client.gui.widgets;

import minex.acoustickey.sound.SoundPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class ScrollablePackList extends ClickableWidget {
	private static final int ITEM_HEIGHT = 30;
	private static final int SCROLLBAR_W = 6;
	private static final int ITEM_PAD = 10;

	private final List<SoundPack> packs;
	private final Consumer<SoundPack> onSelect;
	private int scrollOffset = 0;
	private SoundPack selected;

	private static final int BG = 0xFF141420;
	private static final int SCROLLBAR_BG = 0xFF1A1A28;
	private static final int SCROLLBAR_FG = 0xFF7777BB;
	private static final int HOVER = 0x14FFFFFF;
	private static final int SELECTED_FILL = 0x33FFDD55;
	private static final int SELECTED_BAR = 0xFFDD55;
	private static final int TEXT_MAIN = 0xFFE0E0E0;
	private static final int TEXT_DIM = 0xFF888899;
	private static final int TEXT_SEL = 0xFFFFE899;

	public ScrollablePackList(int x, int y, int width, int height,
				List<SoundPack> packs, SoundPack selected, Consumer<SoundPack> onSelect) {
		super(x, y, width, height, Text.empty());
		this.packs = packs;
		this.selected = selected;
		this.onSelect = onSelect;
	}

	public void setSelected(SoundPack pack) {
		this.selected = pack;
		if (pack != null) {
			int idx = packs.indexOf(pack);
			if (idx >= 0) {
				int targetCenter = idx * ITEM_HEIGHT + ITEM_HEIGHT / 2;
				if (targetCenter < scrollOffset + ITEM_HEIGHT) {
					scrollOffset = Math.max(0, targetCenter - ITEM_HEIGHT / 2);
				} else if (targetCenter > scrollOffset + height - ITEM_HEIGHT) {
					scrollOffset = Math.min(getMaxScroll(), targetCenter - height + ITEM_HEIGHT / 2);
				}
			}
		}
	}

	public SoundPack getSelected() {
		return selected;
	}

	private int getMaxScroll() {
		return Math.max(0, packs.size() * ITEM_HEIGHT - (height - 2));
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		int max = getMaxScroll();
		int step = ITEM_HEIGHT;
		scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int) Math.signum(verticalAmount) * step));
		return true;
	}

	public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		if (!isMouseOver(mouseX, mouseY) || click.button() != 0) {
			return false;
		}
		int innerX = (int) mouseX - getX();
		int innerY = (int) mouseY - getY() + scrollOffset;
		int idx = innerY / ITEM_HEIGHT;
		if (idx >= 0 && idx < packs.size()) {
			selected = packs.get(idx);
			onSelect.accept(selected);
			return true;
		}
		return false;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		int x = getX();
		int y = getY();
		int w = getWidth();
		int h = getHeight();

		context.fill(x, y, x + w, y + h, BG);

		int sbX = x + w - SCROLLBAR_W;
		context.fill(sbX, y + 1, sbX + SCROLLBAR_W, y + h - 1, SCROLLBAR_BG);

		int max = getMaxScroll();
		if (max > 0 && packs.size() > 0) {
			int thumbH = Math.max(20, (int) ((h - 2) * ((float) (h - 2) / (packs.size() * ITEM_HEIGHT))));
			int thumbY = y + 1 + (int) ((h - 2 - thumbH) * ((float) scrollOffset / max));
			context.fill(sbX + 1, thumbY, sbX + SCROLLBAR_W - 1, thumbY + thumbH, SCROLLBAR_FG);
		}

		int innerW = w - SCROLLBAR_W - 1;
		context.enableScissor(x + 1, y + 1, x + innerW, y + h - 1);

		for (int i = 0; i < packs.size(); i++) {
			int entryY = y + i * ITEM_HEIGHT - scrollOffset;
			if (entryY + ITEM_HEIGHT < y || entryY > y + h) {
				continue;
			}
			SoundPack pack = packs.get(i);
			boolean hovered = mouseX >= x && mouseX < x + innerW
					&& mouseY >= entryY && mouseY < entryY + ITEM_HEIGHT;
			boolean isSelected = pack == selected;
			renderEntry(context, pack, x, entryY, innerW, ITEM_HEIGHT, hovered, isSelected);
		}

		context.disableScissor();

		context.fill(x, y, x + w, y + 1, 0xFF333355);
		context.fill(x, y + h - 1, x + w, y + h, 0xFF333355);
		context.fill(x, y, x + 1, y + h, 0xFF333355);
		context.fill(x + w - 1, y, x + w, y + h, 0xFF333355);
	}

	private void renderEntry(DrawContext context, SoundPack pack, int x, int y, int w, int h,
				boolean hovered, boolean selected) {
		if (selected) {
			context.fill(x + 1, y, x + w - SCROLLBAR_W, y + h, SELECTED_FILL);
			context.fill(x + 1, y, x + 4, y + h, SELECTED_BAR);
		} else if (hovered) {
			context.fill(x + 1, y, x + w - SCROLLBAR_W, y + h, HOVER);
		}

		String name = pack.getDisplayName();
		String typeTag = pack.getDefineType() == SoundPack.DefineType.SINGLE ? "sprite" : "files";
		int defineCount = pack.getDefines() != null ? pack.getDefines().size() : 0;

		int nameColor = selected ? TEXT_SEL : (hovered ? 0xFFFFFFFF : TEXT_MAIN);
		int dimColor = selected ? 0xCCFFE899 : TEXT_DIM;

		int textX = x + ITEM_PAD;
		int nameY = y + (h - 18) / 2;

		String prefix = selected ? "\u25B6 " : "";
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
				Text.literal(prefix + name),
				textX, nameY, nameColor);

		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
				Text.literal(typeTag + " \u00B7 " + defineCount + " keys"),
				textX, nameY + 11, dimColor);
	}

	@Override
	protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
		if (selected != null) {
			builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE,
					Text.literal(selected.getDisplayName()));
		}
	}
}
