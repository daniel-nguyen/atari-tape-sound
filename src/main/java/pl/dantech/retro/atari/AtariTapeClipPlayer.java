package pl.dantech.retro.atari;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.ByteOrder;

import static com.google.common.io.Resources.*;
import static java.nio.ByteOrder.BIG_ENDIAN;

public class AtariTapeClipPlayer {

    public static void main(String[] args) throws IOException, InterruptedException, LineUnavailableException {
        final int sampleRate = 44100;
        final int sampleSizeInBits = 16;
        final int channels = 1;
        final boolean valuesWithSign = true;
        final ByteOrder byteOrder = BIG_ENDIAN;


        final byte[] dataBytes = toByteArray(getResource("some_data.txt"));
        AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, valuesWithSign, BIG_ENDIAN.equals(byteOrder));
        byte[] soundWave = AtariBytesToSoundWaveConverter.convert(audioFormat, new AtariTapeFile(dataBytes), 600);
        System.err.println(String.format("samples: %d, seconds: %.2f", soundWave.length/2, 0.0d + soundWave.length/2.0/sampleRate));

        DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
        Clip clip = null;
        try {
            clip = (Clip) AudioSystem.getLine(info);

            clip.open(audioFormat, soundWave, 0, soundWave.length);
            clip.start();
            Thread.sleep(1000L);
            clip.drain();

        } finally {
            if (clip!=null) {
                if (clip.isRunning()) {
                    clip.stop();
                }

                if (clip.isOpen()) {
                    clip.close();
                }
            }
        }

    }



}
