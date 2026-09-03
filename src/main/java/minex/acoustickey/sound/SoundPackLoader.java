package minex.acoustickey.sound;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import minex.acoustickey.AcoustiKey;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class SoundPackLoader {
	private static final Gson GSON = new Gson();

	private static final Path CUSTOM_PACKS_DIR = FabricLoader.getInstance()
				.getConfigDir()
			.resolve("acoustickey/soundpacks");

	private static final Path MOUSE_CUSTOM_PACKS_DIR = FabricLoader.getInstance()
				.getConfigDir()
			.resolve("acoustickey/mousepacks");

	private static final Set<String> DEFAULT_KEYBOARD_FOLDERS = Set.of(
		"default-keyboard", "eg-oreo", "cherrymx-blue-pbt", "cherrymx-brown-pbt", "cherrymx-red-pbt", "default-mouse");
	private static final Set<String> DEFAULT_MOUSE_FOLDERS = Set.of("default-mouse");

	public static List<SoundPack> loadKeyboardPacks() {
		List<SoundPack> packs = new ArrayList<>();
		packs.addAll(loadDefaultPacks(false));
		packs.addAll(loadPacks(CUSTOM_PACKS_DIR, DEFAULT_KEYBOARD_FOLDERS, false));
		return packs;
	}

	public static List<SoundPack> loadMousePacks() {
		List<SoundPack> packs = new ArrayList<>();
		packs.addAll(loadDefaultPacks(true));
		packs.addAll(loadPacks(MOUSE_CUSTOM_PACKS_DIR, DEFAULT_MOUSE_FOLDERS, true));
		return packs;
	}

	private static List<SoundPack> loadDefaultPacks(boolean isMouse) {
		List<SoundPack> packs = new ArrayList<>();
		String[] defaultPacks = isMouse
					? new String[]{"default-mouse"}
					: new String[]{"default-keyboard", "eg-oreo", "cherrymx-blue-pbt", "cherrymx-brown-pbt", "cherrymx-red-pbt"};

		for (String packName : defaultPacks) {
			String resourcePath = "assets/acoustickey/soundpacks/" + packName + "/";
			try {
				SoundPack pack = loadDefaultPack(resourcePath, isMouse);
				if (pack != null) {
					packs.add(pack);
				}
			} catch (Exception e) {
				AcoustiKey.LOGGER.warn("Failed to load default pack: {}", packName, e);
			}
		}
		return packs;
	}

	private static SoundPack loadDefaultPack(String resourcePath, boolean isMouse) {
		String configPath = resourcePath + "config.json";
		ClassLoader cl = AcoustiKey.class.getClassLoader();
		InputStream configStream = cl.getResourceAsStream(configPath);
		if (configStream == null) {
			return null;
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(configStream))) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

			String name = getStringOrDefault(root, "name", "Default Pack");
			String keyDefineType = getStringOrDefault(root, "key_define_type", "individual");
			SoundPack.DefineType defineType = "single".equals(keyDefineType)
						? SoundPack.DefineType.SINGLE
						: SoundPack.DefineType.INDIVIDUAL;

			boolean includesNumpad = getBooleanOrDefault(root, "includes_numpad", false);
			boolean compatibility = getBooleanOrDefault(root, "compatibility", false);

			JsonObject definesJson = root.getAsJsonObject("defines");
			Map<String, String> defines = new java.util.HashMap<>();
			if (definesJson != null) {
				for (Map.Entry<String, com.google.gson.JsonElement> entry : definesJson.entrySet()) {
					com.google.gson.JsonElement el = entry.getValue();
					if (el.isJsonPrimitive()) {
						defines.put(entry.getKey(), el.getAsString());
					} else if (el.isJsonArray() && defineType == SoundPack.DefineType.SINGLE) {
						com.google.gson.JsonArray arr = el.getAsJsonArray();
						if (arr.size() == 2 && arr.get(0).isJsonPrimitive() && arr.get(1).isJsonPrimitive()) {
							String soundFile = getStringOrDefault(root, "sound", "");
							defines.put(entry.getKey(), soundFile);
						}
					}
				}
			}

			String folderName = resourcePath.replaceAll("/+$", "").replaceAll(".*/", "").replaceFirst("^default-", "");
			String packId = "default-" + folderName;

			SoundPack pack = new SoundPack(packId, name, resourcePath, defineType, includesNumpad, compatibility, defines);

			if (defineType == SoundPack.DefineType.SINGLE && definesJson != null) {
				for (Map.Entry<String, com.google.gson.JsonElement> entry : definesJson.entrySet()) {
					com.google.gson.JsonElement el = entry.getValue();
					if (el.isJsonArray()) {
						com.google.gson.JsonArray arr = el.getAsJsonArray();
						if (arr.size() == 2) {
							pack.setSpriteDefine(entry.getKey(), arr.get(0).getAsInt(), arr.get(1).getAsInt());
						}
					}
				}
			}

			return pack;
		} catch (Exception e) {
			AcoustiKey.LOGGER.warn("Failed to parse default pack config: {}", resourcePath, e);
			return null;
		}
	}

	private static List<SoundPack> loadPacks(Path baseDir, Set<String> skipFolders, boolean isMouse) {
		List<SoundPack> packs = new ArrayList<>();

		if (!Files.exists(baseDir)) {
			try {
				Files.createDirectories(baseDir);
			} catch (IOException e) {
				AcoustiKey.LOGGER.warn("Failed to create packs directory: {}", baseDir, e);
			}
			return packs;
		}

		try (Stream<Path> dirs = Files.list(baseDir)) {
			dirs.filter(Files::isDirectory)
						.filter(dir -> !skipFolders.contains(dir.getFileName().toString()))
						.forEach(dir -> {
				try {
					SoundPack pack = loadPack(dir, isMouse);
					if (pack != null) {
						packs.add(pack);
					}
				} catch (Exception e) {
					AcoustiKey.LOGGER.warn("Failed to load pack from {}", dir, e);
				}
			});
		} catch (IOException e) {
			AcoustiKey.LOGGER.warn("Failed to list packs in {}", baseDir, e);
		}

		return packs;
	}

	private static SoundPack loadPack(Path dir, boolean isMouse) {
		Path configPath = dir.resolve("config.json");
		if (!Files.exists(configPath)) {
			return null;
		}

		try {
			String json = Files.readString(configPath);
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();

			String name = getStringOrDefault(root, "name", dir.getFileName().toString());
			String keyDefineType = getStringOrDefault(root, "key_define_type", "individual");
			SoundPack.DefineType defineType = "single".equals(keyDefineType)
						? SoundPack.DefineType.SINGLE
						: SoundPack.DefineType.INDIVIDUAL;

			boolean includesNumpad = getBooleanOrDefault(root, "includes_numpad", false);
			boolean compatibility = getBooleanOrDefault(root, "compatibility", false);

			JsonObject definesJson = root.getAsJsonObject("defines");
			Map<String, String> defines = new java.util.HashMap<>();
			if (definesJson != null) {
				for (Map.Entry<String, com.google.gson.JsonElement> entry : definesJson.entrySet()) {
					com.google.gson.JsonElement el = entry.getValue();
					if (el.isJsonPrimitive()) {
						defines.put(entry.getKey(), el.getAsString());
					} else if (el.isJsonArray() && defineType == SoundPack.DefineType.SINGLE) {
						com.google.gson.JsonArray arr = el.getAsJsonArray();
						if (arr.size() == 2 && arr.get(0).isJsonPrimitive() && arr.get(1).isJsonPrimitive()) {
							int startMs = arr.get(0).getAsInt();
							int durationMs = arr.get(1).getAsInt();
							String soundFile = getStringOrDefault(root, "sound", "");
							defines.put(entry.getKey(), soundFile);
						}
					}
				}
			}

			String folderName = dir.getFileName().toString();
			String packId = "custom-" + folderName;

			SoundPack pack = new SoundPack(packId, name, dir.toString(), defineType, includesNumpad, compatibility, defines);

			if (defineType == SoundPack.DefineType.SINGLE && definesJson != null) {
				for (Map.Entry<String, com.google.gson.JsonElement> entry : definesJson.entrySet()) {
					com.google.gson.JsonElement el = entry.getValue();
					if (el.isJsonArray()) {
						com.google.gson.JsonArray arr = el.getAsJsonArray();
						if (arr.size() == 2) {
							pack.setSpriteDefine(entry.getKey(), arr.get(0).getAsInt(), arr.get(1).getAsInt());
						}
					}
				}
			}

			return pack;
		} catch (Exception e) {
			AcoustiKey.LOGGER.warn("Failed to parse config.json in {}", dir, e);
			return null;
		}
	}

	private static String getStringOrDefault(JsonObject obj, String key, String defaultValue) {
		return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : defaultValue;
	}

	private static boolean getBooleanOrDefault(JsonObject obj, String key, boolean defaultValue) {
		return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsBoolean() : defaultValue;
	}
}
