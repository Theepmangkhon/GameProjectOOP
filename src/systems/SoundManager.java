package systems;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * เล่นเสียง BGM (วนซ้ำ) และ SFX (เล่นครั้งเดียว) จากไฟล์ .mp3
 *
 * - SFX : โหลดเข้าหน่วยความจำตอนเริ่ม เล่นได้ทันทีไม่มีหน่วง
 * - BGM : สตรีมเล่นทีละช่วงใน thread แยก ไม่กินแรมและไม่ทำให้เกมกระตุก
 *
 * ต้องมีไลบรารีถอดรหัส MP3 (mp3spi + jlayer + tritonus-share) ในโปรเจกต์
 * ถ้าไม่มี เกมจะยังรันได้ตามปกติแต่ไม่มีเสียง และจะพิมพ์คำเตือนใน console
 */
public class SoundManager {

    private static final String BGM_FOLDER = "asset/Sound/BGM/";
    private static final String SFX_FOLDER = "asset/Sound/SFX/";

    private final Map<String, Clip> sfxClips = new HashMap<>();

    private float bgmVolume = 0.5f;
    private float sfxVolume = 0.9f;

    // ---- BGM state (ป้องกันด้วย bgmLock) ----
    private final Object bgmLock = new Object();
    private String currentBgm = null;
    private int bgmGeneration = 0;
    private SourceDataLine activeLine = null;

    private boolean decoderWarned = false;

    // =====================================================
    // VOLUME (0.0 - 1.0)
    // =====================================================

    public void setBgmVolume(float volume) {

        bgmVolume = clamp(volume);

        synchronized (bgmLock) {

            if (activeLine != null) {

                setGain(activeLine, bgmVolume);
            }
        }
    }

    public void setSfxVolume(float volume) {

        sfxVolume = clamp(volume);

        for (Clip clip : sfxClips.values()) {

            setGain(clip, sfxVolume);
        }
    }

    // =====================================================
    // SFX
    // =====================================================

    // ชื่อ = ชื่อไฟล์ไม่รวม .mp3 (ตัวพิมพ์เล็กใหญ่ต้องตรง)
    public void loadSfx(String... names) {

        for (String name : names) {

            File file = new File(SFX_FOLDER + name + ".mp3");

            if (!file.exists()) {

                System.out.println("SFX not found: " + file.getPath());

                continue;
            }

            try {

                AudioInputStream decoded = openDecoded(file);

                AudioFormat format = decoded.getFormat();

                byte[] data = readAll(decoded);

                decoded.close();

                Clip clip = AudioSystem.getClip();

                clip.open(format, data, 0, data.length);

                setGain(clip, sfxVolume);

                sfxClips.put(name, clip);

            } catch (Exception e) {

                report("SFX " + name, e);
            }
        }
    }

    public void playSfx(String name) {

        Clip clip = sfxClips.get(name);

        if (clip == null) {

            return;
        }

        if (clip.isRunning()) {

            clip.stop();
        }

        clip.setFramePosition(0);

        clip.start();
    }

    // =====================================================
    // BGM
    // =====================================================

    // สั่งเปลี่ยนเพลง ถ้าเป็นเพลงเดิมอยู่แล้วจะไม่ทำอะไร (เรียกซ้ำทุกเฟรมได้)
    public void playBgm(String name) {

        synchronized (bgmLock) {

            if (name.equals(currentBgm)) {

                return;
            }

            currentBgm = name;

            bgmGeneration++;

            final int generation = bgmGeneration;

            // ล้างเสียงที่ค้างบัฟเฟอร์ของเพลงเก่าให้เงียบเร็ว
            if (activeLine != null) {

                try {

                    activeLine.flush();

                } catch (Exception ignored) {
                }
            }

            Thread thread =
                new Thread(
                    () -> runBgm(name, generation),
                    "BGM-" + name
                );

            thread.setDaemon(true);

            thread.start();
        }
    }

    private boolean isCurrent(int generation) {

        synchronized (bgmLock) {

            return generation == bgmGeneration;
        }
    }

    private void runBgm(String name, int generation) {

        File file = new File(BGM_FOLDER + name + ".mp3");

        if (!file.exists()) {

            System.out.println("BGM not found: " + file.getPath());

            return;
        }

        SourceDataLine line = null;

        try {

            // วนเล่นซ้ำจนกว่าจะมีการสั่งเปลี่ยนเพลง
            while (isCurrent(generation)) {

                AudioInputStream decoded = openDecoded(file);

                try {

                    AudioFormat format = decoded.getFormat();

                    int frameSize = format.getFrameSize();

                    if (line == null) {

                        line = AudioSystem.getSourceDataLine(format);

                        // บัฟเฟอร์ ~0.25 วินาที ทำให้สลับเพลงได้เร็ว
                        int buffer =
                            frameSize * ((int) format.getSampleRate() / 4);

                        line.open(format, buffer);

                        setGain(line, bgmVolume);

                        synchronized (bgmLock) {

                            activeLine = line;
                        }

                        line.start();
                    }

                    byte[] chunk = new byte[4096];

                    int read;

                    while (
                        isCurrent(generation)
                        && (read = decoded.read(chunk, 0, chunk.length)) != -1
                    ) {

                        int usable = read - (read % frameSize);

                        if (usable > 0) {

                            line.write(chunk, 0, usable);
                        }
                    }

                } finally {

                    decoded.close();
                }
            }

        } catch (Exception e) {

            report("BGM " + name, e);

        } finally {

            if (line != null) {

                try {

                    line.stop();

                    line.close();

                } catch (Exception ignored) {
                }
            }

            synchronized (bgmLock) {

                if (activeLine == line) {

                    activeLine = null;
                }
            }
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    // เปิดไฟล์แล้วถอดรหัสเป็น PCM 16-bit (ต้องมี mp3spi สำหรับ .mp3)
    private AudioInputStream openDecoded(File file) throws Exception {

        AudioInputStream in = AudioSystem.getAudioInputStream(file);

        AudioFormat base = in.getFormat();

        if (AudioFormat.Encoding.PCM_SIGNED.equals(base.getEncoding())) {

            return in;
        }

        AudioFormat decoded =
            new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                base.getSampleRate(),
                16,
                base.getChannels(),
                base.getChannels() * 2,
                base.getSampleRate(),
                false
            );

        return AudioSystem.getAudioInputStream(decoded, in);
    }

    private byte[] readAll(AudioInputStream stream) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] chunk = new byte[8192];

        int read;

        while ((read = stream.read(chunk)) != -1) {

            out.write(chunk, 0, read);
        }

        return out.toByteArray();
    }

    private void setGain(Line line, float volume) {

        try {

            if (!line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {

                return;
            }

            FloatControl control =
                (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);

            float db =
                volume <= 0.0001f
                    ? control.getMinimum()
                    : (float) (20.0 * Math.log10(volume));

            db =
                Math.max(
                    control.getMinimum(),
                    Math.min(control.getMaximum(), db)
                );

            control.setValue(db);

        } catch (Exception ignored) {
        }
    }

    private float clamp(float volume) {

        return Math.max(0f, Math.min(1f, volume));
    }

    private void report(String what, Exception e) {

        if (e instanceof UnsupportedAudioFileException) {

            if (!decoderWarned) {

                decoderWarned = true;

                System.out.println(
                    "[Sound] MP3 decoder not found. "
                    + "Add mp3spi, jlayer and tritonus-share jars "
                    + "to the project libraries to enable sound."
                );
            }

            return;
        }

        System.out.println(
            "[Sound] " + what + " failed: " + e
        );
    }
}
