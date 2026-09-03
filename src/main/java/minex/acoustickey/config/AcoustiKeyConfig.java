package minex.acoustickey.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import minex.acoustickey.AcoustiKey;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AcoustiKeyConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
				.getConfigDir()
			.resolve("acoustickey.json");

	public String keyboardPackId = "";
	public String mousePackId = "";
	public int keyboardVolume = 80;
	public int mouseVolume = 80;
	public boolean muted = false;
	public boolean keyUpEnabled = false;
	public boolean mouseSoundsEnabled = true;
	public boolean randomSoundsEnabled = false;

	public static AcoustiKeyConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			AcoustiKeyConfig config = new AcoustiKeyConfig();
			config.save();
			return config;
		}
		try {
			String json = Files.readString(CONFIG_PATH);
			return GSON.fromJson(json, AcoustiKeyConfig.class);
		} catch (IOException e) {
			AcoustiKey.LOGGER.warn("Failed to load config, using defaults", e);
			return new AcoustiKeyConfig();
		}
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException e) {
			AcoustiKey.LOGGER.error("Failed to save config", e);
		}
	}
}
