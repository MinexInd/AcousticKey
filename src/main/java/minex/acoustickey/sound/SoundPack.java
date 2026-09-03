package minex.acoustickey.sound;

import java.util.HashMap;
import java.util.Map;

public class SoundPack {
	public enum DefineType {
		SINGLE,
		INDIVIDUAL
	}

	private final String packId;
	private final String displayName;
	private final String absolutePath;
	private final DefineType defineType;
	private final boolean includesNumpad;
	private final boolean compatibility;
	private final Map<String, String> defines;
	private final Map<String, int[]> spriteDefines;
	private final boolean isDefault;

	public SoundPack(String packId, String displayName, String absolutePath,
				DefineType defineType, boolean includesNumpad, boolean compatibility,
				Map<String, String> defines) {
		this.packId = packId;
		this.displayName = displayName;
		this.absolutePath = absolutePath;
		this.defineType = defineType;
		this.includesNumpad = includesNumpad;
		this.compatibility = compatibility;
		this.defines = defines;
		this.spriteDefines = new HashMap<>();
		this.isDefault = packId.startsWith("default-");
	}

	public void setSpriteDefine(String keycode, int startMs, int durationMs) {
		this.spriteDefines.put(keycode, new int[]{startMs, durationMs});
	}

	public String getPackId() {
		return packId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public DefineType getDefineType() {
		return defineType;
	}

	public boolean includesNumpad() {
		return includesNumpad;
	}

	public boolean isCompatibility() {
		return compatibility;
	}

	public Map<String, String> getDefines() {
		return defines;
	}

	public int[] getSprite(String keycode) {
		return spriteDefines.get(keycode);
	}

	public boolean isDefault() {
		return isDefault;
	}

	public String getSoundPath(String keycode) {
		String relativePath = defines.get(keycode);
		return resolvePath(relativePath);
	}

	public String resolvePath(String relativePath) {
		if (relativePath == null) {
			return null;
		}

		if (isDefault) {
			String folder = absolutePath.replaceAll("/+$", "").replaceAll(".*/", "");
			return "assets/acoustickey/soundpacks/" + folder + "/" + relativePath;
		}

		String base = absolutePath.endsWith("/") ? absolutePath.substring(0, absolutePath.length() - 1) : absolutePath;
		return base + "/" + relativePath;
	}
}
