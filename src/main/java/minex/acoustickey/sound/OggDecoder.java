package minex.acoustickey.sound;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OggDecoder {

	public static class DecodedAudio {
		public final AudioFormat format;
		public final byte[] pcmData;

		public DecodedAudio(AudioFormat format, byte[] pcmData) {
			this.format = format;
			this.pcmData = pcmData;
		}
	}

	public static DecodedAudio decode(InputStream input) throws IOException {
		SyncState oy = new SyncState();
		StreamState os = new StreamState();
		Page og = new Page();
		Packet op = new Packet();
		Info vi = new Info();
		Comment vc = new Comment();
		DspState vd = new DspState();
		Block vb = new Block(vd);

		oy.init();

		boolean streamInited = false;
		boolean initialized = false;
		int headerPackets = 0;
		ByteArrayOutputStream pcmOut = new ByteArrayOutputStream();
		int channels = 0;
		int sampleRate = 0;
		int[] idx = null;
		float[][][] pcm = new float[1][][];

		try {
			boolean eof = false;
			while (true) {
				if (!eof) {
					int bufSize = oy.buffer(4096);
					byte[] buf = oy.data;
					int read = input.read(buf, bufSize, 4096);
					if (read < 0) {
						eof = true;
					} else if (read == 0) {
						continue;
					} else {
						oy.wrote(read);
					}
				}

				int pageResult = oy.pageout(og);
				if (pageResult <= 0) {
					if (eof) {
						break;
					}
					continue;
				}

				if (!streamInited) {
					if (og.bos() == 0) {
						continue;
					}
					os.init(og.serialno());
					streamInited = true;
				}

				os.pagein(og);
				while (os.packetout(op) > 0) {
					if (!initialized) {
						if (vi.synthesis_headerin(vc, op) < 0) {
							throw new IOException("Not a vorbis stream");
						}
						headerPackets++;
						if (headerPackets >= 3) {
							if (vd.synthesis_init(vi) != 0) {
								throw new IOException("DSP init failed");
							}
							vb.init(vd);
							channels = vi.channels;
							sampleRate = vi.rate;
							idx = new int[channels];
							initialized = true;
						}
					} else {
						if (vb.synthesis(op) == 0) {
							vd.synthesis_blockin(vb);
							int samples = vd.synthesis_pcmout(pcm, idx);
							while (samples > 0) {
								float[][] pcmData = pcm[0];
								int ch = pcmData.length;
								int base = idx[0];
								for (int i = 0; i < samples; i++) {
									for (int c = 0; c < ch; c++) {
										float val = pcmData[c][base + i];
										if (val > 1.0f) val = 1.0f;
										if (val < -1.0f) val = -1.0f;
										short pcmVal = (short) (val * 32767.0f);
										pcmOut.write(pcmVal & 0xFF);
										pcmOut.write((pcmVal >> 8) & 0xFF);
									}
								}
								vd.synthesis_read(samples);
								samples = vd.synthesis_pcmout(pcm, idx);
							}
						}
					}
				}
			}
		} finally {
			try { os.clear(); } catch (Throwable ignored) {}
			try { vb.clear(); } catch (Throwable ignored) {}
			try { vd.clear(); } catch (Throwable ignored) {}
			try { oy.clear(); } catch (Throwable ignored) {}
		}

		if (!initialized || channels == 0) {
			throw new IOException("Failed to decode OGG: no vorbis headers found");
		}

		AudioFormat format = new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED,
			sampleRate, 16, channels,
			channels * 2, sampleRate, false
		);
		return new DecodedAudio(format, pcmOut.toByteArray());
	}
}
