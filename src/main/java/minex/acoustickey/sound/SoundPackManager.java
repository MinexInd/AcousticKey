package minex.acoustickey.sound;

import minex.acoustickey.AcoustiKey;
import minex.acoustickey.config.AcoustiKeyConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SoundPackManager {
	private static SoundPackManager instance;
	private AcoustiKeyConfig config;
	private List<SoundPack> keyboardPacks = new ArrayList<>();
	private List<SoundPack> mousePacks = new ArrayList<>();
	private SoundPack currentKeyboardPack;
	private SoundPack currentMousePack;

	private SoundPackManager() {}

	public static SoundPackManager getInstance() {
		if (instance == null) {
			instance = new SoundPackManager();
		}
		return instance;
	}

	public void init(AcoustiKeyConfig config) {
		this.config = config;
		OpenALSoundPlayer.initialize();
		reloadPacks();
	}

	public void reloadPacks() {
		keyboardPacks = SoundPackLoader.loadKeyboardPacks();
		mousePacks = SoundPackLoader.loadMousePacks();

		if (!keyboardPacks.isEmpty()) {
			currentKeyboardPack = keyboardPacks.stream()
						.filter(p -> p.getPackId().equals(config.keyboardPackId))
						.findFirst()
						.orElse(keyboardPacks.getFirst());
			if (!currentKeyboardPack.getPackId().equals(config.keyboardPackId)) {
				config.keyboardPackId = currentKeyboardPack.getPackId();
				config.save();
			}
		}

		if (!mousePacks.isEmpty()) {
			currentMousePack = mousePacks.stream()
						.filter(p -> p.getPackId().equals(config.mousePackId))
						.findFirst()
						.orElse(mousePacks.getFirst());
			if (!currentMousePack.getPackId().equals(config.mousePackId)) {
				config.mousePackId = currentMousePack.getPackId();
				config.save();
			}
		}

		AcoustiKey.LOGGER.info("Loaded {} keyboard packs and {} mouse packs",
					keyboardPacks.size(), mousePacks.size());

		preloadPackSounds(currentKeyboardPack);
		preloadPackSounds(currentMousePack);
	}

	private void preloadPackSounds(SoundPack pack) {
		if (pack == null) {
			return;
		}
		Set<String> relatives = new HashSet<>(pack.getDefines().values());
		for (String relative : relatives) {
			OpenALSoundPlayer.preload(pack.resolvePath(relative));
		}
	}

	public void playKeyDown(int keyCode) {
		if (config.muted || currentKeyboardPack == null) {
			return;
		}

		String soundKey = String.valueOf(KeycodeMapper.glfwToJs(keyCode));
		if (config.randomSoundsEnabled && !currentKeyboardPack.getDefines().isEmpty()) {
			List<String> keys = new ArrayList<>(currentKeyboardPack.getDefines().keySet());
			soundKey = keys.get((int) (Math.random() * keys.size()));
		}

		playKeyboardSound(soundKey, config.keyboardVolume);
	}

	public void playKeyUp(int keyCode) {
		if (config.muted || !config.keyUpEnabled || currentKeyboardPack == null) {
			return;
		}

		String soundKey = String.valueOf(KeycodeMapper.glfwToJs(keyCode));
		playKeyboardSound(soundKey, config.keyboardVolume);
	}

	private void playKeyboardSound(String soundKey, int volume) {
		String soundPath = currentKeyboardPack.getSoundPath(soundKey);
		if (soundPath == null) {
			return;
		}

		float vol = volume / 100.0f;
		if (currentKeyboardPack.getDefineType() == SoundPack.DefineType.SINGLE) {
			int[] sprite = currentKeyboardPack.getSprite(soundKey);
			if (sprite != null) {
				OpenALSoundPlayer.playSound(soundPath, vol, sprite);
				return;
			}
		}
		OpenALSoundPlayer.playSound(soundPath, vol);
	}

	public void playMouseDown(int button) {
		if (config.muted || !config.mouseSoundsEnabled || currentMousePack == null) {
			return;
		}
		playMouseSound(mouseKeyCandidates(button, false), config.mouseVolume);
	}

	public void playMouseUp(int button) {
		if (config.muted || !config.mouseSoundsEnabled || currentMousePack == null) {
			return;
		}
		playMouseSound(mouseKeyCandidates(button, true), config.mouseVolume);
	}

	// Maps a raw Minecraft mouse button to the sound pack keys. MechVibes++ packs
	// use 1=left/2=right/3=middle and a leading "0" for the release (e.g. "01").
	// Falls back to the raw button code for legacy packs that use "0"/"1"/"2".
	private java.util.List<String> mouseKeyCandidates(int button, boolean release) {
		int mechVibesCode = button + 1;
		java.util.List<String> candidates = new java.util.ArrayList<>();
		candidates.add(release ? "0" + mechVibesCode : String.valueOf(mechVibesCode));
		candidates.add(String.valueOf(button));
		return candidates;
	}

	private void playMouseSound(java.util.List<String> soundKeys, int volume) {
		for (String soundKey : soundKeys) {
			String soundPath = currentMousePack.getSoundPath(soundKey);
			if (soundPath == null) {
				continue;
			}
			float vol = volume / 100.0f;
			if (currentMousePack.getDefineType() == SoundPack.DefineType.SINGLE) {
				int[] sprite = currentMousePack.getSprite(soundKey);
				if (sprite != null) {
					OpenALSoundPlayer.playSound(soundPath, vol, sprite);
					return;
				}
			}
			OpenALSoundPlayer.playSound(soundPath, vol);
			return;
		}
	}

	public List<SoundPack> getKeyboardPacks() {
		return keyboardPacks;
	}

	public List<SoundPack> getMousePacks() {
		return mousePacks;
	}

	public SoundPack getCurrentKeyboardPack() {
		return currentKeyboardPack;
	}

	public SoundPack getCurrentMousePack() {
		return currentMousePack;
	}

	public void setCurrentKeyboardPack(SoundPack pack) {
		this.currentKeyboardPack = pack;
		if (pack != null) {
			config.keyboardPackId = pack.getPackId();
			config.save();
			preloadPackSounds(pack);
		}
	}

	public void setCurrentMousePack(SoundPack pack) {
		this.currentMousePack = pack;
		if (pack != null) {
			config.mousePackId = pack.getPackId();
			config.save();
			preloadPackSounds(pack);
		}
	}

	public AcoustiKeyConfig getConfig() {
		return config;
	}
}
