package minex.acoustickey.sound;

import minex.acoustickey.AcoustiKey;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenALSoundPlayer {

	private static final Map<String, byte[]> audioCache = new ConcurrentHashMap<>();
	private static final Map<String, AudioFormat> formatCache = new ConcurrentHashMap<>();
	private static final ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
		Thread t = new Thread(r, "AcoustiKey-Audio");
		t.setDaemon(true);
		return t;
	});

	// Single, last-write-wins playback loop. Keeps only the most recent key sound
	// queued and cuts off any previously-played audio, so sounds never pile up and
	// keep playing after you stop typing.
	private static final Object PLAYBACK_LOCK = new Object();
	private static byte[] pendingPcm;
	private static AudioFormat pendingFormat;
	private static float pendingVolume;
	private static volatile boolean running = true;

	private static boolean initialized = false;

	public static void initialize() {
		if (initialized) {
			return;
		}
		initialized = true;
		Thread playback = new Thread(OpenALSoundPlayer::playbackLoop, "AcoustiKey-Playback");
		playback.setDaemon(true);
		playback.start();
		AcoustiKey.LOGGER.info("Sound system initialized");
	}

	public static void shutdown() {
		executor.shutdownNow();
		running = false;
		synchronized (PLAYBACK_LOCK) {
			pendingPcm = null;
			PLAYBACK_LOCK.notifyAll();
		}
	}

	public static void preload(String filePath) {
		if (filePath == null || audioCache.containsKey(filePath)) {
			return;
		}
		executor.submit(() -> audioCache.computeIfAbsent(filePath, OpenALSoundPlayer::loadFullAudio));
	}

	public static void playSound(String filePath, float volume) {
		playSound(filePath, volume, null);
	}

	public static void playSound(String filePath, float volume, int[] sprite) {
		if (!initialized) {
			initialize();
		}

		// Decode on the worker pool (never blocks the render thread), then hand the
		// resulting PCM to the single last-write-wins playback loop.
		executor.submit(() -> {
			byte[] fullPcm = audioCache.get(filePath);
			AudioFormat format = formatCache.get(filePath);

			if (fullPcm == null || format == null) {
				fullPcm = audioCache.computeIfAbsent(filePath, OpenALSoundPlayer::loadFullAudio);
				format = formatCache.get(filePath);
				if (fullPcm == null || format == null || fullPcm.length == 0) {
					return;
				}
			}

			if (sprite != null) {
				byte[] sliced = sliceAudio(fullPcm, format, sprite[0], sprite[1]);
				if (sliced.length > 0) {
					fullPcm = sliced;
				}
			}

			requestPlayback(fullPcm, format, volume);
		});
	}

	private static void requestPlayback(byte[] pcm, AudioFormat format, float volume) {
		synchronized (PLAYBACK_LOCK) {
			pendingPcm = pcm;
			pendingFormat = format;
			pendingVolume = volume;
			PLAYBACK_LOCK.notifyAll();
		}
	}

	private static void playbackLoop() {
		SourceDataLine line = null;
		AudioFormat lineFormat = null;

		while (running) {
			byte[] data;
			AudioFormat format;
			float volume;

			synchronized (PLAYBACK_LOCK) {
				while (running && pendingPcm == null) {
					try {
						PLAYBACK_LOCK.wait();
					} catch (InterruptedException e) {
						return;
					}
				}
				if (!running) {
					break;
				}
				data = pendingPcm;
				format = pendingFormat;
				volume = pendingVolume;
				pendingPcm = null;
			}

			try {
				// (Re)use a single line per audio format; cut off and discard any
				// previously-queued PCM so old sounds don't keep echoing.
				if (line == null || !format.equals(lineFormat)) {
					if (line != null) {
						line.stop();
						line.close();
					}
					line = AudioSystem.getSourceDataLine(format);
					line.open(format);
					line.start();
					lineFormat = format;
				} else {
					line.stop();
					line.flush();
					line.start();
				}

				if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
					FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
					float dB = (float) (Math.log10(Math.max(volume, 0.001)) * 20.0);
					float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
					gain.setValue(clamped);
				}

				int offset = 0;
				while (offset < data.length) {
					int len = Math.min(2048, data.length - offset);
					line.write(data, offset, len);
					offset += len;
				}
				line.drain();
			} catch (LineUnavailableException e) {
				AcoustiKey.LOGGER.warn("Audio line unavailable for playback");
				closeQuietly(line);
				line = null;
				lineFormat = null;
			} catch (RuntimeException e) {
				AcoustiKey.LOGGER.warn("Audio playback error", e);
				closeQuietly(line);
				line = null;
				lineFormat = null;
			}
		}

		closeQuietly(line);
	}

	private static void closeQuietly(SourceDataLine line) {
		if (line != null) {
			try {
				line.stop();
				line.close();
			} catch (Throwable ignored) {
			}
		}
	}

	private static byte[] loadFullAudio(String filePath) {
		try {
			byte[] rawData;

			if (filePath.startsWith("assets/")) {
				ClassLoader cl = OpenALSoundPlayer.class.getClassLoader();
				try (InputStream is = cl.getResourceAsStream(filePath)) {
					if (is == null) {
						// Resource does not exist - skip instead of synthesizing a tone.
						return null;
					}
					rawData = is.readAllBytes();
				}
			} else {
				Path path = Path.of(filePath);
				if (!Files.exists(path)) {
					// File does not exist - skip instead of synthesizing a tone.
					return null;
				}
				rawData = Files.readAllBytes(path);
			}

			if (rawData == null || rawData.length == 0) {
				return null;
			}

			String lower = filePath.toLowerCase();
			try {
				if (lower.endsWith(".ogg")) {
					OggDecoder.DecodedAudio decoded = OggDecoder.decode(new ByteArrayInputStream(rawData));
					formatCache.put(filePath, decoded.format);
					return decoded.pcmData;
				} else {
					AudioInputStream audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(rawData));
					AudioFormat format = audioStream.getFormat();
					formatCache.put(filePath, format);
					byte[] decoded = audioStream.readAllBytes();
					audioStream.close();
					return decoded;
				}
			} catch (Exception e) {
				AcoustiKey.LOGGER.warn("Failed to decode {}: {}", filePath, e.getMessage());
				return null;
			}
		} catch (Exception e) {
			AcoustiKey.LOGGER.warn("Failed to load audio: {}", filePath, e);
			return null;
		}
	}

	private static byte[] sliceAudio(byte[] fullPcm, AudioFormat format, int startMs, int durationMs) {
		if (fullPcm == null || fullPcm.length == 0) {
			return new byte[0];
		}

		int sampleSize = format.getSampleSizeInBits() / 8;
		int channels = format.getChannels();
		int frameSize = sampleSize * channels;
		float sampleRate = format.getSampleRate();

		int startSample = (int) ((startMs / 1000.0) * sampleRate);
		int durationSamples = (int) ((durationMs / 1000.0) * sampleRate);

		int startByte = startSample * frameSize;
		int endByte = Math.min(startByte + durationSamples * frameSize, fullPcm.length);

		if (startByte >= fullPcm.length || startByte < 0) {
			return new byte[0];
		}

		int length = endByte - startByte;
		byte[] sliced = new byte[length];
		System.arraycopy(fullPcm, startByte, sliced, 0, length);
		return sliced;
	}
}
